# M5 T-006 Vietnamese Tour Guide ExecPlan

Implemented source: cue with source/provenance enforcement, age gating, session cooldown, generic fallback, Android offline TTS voice selection guard, parent opt-in and transient audio focus.
NOT yet implemented: editorially verified named POI content packs, prerecorded Vietnamese audio for devices without offline voice, talking animation synchronization, actual child audio UX, navigation coexistence.
No unsolicited network TTS or cloud calls. No automatic narrator launch in child activity.
Unit tests authored; CI result pending. Device audio focus: NOT VERIFIED.

## T-011 local QA checkpoint — 2026-10-09
Owner Main agent; issue #12, draft PR #1. Actual environment/build/UI/model results and
remaining acceptance gates are in .agent/evidence/simulator/. No milestone DONE.
Baseline60-minute emulator soak and post-fix Android regression are tracked separately;
no physical Fold3/DeX/content/audio acceptance inferred.

Final local validation 2026-10-09: see `.agent/evidence/simulator/final-test-summary.md` and `test-matrix.md`. Real emulator results are scoped; no physical/verified-content/release gate closed. Milestone remains unfinished.


## 2026-10-09 source-grounded HCMC SAMPLE + spoken Vietnamese integration
- [x] Source-cross-checked 4 sample OSM feature IDs, approximate map centroids and separate official/factual editorial sources in `assets/poi/sample-hcm-source-audit.json`; 4 original concise Vietnamese cue drafts, 16-point clearly **synthetic** HCMC route. They are NOT production-reviewed and not in `assets/poi/reviewed.tsv`.
- [x] `OfflineNarrationCatalog`: immutable, bounded-size, UTF-8 fact-source-bound narration CSV/TSV; JUnit checks for invalid source/age/duplicates. Empty approved story catalog keeps default LIVE silent.
- [x] `AdventureScreen` only creates candidate cue after a POI event from the explicit source-cross-checked HCMC sample; never from fictional DEMO, inaccurate GPS, missing POI or PASSING_CANDIDATE. `TourGuideDirector` enforces child age + 30s speech cooldown.
- [x] `KidsGame` atomic speech state drives real Capybara TALKING/open/closed frames only while offline Android TTS utterance callback reports voice playback start. No fake voice animation from a text cue alone.
- [x] Android `KidsActivity` offers sample preview and native Vietnamese captions, uses parent-approved offline TTS only if **allowOfflineSpeech=true**, **quiet=false** and preview mode explicitly launched. If a Vietnamese offline voice is unavailable, caption remains; no network voice fallback. TTS audio focus requested with transient MAY_DUCK for navigation coexistence; parent menu, app background and end of session stop TTS and reset mouth state.
- [x] Dashboard starts HCMC sample GPX on the SAME display as Android game, without selecting phone touchscreen or external URI. Generic DEMO, standard GPX and LIVE remain unchanged.
- [x] CI validates 4 sample OSM IDs/links/cues + source audit ledger, source integration tests, Android APK/AndroidTest builds and desktop GL; latest CI status must be checked after commits. 
- [ ] Editorial/human review of all four places, geometry/centroid proximity vs road position and historical content; currently **PREVIEW ONLY**, never auto-migrated to human-approved production catalog.
- [ ] Full on-device Android TTS output validation, Vietnamese installed offline voice availability, audio-focus handoff with Google Maps, precise subtitle/narrator cue lifetime and UI runtime acceptance (user defers hardware test).
- [ ] M5 release acceptance / final real-route audio performance: NOT VERIFIED.
- Evidence: `.agent/evidence/T-006-M5-HCMC-sample-2026-10-09.md`.
Status **IN_PROGRESS**. Do not merge draft PR or mark DONE.
