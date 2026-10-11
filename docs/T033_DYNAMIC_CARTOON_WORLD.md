# T-033 — AI Dynamic Cartoon World (source implementation)

**Release status: IN_PROGRESS; local Gradle/Fold3/AI quality/real-road QA NOT VERIFIED.**
Branch: `feat/T-033-dynamic-cartoon-world`, based on T-034/main.
Target: Samsung Fold3 DeX single 1920x1080 display, Sâu/Ong configured ages 4–7.

## Architecture implemented

1. **M0 / Contracts:** `story/VisualPlan` safe finite motion vocabulary, `game/AnimationRegistry` allowed actor-action combinations. Never accept arbitrary AI commands, textures or files.
2. **M1 / Animation:** `game/AnimationTimeline` on LibGDX thread; QUESTION, ANSWER, INTERRUPTED phases. `KidsActivity.playEntertainment` sends semantic phases alongside the same beat ID. The renderer applies bounded sprite motion (flight, bob, drift, float, growth, answer celebration) with preloaded atlases only. F10/menu uses zero render delta. No per-frame network or new texture allocation.
3. **M2 / Local storyboard:** `story/LocalVisualPlanner` turns existing T-032 `VisualCue` into a deterministic `VisualPlan`. The cue is computed from *validated* T-034 lesson metadata. Unrecognized or counting topics remain conservative, do not fabricate counts. Does not increase 10-RPM cloud traffic.
4. **M3 / Offline GeoWorld:** `tools/build_geo_world.py` converts locally downloaded OSM Polygon/MultiPolygon GeoJSON with real geometry and OSM source into a sparse **0.002-degree grid**, by testing center-in-polygon including holes. The Android-bundled index lives at `assets/geo/landcover-cells.tsv`. `GeoWorldIndex` loads it once, capped at 100k cells. `GeoWorldDirector` requires accepted accuracy <=25m and 3 separate fixes to change biome, fades and expires after 20s without valid in-index observations, resets on GPX rewind. Coarse biome topics can guide offline authored activity selection, **not cloud generation**.
5. **M4 / Composer:** `DynamicSceneComposer` prioritizes active POI panorama over OSM-derived coarse background; otherwise retains the original deterministic procedural fiction. Environmental props animate gently. Real TTS and POI priority remain owned by `KidsActivity`; no new microphone/GPS permission or cloud location payload.
6. **M5 / QA and future AI metadata:** JVM, Python/static and Android/real-device QA cases written. Optional **cloud-authored storyboard JSON is not enabled**, because T-034 already allocates 1 author+1 independent review request and creative-content factual quality is still under human acceptance. Local storyboard is functional without cloud visual instructions. The source does not claim unlimited animation art or physically faithful road scenery.

## Important data limitation
The shipped `assets/geo/landcover-cells.tsv` intentionally contains **zero sourced cells** until real geometry for chosen road corridors is imported and manually audited. **Do not invent coordinates, road tracks or OSM records.** Without a valid landcover pack, live GPS retains the existing sourced-POI art and fictional world. Delivering actual route-by-route geographic world requires real offline OSM extracts and verified route alternatives. `docs/ROAD_CORRIDOR_POI_PIPELINE.md` is the POI counterpart; the GeoWorld importer is independent.

### Build offline OSM cell data
```bash
# Obtain a real OSM polygon GeoJSON extract lawfully on the developer machine.
# Feature properties need tags:{landuse/natural/leisure/waterway}, osm_type:way|relation, osm_id.
python3 tools/build_geo_world.py \
  --input /path/to/verified-osm-polygons.geojson \
  --output assets/geo/landcover-cells.tsv
python3 tools/test_geo_world.py
```

**OSM license** © OpenStreetMap contributors, ODbL. Respect source attribution and redistribution rules; the coarse grid is illustrative, not a GPS route matcher. Coast may reuse the existing stylized RIVER water animation and wooded areas PARK until new vetted terrain art is added.

## Safety and fallback
- No external AI animation instructions, resource paths, GPS values or child text can reach the render thread.
- Not an assertion of nearby animals, exact roadside objects, conditions or navigation directions.
- Parent pause, audio-only and existing DeX single-screen routing preserved.
- Missing/invalid geo index -> empty index -> existing procedurally seeded world; no crash or request.
- Disallowed asset/action -> neutral pose or safe local motion.
- Creative pack network still follows T-034 1 author+1 whole-pack review, persisted 10 RPM and parent consent; T-033 adds **zero HTTP calls**.

## Acceptance and evidence
See `docs/T033_CODEX_TEST_CASES.md` for 26-task milestone checklist, test data, local commands, physical device runbook and PASS/FAIL evidence template. **Do not mark full task DONE without real Android build, Fold3 DeX smoke, QA routes and adult content review.** Older T-031/T-032 FPS figures are not a T-033 benchmark.
