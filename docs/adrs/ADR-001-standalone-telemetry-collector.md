# ADR-001: Standalone Node.js Telemetry Collector

- **Status**: Accepted
- **Date**: 2026-11-04
- **Deciders**: Engineering Team

## Context and Problem Statement
During mobile app development and testing, developers need a reliable, low-latency method to collect logs, crashes, network requests, and performance telemetry without depending on external cloud SaaS providers that introduce cost, latency, and data privacy risks.

## Decision Drivers
- Zero external dependency / privacy-first local operation.
- Instant feedback loop for developers and QA engineers.
- Support for real-time streaming (SSE) and terminal tailing.
- Simple deployment and minimal resource consumption.

## Considered Options
1. **Third-Party APM SaaS (Firebase Crashlytics / Datadog)**: High setup overhead, cloud dependency, potential privacy leak during staging.
2. **Embedded Local SQLite inside App**: Hard to inspect in real-time across multiple test devices without pulling database files manually via ADB.
3. **Standalone Node.js Express Server with JSONL Persistence**: Lightweight, fast, runs locally or on a staging machine, supports Server-Sent Events (SSE) for live tailing, and stores JSONL files for easy export and inspection.

## Decision Outcome
**Chosen Option**: **Standalone Node.js Express Server with JSONL Persistence**. It provides an optimal balance of developer ergonomics, real-time streaming capability (`npm run tail`), and zero external cloud dependency.
