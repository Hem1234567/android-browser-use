package com.browseruse.server

import kotlinx.serialization.json.JsonObject
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class Store {

    private val sessions = ConcurrentHashMap<String, Session>()
    private val runs = ConcurrentHashMap<String, Run>()
    private val events = ConcurrentHashMap<String, CopyOnWriteArrayList<Event>>()
    private val finished = ConcurrentHashMap.newKeySet<String>()

    fun createSession(request: NewSession): Session {
        val id = "sess_${UUID.randomUUID().toString().take(8)}"
        val session = Session(
            id = id,
            model = request.model,
            liveUrl = "/v1/sessions/$id/live",
            createdAt = System.currentTimeMillis(),
            profileId = request.profileId,
            workspaceId = request.workspaceId,
            proxyCountryCode = request.proxyCountryCode
        )
        sessions[id] = session
        return session
    }

    fun session(id: String): Session? = sessions[id]

    fun createRun(sessionId: String, task: String): Run {
        val run = Run(
            id = "run_${UUID.randomUUID().toString().take(8)}",
            sessionId = sessionId,
            task = task
        )
        runs[run.id] = run
        events[run.id] = CopyOnWriteArrayList()
        sessions[sessionId]?.status = "running"
        return run
    }

    fun run(id: String): Run? = runs[id]

    fun addEvent(runId: String, type: String, payload: JsonObject = kotlinx.serialization.json.buildJsonObject { }) {
        events.getOrPut(runId) { CopyOnWriteArrayList() }.add(Event(type, payload))
    }

    fun eventList(runId: String): List<Event> = events[runId] ?: emptyList()

    fun finish(runId: String, status: String = "idle", output: String? = null) {
        runs[runId]?.status = "done"
        finished.add(runId)
        val run = runs[runId]
        if (run != null) {
            val session = sessions[run.sessionId]
            if (session != null) {
                session.status = status
                if (output != null) session.output = output
            }
        }
    }

    fun isFinished(runId: String): Boolean = finished.contains(runId)

    fun stop(sessionId: String): Boolean {
        val session = sessions[sessionId] ?: return false
        session.status = "stopped"
        runs.values.filter { it.sessionId == sessionId && it.status == "running" }.forEach {
            it.status = "stopped"
            finished.add(it.id)
        }
        return true
    }
}
