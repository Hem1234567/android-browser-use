"""
browser-use engine wrapper (v0.1 scaffold).

Two modes:
  - mock: deterministic event stream, no Chromium. Used by tests and the demo.
  - browser: reserved for the real browser-use==0.13.13 adapter. It is intentionally
    not enabled in this scaffold; see README and IMPLEMENTATION_PLAN.md for the
    production design (one session pod: Python engine + Chromium + browserd).

The event schema emitted here is the contract the Kotlin backend/Android app consume.
"""

from __future__ import annotations

import asyncio
import json
import time
import uuid
from typing import Any

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

app = FastAPI(title="browser-use engine", version="0.1.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

SESSIONS: dict[str, dict[str, Any]] = {}
RUNS: dict[str, dict[str, Any]] = {}
EVENTS: dict[str, list[dict[str, Any]]] = {}
FINISHED: set[str] = set()


class BrowserConfig(BaseModel):
    mode: str = "mock"
    cdp_url: str | None = None
    profile_dir: str | None = None
    downloads_dir: str | None = None
    files_dir: str | None = None
    viewport: dict[str, int] = Field(default_factory=lambda: {"width": 1280, "height": 720})


class NewSession(BaseModel):
    browser: BrowserConfig = Field(default_factory=BrowserConfig)


class NewRun(BaseModel):
    task: str
    settings: dict[str, Any] = Field(default_factory=dict)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "version": "0.1.0", "engine": "mock"}


@app.post("/engine/v1/sessions", status_code=201)
def create_session(body: NewSession | None = None) -> dict[str, Any]:
    config = (body or NewSession()).browser
    if config.mode not in ("mock", "browser"):
        raise HTTPException(status_code=400, detail="browser.mode must be mock or browser")
    session_id = f"engine_{uuid.uuid4().hex[:8]}"
    session = {
        "id": session_id,
        "status": "idle",
        "created": time.time(),
        "mode": config.mode,
        "live_url": f"/engine/v1/sessions/{session_id}/live",
    }
    SESSIONS[session_id] = session
    return session


@app.get("/engine/v1/sessions/{session_id}")
def get_session(session_id: str) -> dict[str, Any]:
    session = SESSIONS.get(session_id)
    if session is None:
        raise HTTPException(status_code=404, detail="session not found")
    return session


@app.post("/engine/v1/sessions/{session_id}/runs", status_code=201)
async def create_run(session_id: str, body: NewRun) -> dict[str, Any]:
    session = SESSIONS.get(session_id)
    if session is None:
        raise HTTPException(status_code=404, detail="session not found")
    if not body.task.strip():
        raise HTTPException(status_code=400, detail="task is required")
    if session["mode"] == "browser":
        raise HTTPException(
            status_code=501,
            detail="real browser-use adapter is not enabled in this scaffold; see IMPLEMENTATION_PLAN.md",
        )
    run_id = f"run_{uuid.uuid4().hex[:8]}"
    RUNS[run_id] = {"id": run_id, "session_id": session_id, "task": body.task, "status": "running"}
    EVENTS[run_id] = []
    session["status"] = "running"
    asyncio.create_task(_scenario(run_id, session_id, body.task))
    return {"runId": run_id}


@app.get("/engine/v1/runs/{run_id}/events")
async def run_events(run_id: str) -> StreamingResponse:
    if run_id not in RUNS:
        raise HTTPException(status_code=404, detail="run not found")

    async def stream():
        index = 0
        yield "retry: 1000\n\n"
        while True:
            events = EVENTS.get(run_id, [])
            while index < len(events):
                event = events[index]
                index += 1
                yield f"id: {index}\ndata: {json.dumps(event)}\n\n"
            if run_id in FINISHED:
                break
            await asyncio.sleep(0.12)

    return StreamingResponse(
        stream(),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )


@app.post("/engine/v1/sessions/{session_id}/stop")
def stop_session(session_id: str) -> dict[str, str]:
    session = SESSIONS.get(session_id)
    if session is None:
        raise HTTPException(status_code=404, detail="session not found")
    session["status"] = "stopped"
    for run in RUNS.values():
        if run["session_id"] == session_id and run["status"] == "running":
            run["status"] = "stopped"
            FINISHED.add(run["id"])
    return {"status": "stopped"}


def _emit(run_id: str, event_type: str, payload: dict[str, Any] | None = None) -> None:
    EVENTS.setdefault(run_id, []).append({"type": event_type, "payload": payload or {}})


async def _scenario(run_id: str, session_id: str, task: str) -> None:
    """Same shape as backend DemoScenario and the real agent step stream."""
    _emit(run_id, "run.started", {"runId": run_id, "sessionId": session_id, "task": task})
    await asyncio.sleep(0.35)

    _emit(run_id, "step.started", {"n": 1})
    _emit(run_id, "state.captured", {"url": "https://github.com/browser-use/browser-use", "title": "browser-use/browser-use"})
    await asyncio.sleep(0.3)
    _emit(
        run_id,
        "model.output",
        {
            "thinking": "Open the repository page to read the star count.",
            "evaluation_previous_goal": "No previous action. Verdict: Success",
            "memory": "Task: find stars.",
            "next_goal": "Navigate to the repository.",
            "actions": [{"name": "navigate", "params": {"url": "https://github.com/browser-use/browser-use"}}],
        },
    )
    _emit(run_id, "action.started", {"name": "navigate", "params": {"url": "https://github.com/browser-use/browser-use"}})
    await asyncio.sleep(0.4)
    _emit(run_id, "action.finished", {"name": "navigate", "result": "Navigated"})
    _emit(run_id, "step.finished", {"n": 1, "duration_ms": 900})

    _emit(run_id, "step.started", {"n": 2})
    await asyncio.sleep(0.3)
    _emit(
        run_id,
        "model.output",
        {
            "thinking": "The star count is in the header. Extract it.",
            "evaluation_previous_goal": "Navigation succeeded. Verdict: Success",
            "memory": "On the repository page.",
            "next_goal": "Extract stars.",
            "actions": [{"name": "extract", "params": {"query": "star count"}}],
        },
    )
    _emit(run_id, "action.started", {"name": "extract", "params": {"query": "star count"}})
    await asyncio.sleep(0.4)
    _emit(run_id, "action.finished", {"name": "extract", "result": "123,456 stars"})
    _emit(run_id, "step.finished", {"n": 2, "duration_ms": 800})

    _emit(run_id, "step.started", {"n": 3})
    await asyncio.sleep(0.25)
    _emit(
        run_id,
        "model.output",
        {
            "thinking": "Save the result and finish.",
            "evaluation_previous_goal": "Extraction succeeded. Verdict: Success",
            "memory": "Stars: 123,456.",
            "next_goal": "Write stars.txt.",
            "actions": [{"name": "write_file", "params": {"file_name": "stars.txt", "content": "123456"}}],
        },
    )
    _emit(run_id, "action.started", {"name": "write_file", "params": {"file_name": "stars.txt"}})
    await asyncio.sleep(0.3)
    _emit(run_id, "action.finished", {"name": "write_file", "result": "Wrote stars.txt"})
    _emit(run_id, "step.finished", {"n": 3, "duration_ms": 700})

    output = "browser-use has 123,456 stars. Saved to stars.txt."
    _emit(run_id, "run.finished", {"success": True, "output": output, "costUsd": 0.012, "tokens": 842})
    RUNS[run_id]["status"] = "done"
    SESSIONS[session_id]["status"] = "idle"
    FINISHED.add(run_id)
