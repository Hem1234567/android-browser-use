package com.browseruse.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class Health(val status: String, val version: String, val engine: String)

@Serializable
data class NewSession(
    val model: String = "bu-mini",
    val profileId: String? = null,
    val workspaceId: String? = null,
    val proxyCountryCode: String? = null
)

@Serializable
data class Session(
    val id: String,
    var status: String = "idle",
    val model: String = "bu-mini",
    val liveUrl: String,
    val createdAt: Long,
    val profileId: String? = null,
    val workspaceId: String? = null,
    val proxyCountryCode: String? = null,
    var output: String? = null
)

@Serializable
data class NewRun(val task: String)

@Serializable
data class Run(
    val id: String,
    val sessionId: String,
    val task: String,
    var status: String = "running"
)

@Serializable
data class RunCreated(val runId: String)

@Serializable
data class Event(val type: String, val payload: JsonObject = buildJsonObject { })

@Serializable
data class SimpleStopped(val status: String)
