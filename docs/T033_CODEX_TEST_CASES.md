> **M10 test reuse scope (planned 2026-10-12):** T-042.A/B **extends**
> the existing T-033 QA matrix with source/atlas manifest validation,
> diversity histogram and repeat-signature metrics. Do not create a parallel
> GPS harness or regard any historic performance result as M10 PASS.
> Canonical: `docs/M10_REUSE_DEPENDENCY_REVIEW.md`. M10 remains PLANNED.

> **T-035 addition:** The source-backed landcover import pipeline is now implemented. After acquiring a real detailed road polyline and Overpass extracts, run `python3 tools/test_landcover_overpass.py` and follow `docs/T035_OSM_LANDCOVER_INGESTION.md`. This does not replace the real five-route GeoWorld gate.

# T-033 — Codex acceptance test cases (Fold3 DeX + offline GPS/AI animation)

**State: READY FOR LOCAL EXECUTION; none of the physical/Gradle cases below are marked PASS.**
Owner: local Codex on Ubuntu 24.04 + Samsung SM-F926B, external DeX 1920×1080.
Do not modify real user keys/consent/session/caches without snapshot and restoration.

## Codex launch prompt (copy/paste)
> Read `AGENTS.md`, `.agent/STATUS.md`, `.agent/TASKS.md`,
> `.agent/plans/active/T-033-dynamic-cartoon-world.md`,
> `docs/T033_DYNAMIC_CARTOON_WORLD.md`, and this test plan.
> Checkout latest main containing PR T-033, run T-033 QA phases Q0–Q5
> sequentially, with deterministic core tests before hardware.
> Keep production GPS permissions, native offline Vietnamese TTS, F10,
> one-screen external DeX, 4–7 learning age, 10-RPM limiter and creative
> 1-generate/1-whole-pack-review unchanged. Use only approved/test-owned
> OSM/geographic fixtures and never invent road pass/location facts.
> For each case write PASS/FAIL/BLOCKED plus exact command, build SHA,
> device/DeX display, timestamps, redacted logs, screenshots and metrics
> into `.agent/evidence/T-033-local-qa/`. Preserve all original failures.
> If a case fails, make a scoped fix on a new branch, add regression test,
> rerun the affected cases; do not declare DONE without complete evidence.
> Restore secrets/settings/location provider, close temporary test workers.

## Q0 — Checkout, baseline and build

| ID | Test and execution | Expected |
|---|---|---|
| Q0-01 | `git checkout main && git pull --ff-only`, record `git rev-parse HEAD` and `git status --short` | Clean base, target T-033 commit recorded |
| Q0-02 | `python3 tools/test_geo_world.py` and `python3 tools/test_t033_dynamic_world.py` | PASS offline geometric fixture and source contract; no network |
| Q0-03 | `bash scripts/test-local.sh` | All pre-existing source/Python/JUnit tests PASS; failures preserved, no CI |
| Q0-04 | `./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug :android:assembleDebugAndroidTest` | BUILD SUCCESSFUL, no test failures; confirm generated assets included |
| Q0-05 | Audit `adb devices -l`, `adb shell dumpsys display` and SHA of tested APK | Actual SM-F926B + external DeX display ID/resolution logged; no default phone fallback |

## Q1 — Animation engine and safe local storyboard (M1/M2)

| ID | Test scenario | Expected |
|---|---|---|
| Q1-01 | Run `LocalVisualPlannerTest`, `AnimationRegistryTest` | No unknown prop/action/remote filename accepted; butterfly FLY, flower GROW |
| Q1-02 | Run `AnimationTimelineTest`, render/record butterfly, flower, boat, bird, rabbit and unknown | Question and answer have different motion; no invented props/objects |
| Q1-03 | 15–20 reviewed AI lessons (5/4, 4/7, 6/7) with SAU/ONG/BOTH | Correct focus and ages; character reactions match topic; single shared prop |
| Q1-04 | TTS question → child interaction/timeout → answer → next lesson | One answer event per beat; no stale focus/overlapping TTS; animated celebration after answer |
| Q1-05 | F10 during moving butterfly, hold 10 s then resume | Animation time frozen; no sudden teleport/large delta jump |
| Q1-06 | POI narration interrupts quiz; parent pause/cancel/next beat | POI priority, no stale resumed visual or doubled narration |
| Q1-07 | Unrecognized theme, fictional counting, malicious "assets/../" as hypothetical prompt | Only preloaded sprites used; neutral fallback; never fabricate exact numerical images |
| Q1-08 | Visual tour at 1920×1080 with long 190-char prompt/240-char answer | No HUD/provenance overlap or cropped child faces; source vs simulated text legible |

## Q2 — Offline geographic scene (M3)

