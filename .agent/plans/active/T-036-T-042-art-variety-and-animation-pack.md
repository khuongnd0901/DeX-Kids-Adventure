# ExecPlan M10 — Art Variety & Animation Pack (T-036–T-042)

**Date:** 2026-10-12 · **Owner:** Main Agent · **Implementers:** Game/Art, Renderer, QA · **Baseline:** main @ `7764e765a3725a6c328606ed13c0ef43b5d413e2` · **Status:** PLANNED (documentation only, implementation NOT STARTED)

## Problem / scope

Parents want varied original 2D illustrations and animation across repeated DeX road trips, without the same bus/landscape/pose looping monotonously. Expand project-controlled art **without** changing existing AI provider behavior, T-034 request budget (1 creative generation + 1 whole-pack review, persisted 10 RPM), offline GPS provenance, parent no-PIN preference, or single-display DeX routing. Age focus is 4–7; current Sâu 5, Ong 4 defaults; Sâu/Ong are two children's avatar names **not animal/insect sprites**.

Source audit: `tools/build_sprites.py` currently packs fixed `SOURCES` into one 2048^2 page; `tools/build_extras.py` allows 14–24 sprites in a 1024^2 page; existing `CartoonSprites`, `AnimationTimeline`, `VisualCue`, `AnimationRegistry`, `DynamicSceneComposer`, `PoiSceneDirector` own rendering/semantics. Existing 6 Capybara SVG poses, 5 companions, 6 traffic sprites, 18 extras, 22 POI backgrounds, and pre-existing Sâu/Ong portraits must not be double-counted.

**Canonical asset register:** `art/M10_ASSET_INVENTORY.csv` — exactly **172 PLANNED unique SVG sources** (32 character, 12 vehicle, 96 biome props, 16 VFX, 16 UI). `docs/ASSET_VARIETY_GUIDE.md` contains exact 172-item art briefs. M10 initial release gate: **at least 100 approved, atlas-packed, in-game-used new assets**; do not call any merely planned row “created” or “shipped.” Motion profiles are separate.

## Execution order / dependencies

```text
M10-M0 audit & conventions
        │
        ├─ T-036 characters (32) ─┐
        ├─ T-037 vehicles (12) ───┤
        └─ T-038 biome props (96) ├─ T-039 VFX/UI (32), multi-page art pipeline
                                 └─────────────┬──────────────┐
                                               T-040 variety logic
                                                        │
                                               T-041 render integration
                                                        │
                                               T-042 QA / demos / acceptance
```

T-036/T-037/T-038 can proceed on independent task branches; T-039 must consume reviewed source and own packer edits. T-040 is pure Java/stateless logic and may be developed independently after M0 metadata freeze. T-041 integrates only passed slices. Each task has its own branch from latest main, written evidence, unit tests, code review and merge; avoid simultaneous changes to `CartoonSprites.java` and the build scripts.

## M0 — Audit + contracts, prerequisite for T-036–T-042

- [ ] Freeze original asset inventory against current `SOURCES`, `EXTRA_NAMES`, backdrop PNGs and Sâu/Ong assets; compare exact file hashes/IDs.
- [ ] Accept `art/M10_ASSET_INVENTORY.csv` IDs, source layout, color palette, silhouette/pivot coordinate contract, attribution/licensing and no-raw-child-photo rule.
- [ ] Define source-to-atlas manifest with fields `asset_id,region,atlas_page,biome,layer,pivot,visual_role,license,review_status`.
- [ ] Choose atlas strategy without expanding existing extras past its hard cap; define runtime residents and lazy-loading fallback to legacy art. No asset URL/filename from LLM.
- [ ] Compare prior M1/M2 and current M10 on a static GPX/demo fixture; collect existing performance baseline on actual target before changing renderer.

## T-036 — Character Expression Pack (32 new sources)

**Owner:** Art/Character · **Depends:** M0 · **Branch:** `feat/T-036-character-expression-pack`.

- [ ] Produce `CAP-001..012` as new Capybara expressive poses; preserve 6 existing poses.
- [ ] Produce `SAU-001..010` and `ONG-001..010` as child-avatar expressions based on existing cartoon design; no real child image in runtime source.
- [ ] Fixed local centers for eyes/muzzle/mouth and bus-window alignment; pose transition cannot move face landmarks unpredictably. Document pivots and optional hand/head separations.
- [ ] Include 3 audience focuses (SAU, ONG, BOTH) and read-only non-verbal reactions. Only `narrationActive` may request speaking mouth pose; no false narrated claims.

**Acceptance gate:** 32 unique reviewed sources with stable IDs, all valid transparent SVGs, 4–7-year appropriate visual review and 1920×1080 cabin screenshots; scripted face-anchor/alpha checks PASS. No DONE for source-only drawings unreferenced by atlas.

## T-037 — Vehicle Variety Pack (12 new sources)

