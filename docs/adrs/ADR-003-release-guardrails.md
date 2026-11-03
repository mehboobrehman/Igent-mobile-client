# ADR-003: Release Guardrails & Telemetry Disabling

- **Status**: Accepted
- **Date**: 2026-11-04
- **Deciders**: Engineering Team

## Context and Problem Statement
Debug telemetry features (such as debug log streaming and local telemetry collection endpoints) must never leak into production release builds or compromise app performance and user privacy.

## Decision Drivers
- Zero telemetry overhead in release builds.
- Automated CI release verification gating (`verifyReleaseTelemetry`).
- Compile-time conditional compilation via `BuildConfig`.

## Considered Options
1. **Runtime Flag Checking**: Relies on mutable settings; vulnerable to misconfiguration or tampering.
2. **Compile-Time Build Config Flags & Gradle Verification Tasks**: Enforces disablement at build time via `BuildConfig.DEBUG_TELEMETRY_ENABLED` and verified via Gradle task `verifyReleaseTelemetry`.

## Decision Outcome
**Chosen Option**: **Compile-Time Build Config Flags & Gradle Verification Tasks**. Release builds explicitly disable telemetry at compile time, verified automatically by CI checks.
