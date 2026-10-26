package com.browseruse.android.data

import com.browseruse.android.model.AppSettings
import com.browseruse.android.model.RunEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AgentRepository(
    private val api: ApiClient = ApiClient(com.browseruse.android.BuildConfig.API_BASE_URL)
) {

    fun run(task: String, settings: AppSettings): Flow<RunEvent> = flow {
        val prepared = runCatching { api.prepare(task, settings) }.getOrNull()
        if (prepared == null) {
            emitAll(DemoEngine.events(task))
        } else {
            emit(RunEvent("backend.status", buildJsonObject { put("online", true) }))
            emit(
                RunEvent(
                    "session.created",
                    buildJsonObject {
                        put("sessionId", prepared.sessionId)
                        put("liveUrl", prepared.liveUrl ?: "")
                    }
                )
            )
            emitAll(api.stream(prepared.runId))
        }
    }.catch { throwable ->
        emit(
            RunEvent(
                "run.failed",
                buildJsonObject { put("message", throwable.message ?: "Connection failed") }
            )
        )
    }
}
