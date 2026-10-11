# T-033 source evidence (connector-side only)

This record documents GitHub code changes and human-inspectable source/test cases; **it is not a Gradle, network, GPS or GPU benchmark**.

- Existing T-034 main already provided 4–7 creative lessons, safe VisualCue and 10 RPM shared limiter.
- T-033 adds local planner/timeline, Android event propagation, OSM cell parser/GPX reset heuristics, offline polygon importer, POI-priority composer and safe motion.
- Source tests include LocalVisualPlannerTest, AnimationRegistryTest, AnimationTimelineTest, GeoWorldIndexTest, GeoWorldDirectorTest, DynamicSceneComposerTest, test_geo_world.py and test_t033_dynamic_world.py.
- Existing `bash scripts/test-local.sh` can run them plus legacy tests locally; no CI.
- Production GIS dataset intentionally empty pending external sourced extraction. Full geo coverage and performance NOT VERIFIED.
- Acceptance and device execution instructions: `docs/T033_CODEX_TEST_CASES.md`.
