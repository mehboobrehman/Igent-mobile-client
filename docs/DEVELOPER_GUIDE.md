# Igent Developer Guide & Troubleshooting

## 1. What This Is and Is Not: Observability vs Interactive Debugger

It is vital to understand the precise scope and boundaries of the Igent telemetry and diagnostics system:

### **What It IS:**
- **Lightweight Observability & Telemetry Collector:** A production-grade telemetry pipeline for capturing app logs, network requests, crashes, ANRs, and performance metrics.
- **Real-Time Event Streamer:** Allows developers and QA testers to view live events streaming from physical devices or emulators into a terminal tailer (`npm run tail`) or web dashboard (`http://localhost:3000`).
- **Session Recorder & Exporter:** Automatically persists session logs to disk in JSONL format and supports exporting sessions to JSON or HAR (HTTP Archive) for network analysis.

### **What It IS NOT:**
- **Not an Interactive Live Debugger:** It does **not** support interactive breakpoint stepping, line-by-line code debugging, runtime variable mutation, or expression evaluation. For interactive debugging, use the Android Studio Debugger.
- **Not a Production APM SaaS:** It is designed for local development, QA staging, and rapid telemetry triage without heavy third-party SDK overhead.

---

## 2. Quickstart to First Event

### Step 1: Start the Telemetry Collector & Port Forwarding
Run the unified developer startup script from the root workspace:

- **Windows:**
  ```cmd
  start-dev.bat
  ```
- **macOS / Linux:**
  ```bash
  ./start-dev.sh
  ```

This script automatically:
1. Installs Node.js dependencies in `telemetry-server`.
2. Detects connected Android devices/emulators via `adb`.
3. Sets up port forwarding (`adb reverse tcp:3000 tcp:3000`).
4. Starts the Express telemetry server on port `3000`.

### Step 2: Start the Real-Time Tailing Viewer
In a separate terminal window, run:
```bash
cd telemetry-server
npm run tail
```

### Step 3: Launch the Mobile App & Trigger an Event
1. Build and install the app on your emulator or connected device (Debug build).
2. Interact with the app (e.g., make network requests, trigger test logs).
3. Observe live events streaming into your tail terminal and web dashboard (`http://localhost:3000`).

---

## 3. Configuration Reference

Environment variables supported by the telemetry server:

| Variable | Default Value | Description |
|---|---|---|
| `PORT` | `3000` | Port on which the telemetry server listens. (If busy, automatically falls back to an available port). |
| `TELEMETRY_AUTH_TOKEN` | `igent-telemetry-secret-token-2026` | Secret token required in `x-telemetry-token` header, Bearer auth, or `?token=` query parameter. |

---

## 4. Troubleshooting Guide

### Issue 1: Telemetry not reaching server from emulator / device
- **Symptom:** Logs appear locally on device but never arrive at `http://localhost:3000`.
- **Cause:** ADB reverse port forwarding is not active.
- **Resolution:** Run `adb reverse tcp:3000 tcp:3000` manually or use `start-dev.bat` / `start-dev.sh`.

### Issue 2: Unauthorized 401 errors
- **Symptom:** HTTP requests to `/api/telemetry` return `401 Unauthorized`.
- **Cause:** Mismatched `TELEMETRY_AUTH_TOKEN` between mobile app config and telemetry server.
- **Resolution:** Ensure both use `igent-telemetry-secret-token-2026` or match the environment variable.

### Issue 3: Port 3000 already in use
- **Symptom:** `EADDRINUSE` error when starting server.
- **Resolution:** The server automatically detects busy ports and falls back to an open port (logging active port to `telemetry-server/.active-port`).
