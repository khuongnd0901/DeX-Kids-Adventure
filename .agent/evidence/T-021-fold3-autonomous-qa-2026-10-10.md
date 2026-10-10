# T-021 — Physical Fold3 autonomous QA

Owner: Main. Date: 2026-10-10. State: automatic scope COMPLETE; manual/external gates NOT VERIFIED.
Source baseline main `26c85e0f8edcbc9c50d6e6f1d347a3695035db5b`; app production source unchanged during initial checks/endurance. Test runner changes are recorded separately in this task.
Hardware: physical SM-F926B, Android 15, Samsung DeX display 2; external display 1920×1080. No emulator inference.
Installed app SHA256: `546dcdfe2649a5871b1b861acc932be72316cbc3a25f8177036f45161f7dcdcf`.
Installed endurance AndroidTest SHA256: `e0a962ccfffe1600c67f5390b1f5ff731451d768791d18f932a6ba099cc56794`.
Raw local artifacts: `/tmp/dex-fold3-qa`; unrelated screenshots and raw device dumps are excluded from Git.

## Verified checks

- `scripts/test-local.sh`: all ten contract scripts PASS; 39 core suites, 93 tests, zero failures/errors. `:android:testDebugUnitTest` NO-SOURCE; Android production classes compile.
- `:android:assembleDebug :android:assembleDebugAndroidTest`: SUCCESS, including arm64 native libraries and deterministic existing 30-sprite atlas.
- Installed both APKs successfully without clearing data; no app was installed at inventory.
- Final physical instrumentation modes PASS: `gpx_parser`, `recreation`, `single_display`, `single_display_mouse`, `p0_hcm`, `p0_route`, `p0_child_mic_privacy`, `p0_ai_cache`, `permission`, `gpx_sample`, `gpx_picker`.
- Recreation preserved absolute deadline and same journey feed; F10/button paused and End adventure closed child; display_id=2.
- Mic remains denied; no recognizer instance. Local AI cache tested with ten synthetic cards, age/source isolation, deletion, child-cloud consent OFF, no HTTP.
- GPX parser rejected unsafe DTD/entities across UTF8/UTF16 variants. SAF used only bundled synthetic `hcm-preview.gpx`, selected in Downloads; replay advanced. Temporary Downloads fixture deleted after final result.
- Direct shell KidsActivity launch rejected with SecurityException: not exported. This does not prove separate-APK signature IPC behavior.
- First short **windowed**, not FullHD, A/B/A/B: full FPS mean 59.928, P95 mean 19.849ms; minimal FPS mean 59.877, P95 mean 18.668ms. Last 180 frames per window, not sustained whole-window cadence.

## Failures retained

- Initial static render guard falsely matched `HashMap` in a Javadoc comment; fixed guard and reran all local checks successfully.
- GPS refusal harness initially used runtime package for resource IDs. Actual Google controller on Samsung uses AOSP resource IDs; deny control was below a scroll fold. Added bounded scroll and both normal/repeated-denial IDs. The old English explanation expectation also needed current Vietnamese UI text. Final actual denial + both permissions denied + parent explanation PASS. Earlier failures retained locally.
- First SAF attempt removed the fixture immediately after clicking it, before deferred reading finished; test failed. Retried with fixture retained through completion: PASS. No parser fix attributed to this fixture mistake.
- Default UiAutomation screenshots capture phone display 0; suppressed for explicit DeX tests. Host captures external SurfaceFlinger display explicitly. Only full-screen game/parent screenshots were inspected for relevant QA; no human art/voice acceptance inferred.

## Reproduced production issue and final source

Expanded Parent controls test reproduced a hidden list: Android AlertDialog message suppressed the Audio-only/AI list. Removed the message and retained the pause notice in the title. Before-fix failure and after-fix pause/resume/deadline, visible Audio-only and immediate AI/cloud disable results are retained. Original preference presence/values restored (both preference maps empty on this fresh install).

Final debug APK SHA256 `b13f3d45dffd4bdce500b04980dac3befc3ddf01041297a4bfc3cbaff797f139`; final AndroidTest SHA256 `1e2208646f7e3ec45a42f3e0ff8c623e400c2b0509ec51b55a768d2b9af3953a`. Final build SUCCESS; local ten contracts and core 93 tests rerun PASS. Final script eleven modes PASS: parser, recreation, F10, button, expanded Parent controls, GPX sample, HCMC, route, mic privacy, cache and permission. Real SAF picker PASS was from the initial production APK; its parser/advanced Parent code is unchanged.

## Physical endurance and playback

Real-time endurance PASS: instrumentation elapsed 3600724ms, GL 1920×1080, 120 focused external-display/frame-progress checks, normal `limit_reached` expiry. Final cumulative GL telemetry: 215858 frames / 3599.881s = 59.963 FPS; 1ms histogram P95 upper bound 19ms. Endurance APK is the baseline hash above, before the Parent menu-only fix; final APK did not receive another full hour.

118 numeric resource samples, final sample elapsed 3583.35s. After 120s warmup, PSS 147007–147883kB; final 147650kB (~144MiB). No continuously growing PSS observed in this workload; this is not proof of zero memory leaks. `dumpsys cpuinfo` windows 29–30%; thermal status consistently 0. Whole-device battery 96→90%, battery sensor 30.8–31.5°C. No app-only battery attribution, extrapolation or SoC temperature claim. Raw GL and numeric resource evidence accompany this file.

Audio PASS on the physical phone: one-second local tone started and media was active; production narrator queued/started/finished Vietnamese offline TTS, plus a native TTS `onDone` (not just the production finish callback, which also handles errors). Google TTS reports six offline Vietnamese voices. Original media volume 15/15 preserved; no microphone recording or permission grant. On-device ASR service exists, but Vietnamese model/recognition accuracy NOT VERIFIED. Human audibility/pronunciation and navigation mixing NOT VERIFIED.

## Remaining gates

Real GPS movement/nearby POI geometry, field content approval, real Vietnamese ASR speech, human audio quality, Maps/Vietmap concurrency, external cable loss/recovery, actual Assistant signing and signed release remain NOT VERIFIED. Maps and Google TTS are installed; no Vietmap package found in inventory.

Final short FullHD A/B/A/B regression: full mean 60.003FPS / P95 mean 18.350ms; minimal mean 60.000FPS / P95 mean 18.755ms. These are last 180 GL frames per window, not a second endurance run.

A repeated local PROP_BEEP active-media sample failed after 200ms despite the earlier pass. Changed only the test tone to a continuous one-second TONE_SUP_DIAL; final audio rerun result is retained. No production voice path change.

Final continuous-tone + production/native Vietnamese offline playback rerun PASS (11630ms). Cleanup: removed test APK and exact test-owned screenshot directories and XML fixture; Downloads fixture already deleted. Cleared only fine/coarse USER_SET/USER_FIXED flags on this newly installed test app; fine/coarse/microphone remain denied. Parent displayed on DeX2, original duration/Audio-only/AI preference maps empty, media volume 15 preserved. No owner Accessibility or navigation changes.
