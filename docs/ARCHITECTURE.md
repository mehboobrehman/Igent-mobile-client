# Igent Live Telemetry Architecture & Protocol Specification

## 1. Architecture Overview

The **Igent** live debug telemetry and diagnostics system is engineered for lightweight, high-performance, non-blocking telemetry collection from Android mobile devices during development, QA testing, and staging.

### Text-Based Architecture Diagram

```
+---------------------------------------------------------------------------------+
|                                 Android App                                     |
|                               (com.igent.mobile)                                |
|                                                                                 |
|  +------------------+    +-------------------+    +--------------------------+  |
|  |  Crash & ANR     |    |  Network Interceptor|    | Application Logs & Events|  |
|  |  Handlers        |    |  (OkHttp / Retrofit)|    | (Lifecycle, State, etc.) |  |
|  +--------+---------+    +---------+---------+    +------------+-------------+  |
|           |                        |                           |                |
|           +------------------------+---------------------------+                |
|                                    v                                            |
|                       +--------------------------+                              |
|                       | Telemetry Buffer & Queue  |                              |
|                       | (Local Ring Buffer/Room) |                              |
|                       +------------+-------------+                              |
|                                    |                                            |
+------------------------------------+--------------------------------------------+
                                     | HTTP POST (/api/telemetry)
                                     | [ADB Reverse Port Forwarding / Wi-Fi]
                                     v
+---------------------------------------------------------------------------------+
|                         Telemetry Collector Server                              |
|                             (Node.js / Express)                                 |
|                                                                                 |
|  +---------------------------------------------------------------------------+  |
|  | Authentication & Validation Middleware (X-Telemetry-Token / Schema V1.0.0)|  |
|  +-----------------------------------+---------------------------------------+  |
|                                      |                                          |
|         +----------------------------+----------------------------+             |
|         v                                                         v             |
|  +------------------------------+                       +--------------------+  |
|  | In-Memory Ring Buffer        |                       | JSONL Disk Storage |  |
|  | (Max 2000 events)            |                       | (/sessions/*.jsonl)|  |
|  +------+-----------------------+                       +---------+----------+  |
|         |                                                         |             |
|         +----------------------------+----------------------------+             |
|                                      |                                          |
|        +-----------------------------+-----------------------------+            |
|        v                             v                             v            |
|  +---------------+             +---------------+             +---------------+  |
|  | Web Dashboard |             | CLI Tailing   |             | REST / HAR    |  |
|  | (HTML/SSE)    |             | (`npm run tail)|            | Export API    |  |
|  +---------------+             +---------------+             +---------------+  |
+---------------------------------------------------------------------------------+
```

---

## 2. Protocol Specification

All events transmitted from the mobile client to the telemetry server are encapsulated in a strict versioned JSON envelope adhering to schema version `1.0.0`.

### Envelope Structure

```json
{
  "schemaVersion": "1.0.0",
  "sessionId": "session_1711234567890",
  "deviceId": "emulator-5554",
  "seq": 1,
  "timestamp": 1711234567890,
  "wallTime": 1711234567890,
  "type": "logs",
  "payload": {
    "level": "INFO",
    "tag": "NetworkModule",
    "message": "Fetching user profile"
  }
}
```

### Required Envelope Fields

| Field | Type | Description |
|---|---|---|
| `schemaVersion` | String | Must be `"1.0.0"` |
| `sessionId` | String | Unique identifier for the current app session |
| `deviceId` | String | Unique hardware or emulator identifier |
| `seq` | Number | Monotonically increasing sequence number per session |
| `timestamp` | Number | Epoch millisecond timestamp of event occurrence |
| `wallTime` | Number | Wall clock millisecond timestamp |
| `type` | String | Telemetry category (see supported types below) |
| `payload` | Object | Event-specific data structure |

### Supported Telemetry Types (`type`)

1. **`logs`**: Structured application logs (`DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL`).
2. **`crashes`**: Uncaught exceptions and fatal runtime errors.
3. **`anrs`**: Application Not Responding (UI thread blockage events).
4. **`network`**: HTTP/HTTPS request and response telemetry (method, URL, status code, duration, sizes).
5. **`performance`**: CPU, memory, frame drop, and startup latency metrics.
6. **`lifecycle`**: Activity/Fragment lifecycle events (created, started, resumed, paused, stopped, destroyed).
7. **`state`**: ViewState, ViewModel state changes, and state machine transitions.
8. **`config`**: Remote configuration fetches and feature flag evaluations.
9. **`commands`**: Developer remote commands issued to the app.
10. **`heartbeats`**: Periodic liveness pings from the client.
