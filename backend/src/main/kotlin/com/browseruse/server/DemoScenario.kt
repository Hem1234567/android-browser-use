package com.browseruse.server

import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Deterministic mock of the browser-use agent loop. The real engine (engine/) emits the
 * same event schema; this lets the Android app and API be developed without Chromium.
 */
suspend fun runScenario(store: Store, run: Run) {
    fun event(type: String, block: JsonObjectBuilder.() -> Unit = {}) {
        store.addEvent(run.id, type, buildJsonObject(block))
    }

    event("run.started") {
        put("runId", run.id)
        put("sessionId", run.sessionId)
        put("task", run.task)
    }
    delay(350)

    event("step.started") { put("n", 1) }
    event("state.captured") {
        put("url", "https://github.com/browser-use/browser-use")
        put("title", "browser-use/browser-use")
    }
    delay(450)
    event("model.output") {
        put("thinking", "The task is to find the repository star count. I will open the repository page first.")
        put("evaluation_previous_goal", "No previous action. Starting the task. Verdict: Success")
        put("memory", "Task: find stars of browser-use. No page open yet.")
        put("next_goal", "Navigate to the browser-use GitHub repository.")
        put("actions", buildJsonArray {
            add(buildJsonObject {
                put("name", "navigate")
                put("params", buildJsonObject { put("url", "https://github.com/browser-use/browser-use") })
            })
        })
    }
    event("action.started") {
        put("name", "navigate")
        put("params", buildJsonObject { put("url", "https://github.com/browser-use/browser-use") })
    }
    delay(650)
    event("action.finished") {
        put("name", "navigate")
        put("result", "Navigated to https://github.com/browser-use/browser-use")
    }
    event("step.finished") { put("n", 1); put("duration_ms", 1100) }

    delay(250)
    event("step.started") { put("n", 2) }
    event("state.captured") {
        put("url", "https://github.com/browser-use/browser-use")
        put("title", "browser-use/browser-use: Make websites accessible for AI agents")
    }
    delay(400)
    event("model.output") {
        put("thinking", "The star count is visible near the top of the repository page. I will extract it.")
        put("evaluation_previous_goal", "Navigation succeeded and the repository page is visible. Verdict: Success")
        put("memory", "On the browser-use repository page. Star count appears in the header area.")
        put("next_goal", "Extract the star count from the page.")
        put("actions", buildJsonArray {
            add(buildJsonObject {
                put("name", "extract")
                put("params", buildJsonObject { put("query", "star count of the repository") })
            })
        })
    }
    event("action.started") {
        put("name", "extract")
        put("params", buildJsonObject { put("query", "star count of the repository") })
    }
    delay(700)
    event("action.finished") {
        put("name", "extract")
        put("result", "browser-use has 123,456 stars (mock fixture).")
    }
    event("step.finished") { put("n", 2); put("duration_ms", 1300) }

    delay(250)
    event("step.started") { put("n", 3) }
    delay(300)
    event("model.output") {
        put("thinking", "I have the number. I will save it to a file and finish.")
        put("evaluation_previous_goal", "The extraction returned the star count. Verdict: Success")
        put("memory", "Star count: 123,456.")
        put("next_goal", "Write the result to stars.txt and complete the task.")
        put("actions", buildJsonArray {
            add(buildJsonObject {
                put("name", "write_file")
                put("params", buildJsonObject {
                    put("file_name", "stars.txt")
                    put("content", "123456")
                })
            })
        })
    }
    event("action.started") {
        put("name", "write_file")
        put("params", buildJsonObject { put("file_name", "stars.txt") })
    }
    delay(450)
    event("action.finished") {
        put("name", "write_file")
        put("result", "Wrote 6 characters to stars.txt")
    }
    event("step.finished") { put("n", 3); put("duration_ms", 850) }

    delay(200)
    val output = "browser-use has 123,456 stars. I saved the number to stars.txt."
    event("run.finished") {
        put("success", true)
        put("output", output)
        put("costUsd", 0.012)
        put("tokens", 842)
    }
    store.finish(run.id, status = "idle", output = output)
}