| ID | Test scenario | Expected |
|---|---|---|
| Q2-01 | Run `GeoWorldIndexTest`, `GeoWorldDirectorTest` | Invalid IDs/cells/index rejected; 3 accepted fixes; stale fade, rewind reset |
| Q2-02 | Build synthetic polygon GeoJSON in an isolated fixture directory; run `build_geo_world.py` | Cells only for centers inside polygon; holes excluded; source OSM way/relation ID retained |
| Q2-03 | Missing/corrupt `geo/landcover-cells.tsv` in temporary test APK | Neutral procedural fallback, no crash/HTTP/geographic claim |
| Q2-04 | GPX replay with exact 3 accepted 8m-accuracy fixes in sourced cell | Biome becomes visible after stabilizing; scenery changes without named POI |
| Q2-05 | Inject 70m-accuracy GPS fix, duplicate/old sample IDs and jitter across tile border | Reject noisy fixes, no flicker, no false street/landmark assertions |
| Q2-06 | Stop valid geo updates for >20 seconds and rewind GPX | Fade to neutral / reset, no stale geo topic |
| Q2-07 | Verify live feature properties from actual OSM extracts from route corridor | No OSM provenance = no output; GDPR/PII-free; license attribution © OSM |

**Actual five-route data task:** acquire **real road polylines and locally downloaded OSM polygons** for Đồng Nai–Vũng Tàu, Phan Thiết, Bảo Lộc, Đà Lạt and Nha Trang; check route alternatives and road proximity using `docs/ROAD_CORRIDOR_POI_PIPELINE.md`. Run the geo importer only on actual source geometry; verify 50 representative on-route/off-route samples per route, record false classifications and gaps. Do not fill production index from imagined points. OSM PBF/Overpass terms and ODbL apply.

## Q3 — Composition, runtime and offline/online (M4/M5)

| ID | Test scenario | Expected |
|---|---|---|
| Q3-01 | Run `DynamicSceneComposerTest`, then overlap sourced POI and GeoWorld biome | Source-backed POI art outranks geo scenery; safe fade back to coarse biome |
| Q3-02 | Live GPS without nearby named POI but with verified geo cell | Only coarse world theme, not "xe vừa đi qua..." claims |
| Q3-03 | Map cell suggests PARK, creative question about a boat | Lesson illustration can be fantasy; don't assert real boat/sightings |
| Q3-04 | Android offline mode, empty creative cache, no GPS | Existing 66 authored beats, safe procedural world and offline TTS continue |
| Q3-05 | AI enabled/disabled, HTTP 429, malformed creative pack and rejected review | Existing T-034 last-good, consent, up to six attempts, 10RPM shared gate unchanged; T-033 adds 0 HTTP |
| Q3-06 | Android Activity recreation, background/foreground, DeX density change | No dangling sprite textures, corrupted state, leaked context or forced phone-screen child session |
| Q3-07 | Audio-only mode, then switch visuals back on | Audio-only has no scenery render; animation recovers on same display |

## Q4 — Performance and device soak

| ID | Execution | Required evidence |
|---|---|---|
| Q4-01 | 30–60min FullHD DeX with 15+ visual theme changes | Raw GL FPS, P95 frame interval, frames and screenshot sequence; target avg >=55FPS, P95 <=20ms |
| Q4-02 | Collect actual process PSS and texture handle/size samples, thermal | No monotonic unbounded memory growth, no repeated per-frame texture allocations |
| Q4-03 | Run AI + real GPS + offline TTS alongside Google Maps/VietMap voice navigation | No TTS collisions, Android audio focus failures, animation freeze or hotspot thermal collapse |
| Q4-04 | Simulate revoked FINE location, mic refused, provider offline, Android kill/restart | Permission failure safe, no silent override; last-good and parent prefs preserved |

## Q5 — Release gate

| ID | Requirement | Result |
|---|---|---|
| Q5-01 | All source/JUnit/Android test failures closed with evidence | Must PASS |
| Q5-02 | Real five-route geographic dataset collected, source-verified and classified | Must PASS for geographic-coverage claim |
| Q5-03 | Adult approves age 4–7 lesson quality, Vietnamese, English translation and safety | Must PASS for child-content acceptance |
| Q5-04 | Real Fold3 DeX soak and live driving/audio regression | Must PASS for device acceptance |
| Q5-05 | Update `.agent/STATUS.md`, `.agent/TASKS.md`, plan and evidence with exact gates | Mark DONE **only** after Q5-01..04 PASS |

## Evidence template
For every test save a redacted `QX-YY.md` with:
```
Build SHA:
Device/model + Android + external display id/resolution:
Date/time/command:
Input fixture and true/simulated source:
Expected:
Observed:
PASS/FAIL/BLOCKED:
Evidence paths (no raw GPS/child audio/secret):
Follow-up bug/PR:
```
Do not upload private GPX routes, API keys, generated unsafe content, child transcripts or unredacted device records.
