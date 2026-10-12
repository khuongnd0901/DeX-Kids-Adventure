# Continuous journey without automatic goodbye — acceptance and Codex handoff

**Implementation:** `feat/unlimited-journey-no-farewell-20261012`, 2026-10-12
**Status:** source changed; local Gradle/device QA NOT RUN by GitHub connector.

## Product contract

- LIVE, DEMO, GPX/HCMC: keep the child Activity running indefinitely
  from start until **Parents · menu → End adventure**, parent dashboard
  **Dừng hành trình**, user-initiated Activity exit or Android OS shutdown.
- No 15/30/60-minute selection, expiration timer, saved deadline,
  auto-navigation to parent dashboard or scripted end-of-trip TTS/goodbye.
- Parent F10/Menu/Back freezes narration/location/scene; **Continue**
  resumes without any time-gate even after 60+ minutes.
- An exhausted GPX route may show FINISHED motion, but **KidsActivity
  remains open** until manually stopped; no extra looping/made-up GPS.
- AI content refill remains optional, consent-gated and quota-limited;
  a long-running app does not circumvent existing AI provider limits.
- Replace the two offline goodbye episodes with normal discovery beats.
  Retain 66 authored beats and deterministic user-focus ordering.
- `session_minutes` legacy preference purged. Other settings/keys
  and PIN removal remain unchanged.
- QA 30/60-minute windows are **test durations**, not app limits.

## Implementation file map

| File | Contract |
|---|---|
| `KidsActivity.kt` | No deadline fields/handler, saved deadline key, expired onResume check or GPS resume deadline guard |
| `ParentSettings.kt` | No sessionMinutes getter/setter; migrate/remove old stored session_minutes |
| `ParentActivity.kt` | Duration removed from summary/settings dialog, other indices corrected |
| `SessionTimeLimit.java` + test | Removed dead timer utility |
| `EntertainmentDirector.java` | Two goodbye-themed episodes replaced with discovery episodes |
| `EntertainmentDirectorTest.java` | Assert no authored goodbye/ending dialog while preserving episode count |
| `LifecycleValidationRunner.java` | Recreation, Parent F10, GPX and 60m endurance now require no deadline |
| `RouteThirtyValidation.java` | 30m harness asserts still active; explicit test cleanup |
| `tools/test_unlimited_session.py` | Static regression checks for unlimited lifecycle, manual stop, and content |
| `docs/ART_EXPANSION_PACK.md` | Replace future planned GOODBYE_FADE with SCENE_TRANSITION_SOFT |
| `art/M10_ASSET_INVENTORY.csv` | Planned UI-016 changed to sticker_more_explore |

## Commands for Codex after pulling the branch/main

```bash
python3 tools/test_unlimited_session.py
bash scripts/test-local.sh
./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug :android:assembleDebugAndroidTest
```

**Android 15 / Z Fold3 DeX test matrix:**

1. Start DEMO and LIVE: no time label/duration picker; app remains after
   >60 minutes (physical QA if available).
2. After 10, 30, 60 minutes: F10 → pause → Continue resumes GPS/TTS
   correctly, without hidden timeout or extra cloud permissions.
3. Rotate/recreate while playing; state remains (including GPX replay
   position). `kids.session.deadline.elapsed` absent from saved state.
4. End manually via F10/Back and from dashboard. Return cleanly
   without goodbye narration or stale GPS/microphone/AI threads.
5. DEMO/AI/offline lesson cycle: no `both-goodbye`/`common-goodbye`
   or final farewell after finishing a question pack. Offline repeat
   content remains age-appropriate and no fabricated POI claims.
6. GPX reaches end; remains on screen and offers restart/speed/pause.
   End only manually; LIVE loses and recovers GPS without session stop.
7. Audio-only, parent pause/resume, no PIN and cloud/child-speech
   consent unchanged; capture Android logcat and FullHD screenshots.

**Evidence:** Write `.agent/evidence/unlimited-journey-local-qa/`
with tested commit SHA, APK SHA, commands, PASS/FAIL/BLOCKED,
DeX display ID/resolution, measured run length, exit behavior,
screen recording/screenshots without private child/GPS data.
Do not declare physical tests PASS from code inspection.

## Codex execution prompt

> Checkout the latest main containing the unlimited-journey change.
> Read this file, `.agent/STATUS.md`, `.agent/TASKS.md`,
> `docs/PARENT_CONTROLS.md`. Audit and test all session timeout
> removal points, including Activity recreation, F10 pause/Continue,
> LIVE GPS reactivation, GPX finish, AI refill and Audio-only.
> Execute source guards, JUnit, Gradle, Android and Fold3 DeX tests.
> Repair failures in scoped commits, record verified results, then
> update STATUS/TASKS. Do not restore a viewing timer or farewell.
