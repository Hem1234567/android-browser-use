package com.browseruse.android

import com.browseruse.android.data.EventReducer
import com.browseruse.android.model.ActionStatus
import com.browseruse.android.model.AppUiState
import com.browseruse.android.model.RunEvent
import com.browseruse.android.model.SessionStatus
import com.browseruse.android.model.SessionUi
import com.browseruse.android.model.StepStatus
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EventReducerTest {

    private fun event(type: String, block: JsonObjectBuilder.() -> Unit = {}): RunEvent =
        RunEvent(type, buildJsonObject(block))

    @Test
    fun `run produces steps, actions and final answer`() {
        var state = AppUiState(current = SessionUi(id = "s1", task = "find stars"))

        state = EventReducer.reduce(state, event("run.started"))
        state = EventReducer.reduce(state, event("step.started") { put("n", 1) })
        state = EventReducer.reduce(
            state,
            event("model.output") {
                put("thinking", "Open the repo")
                put("evaluation_previous_goal", "Starting")
                put("memory", "No page yet")
                put("next_goal", "Navigate")
                put(
                    "actions",
                    buildJsonArray {
                        add(
                            buildJsonObject {
                                put("name", "navigate")
                                put("params", buildJsonObject { put("url", "https://example.com") })
                            }
                        )
                    }
                )
            }
        )
        state = EventReducer.reduce(state, event("action.started") { put("name", "navigate") })
        state = EventReducer.reduce(state, event("action.finished") {
            put("name", "navigate")
            put("result", "ok")
        })
        state = EventReducer.reduce(state, event("step.finished") { put("n", 1) })

        val step = state.current!!.steps.single()
        assertEquals(StepStatus.Done, step.status)
        assertEquals(1, step.actions.size)
        assertEquals(ActionStatus.Done, step.actions.single().status)
        assertEquals("ok", step.actions.single().result)

        state = EventReducer.reduce(state, event("run.finished") {
            put("success", true)
            put("output", "123,456 stars")
            put("tokens", 100)
        })

        assertEquals(SessionStatus.Done, state.current!!.status)
        assertEquals("123,456 stars", state.current!!.finalAnswer)
        assertEquals(1, state.history.size)
        assertNotNull(state.history.single().finalAnswer)
    }

    @Test
    fun `failure marks running steps as error`() {
        var state = AppUiState(current = SessionUi(id = "s2", task = "fail"))
        state = EventReducer.reduce(state, event("step.started") { put("n", 1) })
        state = EventReducer.reduce(state, event("run.failed") { put("message", "boom") })

        assertEquals(SessionStatus.Error, state.current!!.status)
        assertEquals(StepStatus.Error, state.current!!.steps.single().status)
        assertEquals("boom", state.current!!.error)
    }
}
