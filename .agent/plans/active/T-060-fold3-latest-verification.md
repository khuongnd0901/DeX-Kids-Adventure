# T-060 — latest-source physical Fold3 verification
Owner Main; dependencies integrated T-048–T-053, source b5b5423.
Status IN_PROGRESS. Publication on main per durable main-only decision; initial evidence branch fast-forwarded into main.

1. Fetch/fast-forward latest main, identify actual external DeX display.
2. Run local contracts/core tests and build debug + test APKs; record failures honestly.
3. Enable test-only explicit external SurfaceFlinger capture for existing T-051/T-053 fixtures (no display0 fallback), compile and verify actual capture. Install matching APKs; run offline physical functional modes (GPX/parser, lifecycle, display/mouse, parent controls, privacy/cache, atlas/residency). Record each result and publish checkpoint after each mode.
4. Run bounded 600s offline creative playback/resource sampling on display2; inspect actual FPS/P95 and continuity. Then run actual3600s offline DEMO with providers/microphone disabled, resource/FPS/thermal capture and exact settings restoration. Independent30min, real-road, navigation/audio coexistence and adult art signoff remain separate unverified gates unless executed.
5. Restore parent dashboard, record limitations and clean Git/publication state.

Each step updates STATUS/TASKS and commits/pushes verified evidence; FAIL/BLOCKED is never DONE. No cloud calls, precise GPS or child audio upload. No production feature implementation for design-only T-054–T-059. Preserve accessibility/device-owner/navigation.
