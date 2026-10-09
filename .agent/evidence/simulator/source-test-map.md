# Simulator source-to-test map

Audit: 2026-10-09; owner: QA source audit (main agent owns integration and execution). Read AGENTS, STATUS, TASKS, required test/art/world documents, active T-003–T-009 plans, core production/test source and Android adapters/manifest. This document records **source inspection only**: every implemented test is NOT_RUN here; execution results belong in the main agent's run evidence. No screenshot, runtime or hardware acceptance is inferred from source. Exact methods below are in `core/src/test/java/com/khuongnd/dexkids/` under the named package; run with `:core:test --tests '*ClassName'`.

## M2 mandatory cases

All rows: actual result = not executed by this audit; evidence = cited source/test mapping; status = NOT_RUN unless explicitly NOT_IMPLEMENTED. Visual rows require actual emulator screenshots/video in addition to assertions.

| ID | Steps and expected result | Implementation / exact existing test or coverage gap | Status |
| --- | --- | --- | --- |
| CAP-001 | Start preview, wait beyond greeting; idle while stationary | `game.CharacterAnimationController`; constructor defaults IDLE but renderer requests initial wave. No startup-idle assertion; screenshot needed | NOT_RUN |
| CAP-002 | Run equal elapsed time at 30/60 FPS across 4.17–4.3s blink; blink timing stable | `isBlinking()` uses global elapsed modulo 4.3. No cadence/blink boundary test | NOT_RUN |
| CAP-003 | Start journey and capture greeting; wave expires after 1.4s | `CartoonSprites` constructor requestWave; `game.CharacterAnimationJourneyTest.drivingWavingStationarySleepingAndResuming` tests explicit wave, not rendered startup | NOT_RUN |
| CAP-004 | Cross 0.65m/s; ROLLING selected | `game.CharacterAnimationJourneyTest.drivingWavingStationarySleepingAndResuming`; threshold boundary absent | NOT_RUN |
| CAP-005 | Stay below movement threshold for 50s; SLEEPING selected | Same method checks 60s stationary; exact 50s boundary absent | NOT_RUN |
| CAP-006 | Resume after sleep; ROLLING | Same method tests resume at 3m/s | NOT_RUN |
| CAP-007 | Run without narration; no TALKING | `game.CharacterAnimationJourneyTest.speakingFramesRequireExplicitNarrationFlag`; renderer never activates narration | NOT_RUN |
| CAP-008 | Explicit model narration flag; open/closed alternate at 5Hz, stop returns rolling | Same method; `game.CharacterAnimationControllerTest.transitionResetsStateTimeAndKeepsGlobalTime`. Actual audio-to-render lifecycle is NOT_IMPLEMENTED | NOT_RUN |
| CAP-009 | dt=0, negative/nonfinite dt, slow frame; no invalid animation | `game.CharacterAnimationJourneyTest.rejectsNonFiniteGpsAndNegativeTime`, `game.CharacterAnimationControllerTest.invalidDeltaRejected`; zero/slow/cadence assertions absent; screen clamps dt to 0.1s | NOT_RUN |
| CAP-010 | Background/foreground and recreate activity; atlas character visible | `AdventureScreen.resume/dispose`, `CartoonSprites.dispose`; no Android lifecycle automation | NOT_RUN |
| BUS-001 | Hold distance constant while time advances; wheel unchanged, zero-speed bob zero | `game.VehicleMotionModelTest.wheelRotationDependsOnDistanceNotRenderingCadence` checks initial angle/bob, not repeated fixed-distance samples | NOT_RUN |
| BUS-002 | Ramp speed from zero; stable body and wheel movement | `VehicleMotionModel.advance`; no ramp-specific test | NOT_RUN |
| BUS-003 | Decelerate to zero; wheels stop at final distance | Model implemented; no deceleration-specific test | NOT_RUN |
| BUS-004 | Change distance independently of render time; wheel angle follows distance | `game.VehicleMotionModelTest.wheelRotationDependsOnDistanceNotRenderingCadence` | NOT_RUN |
| BUS-005 | Inject rapid acceleration/deceleration; pitch within ±3.5 degrees | `game.VehicleMotionModelTest.suspensionRemainsBoundedDuringGpsSpike` | NOT_RUN |
| BUS-006 | Same final distance at 30/60 FPS; identical wheel angle | `game.VehicleMotionModelTest.wheelRotationDependsOnDistanceNotRenderingCadence` | NOT_RUN |
| BUS-007 | Capture 1920×1080 preview; bus fits viewport without clipping | `AdventureScreen` FitViewport 1920×1080 and fixed sprite positions; screenshot required | NOT_RUN |
| BUS-008 | Real 30/60min scrolling; bounded PSS/resource trend | Virtual `game.SimulationSoakTest.simulatedSixtyMinuteTripNeverExpandsChunkCacheUnboundedly` only proves six cached chunks, not memory/GPU/runtime | NOT_RUN |
| WORLD-001 | Capture URBAN/RESIDENTIAL/PARK/RIVER/BRIDGE/COUNTRYSIDE/GENERAL | Seven biome render switches exist in `CartoonSprites`/`WorldPainter`; no all-biome visual coverage | NOT_RUN |
| WORLD-002 | Capture before/after 640px boundary; continuous scenery without seam | `game.SceneryLayoutTest.absoluteOffsetIsContinuousAcrossChunks` checks geometry only | NOT_RUN |
| WORLD-003 | Controlled replay through ≥100 contiguous chunks; no missing scene/crash | `world.WorldWindowTest.boundedWhileScrollingAndReentryIsDeterministic` skips indices by 101; virtual soak covers continuous demo. No actual 100-chunk GPX render test | NOT_RUN |
| WORLD-004 | Cross district boundary; four consecutive chunks retain biome | `world.BiomeDistrictTest.consecutiveChunksShareBiomeForLongerStoryTransitions`, `.distinctChunksRemainDeterministicDespiteGrouping`; visual boundary inspection needed | NOT_RUN |
| WORLD-005 | Repeat seed/index/distance; same chunk/offset | `world.ProceduralWorldGeneratorTest.sameSeedAndIndexProduceSameChunk`, `game.SceneryLayoutTest.parallaxOffsetRepeatsExactlyWithoutAccumulatedFrameDrift` | NOT_RUN |
| WORLD-006 | Reset replay and revisit same distance; same scene | `world.WorldWindowTest.boundedWhileScrollingAndReentryIsDeterministic`, `journey.GpxReplayFeedTest.replayIsDeterministicAndPauses`; no combined reset render assertion | NOT_RUN |
| WORLD-007 | Resolve day/dusk/night and capture actual palettes | `game.WorldMoodResolverTest.resolvesDayDuskAndNightBoundaries` tests mood only; `WorldPainter.sky` uses DAY sky, CORAL dusk, NAVY night | NOT_RUN |
| WORLD-008 | Move through route; hills/clouds move at distinct ratios | `CartoonSprites.drawFar` (1.1 and 0.25 ratio, independent cloud drift); `game.SceneryLayoutTest.parallaxOffsetRepeatsExactlyWithoutAccumulatedFrameDrift` does not compare layers | NOT_RUN |
| WORLD-009 | Inspect texture creation over real repeated frames/reloads | Single atlas constructed once in `CartoonSprites`, disposed at screen teardown; source suggests bounded ownership but no runtime texture counter test | NOT_RUN |
| WORLD-010 | Demo captures contain no invented real POI names | Generator outputs only enum and integer detail seed; HUD says DEMO WORLD. No automated rendered-text assertion | NOT_RUN |

