# M10 / T-014: GPS-driven local companion, no Quiet mode

**Status: IN_PROGRESS** until CI, source editorial sign-off and Fold3 live-drive. This feature is a local GPS **proximity estimate**, NOT lane-level road matching.

## Behaviour (parent/child)
- Quiet-mode toggle and setting removed. Keep a distinct, explicit **Bật giọng kể offline tiếng Việt** parent setting, because the installed device may have no local Vietnamese TTS voice. With speech enabled, Capybara speaks automatically on matched POIs; with speech disabled/unavailable, native Vietnamese captions still appear.
- Parent starts **Bắt đầu GPS THẬT · nhận diện địa danh · kể chuyện và đố vui**; same external Samsung DeX monitor with mouse/keyboard. A generic LIVE GPS button also activates all bundled POIs.
- Android GPS provider → `LiveJourneyFeed` → reliable `JourneyPosition` → 2-new-real-fix `LiveFixGate` (fresh timestamps, <=25m accuracy, no teleport) → spatial tile `OfflinePoiEngine` (~150m NEAR, <=200m query, 30s engine cooldown) → explicit source-checked OSM POI/name + matched narration + matching quiz/answer/chat.
- On a stable new POI, show **GPS GẦN ĐỊA DANH · ƯỚC TÍNH**, say "Có thể xe đang ở gần..." and introduce an independently source-attributed location. After 16 seconds ask a quiz, after another 16s give the answer, after another 16s invite the child to talk.
- This is **turn-taking with prewritten prompts**, **not** microphone-based two-way listening or on-device ASR. Do not claim the character knows what the child said. On long drives, optional generic prompts at 5-minute gaps explicitly say **KHÔNG THEO GPS**, without inventing nearby places. Simulated DEMO may still play its own clearly labelled topic preview; real LIVE never uses time-based named places.
- Opening parent controls, Android backgrounding, ending a session or arriving at another eligible new POI cancels pending follow-up talk. Do not log raw GPS, upload GPS or use any external AI API. Speech is locally synthesized, parent-enabled only.

## Current source coverage
13 representative OSM points checked through OSM-derived Mapcarta, distributed across Đồng Nai, Vũng Tàu, Phan Thiết, Bảo Lộc, Đà Lạt and Nha Trang. Examples:
- Đồng Nai: [Đá Ba Chồng](https://mapcarta.com/N9974267816) (on Quốc lộ 20), [Suối Tre](https://mapcarta.com/W717517497), [Công viên Xuân An](https://mapcarta.com/W530247697).
- Vũng Tàu: [Bãi Sau](https://mapcarta.com/29188448), [Quảng trường Bãi Sau](https://mapcarta.com/W612283152).
- Phan Thiết: [Tháp Pô Sah Inư](https://mapcarta.com/N5933150585), [Công viên Đồi Dương](https://mapcarta.com/W261042873).
- Bảo Lộc: [Bùng binh Đồng Nai](https://mapcarta.com/W238544471), [Thác Đambri](https://mapcarta.com/N3093994368).
- Đà Lạt: [Thác Datanla](https://mapcarta.com/N4502225092), [Chùa Linh Phước](https://mapcarta.com/W522159670).
- Nha Trang: [Tháp Bà Po Nagar](https://mapcarta.com/W146780347), [Hòn Chồng](https://mapcarta.com/16149794).

Each location has an OSM feature ID, geographic representative coordinates, map source URL, and one matched sourced VN intro + prewritten quiz/answer/chat in `assets/poi/live-landmarks.tsv`, `assets/narration/live-landmarks.tsv`, `assets/poi/live-dialogue.tsv`. Audit: `assets/poi/live-landmarks-source-audit.json`.

© OpenStreetMap contributors, ODbL 1.0. Mapcarta is an OSM-derived secondary reference, **not** direct OSM geometry verification or surveyed vehicle roadway/entrance. Geometry may be the center of a large polygon far from the drivable road. **Only 13 locations exist in this starter pack**: long portions of all five journeys currently have no triggers. Missing points do NOT imply there are no nearby interesting places. No OSM `reviewed.tsv` records have been automatically promoted.

### Important limitations to communicate truthfully
- Not real-time online POI discovery, route planning, turn-by-turn navigation, or verified passage. Even when the GPS trigger is real, the OSM point may represent a centroid or nearby place; wording must always be qualified.
- Audio quality/installed Vietnamese non-network voice, interaction timing, interruption by Maps/Vietmap audio focus, 30FPS Fold3 external DeX operation, route-long source coverage, and real road proximity have not been field tested. Existing emulator graphics benchmark is below 30 FPS and remains an open issue.
- If reliable GPS fixes are absent or no POI is within the strict detection radius, the app will **not fabricate a nearby named site**.
- At most one intro for an eligible POI in a journey; cooldown and stable GPS requirements prevent repeated automatic place spam. No user-facing PIN or lock screen.
- Child is a passenger; the app must not ask the driver to look at the display, answer quizzes or handle the phone.

## Tests
- `LiveGpsPoiConversationTest` and `tools/test_live_gps_pois.py`: 13 OSM IDs, source URLs, one-to-one intro/dialogue links; simulated GPS events do not pass live gate, accuracy/teleports rejected; Quiet removed from current source.
- Android `p0_live_nearby`: disposable API35 emulator GPS provider injections near sourced Đá Ba Chồng, observe actual Android native GPS → LibGDX POI → TTS-off intro → later quiz. Existing `p0_hcm`, `p0_route`, `p0_live`, `single_display_mouse` regressions remain.
- Only field QA can approve release-grade lane proximity. Next data milestone: offline OSM geographical extracts of POIs along the *actual* five driven corridors, manual source+geometry review, GPS replay/drive acceptance and signed source/version updates.
