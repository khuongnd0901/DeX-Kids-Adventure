# T-030 implementation evidence — 2026-10-11 (GitHub connector)

## What changed (SOURCE IMPLEMENTED; not DONE)
- Local reviewed catalog expanded **72 -> 120 QUIZ** (48 age-4+ cards in eight topics); the 80 reviewed English WORD cards stay intact. AI outputs only local quiz/word IDs and focus; questions, factual answers, Vietnamese meaning and English spelling are resolved offline.
- Validated `TripQuestionPack` requires 15–20 ID pairs, unique quiz/text, at least four topics, same-topic word, correct SOLO/BOTH focus and 5/4/4 age gating.
- `TripPackStore` uses `AtomicFile` + SHA-256 + version/audience and keeps last valid file on interrupted writes. Separate profile files (SAU, ONG, BOTH), cursor and bounded last-50 references. Parent can clear all packs.
- `TripPackCoordinator` loads last good cache without blocking the game; fresh attempt only on a newly launched session with separate opt-in; initial+up-to-five retries and `Retry-After`; one in-flight cycle; cancellation on pause/menu/destroy; old pack replay + recent-card avoidance after exhaustion; swap after full validated persist.
- Integrated into the existing reviewed `EntertainmentDirector` learning cadence with offline fallback and POI priority. Pack cursor increments when the authored answer has been offered, not when first queued.
- Direct Gemini/Groq gateway now enforces a **shared persisted 5 HTTP requests/rolling 60 seconds, >=12.1s starts** for pack/POI/optional child text and all provider fallbacks; removed old per-instance 20-request counter.
- Parent controls have a separate **automatic trip packs OFF-by-default** toggle, cache clear, and in-trip immediate disable; child transcript cloud consent stays separate OFF. No exact GPS, child recording, identity, or route is included in the trip-pack prompt. No backend/CI added.

## Review performed (not equivalent to build/runtime QA)
- GitHub source inspection of gateway, store, coordinator, activity, parent, core model and data.
- Source/data audit of 200 TSV cards: 120 QUIZ, 80 WORD; 96 available quizzes for age 4 and 120 for age 5; 8 columns per row, unique IDs. The first manual `trimEnd()` check incorrectly removed the final empty TSV column; corrected check passes without trimming delimiter.
- Added Java JUnit cases: 15/20 bounds, 14/21 rejection, duplicate, forged ID, wrong focus, initial + 5 retry policy / Retry-After minimum.
- Added Python source contract `tools/test_t030_ai_packs.py` and extended existing local-only script and tests.

## OPEN verification (no pass claimed)
- `./scripts/test-local.sh`
- `./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug`
- Android instrumented corrupt-write, kill/recreation, prefetch concurrency, consent revocation, offline/429 real traffic and 5 rolling-60s tests.
- Actual selected Gemini/Groq model/account quota, daily/token limits and spend verified by parent. 5 RPM is user reported, not independent provider confirmation.
- Real Samsung Fold3 single 1920×1080 DeX, 30–60 minute audio/subtitle/POI interruptions, FPS/PSS smoke and content review by a parent.
- No local Gradle runner or device was available through this GitHub connector. **T-030 remains IN_PROGRESS, not DONE.**

## Repeatable local commands
```bash
./scripts/test-local.sh
./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug
```