## M3–M8 functional map

| ID | Steps and expected result | Source / exact existing test and limitations | Status |
| --- | --- | --- | --- |
| GPX-001 | Parse existing synthetic fixture and repeat replay; same timeline/distance | `GpxReplayFeed.fromGpx`; `journey.GpxReplayFeedTest.replayIsDeterministicAndPauses` uses inline synthetic points; `.safeXmlParserAndTimestampOrder` checks XML/DOCTYPE, despite name does not assert timestamp rejection | NOT_RUN |
| GPX-002 | Pause/resume/stop/reset during playback; timeline frozen while paused and reset zeroes all state | `.replayIsDeterministicAndPauses` pauses after completion, resets distance only; mid-route pause/resume coverage missing; stop uses pause (no dedicated stop API) | NOT_RUN |
| GPX-003 | Complete route and update further; final distance stable and speed zero | `finished()` exists; existing deterministic test asserts completion/distance, not zero terminal speed. Source currently gives smoother zero delta after completion, potentially retaining nonzero speed; needs reproduction | NOT_RUN |
| GPX-004 | Inject implausible jump; no extra distance | `journey.GpxReplayFeedTest.improbableTeleportDoesNotMoveTheCartoonBus`; threshold 55m/s | NOT_RUN |
| GPX-005 | Duplicate/reversed timestamp and invalid coordinates; explicit reject | Constructor/Point guards exist; no exact rejection test | NOT_RUN |
| GPX-006 | Vary speed profile, 0m/s, cadence; smooth deterministic motion | `journey.SpeedSmootherTest.smoothingIsFrameRateIndependent`, `.negativesAreClampedAndInvalidNumbersRejected`; route profile not covered | NOT_RUN |
| GPX-007 | User-select playback multiplier/long route | No multiplier API/UI and only `test-data/gps/synthetic-urban-short.gpx`; custom long synthetic Point list possible | NOT_IMPLEMENTED |
| GPS-001 | Good fixes then jump/poor accuracy; no teleport | `journey.LiveJourneyFeedTest.validFixesAdvanceWithoutTeleport`, `.poorQualityFixDoesNotAdvance` | NOT_RUN |
| GPS-002 | No fixes >10s, then recovery >30s; decelerate, do not count unknown interval | `LiveJourneyFeed` uses system clock; no loss/recovery test; requires injectable clock or real waiting | NOT_RUN |
| GPS-003 | Duplicate/out-of-order/stale/future fixes; do not extend accepted travel | `GeoFix.reliable`, `LiveJourneyFeed.update` dt guards; no live-feed ordering/staleness tests. Rejected jump/ordering still refresh latestFixAt | NOT_RUN |
| GPS-004 | Parent opt-in fine location, deny/revoke, background/resume; tracking stops safely | `AndroidGpsSource`, `KidsActivity`; no Android instrumentation. GPX core replay is distinct from emulator ADB location injection | NOT_RUN |
| POI-001 | Near→approach→pass synthetic POI; no near-as-pass claim | `geo.PoiEventDetectorTest.aSingleNearFixIsNotPassing`, `.conservativeCrossingSequence` | NOT_RUN |
| POI-002 | Poor/stale GPS; suppress events | `geo.PoiEventDetectorTest.staleOrUncertainGpsDoesNotTrigger`; quality threshold, not confidence scoring model | NOT_RUN |
| POI-003 | Repeat passes; suppress duplicates and cooldown | Detector tracks passed once, may return VISITED repeatedly; `story.TourGuideDirectorTest.unverifiedNamedFactsNeverPlay` checks cue cooldown; no end-to-end detector/cooldown test | NOT_RUN |
| POI-004 | Query offline OSM and compare verified road-direction ground truth | No database/import/index/GeoContext wiring; tests use explicitly synthetic example.org provenance | NOT_IMPLEMENTED |
| NAR-001 | Select only age-compatible reviewed cue; generic fallback without POI | `story.TourGuideDirectorTest.unverifiedNamedFactsNeverPlay`, `.genericFallbackDoesNotRequireNamedPoi`, `.rejectUnattributedPoiAndWrongAge`; record validates metadata, cannot prove human review | NOT_RUN |
| NAR-002 | Parent approval + installed offline vi voice + audio focus; no network fallback | `OfflineVietnameseNarrator.speakReviewed`; no Android tests and child does not instantiate adapter | NOT_RUN |
| NAR-003 | Audio start/done/error/focus-loss drives talking and subtitles | No utterance progress listener, render binding or subtitles; no focus abandon on natural completion/error | NOT_IMPLEMENTED |
| NAR-004 | Prerecorded fallback/verified Vietnamese POI pack | No reviewed production audio/content pack | NOT_IMPLEMENTED |
| JOURNEY-001 | Day/dusk/night at boundary hours | `game.WorldMoodResolverTest.resolvesDayDuskAndNightBoundaries`; runtime uses local wall clock | NOT_RUN |
| JOURNEY-002 | Session visit rotates variant, clear removes counts | `story.JourneyMemoryTest.contentVariantCanRotateWithoutSavingLocation`; not connected to child rendering/story playback | NOT_RUN |
| JOURNEY-003 | Real geographic context causes biome transition; different journey seed changes scene | Seeded generator exists but screen seed fixed 20261008; GeoContext binding, transition blending and achievements absent | NOT_IMPLEMENTED |
| PARENT-001 | Change age/quiet/session/TTS consent; restart parent; settings persist | `ParentSettings` SharedPreferences, ParentActivity buttons; no Android persistence tests; age/quiet/speech do not drive narration yet | NOT_RUN |
| PARENT-002 | Explicit preview/start and stop; live start requires grant | `ParentActivity`, `KidsSessionControl`, `KidsActivity`; no Android tests | NOT_RUN |
| PARENT-003 | Session reaches limit; recreation cannot grant extra time | Android Handler timer exists and restarts on recreation. `session.SessionTimeLimitTest.limitAccumulatesOnlyRunningTime` tests unused pure helper, not activity timeout | NOT_RUN |
| PARENT-004 | Pause/resume, PIN access gate, audio-only, child isolation | PIN/audio-only/session pause controls absent; nonexported child and separate parent activity are partial isolation only | NOT_IMPLEMENTED |
| DISPLAY-001 | No external display; start refuses and no phone fallback | `DisplayRouter.launchOnExternalDisplay`; no Android test. Explicit phone preview is separate authorized action | NOT_RUN |
| IPC-001 | Authorized signature STOP ends session; unrelated action rejected | Manifest signature permission + `KidsCommandReceiver`; runtime test requires sender signed with matching cert | NOT_RUN |
| IPC-002 | Unsigned caller attempts STOP; denied | Permission declaration exists; actual Android permission enforcement test absent | NOT_RUN |
| IPC-003 | START/PAUSE/RESUME/VOLUME/AGE/STATUS and Assistant voice contract | Only STOP endpoint implemented; matching production signing and integration absent | NOT_IMPLEMENTED |
| DISPLAY-002 | Physical Samsung DeX routing/audio-navigation coexistence | Emulator cannot satisfy acceptance; hardware/signed Assistant app needed | BLOCKED |
| PRIV-001 | Offline preview, denied location, inspect network/microphone access | Manifest has no INTERNET/RECORD_AUDIO permission, GPS opt-in, no coordinate persistence/upload found. Runtime offline/permission evidence still required | NOT_RUN |

