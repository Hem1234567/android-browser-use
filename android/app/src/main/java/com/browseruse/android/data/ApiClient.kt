package com.browseruse.android.data

import com.browseruse.android.model.AppSettings
import com.browseruse.android.model.CreateRunRequest
import com.browseruse.android.model.CreateSessionRequest
import com.browseruse.android.model.RunCreated
import com.browseruse.android.model.RunEvent
import com.browseruse.android.model.SessionDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class ApiClient(private val baseUrl: String) {

    data class Prepared(val sessionId: String, val liveUrl: String?, val runId: String)

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val mediaType = "application/json".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    private val streamClient = client.newBuilder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    suspend fun prepare(task: String, settings: AppSettings): Prepared = withContext(Dispatchers.IO) {
        val health = Request.Builder().url("$baseUrl/health").build()
        client.newCall(health).execute().use { response ->
            if (!response.isSuccessful) error("Backend unavailable")
        }

        val sessionBody = json.encodeToString(
            CreateSessionRequest.serializer(),
            CreateSessionRequest(
                model = settings.model,
                profileId = settings.profileId,
                workspaceId = settings.workspaceId,
                proxyCountryCode = settings.proxyCountryCode
            )
        )
        val session = post("$baseUrl/v1/sessions", sessionBody, SessionDto.serializer())
        val runBody = json.encodeToString(CreateRunRequest.serializer(), CreateRunRequest(task))
        val run = post("$baseUrl/v1/sessions/${session.id}/runs", runBody, RunCreated.serializer())
        Prepared(session.id, session.liveUrl, run.runId)
    }

    private fun <T> post(url: String, body: String, serializer: KSerializer<T>): T {
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody(mediaType))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code} for $url")
            val text = response.body?.string().orEmpty()
            return json.decodeFromString(serializer, text)
        }
    }

    fun stream(runId: String): Flow<RunEvent> = callbackFlow {
        val request = Request.Builder()
            .url("$baseUrl/v1/runs/$runId/events")
            .header("Accept", "text/event-stream")
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                runCatching { json.decodeFromString(RunEvent.serializer(), data) }
                    .onSuccess { trySend(it) }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                runCatching { close(t ?: IllegalStateException("event stream failed")) }
            }

            override fun onClosed(eventSource: EventSource) {
                runCatching { close() }
            }
        }

        val source = EventSources.createFactory(streamClient).newEventSource(request, listener)
        awaitClose { source.cancel() }
    }

    fun liveUrl(sessionId: String, relative: String?): String {
        if (!relative.isNullOrBlank()) {
            if (relative.startsWith("ws://") || relative.startsWith("wss://")) return relative
            val scheme = if (baseUrl.startsWith("https")) "wss" else "ws"
            val host = baseUrl.substringAfter("://").trimEnd('/')
            val path = if (relative.startsWith("/")) relative else "/$relative"
            return "$scheme://$host$path"
        }
        val scheme = if (baseUrl.startsWith("https")) "wss" else "ws"
        val host = baseUrl.substringAfter("://").trimEnd('/')
        return "$scheme://$host/v1/sessions/$sessionId/live"
    }

    suspend fun stop(sessionId: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/v1/sessions/$sessionId/stop")
            .post("{}".toRequestBody(mediaType))
            .build()
        runCatching { client.newCall(request).execute().close() }
    }
}
