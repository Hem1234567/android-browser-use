from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "ok"
    assert body["engine"] == "mock"


def test_session_and_run_events():
    session = client.post("/engine/v1/sessions", json={"browser": {"mode": "mock"}})
    assert session.status_code == 201
    session_id = session.json()["id"]

    run = client.post(f"/engine/v1/sessions/{session_id}/runs", json={"task": "find stars"})
    assert run.status_code == 201
    run_id = run.json()["runId"]

    with client.stream("GET", f"/engine/v1/runs/{run_id}/events") as response:
        assert response.status_code == 200
        body = "".join(response.iter_text())

    assert "run.finished" in body
    assert "navigate" in body
    assert "123,456" in body


def test_missing_session_is_404():
    assert client.get("/engine/v1/sessions/nope").status_code == 404
