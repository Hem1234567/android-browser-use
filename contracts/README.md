# Wire contracts

## Run events (SSE)

`GET /v1/runs/{runId}/events` returns `text/event-stream`:

```
id: 1
data: {"type":"run.started","payload":{"runId":"run_ab12","sessionId":"sess_cd34"}}

id: 2
data: {"type":"step.started","payload":{"n":1}}
```

- `id` is the monotonically increasing event sequence. Clients may reconnect with
  `Last-Event-ID` (future backend work) or replay from zero.
- `data` is one JSON object validated by [`events.schema.json`](events.schema.json).
- A heartbeat comment (`: ping`) will be added in the production backend; the scaffold streams
  a `retry:` directive and relies on the load balancer idle timeout.

### Event payloads used by the scaffold

| Type | Payload |
|---|---|
| `backend.status` | `{online: bool}` |
| `session.created` | `{sessionId, liveUrl}` |
| `run.started` | `{runId, sessionId, task}` |
| `step.started` | `{n}` |
| `state.captured` | `{url, title}` |
| `model.output` | `{thinking, evaluation_previous_goal, memory, next_goal, actions:[{name, params}]}` |
| `action.started` | `{name, params}` |
| `action.finished` | `{name, result?, error?}` |
| `step.finished` | `{n, duration_ms}` |
| `run.finished` | `{success, output, costUsd?, tokens?}` |
| `run.failed` | `{message}` |

The engine emits the same shapes; `payload.actions` also accepts the native browser-use form
`[{"navigate": {"url": "..."}}]` — the Android reducer supports both.

## Live browser (WebSocket)

`WS /v1/sessions/{id}/live`

Server → client binary frame:

```
+--------+----------------+----------------+------------------+
| type 1 | length (4, BE) | seq (4, BE)    | payload (JPEG)   |
+--------+----------------+----------------+------------------+
```

- `type=1`: JPEG frame. The client decodes the payload and draws it.
- `type=2`: reserved for JSON status frames (the scaffold sends status as text frames).
- Text frames: `{"type":"connected"|"ack", ...}`.

Client → server: UTF-8 JSON messages (tap/key/navigation). The scaffold echoes an `ack`; input
injection over CDP is part of the next phase.

## Endpoint summary

| Method | Path | Notes |
|---|---|---|
| GET | `/health` | unauthenticated |
| POST | `/v1/sessions` | `{model, profileId?, workspaceId?, proxyCountryCode?}` |
| GET | `/v1/sessions/{id}` | status |
| POST | `/v1/sessions/{id}/runs` | `{task}` → `{runId}` |
| GET | `/v1/runs/{id}/events` | SSE |
| POST | `/v1/sessions/{id}/stop` | stop task/session |
| WS | `/v1/sessions/{id}/live` | frames + input |
