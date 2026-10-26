package com.browseruse.android.model

import com.browseruse.android.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

enum class Screen { Home, Session, Library, Settings }

enum class SessionStatus { Idle, Running, Done, Error, Stopped }

enum class StepStatus { Pending, Running, Done, Error }

enum class ActionStatus { Pending, Running, Done, Error }

data class UiAction(
    val name: String,
    val label: String,
    val value: String = "",
    val status: ActionStatus = ActionStatus.Pending,
    val result: String? = null
)

data class UiStep(
    val index: Int,
    val title: String,
    val status: StepStatus = StepStatus.Pending,
    val actions: List<UiAction> = emptyList(),
    val thinking: String? = null,
    val evaluation: String? = null,
    val memory: String? = null,
    val nextGoal: String? = null,
    val url: String? = null,
    val pageTitle: String? = null,
    val durationMs: Long? = null
)

data class SessionUi(
    val id: String,
    val task: String,
    val liveUrl: String? = null,
    val status: SessionStatus = SessionStatus.Running,
    val steps: List<UiStep> = emptyList(),
    val finalAnswer: String? = null,
    val error: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val model: String = "bu-mini",
    val costUsd: Double = 0.0,
    val tokens: Int = 0
)

data class AppSettings(
    val model: String = "bu-mini",
    val vision: Boolean = true,
    val flash: Boolean = false,
    val maxSteps: Int = 50,
    val backendUrl: String = BuildConfig.API_BASE_URL,
    val profileId: String? = null,
    val workspaceId: String? = null,
    val proxyCountryCode: String? = null
)

data class AppUiState(
    val screen: Screen = Screen.Home,
    val current: SessionUi? = null,
    val history: List<SessionUi> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val busy: Boolean = false,
    val banner: String? = null,
    val backendOnline: Boolean = false,
    val showBrowser: Boolean = false
)

@Serializable
data class RunEvent(
    val type: String,
    val payload: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class CreateSessionRequest(
    val model: String,
    val profileId: String? = null,
    val workspaceId: String? = null,
    val proxyCountryCode: String? = null
)

@Serializable
data class SessionDto(
    val id: String,
    val status: String = "idle",
    val liveUrl: String? = null
)

@Serializable
data class CreateRunRequest(val task: String)

@Serializable
data class RunCreated(val runId: String)
