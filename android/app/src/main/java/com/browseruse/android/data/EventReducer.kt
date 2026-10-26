package com.browseruse.android.data

import com.browseruse.android.model.ActionStatus
import com.browseruse.android.model.AppUiState
import com.browseruse.android.model.RunEvent
import com.browseruse.android.model.SessionStatus
import com.browseruse.android.model.SessionUi
import com.browseruse.android.model.StepStatus
import com.browseruse.android.model.UiAction
import com.browseruse.android.model.UiStep
import com.browseruse.android.util.arr
import com.browseruse.android.util.bool
import com.browseruse.android.util.double
import com.browseruse.android.util.int
import com.browseruse.android.util.obj
import com.browseruse.android.util.str
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

object EventReducer {

    fun reduce(state: AppUiState, event: RunEvent): AppUiState {
        val current = state.current ?: return state
        val p = event.payload
        return when (event.type) {
            "backend.status" -> state.copy(backendOnline = p.bool("online") ?: false)

            "session.created" -> state.copy(
                current = current.copy(
                    id = p.str("sessionId") ?: current.id,
                    liveUrl = p.str("liveUrl")?.takeUnless { it.isBlank() } ?: current.liveUrl
                )
            )

            "run.started" -> state.copy(current = current.copy(status = SessionStatus.Running))

            "step.started" -> {
                val n = p.int("n") ?: (current.steps.size + 1)
                state.copy(
                    current = current.copy(
                        steps = current.steps + UiStep(n, "Step $n", StepStatus.Running)
                    )
                )
            }

            "model.output" -> {
                val fresh = UiStep(
                    index = (current.steps.lastOrNull()?.index ?: 0) + if (current.steps.isEmpty()) 1 else 0,
                    title = "Step ${current.steps.size.coerceAtLeast(1)}",
                    status = StepStatus.Running,
                    thinking = p.str("thinking"),
                    evaluation = p.str("evaluation_previous_goal"),
                    memory = p.str("memory"),
                    nextGoal = p.str("next_goal"),
                    actions = parseActions(p)
                )
                val steps = if (current.steps.isEmpty()) {
                    listOf(fresh)
                } else {
                    current.steps.dropLast(1) + current.steps.last().copy(
                        thinking = p.str("thinking") ?: current.steps.last().thinking,
                        evaluation = p.str("evaluation_previous_goal") ?: current.steps.last().evaluation,
                        memory = p.str("memory") ?: current.steps.last().memory,
                        nextGoal = p.str("next_goal") ?: current.steps.last().nextGoal,
                        actions = parseActions(p),
                        status = StepStatus.Running
                    )
                }
                state.copy(current = current.copy(steps = steps))
            }

            "action.started" -> {
                val name = p.str("name") ?: return state
                val params = p.obj("params")
                state.copy(current = current.updateLastStep { step ->
                    val existing = step.actions.indexOfFirst { it.name == name && it.status == ActionStatus.Pending }
                    val action = UiAction(name, com.browseruse.android.util.ToolLabels.label(name), com.browseruse.android.util.ToolLabels.value(name, params), ActionStatus.Running)
                    step.copy(
                        actions = if (existing >= 0) {
                            step.actions.toMutableList().also { it[existing] = it[existing].copy(status = ActionStatus.Running) }
                        } else {
                            step.actions + action
                        }
                    )
                })
            }

            "action.finished" -> {
                val name = p.str("name") ?: return state
                val error = p.str("error")
                val result = p.str("result") ?: p.str("extracted_content") ?: p.str("long_term_memory")
                state.copy(current = current.updateLastStep { step ->
                    val idx = step.actions.indexOfFirst { it.name == name && it.status != ActionStatus.Done }
                    if (idx < 0) {
                        step
                    } else {
                        step.copy(
                            actions = step.actions.toMutableList().also {
                                it[idx] = it[idx].copy(
                                    status = if (error.isNullOrBlank()) ActionStatus.Done else ActionStatus.Error,
                                    result = error ?: result
                                )
                            }
                        )
                    }
                })
            }

            "state.captured" -> state.copy(
                current = current.updateLastStep {
                    it.copy(url = p.str("url") ?: it.url, pageTitle = p.str("title") ?: it.pageTitle)
                }
            )

            "step.finished" -> state.copy(
                current = current.updateLastStep {
                    it.copy(status = StepStatus.Done, durationMs = p.int("duration_ms")?.toLong())
                }
            )

            "run.finished" -> {
                val ok = p.bool("success") ?: true
                val finished = current.copy(
                    status = if (ok) SessionStatus.Done else SessionStatus.Error,
                    finalAnswer = p.str("output") ?: p.str("result") ?: current.finalAnswer,
                    error = p.str("error"),
                    costUsd = p.double("costUsd") ?: current.costUsd,
                    tokens = p.int("tokens") ?: current.tokens,
                    steps = current.steps.map {
                        if (it.status == StepStatus.Running) it.copy(status = StepStatus.Done) else it
                    }
                )
                state.copy(
                    current = finished,
                    history = (listOf(finished) + state.history.filterNot { it.id == finished.id }).take(50),
                    busy = false
                )
            }

            "run.failed" -> {
                val failed = current.copy(
                    status = SessionStatus.Error,
                    error = p.str("message") ?: p.str("error") ?: "Run failed",
                    steps = current.steps.map {
                        if (it.status == StepStatus.Running) it.copy(status = StepStatus.Error) else it
                    }
                )
                state.copy(
                    current = failed,
                    history = (listOf(failed) + state.history.filterNot { it.id == failed.id }).take(50),
                    busy = false
                )
            }

            "error" -> state.copy(banner = p.str("message") ?: "Unexpected error")

            else -> state
        }
    }

    private fun SessionUi.updateLastStep(transform: (UiStep) -> UiStep): SessionUi {
        if (steps.isEmpty()) {
            return copy(steps = listOf(transform(UiStep(1, "Step 1", StepStatus.Running))))
        }
        return copy(steps = steps.dropLast(1) + transform(steps.last()))
    }

    private fun parseActions(p: JsonObject): List<UiAction> {
        val array = p.arr("actions") ?: return emptyList()
        return array.mapNotNull { element ->
            val obj = runCatching { element.jsonObject }.getOrNull() ?: return@mapNotNull null
            val named = obj.str("name")
            if (named != null) {
                val params = obj.obj("params")
                UiAction(
                    name = named,
                    label = com.browseruse.android.util.ToolLabels.label(named),
                    value = com.browseruse.android.util.ToolLabels.value(named, params)
                )
            } else {
                val entry = obj.entries.firstOrNull() ?: return@mapNotNull null
                val params = runCatching { entry.value.jsonObject }.getOrNull()
                UiAction(
                    name = entry.key,
                    label = com.browseruse.android.util.ToolLabels.label(entry.key),
                    value = com.browseruse.android.util.ToolLabels.value(entry.key, params)
                )
            }
        }
    }
}