**Owner:** Art/Vehicle · **Depends:** M0 · **Branch:** `feat/T-037-vehicle-variety-pack`.

- [ ] Create exact `VEH-001..012` including school bus recolors, mini train, small tractor, fire engine, ambulance, ice-cream van, family van, cable car, recycling truck and boat.
- [ ] Use shared wheel anchor metadata where relevant; independent wheel rotation does not require frame texture variants.
- [ ] Motion curves bounded; no real emergency flashing/sirens or live traffic/weather assertion.

**Acceptance gate:** 12 distinct new source regions + one staged traffic gallery; alpha and pivot checks; compatible shadows/orientation and no inconsistent travel direction.

## T-038 — Biome Prop Library (96 new sources)

**Owner:** Art/World · **Depends:** M0 · **Branch:** `feat/T-038-biome-prop-library`.

- [ ] Create `URB-001..016` city, `VIL-001..016` village, `FOR-001..016` forest, `CST-001..016` coast, `MTN-001..016` highlands, `FRM-001..016` farmland. Exact filenames/descriptions in canonical CSV.
- [ ] Maintain L0 skyline / L1 structure / L2 foreground / L3 transient FX tag per asset and margins for dashboard/text.
- [ ] Assign POI-vs-fiction provenance: only reviewed POI/backdrop may imply named real attraction; biome props imply no nearby factual object.

**Acceptance gate:** 96 original valid sources and 6 curated 16-item contact sheets; correct layer/biome/scale, no overlaps with old extras IDs. Device visibility and source audit still required before accepting.

## T-039 — VFX/UI Pack + multi-page atlas pipeline (32 new sources, animation definitions)

**Owner:** Art Tools + Game · **Depends:** T-036–T-038 reviewed art (may stage early subsets) · **Branch:** `feat/T-039-m10-fx-ui-atlas`.

- [ ] Draw `FX-001..016`, `UI-001..016` for effects/reaction overlays. Use native UI for Vietnamese strings; text-bearing SVGs require font and accessibility review.
- [ ] Implement a new dedicated **deterministic multi-page M10 packer**, not additional files in current `props/`. Emit `assets/generated/m10/*.png`, atlas metadata, per-page checksum/manifest and sources → regions audit.
- [ ] Planned authoring sizes: child/Capybara 256×320, vehicles 280×160, props/FX 160×160, UI 160×96 — conditional guidelines; resize and trim alpha before allocating textures.
- [ ] Target max 1024×1024 per atlas page; default source build is reproducible with pinned `cairosvg==2.8.2` and `Pillow==12.3.0`. Pack losslessly with 4px padding and no data-dependent ordering.
- [ ] Produce allowlisted **26 animation profiles** and **8 distinct one-shot triggers** from `docs/ART_EXPANSION_PACK.md`. Motion is mostly UV/pivot/tint/transform, not 26 additional full textures.
- [ ] No GL resources created in `render()`; batch load per scene, cache, dispose on scene exit. **Resident incremental VRAM target <=12 MiB**, total additional pack backing <=24 MiB (both subject to real-device measurement); do not hardcode a 1-page assumption.

**Acceptance gate:** deterministic packer rerun byte-identical; all bundled manifest IDs resolve; invalid/missing artwork causes safe fallback, no crash; 26 registered profiles + 8 triggers with non-flashing, bounded motion; GL thread and resource tests.

## T-040 — Anti-Repetition Variation System

**Owner:** Game logic · **Depends:** M0 manifest, stable asset tags · **Branch:** `feat/T-040-seeded-scene-variation`.

- [ ] Pure Java local deterministic variant selection with **per-trip random seed** (injected/reproducible for tests), chunk ID, biome, time-of-day, focus, activity and layer. Never need full GPS coordinates or network in the seed/hash.
- [ ] Scene slot selection by weighted **shuffle bags** and biome tags; exclude incompatible props, road/child collision zones, large UI occlusions and stale POI scenes.
- [ ] Rolling in-memory history of **last 8 scene signatures** for cooldown. Separate history for 6 prop sets / 3 actors / events, with sensible fallback if candidate pool is exhausted. No unbounded cache; no persistent child location.
- [ ] Palette variants (day/dusk/night) and phase offsets for repeated idle loops (deterministic), scene dwell-time constraints and gentle transitions. Do not reseed on each render frame.
- [ ] Parent pause/F10 freezes semantic timeline, resume continues without repeated welcome/reward; replay rewind resets deterministic scene timeline, not persistent GPS history.
- [ ] Local VisualCue/VisualPlan/AnimationRegistry remain allowlisted, zero new HTTP requests. POI art precedence **POI > sourced GeoWorld > fictional** remains.

**Acceptance gate:** deterministic equality for same input seed, meaningful variation for 20 differing seeds, no exact same scene signature within preceding 8 when candidate count permits; 30-min synthetic replay reports scene/pose histogram and duplicate-rate. No double-trigger rewards after pause/recreate/POI interruption.

