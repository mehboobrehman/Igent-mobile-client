# Igent Mobile Client & Live Debug Telemetry System

Welcome to the **Igent** mobile client repository, featuring an integrated real-time debug telemetry, crash reporting, and diagnostics system.

---

## Documentation & Guides

- **[Architecture & Protocol Specification](docs/ARCHITECTURE.md)**: Text-based architecture diagram and envelope protocol spec (`schemaVersion 1.0.0`).
- **[Developer Guide & Troubleshooting](docs/DEVELOPER_GUIDE.md)**: Quickstart, unified developer startup scripts (`start-dev.bat` / `start-dev.sh`), and **What this is and is not: Observability vs Interactive Debugger**.
- **[Security Model & Privacy](docs/SECURITY.md)**: Token authentication and release build telemetry guardrails.
- **[Architectural Decision Records (ADRs)](docs/adrs/)**:
  - [ADR-001: Standalone Telemetry Collector](docs/adrs/ADR-001-standalone-telemetry-collector.md)
  - [ADR-002: Envelope Architecture](docs/adrs/ADR-002-envelope-architecture.md)
  - [ADR-003: Release Guardrails](docs/adrs/ADR-003-release-guardrails.md)

---

## Quickstart Developer Tooling

One command sets up port forwarding, collector server, and dashboard:

- **Windows**:
  ```cmd
  start-dev.bat
  ```
- **macOS / Linux**:
  ```bash
  ./start-dev.sh
  ```

To view live telemetry events in your terminal:
```bash
cd telemetry-server
npm run tail
```

---

## Testing & CI Checks

- **Telemetry Server Tests**:
  ```bash
  cd telemetry-server
  npm test
  ```
- **Mobile Release Telemetry Verification**:
  ```bash
  ./gradlew verifyReleaseTelemetry
  ```
