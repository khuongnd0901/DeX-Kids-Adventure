
# Tasks

State vocabulary: BACKLOG, PLANNED, READY, IN_PROGRESS, BLOCKED, DONE.
Status is based on evidence, not code presence.

| ID | Milestone | Owner | Dependencies | State | Acceptance / evidence |
| --- | --- | --- | --- | --- | --- |
| T-001 | M0 bootstrap | Main | none | IN_PROGRESS | Multi-module Gradle build, Android APK build, desktop classes, initial docs, GitHub push. Build evidence pending. |
| T-002 | M1 LibGDX foundation | Game | T-001 | IN_PROGRESS | Runs desktop+emulator, screen lifecycle + 30 FPS diagnostics. Code started; runtime acceptance pending. |
| T-003 | M2 character/world | Game | T-002 | PLANNED | Frame assets, capybara/bus animations, chunk streaming, no seams, visual acceptance. Code-drawn visual is temporary. |
| T-004 | M3 GPS simulator | Geo | T-002 | PLANNED | Deterministic GPX replay, smoothing/jump/loss tests, travel event mapping. |
| T-005 | M4 real GPS + OSM | Geo | T-004 | BACKLOG | Verified offline POI DB, spatial accuracy, no incorrect PASSING claims, GPS provider. |
| T-006 | M5 story + VN speech | Story | T-005 | BACKLOG | Curated verified facts, local narration, cooldown, audio focus and subtitles. |
| T-007 | M6 dynamic adventure | Game/Story | T-003,T-006 | BACKLOG | Day/night, world biome context, reproducible events, journey memory. |
| T-008 | M7 parent controls | Android | T-002 | BACKLOG | Separate parent interface, session time, quiet/audio-only/emergency stop. |
| T-009 | M8 DeX + Assistant IPC | Android/Main | T-008,T-006 | BACKLOG | Samsung external display real-hardware tests; authenticated explicit IPC; navigation coexistence; preserve existing apps. |
| T-010 | M9 benchmark/release | QA/Main | T-001..T-009 | BACKLOG | Signed APK, security/license audit, Fold3 60-min soak, FPS/resource evidence, checksum and release notes. |

Open blocker: repo visibility PUBLIC; initial prompt specifies PRIVATE.
