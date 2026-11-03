# ADR-002: Envelope Architecture & Payload Versioning

- **Status**: Accepted
- **Date**: 2026-11-04
- **Deciders**: Engineering Team

## Context and Problem Statement
The mobile client transmits diverse telemetry event types (logs, crashes, ANRs, network requests, performance metrics, lifecycle events). A robust, unified envelope structure is required to ensure schema validation, backward compatibility, and structured querying.

## Decision Drivers
- Consistent metadata (session ID, device ID, sequence number, timestamps) across all event types.
- Polymorphic payload support for different telemetry categories.
- Strict schema versioning (`1.0.0`) to handle future protocol evolution gracefully.

## Considered Options
1. **Unstructured Free-form JSON**: Prone to missing fields, difficult to index or validate.
2. **Strict Versioned Envelope (`schemaVersion`, metadata header, polymorphic payload)**: Standardized validation, clear separation of metadata and event-specific payloads.

## Decision Outcome
**Chosen Option**: **Strict Versioned Envelope Architecture (Schema V1.0.0)**. All incoming payloads are validated against required envelope fields and type enumerations before acceptance.
