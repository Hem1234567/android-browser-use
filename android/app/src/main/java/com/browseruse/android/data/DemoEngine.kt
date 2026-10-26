package com.browseruse.android.data

import com.browseruse.android.model.RunEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object DemoEngine {

    fun events(task: String): Flow<RunEvent> = flow {
        val id = "demo-${System.currentTimeMillis()}"
        emit(event("backend.status") { put("online", false) })
        emit(event("session.created") {
            put("sessionId", id)
            put("liveUrl", "")
        })
        emit(event("run.started") {
            put("runId", "run-$id")
            put("task", task)
        })

        delay(350)
        emit(event("step.started") { put("n", 1) })
        emit(event("state.captured") {
            put("url", "https://github.com/browser-use/browser-use")
            put("title", "browser-use/browser-use")
        })
        delay(450)
        emit(event("model.output") {
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
        })
        emit(event("action.started") {
            put("name", "navigate")
            put("params", buildJsonObject { put("url", "https://github.com/browser-use/browser-use") })
        })
        delay(700)
        emit(event("action.finished") {
            put("name", "navigate")
            put("result", "Navigated to https://github.com/browser-use/browser-use")
        })
        emit(event("step.finished") { put("n", 1); put("duration_ms", 1200) })

        delay(300)
        emit(event("step.started") { put("n", 2) })
        emit(event("state.captured") {
            put("url", "https://github.com/browser-use/browser-use")
            put("title", "browser-use/browser-use: Make websites accessible for AI agents")
        })
        delay(450)
        emit(event("model.output") {
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
        })
        emit(event("action.started") {
            put("name", "extract")
            put("params", buildJsonObject { put("query", "star count of the repository") })
        })
        delay(800)
        emit(event("action.finished") {
            put("name", "extract")
            put("result", "browser-use has 123,456 stars (demo fixture).")
        })
        emit(event("step.finished") { put("n", 2); put("duration_ms", 1400) })

        delay(300)
        emit(event("step.started") { put("n", 3) })
        delay(300)
        emit(event("model.output") {
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
        })
        emit(event("action.started") {
            put("name", "write_file")
            put("params", buildJsonObject { put("file_name", "stars.txt") })
        })
        delay(500)
        emit(event("action.finished") {
            put("name", "write_file")
            put("result", "Wrote 6 characters to stars.txt")
        })
        emit(event("step.finished") { put("n", 3); put("duration_ms", 900) })

        delay(250)
        emit(event("run.finished") {
            put("success", true)
            put("output", "browser-use has 123,456 stars. I saved the number to stars.txt.")
            put("costUsd", 0.012)
            put("tokens", 842)
        })
    }

    private fun event(type: String, block: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit): RunEvent =
        RunEvent(type, buildJsonObject(block))
}
