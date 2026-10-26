package com.browseruse.server

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun `health endpoint reports ok`() = testApplication {
        application { module() }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("ok"))
    }

    @Test
    fun `creating a session returns a live url`() = testApplication {
        application { module() }
        val response = client.post("/v1/sessions") {
            contentType(ContentType.Application.Json)
            setBody("""{"model":"bu-mini"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("sess_"))
        assertTrue(body.contains("liveUrl"))
    }

    @Test
    fun `starting a run returns a run id`() = testApplication {
        application { module() }
        val sessionResponse = client.post("/v1/sessions") {
            contentType(ContentType.Application.Json)
            setBody("""{"model":"bu-mini"}""")
        }
        val sessionBody = sessionResponse.bodyAsText()
        val sessionId = Regex("\"id\"\\s*:\\s*\"([^\"]+)\"").find(sessionBody)!!.groupValues[1]

        val runResponse = client.post("/v1/sessions/$sessionId/runs") {
            contentType(ContentType.Application.Json)
            setBody("""{"task":"find the star count"}""")
        }
        assertEquals(HttpStatusCode.Created, runResponse.status)
        assertTrue(runResponse.bodyAsText().contains("run_"))
    }
}