## T-041 — Renderer/Scene Composition Upgrade

**Owner:** Game/Renderer · **Depends:** T-039, T-040 · **Branch:** `feat/T-041-scene-composition-upgrade`.

- [ ] Extend `CartoonSprites`/scene composer to resolve manifest IDs into preloaded texture regions. Integrate existing `VisualCue` focus and animation signals; fallback if M10 page absent.
- [ ] Preserve old bus, existing Capybara, real Sâu/Ong cartoon faces, 22 POI backgrounds, caption safe areas, audio-only path, and UI accessibility.
- [ ] One animated scene priority, bounded selection and no accidental real road claims. Restrict animation glow to child-safe smooth transitions (no intense strobe or fast repeated flashing).
- [ ] Measure resident GPU resources and scene draw calls, use lazy loading/eviction and no per-frame atlas scans/allocations; avoid full-size 1024px textures for small icons.

**Acceptance gate:** 100+ **new actual** assets drawn in context across demo scenes, 26 profiles registered, 8 one-shots reachable, SAU/ONG/BOTH/POI/audio-only/GPX pass, resource cleanup pass. Count only source+packed+rendered+approved.

## T-042 — Demo Gallery + Quality & Release Gate

**Owner:** QA + Main · **Depends:** T-036–T-041 · **Branch:** `feat/T-042-m10-art-demo-validation`.

- [ ] Author an offline gallery/script with atlas region thumbnails, pose transition sheets, six biome walkthroughs, 12 vehicle variants, 16 FX and 16 UI samples. Use real rendered frames, not mock screenshots.
- [ ] Compare deterministic 10/30-minute demo + GPX at 1920×1080: count exact scene signatures and prop repeats, sample 8-stage diversity cooldown and palette/idle phase offsets.
- [ ] Test local commands: `python3 tools/build_sprites.py`; future `python3 tools/build_m10_atlas.py`; `python3 tools/test_sprite_atlas.py`; future `python3 tools/test_m10_art.py`; `bash scripts/test-local.sh`; `./gradlew --no-daemon :core:test :android:assembleDebug :android:assembleDebugAndroidTest`. Run on local host only; no CI.
- [ ] Android API35 emulator and physical SM-F926B Fold3 DeX FullHD, GPS/POI, voice/captions, F10/menu/no PIN, audio-only; 30-min measured FPS, P95 frame ms, GPU/PSS/thermal, missing texture count.
- [ ] Acceptance target: 30-minute actual device **average >=55FPS, p95 frame <=25ms**, no crash/GL leak and no >10% loss from measured existing baseline; if hardware cannot meet it, reduce resident art/page loads and preserve a safe fallback.
- [ ] Adult visual/age-suitability review: comfortable readable 4–7-year audience, gentle motion without flashing, no scene implying false local geography, no unwanted photo likeness, no long answer occlusion.
- [ ] Update `STATUS.md`, `TASKS.md`, `docs/ASSET_PIPELINE.md`, evidence screenshots hashes and source provenance. Mark **DONE** only for passes observed on the exact merged revision; do not reuse T-031/T-032 historical FPS.

## Cross-cutting constraints / risks

1. **Art volume != visual variety.** Anti-repetition scheduling is as important as generating source art; avoid merely random rotation every frame.
2. **Packing risk:** current `extras.atlas` 14–24 guard and 2048² `kids.atlas` are near fixed designs. Never append 96 props to existing tools unchanged.
3. **GPU risk:** 172 full-resolution sprites could exceed Fold3 texture memory. Ship staged bundles with audited resident budget, no per-frame I/O, no eager load of all pages.
4. **Art provenance:** illustrated props are not live observation of traffic, weather, animals or buildings; no invented road polyline, OSM ID or POI geography.
5. **AI safety/cost:** no AI visual path injection, no new cloud API request, no modification to 10 RPM and independent review controls.
6. **Child attention:** animation should be gentle, brief and varied; no rapidly flashing/strobing effects or fake emergency alerts; no touch requirements on broken Fold3 phone screen.
7. **Quality status:** all tasks start **PLANNED**, source tests/Gradle/emulator/device **NOT RUN** for M10. Only concrete evidence advances status.

## Evidence protocol

For each T-036..042, write `.agent/evidence/T-0XX-<topic>-YYYY-MM-DD.md` with commit SHA, source counts, exact executed commands, PASS/FAIL, screenshots contact sheets, asset SHA/atlas hashes, GPU/render findings and blockers. Status stages: PLANNED → IN_PROGRESS → SOURCE_CREATED → PACKED → RENDER_VERIFIED → APPROVED → DONE. Do not conflate stages.

**This planning commit delivers specifications + a 172-row inventory only. No drawings, animations or runtime changes are included.**
