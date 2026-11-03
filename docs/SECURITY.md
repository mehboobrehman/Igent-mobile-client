# Igent Security Model & Privacy Policy

## 1. Authentication & Authorization

The telemetry collector server enforces token-based authentication across all administrative and telemetry ingestion endpoints (excluding health checks and dashboard views).

- **Token Transmission Methods**:
  1. Header: `x-telemetry-token: <TOKEN>`
  2. Authorization Header: `Bearer <TOKEN>`
  3. Query Parameter: `?token=<TOKEN>`
- **Default Secret**: `igent-telemetry-secret-token-2026` (configurable via `TELEMETRY_AUTH_TOKEN` environment variable).

---

## 2. Release Build Guardrails & Telemetry Disabling

To prevent telemetry overhead, code leakage, and data leakage in production release artifacts:

1. **Compile-Time Build Config Flag**:
   - In `app/build.gradle.kts`, debug builds set `DEBUG_TELEMETRY_ENABLED = true`, while release builds set `DEBUG_TELEMETRY_ENABLED = false`.
2. **Release Verification Gating (`verifyReleaseTelemetry`)**:
   - Gradle verification task `verifyReleaseTelemetry` ensures that release build configurations correctly disable debug telemetry.
3. **ProGuard / R8 Obfuscation**:
   - Release builds enable code shrinking and obfuscation (`isMinifyEnabled = true`, `isShrinkResources = true`).

---

## 3. Data Privacy & Compliance

- **No Sensitive PII**: Telemetry data is restricted to operational logs, network metadata, crashes, and performance metrics. Passwords, auth tokens, and sensitive user data must never be logged in telemetry payloads.
- **Local Storage**: Telemetry sessions are stored locally in JSONL format under `telemetry-server/sessions/` and can be deleted via the API or dashboard (`DELETE /api/sessions/:sessionId`).
