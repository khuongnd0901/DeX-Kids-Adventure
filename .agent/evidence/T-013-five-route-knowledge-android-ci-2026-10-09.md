# T-013 — Five offline Dong Nai journey knowledge corridors / Android CI

Date: 2026-10-09. Source commits: `d04293bf` (route pack + application), `5b6caf4c` (reliable Android dialog QA harness).

## Implementation
- Five route labels from Đồng Nai to Vũng Tàu, Phan Thiết, Bảo Lộc, Đà Lạt and Nha Trang.
- 35 source-attributed original Vietnamese factual stories for ages 2–6. Seven per route, diverse geography, nature, culture, science and environment.
- `assets/routes/knowledge.tsv`, strict bounded `RouteKnowledgeCatalog`, source/duplicate/age/location-claim fail-closed tests, `tools/test_route_knowledge.py`.
- Parent selects/view cards offline on SAME DeX display; route-aware DEMO and native LIVE GPS. Time-based topic narration every ~90s, explicit NOT LOCATION-VERIFIED disclaimer.
- Offline Vietnamese TTS only if parent separately opts in and turns Quiet OFF; no online TTS, no unsourced GPS marker or route/turn recommendation. No PIN/lock/phone-touch, no changes to DeX-Assistant projects.

## Actual CI evidence
- [Build workflow SUCCESS #37940969993](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37940969993): static route pack + 5-corridor tests, unit tests, Android Debug + AndroidTest APK, OpenGL screenshot build.
- [Android emulator E2E SUCCESS #37940969959](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37940969959): Android 15/API35 disposable software GPU.
- `single_display_mouse`: PASS, displayId0 parent menu/pause/end.
- `p0_hcm`: PASS, synthetic HCMC GPX ~180.93sec→unreviewed Tao Đàn PARK scene→native VI subtitle; voice not played.
- **`p0_route`: PASS**, selected `dong-nai-vung-tau` parent DEMO, 7 source-backed cards loaded, first Hồ Trị An story displayed natively after ~8 sec, visible `KHÔNG ĐỊNH VỊ` disclaimer, no unexpected GPS or TTS.
- `p0_live`: PASS native Android GPS provider with emulator geo-injection, positive ~11.106m distance, provider accuracy reported 5m.
- `p0_perf_ab`: QA collection PASS but **performance target FAIL**; Full render 19.23/19.79 FPS (mean19.51), P95 82.15/80.35ms (mean81.25). Minimal Audio-only 26.96/26.35 FPS (mean26.66), P95 61.68/63.86ms (mean62.77). A/B/A/B 6 sec windows, rolling 180-frame samples. Emulator result not Samsung Fold3 hardware result; do not compare raw absolute values to different GitHub hosts.
- Screenshot artifacts produced incl. p0-route.png and p0-hcm.png; visual human review NOT VERIFIED.

## Open gates / limitation
T-013 remains **IN_PROGRESS** pending 5-option manual mouse UI usability on physical external Samsung DeX, actual offline Vietnamese voice/ducking, family/age/editorial review, all five corridors E2E selected by mouse and long-form drive. The data is an intro playlist, **not** geographic road-POI matching or turn-by-turn navigation. OSM production reviewed.tsv intentionally EMPTY until road-adjacent geofeatures are human verified. 30 FPS target on device still open under T-012. Existing privacy/security restrictions unchanged.

Review screen capture and practical family trip experience; only promote to production geofenced content after trustworthy coordinates, provenance, access permissions and outdoor field acceptance.
