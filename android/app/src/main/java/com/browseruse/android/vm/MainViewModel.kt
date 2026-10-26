package com.browseruse.android.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.browseruse.android.data.AgentRepository
import com.browseruse.android.data.ApiClient
import com.browseruse.android.data.EventReducer
import com.browseruse.android.model.AppSettings
import com.browseruse.android.model.AppUiState
import com.browseruse.android.model.Screen
import com.browseruse.android.model.SessionStatus
import com.browseruse.android.model.SessionUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    var state by mutableStateOf(AppUiState())
        private set

    private val repository = AgentRepository()
    private val api = ApiClient(com.browseruse.android.BuildConfig.API_BASE_URL)
    private var runJob: Job? = null

    fun startTask(task: String) {
        val text = task.trim()
        if (text.isEmpty()) return
        runJob?.cancel()
        val session = SessionUi(
            id = "local-${System.currentTimeMillis()}",
            task = text,
            model = state.settings.model
        )
        state = state.copy(
            screen = Screen.Session,
            current = session,
            busy = true,
            banner = null,
            showBrowser = false
        )
        runJob = viewModelScope.launch {
            repository.run(text, state.settings).collect { event ->
                state = EventReducer.reduce(state, event)
            }
            state = state.copy(busy = false)
        }
    }

    fun stop() {
        val current = state.current ?: return
        runJob?.cancel()
        val stopped = current.copy(
            status = SessionStatus.Stopped,
            finalAnswer = current.finalAnswer ?: "Stopped by user."
        )
        state = state.copy(
            current = stopped,
            busy = false,
            history = (listOf(stopped) + state.history.filterNot { it.id == stopped.id }).take(50)
        )
        viewModelScope.launch { api.stop(current.id) }
    }

    fun goHome() {
        state = state.copy(screen = Screen.Home, current = null, busy = false, showBrowser = false)
    }

    fun open(session: SessionUi) {
        state = state.copy(screen = Screen.Session, current = session, busy = session.status == SessionStatus.Running, showBrowser = false)
    }

    fun navigate(screen: Screen) {
        state = state.copy(screen = screen)
    }

    fun back() {
        if (state.screen != Screen.Home) {
            state = state.copy(screen = Screen.Home, showBrowser = false)
        }
    }

    fun toggleBrowser() {
        state = state.copy(showBrowser = !state.showBrowser)
    }

    fun setModel(model: String) {
        state = state.copy(settings = state.settings.copy(model = model))
    }

    fun setVision(value: Boolean) {
        state = state.copy(settings = state.settings.copy(vision = value))
    }

    fun setFlash(value: Boolean) {
        state = state.copy(settings = state.settings.copy(flash = value))
    }

    fun setMaxSteps(value: Int) {
        state = state.copy(settings = state.settings.copy(maxSteps = value.coerceIn(5, 200)))
    }

    fun clearBanner() {
        state = state.copy(banner = null)
    }

    fun liveUrl(): String? {
        val current = state.current ?: return null
        return api.liveUrl(current.id, current.liveUrl)
    }

    fun updateSettings(settings: AppSettings) {
        state = state.copy(settings = settings)
    }

    override fun onCleared() {
        runJob?.cancel()
        super.onCleared()
    }
}
