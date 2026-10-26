package com.browseruse.server

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ErrorBody(val error: String)

fun main() {
    System.setProperty("java.awt.headless", "true")
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val store = Store()
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    monitor.subscribe(ApplicationStopped) { scope.cancel() }

    install(ContentNegotiation) { json(json) }
    install(CallLogging)
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Options)
    }
    install(WebSockets)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            log.error("Unhandled request failure", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorBody(cause.message ?: "internal error"))
        }
    }

    routing {
        get("/health") {
            call.respond(Health(status = "ok", version = "0.1.0", engine = "mock-agent"))
        }

        post("/v1/sessions") {
            val request = call.receive<NewSession>()
            val session = store.createSession(request)
            call.respond(HttpStatusCode.Created, session)
        }

        get("/v1/sessions/{id}") {
            val id = call.parameters["id"]
            val session = id?.let { store.session(it) }
            if (session == null) {
                call.respond(HttpStatusCode.NotFound, ErrorBody("session not found"))
            } else {
                call.respond(session)
            }
        }

        post("/v1/sessions/{id}/runs") {
            val sessionId = call.parameters["id"]
            val session = sessionId?.let { store.session(it) }
            if (session == null) {
                call.respond(HttpStatusCode.NotFound, ErrorBody("session not found"))
                return@post
            }
            val request = call.receive<NewRun>()
            if (request.task.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody("task is required"))
                return@post
            }
            val run = store.createRun(session.id, request.task)
            scope.launch { runScenario(store, run) }
            call.respond(HttpStatusCode.Created, RunCreated(run.id))
        }

        get("/v1/runs/{id}/events") {
            val runId = call.parameters["id"]
            if (runId == null || store.run(runId) == null) {
                call.respond(HttpStatusCode.NotFound, ErrorBody("run not found"))
                return@get
            }
            call.response.header(HttpHeaders.CacheControl, "no-cache")
            call.response.header("X-Accel-Buffering", "no")
            call.respondTextWriter(contentType = ContentType.parse("text/event-stream")) {
                write("retry: 1000\n\n")
                flush()
                var index = 0
                while (true) {
                    val events = store.eventList(runId)
                    while (index < events.size) {
                        val event = events[index]
                        index += 1
                        write("id: $index\n")
                        write("data: ${json.encodeToString(Event.serializer(), event)}\n\n")
                        flush()
                    }
                    if (store.isFinished(runId)) break
                    delay(120)
                }
            }
        }

        post("/v1/sessions/{id}/stop") {
            val id = call.parameters["id"] ?: ""
            val stopped = store.stop(id)
            if (!stopped) {
                call.respond(HttpStatusCode.NotFound, ErrorBody("session not found"))
            } else {
                call.respond(SimpleStopped("stopped"))
            }
        }

        webSocket("/v1/sessions/{id}/live") {
            val sessionId = call.parameters["id"] ?: return@webSocket
            val renderer = DemoFrames()
            send(Frame.Text("""{"type":"connected","sessionId":"$sessionId"}"""))
            var sequence = 0
            val sender = launch {
                while (isActive) {
                    val jpeg = renderer.frame(sessionId, sequence)
                    val header = ByteArray(9)
                    header[0] = 1
                    writeInt(header, 1, jpeg.size)
                    writeInt(header, 5, sequence)
                    send(Frame.Binary(true, header + jpeg))
                    sequence += 1
                    delay(1000)
                }
            }
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        send(Frame.Text("""{"type":"ack","message":"${text.take(80).replace("\"", "'")}"}"""))
                    }
                }
            } finally {
                sender.cancel()
            }
        }
    }
}

private fun writeInt(bytes: ByteArray, offset: Int, value: Int) {
    bytes[offset] = (value ushr 24).toByte()
    bytes[offset + 1] = (value ushr 16).toByte()
    bytes[offset + 2] = (value ushr 8).toByte()
    bytes[offset + 3] = value.toByte()
}
