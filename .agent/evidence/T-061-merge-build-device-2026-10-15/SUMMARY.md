## Integration checkpoint — local PASS; physical UI gate OPEN

Merged source candidate: story tip0f5a199 plus main7927000 and retained external-display QA fixes. Both debug APKs build successfully. Full local suite PASS;211 core tests,0failures. Both APKs installed successfully on real SM-F926B/API35. APK SHA256 in apk-sha256.txt. Physical safety,author failure,55-story SQLite,cancellation,replay no-network and recreation/stale revision checks PASS.

Initial manager UI gate FAILED: editor positive button was not visible to accessibility. Raw initial failure retained. Investigation and physical story/visual/outfit verification continue. This checkpoint does not mark integration acceptance DONE. Prior b5b5423 hour remains historical,not this merged APK. Old semantic bridge/RAM/release findings remain OPEN.

## Fixed-cue physical render checkpoint
`t059_story_visual` PASS:12 real1920×1080 DeX captures, LOOK_UP/FLOWER and PET/RABBIT across SAU/ONG/BOTH. Captures retained in visual/. BOTH PET inspected: rabbit and complete Ong artwork visible; this is fixture render evidence, not broad visual/content acceptance or real ASR. Manager editor initial failure remains OPEN.

## Integrated physical story smoke PASS
Real Fold3/DeX2: actual_duration_ms=180081
captures=16
completed_audiences=[]
journey_distance_delta_m=1499.071524434807
limits=Local fixtures; optional pre-existing reviewed draft identified by both_source; sourced downstream POI simulated; DEMO journey; display=2; no real-worldGPS or real ASR recognition
live_gps=false
sqlite_crud=edit invalidates approval; undo; soft-delete; undo
story_loads=3
tts_active_samples=37
INSTRUMENTATION_CODE: -1

Last sampled render/resource counters: {"elapsed_ms": 176334, "frames": 10567, "render_seconds": 176.2941932749236, "average_fps": 59.93958055964551, "p95_upper_ms": 19, "draw_calls": 11, "pss_kib": 166641, "managed_textures": 9}. This ~3-minute smoke is not the previous60-minute acceptance run. Full raw events/screenshots/performance retained in story-smoke/.

## Physical outfit and regression checkpoint PASS
`t053_outfits`:72actual frozen pose captures across3audiences; complete Ong
pack required by fixture, six unique costumes per child, shared atlas identity,
simulated240s rotation boundaries,pause/resume and real Activity/GL recreation.
74PNGs plus performance/transitions retained in outfits/. This is not24min
elapsed rotation and not a blanket aesthetic acceptance.
`recreation`,`single_display_mouse`,`parent_controls` PASS on merged APK/DeX2.
