# Architectural Decisions

## Project Initiation: Igent Android Parity

**Date:** 2026-11-06

**Decision:** Initiate the Igent Android Parity project to develop an Android mobile client based on the existing Igent desktop application (version 1.0.7).

**Rationale:** To expand Igent's reach to mobile platforms, leveraging the existing desktop application's features and data structures as a reference. This project aims to achieve feature parity with the desktop version on Android.

**Impact:**
- Establishment of a `reference/desktop-1.0.7/` directory containing key desktop application assets (drizzle, knowledge, main/index.js, main/ingest-worker.js, renderer/, package.json).
- Creation of `docs/PORT_STATUS.md` to track the porting progress of various modules and features.
- All future development for the Android client will refer to the extracted desktop assets for behavior and structure.
