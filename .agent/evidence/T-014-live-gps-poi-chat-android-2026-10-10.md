# T-014 — LIVE GPS → sourced nearby POI → quiz: Android CI evidence

Date: 2026-10-10 (Asia/Ho_Chi_Minh).
Main source commits: `f1d7f5b`, schema fix `f80d788`, DEMO QA expectation `acd170b`, location-provider fixture `e324069`.
Task: [Issue #17](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/17). Runbook: [docs/LIVE_GPS_POI_CHAT.md](../../docs/LIVE_GPS_POI_CHAT.md).

## Verified
- [Build & unit test GitHub run **SUCCESS** #37967017188](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37967017188), source SHA e324069, including asset provenance contract, 13 OSM-derived live features, 13 attributed VI introductions, 13 quiz/answer/chat cards, Java stability/accuracy/teleport tests, single-display guards, Android Debug & AndroidTest compilation and visual smoke.
- [Android API35 emulator real framework/GL instrumentation **SUCCESS** #37967017134](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37967017134). [Runtime screenshots/log artifact #11634521711](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37967017134/artifacts/11634521711).
- `single_display_mouse` **PASS** (F10/menu one click, display 0, pause and End).
- `p0_hcm` **PASS** (source-audited simulated GPX to Tao Đàn PARK, native VI subtitle).
- `p0_route` **PASS** (only explicit DEMO playlist uses time-based *non-geolocated* story; safe no GPS location claim).
- `p0_live` **PASS** (GPS_PROVIDER Android location injected by real ADB emulator geo fixes, 11.1 m positive distance, reported GPS accuracy 5m; not field-tested).
- **`p0_live_nearby` PASS**: isolated emulator receives 3+ GPS fixes separated by 8–12m on a sourced OSM node **Đá Ba Chồng** (Quốc lộ 20). AndroidGpsSource minDistance 2m, LiveJourneyFeed accepted ~21.978m, LiveFixGate requires 2 valid **real** positions, OfflinePoiEngine produced OSM approximate nearby notification, Android native VI quiz **'Những tảng đá có thể lớn hơn một chiếc xe buýt không?'** appeared after introduction. Exact raw GPS coordinates are not logged. TTS default parental opt-in OFF so no audible output claimed. Source-audited map point has **not** been field reviewed for lane proximity, therefore app describes 'Có thể ở gần...', not 'đã đi qua'.
- No Quiet-mode toggle/property is used by current Android source. `allowOfflineSpeech` remains independent explicit parent enablement, using on-device Vietnamese voice only.

## Open gates — NOT DONE
- Software-emulator frame target **FAIL**: full render average **15.02 FPS**, P95 **116.98ms**; Audio-only average **19.79 FPS**, P95 **78.21ms**. 6-sec A/B/A/B windows, not sustained Fold3 conditions, not deterministic cross-host benchmark.
- Full real journey POI coverage: only **13** source-cross-checked representative points (OSM-derived Mapcarta) across five multi-hour routes, no dense roadside spatial index or accurate entrance/motor-road matching. Manual verification / exact geometry and broader extracts needed.
- Physical Samsung Fold3 DeX (external-only mouse + keyboard), Vietnamese **offline voice actually playing**, simultaneous Google Maps/Vietmap audio focus, movement latency, long-run thermal/CPU/PSS/battery and real driving GPS accuracy NOT_VERIFIED.
- Child speech recognition / answering questions **NOT IMPLEMENTED**: existing scripted 'question → pause → supplied answer → open-ended prompt', NOT two-way listening. Real child voice input requires separate on-device ASR, microphone consent, QA and safety review.
- Approved `assets/poi/reviewed.tsv` stays EMPTY; the 13 `live-landmarks.tsv` are visibly marked OSM/source-audit-only and cannot be called production verified.
- T-014 remains IN_PROGRESS until full real-world acceptance. No extra Git branches, only `main`.

## What was fixed during CI
- First CI failure: answer text below minimum dialogue schema length; corrected without relaxing validator.
- Second E2E failure: legacy DEMO test expected an old subtitle; adjusted test to new explicitly non-geolocated DEMO label.
- Third E2E failure: injected GPS location increments below AndroidGpsSource `minDistance=2m`; increased *test-only* positions to 8–12m increments, kept all production GPS quality checks intact, recorded provider/overlay/distance diagnostics.
- Latest CI #37967017134 SUCCESS; all earlier failed attempts retained as honest evidence.