## Coverage and acceptance risks

- No Android `androidTest` suite exists in inspected tree; lifecycle, permission, IPC and audio behavior require emulator execution or added platform tests.
- Existing fast virtual 60-minute test is not a real elapsed-time soak, GPU memory test or Fold3 evidence.
- Atlas disposal is implemented; activity recreation/context-loss correctness remains runtime-only until exercised.
- Renderer clamps elapsed delta to 0.1s; slow-frame route timing and time-driven animation may lag wall time. Must distinguish simulated timeline from actual soak duration.
- M3 terminal-speed, M7 timer recreation and M5 focus-release are source observations requiring reproduction before FAIL classification.
- Seven fictional demo biomes exist; airport is absent from enum and not part of requested seven-biome M2 case. No demo biome proves real geographic context.
- Main agent must update task/plan/status with execution results. This read-only audit does not close any milestone or issue.

## Execution follow-up 2026-10-09

This table is the original source audit, not the final result matrix. New cadence, terminal/loss/recovery, lifecycle and >100-chunk replay tests now exist and the expanded 56-test suite passed. A real Android instrumentation runner was added. The original atlas-disposal inference was incomplete: LibGDX Game.dispose only hides its screen, reproduced as a failing ownership test and fixed. See test-matrix.md and regression-results.md for executed outcomes; no historical NOT_RUN row is silently promoted to PASS.
