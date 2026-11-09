# Igent Android Parity Porting Status

| ID | Title | Desktop ref | Status | Tests | Notes |
|---|---|---|---|---|---|
| IGEN-ANDROID-004 | Core Application Logic | N/A | DONE | YES | Porting of core business logic, A1-A14 engines, use cases, and repositories. |
| IGEN-ANDROID-005 | UI/UX Implementation | N/A | DONE | YES | Re-implementation of user interfaces using Jetpack Compose, Material3 dark theme, and persistent disclaimer. |
| IGEN-ANDROID-006 | Data Persistence | N/A | DONE | YES | Room database setup with Entity & DAO storage for portfolio, watchlist, logs, courses, and market snapshots. |
| IGEN-ANDROID-007 | Network & API Integration | N/A | DONE | YES | Retrofit & OkHttp integration for PSX feeds, AI models, RSS, and telemetry. |
| IGEN-ANDROID-008 | Debugging & Telemetry Parity | N/A | DONE | YES | Telemetry logging, WorkManager background sync, and debug UI screens implemented. |
| F0 | Oracle Harness Vectors | N/A | DONE | YES | Golden test vectors generated in `app/src/test/resources/golden/`. |
| S1 | Idea Cards Cleanup | N/A | DONE | YES | Removed hardcoded cards and disabled seed generation. |
| S2 | Fake Prices Removal | N/A | DONE | YES | Removed seed stocks and implemented `MarketDataState`. |
| S3 | RSS Feed Cleanup | N/A | DONE | YES | Authentic Pakistani feeds integrated, sample links removed. |
| S4 | Analysis Verdict Fix | N/A | DONE | YES | Fragile verdict extractors removed, default verdict implemented. |
| S5 | Weekly Summary Fix | N/A | DONE | YES | ISO week format implemented, fake picks removed. |
| S6 | UI Disclaimer | N/A | DONE | YES | "Information, not advice" disclaimer added to key screens. |
| S8 | AES-GCM Encryption | N/A | DONE | YES | Upgraded to AndroidKeyStore-backed AES-GCM. |
| S11 | AI Provider Cleanup | N/A | DONE | YES | Removed pollinations.ai silent fallback. |
| S12 | Security Hardening | N/A | DONE | YES | backup=false and Auth header redaction implemented. |
| D1-D6 | Phase 3 Data Ingestion (Schedulers, Parsers, WorkManager, sensor_runs) | N/A | DONE | YES | Implemented D1-D6 scrapers, parsers with Jsoup, test fixtures under app/src/test/resources/fixtures/, conditional GETs, sensor_runs persistence, and BackgroundSyncWorker. |
