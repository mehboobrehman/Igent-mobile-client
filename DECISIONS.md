# Project Decisions

Architectural and design decisions made during development.
All agents should read this before starting work and log new decisions here.

## Apply variant-specific versionCode in Gradle
**When**: 2026-11-13 23:06:33

To fix versioning issues and comply with Android build requirements, variant-specific versionCode must be set programmatically.

**Impact**: App build configuration, variant artifacts.
