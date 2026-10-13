## SOURCE implementation handoff — 2026-10-12 (Codex review, branch not merged)

92 SOURCE_CREATED_UNREVIEWED SVGs produced, 80 remaining PLANNED
(16 Sâu/Ong P0 and 64 P1). T-039.A/040.A/041.A/042.A authored in source,
but **none of their test/runtime acceptance gates have been executed** in
the connector session. The existing `AnimationTimeline` remains the only
animation controller; M10 motion-profile integrations and resource-controlled
runtime use of CAP/vehicle/FX/UI and animation profiles remain open in
T-041.B. **A scoped T-041.B props-only vertical slice was added**:
`build_m10_prop_atlas.py` derives an optional <=3-page environment
atlas, `M10PropAtlas` loads it once and `CartoonSprites` renders
new source props where biome/layer context is safe. Actual memory and
LibGDX loader tests NOT RUN.
Do not merge the feature branch without Codex source review, Gradle,
Android runtime, art gallery and actual Samsung Fold3 DeX review.
`docs/M10_CODEX_IMPLEMENTATION_REVIEW.md` is the canonical
remaining-runbook. All stale "PLANNED 0 art" statements below describe the
prior baseline and have been superseded by this source checkpoint.

---

# ExecPlan M10 — Art Variety & Animation Pack (T-036..T-042)

**Replanned 2026-10-12 · status PLANNED · no new art/runtime implementation.**
Source baseline: `main` `24bf3a29e4b9618d6219cb235245482317493180`.
Canonical overlap audit and dependency/ownership crosswalk:
`docs/M10_REUSE_DEPENDENCY_REVIEW.md`. Exact inventory:
`art/M10_ASSET_INVENTORY.csv` (172 planned, 108 P0 + 64 P1).
Individual drawing briefs: `docs/ASSET_VARIETY_GUIDE.md`;
motion profiles/one-shots: `docs/ART_EXPANSION_PACK.md`.

## Goal and non-goals

Reduce repetition across real 30-minute trips for ages 4–7 and audiences
SAU, ONG and BOTH using 2D original art + stable seeded variety.
**Do not reimplement** existing:
- T-003 art pipeline, Capybara base poses, wheel motion, atlas lifetime.
- T-027 original 18 props / extras atlas / old friends and traffic.
- T-026 22 named POI panoramas + trusted source-backed POI mapping.
- T-007 procedural world seed, WorldWindow ring and day/night palette.
- T-032 semantic `VisualCue` and DeX UI/child subtitle layout.
- T-033 `VisualPlan`, `AnimationRegistry`, `AnimationTimeline` and `DynamicSceneComposer`.
- T-035 verified GeoWorld importer, road corridor data (still incomplete).
- T-026/T-033 existing Android/GPX/performance test runners.

Preserve existing T-034 **one generation + one independent whole-pack review,
10 RPM**, local-only UI behavior, Android consent, no parent PIN, Audio-only,
single external DeX display, and source vs fiction GPS labels.

## Task ownership rules

| Task | Exclusive new ownership | May read/reuse but MUST NOT reimplement |
|---|---|---|
| **T-039.A** | M10 SVG schema, manifest validation and separate reproducible multi-page atlas packer | `build_sprites.py`, `build_extras.py`, visual motion engine |
| **T-040.A** | Pure selectable variation scheduler (shuffle bag/history/cooldown on existing props) | procedural chunk/world generation, POI provenance and scene priority |
| **T-041.A/B** | **Only writer of `CartoonSprites`/existing `AnimationTimeline` integration**; M10 registry to drawn region | new SceneComposer, new NLP resolver, duplicated GL engine |
| **T-042.A/B** | Extend existing T-026/T-033 QA/art-gallery outputs; repeat metrics and handoff | second GPS simulator/test runner |
| **T-038** | Original biome-specific props, non-POI fictional decor | 18 T-027 original extras or 22 T-026 landmarks |
| **T-036** | Additional consistent child avatar and Capybara pose SVG | redraw old face/silhouette, synthetic speech timing |
| **T-037** | New identifiable vehicle silhouettes/skins | same old six traffic SVGs renamed |

## Dependency DAG: implement infrastructure and runtime first, draw in reusable batches

```text
M10-M0 audit/style/id contract
    └─ T-039.A atlas registry + incremental packer  [DO FIRST]
        ├─ T-040.A shuffle-bag on existing assets     [DO SECOND]
        │       └─ T-041.A existing renderer adapter [DO THIRD]
        │                └─ T-042.A baseline/gallery test extension [DO FOURTH]
        └─ reusable art packaging for P0:
            ├─ T-038.P0: 48 props
            ├─ T-036.P0: 24 actor expressions
            ├─ T-037.P0: 12 vehicles
            └─ T-039.B: 16 FX + 8 UI; 26 motion *profiles*/8 events
                     └─ T-041.B integrate with existing AnimationTimeline
                                └─ T-042.B real FullHD DeX acceptance
                                        └─ P1: 64 postponed assets
```

T-040.A and T-042.A can proceed partly in parallel once their local contracts
are frozen. Each artwork task uses its own branch/review, and **T-041** owns
the one-file-at-a-time integration conflict surface. No big-bang requirement
that 172 drawings exist before atlas/renderer/test work. Re-use passed artifacts
from earlier checkpoints; no churn in source of truth.

## M10-M0 — audit/contract checkpoint

- [ ] Compare hashes/IDs vs T-003's six Capybara sprites, five friends,
  six traffic, 18 T-027 props, POI backgrounds and existing child portrait sheets.
  **Seven candidate slugs have been normalized to avoid duplicate WAVE poses,
  bus recolors, repeated pine/boats; see section 1A of M10_REUSE_DEPENDENCY_REVIEW.md.**
- [ ] Freeze name/anchor/layer/color palettes and no raw child photo copies; get parent
  approval for Sâu/Ong/Capybara expressions.
- [ ] Schema: `asset_id,source_path,region,page,category,layer,anchor,art_style_tag,license,review_state`.
- [ ] **Biome mapping:** existing Java enum is seven
  `URBAN/RESIDENTIAL/PARK/RIVER/BRIDGE/COUNTRYSIDE/GENERAL`;
  six `m10/props/` folders are **art vocabulary**, not new geographic states.
  `coast` must not be inferred from `RIVER`, etc. `DynamicSceneComposer`
  retains POI > sourced GeoWorld > procedural priority.
- [ ] Define no-false-world facts, safe motion speed, package/page budgets and
  inject stable `tripSeed` independent of precise GPS/network.
- [ ] Reserve baseline capture via existing T-026/T-033 before modifying rendering;
  current device-revision FPS/performance is **not yet measured for M10**.

## T-039.A — Shared atlas/registry infrastructure (FIRST)

**Owner:** Art Tooling · **Branch:** `feat/T-039-m10-asset-pipeline`.

- [ ] Build `tools/build_m10_atlas.py` and source manifest reader,
  validate the checked-in 172-row register but allow 0..N physically present
  assets to pack without wrongly claiming those are produced.
- [ ] Stable deterministic sorted region names, transparent alpha checks,
  anti-script/network SVG guard, no overwriting `assets/generated/kids.atlas`
  or `extras.atlas`, incremental staged atlas pages <=1024x1024 RGBA.
- [ ] Pack to ignored `assets/generated/m10/`; include checksums,
  per-page region lists, metadata and source→runtime registry.
  Tools install/pin same existing CairoSVG/Pillow versions.
- [ ] Create `tools/test_m10_art.py`: duplicate ID/path, allowed source count,
  page bounds, transparent nonempty regions, artifact reproducibility,
  missing asset fallback, compatibility with old atlas contracts.
- [ ] Build initial original/legacy region mapping and contact sheet skeleton.
- [ ] **No new animation timeline and no new GL renderer**; no dependency on
  completion of T-036/T-037/T-038.
- Gate: pack/check demo *existing* catalog art without new drawn P0,
  deterministic output and no build regression. Device memory remains unverified.

## T-040.A — Anti-repetition with original sprites (SECOND)

**Owner:** Pure Core/Variation · **Branch:** `feat/T-040-anti-repeat-selector`.

- [ ] Extend/combine with `SceneryCast` / `ProceduralWorldGenerator`;
  do not write another world/chunk generator or override `DynamicSceneComposer`.
- [ ] Per-trip seeded weighted shuffle bags, last-8 **scene signatures**,
  bounded memory, independent actor/prop/event cooldown; refresh only on chunk,
  scene, or validated beat event — **not each render frame**.
- [ ] Same seed + same inputs => reproducible; different trip seeds
  increase variety; when eligible candidates <=8, graceful legal repetition.
- [ ] Frozen by parent pause; GPX rewind clears scene schedule/semantics;
  no new HTTP or child's GPS stored persistently.
- [ ] Initially select from pre-existing 18 prop regions, five friends and
  six traffic regions to demonstrate reduced repetition **without new art**.
- Gate: unit test 10/30min fixtures, cooldown when >=9 candidates,
  deterministic replay/rewind, no repeated reward for same beat.

## T-041.A — Tiny vertical runtime adapter with old art (THIRD)

**Owner:** Renderer (exclusive edits) · **Branch:** `feat/T-041-m10-renderer-adapter`.

- [ ] Resolve asset registry selections from T-039.A/T-040.A in
  `CartoonSprites` (with existing `AdventureScreen` entry only if needed);
  preserve existing renderer/AnimationTimeline/VisualCue and POI priority.
- [ ] Draw **only existing** arts at first. One end-to-end scene proves
  packer→selector→renderer→screenshot, independent of newly generated art.
- [ ] Preserve color/alpha, one-screen UI safe areas, bus windows, voice and
  Audio-only. No GL per-frame file IO or extra allocations.
- Gate: actual visual replay vs baseline + missed asset fallback, no regressions.

## T-042.A — Reusable visual/test contract (FOURTH)

**Owner:** QA · **Branch:** `feat/T-042-m10-qa-gallery`.

- [ ] Extend the **existing** GPX/story/device QA harness instead of writing
  a parallel GPS simulator or a second performance infrastructure.
- [ ] 10/30min scene-signature histogram, coverage by region/biome,
  8-scene repeat counts, phase offsets, missing texture/atlas reference flags;
  produce real 1920×1080 OpenGL frames and contact sheets.
- [ ] Record current-build baseline FPS/P95/PSS from *same* Fold3/DeX
  conditions where possible; do not reuse unrelated old measurements as M10.
- Gate: repeat/QA reports generated using old 18 props + T-040 selector before
  mass asset production. Physical device can stay BLOCKED until available.

## T-038.P0 — Scene variety first (48 of 96 props)

**Owner:** Art Environment · **Branch:** `feat/T-038-biome-props`.

- [ ] P0 `URB/VIL/FOR/CST/MTN/FRM-001..008` (8×6).
  Check each against existing T-027 prop and T-026 POI artwork before drawing.
- [ ] 6 contact sheets and atlas/region validation per batch. Restrict props
  to eligible background/fictional art tags; do not infer real forests/beaches.
- [ ] Four visual safe layers; maintain 640px chunk view and HUD clearance.
- Gate: first 48 new SVGs pack successfully; renderer/gallery shows intended
  variety without duplicate existing silhouettes or false GPS claim.
- P1 `009..016` per family (48) only **after P0 user/device review**.

## T-036.P0 — Character reaction pack (24 of 32 poses)

**Owner:** Character Art · **Branch:** `feat/T-036-character-poses`.

- [ ] First eight CAP, first eight SAU and first eight ONG
  (`CAP/SAU/ONG-001..008`), matching approved cartoon portraits.
  Parent review of facial geometry and age-appropriate expressions.
- [ ] Stable face landmarks/anchor positions, bus-window and outside
  story-mode placement, transparent background, no original child photos.
- [ ] Only real offline narrator may drive TALK; QUIZ/REWARD uses safe
  local events, never false speech/caption.
- Gate: 24 unique sources/atlas regions, 3 audience screenshots and
  pause/transition tests. Remaining 8 P1 afterward.

## T-037.P0 — Vehicle variety (12 sources)

**Owner:** Vehicle Art · **Branch:** `feat/T-037-vehicles`.

- [ ] Add `VEH-001..012` with original silhouette/skin and source inspection
  against existing bus/traffic. Reuse `VehicleMotionModel` wheel rotation
  and correct pivots; don't show trains/ships as literal on-road GPS traffic.
- [ ] Safe vehicle-specific placement (water, roads, mountain) and
  no emergency siren/fake road alert; tags may be *fiction-only*.
- Gate: 12 distinct sources, gallery and non-broken wheel/bus tests.

## T-039.B — Reactions/FX and animation metadata (24 P0 new sources)

**Owner:** Art + Tooling · **Branch:** `feat/T-039-m10-fx-ui-batch`.

- [ ] P0 `FX-001..016` and `UI-001..008`. UI text via native
  font/captions unless separately validated; optional `UI-009..016` P1.
- [ ] Define/validate 26 **motion profiles**, 8 allowed semantic one-shot
  triggers in `docs/ART_EXPANSION_PACK.md` and asset manifest. **They are
  profiles to apply in existing T-033 animation code, not 26 new engines**.
- [ ] T-041.B, not T-039, owns runtime integration of profile scheduling.
- Gate: source atlas passes, no flashing/strobe, event mapping safe/fallback.

## T-041.B — Integrate content batches (single runtime owner)

**Owner:** Renderer · **Branch:** `feat/T-041-m10-render-integration`.

- [ ] Extend only existing `CartoonSprites`, `AnimationTimeline`,
  `AnimationRegistry` and any narrow adapter; asset-key allowlist from
  manifest, no external AI filename/GPS or arbitrary motion.
- [ ] Integrate P0 48+24+12+24 = **108 planned** source IDs
  using lazy/pooled atlas paging. Missing pages fallback to old original art.
- [ ] Preserve `DynamicSceneComposer` POI provenance and T-035 GeoWorld
  source status. No duplicate `SceneComposer`, `AnimationEngine` or
  `ProceduralWorldGenerator`.
- [ ] GPU incremental target <=12MiB resident and <=24MiB backing,
  **tentative until measured**. Protect parent controls/no PIN, Audio-only,
  TTS and 4–7 UI contrast/readability.
- Gate: >=100 new reviewed source assets are PACKED, RENDERED in tested scene
  and APPROVED (not just listed) before M10 P0 release; 26 profiles and
  8 event triggers integrated where relevant.

## T-042.B — Final acceptance (reuse existing QA)

**Owner:** QA + Main · **Branch:** `feat/T-042-m10-final-qa`.

- [ ] Existing `scripts/test-local.sh`, Gradle core/JUnit/Android debug
  and AndroidTest; existing T-026/T-033 runbooks extended with M10 metrics.
- [ ] 30-minute synthetic/demo/GPX, POI precedence and empty GeoWorld fallback,
  SAU/ONG/BOTH, long captions, no PIN/F10/Back, Audio-only and T-034
  existing AI quota behavior.
- [ ] Real Samsung Fold3 one-screen DeX 1920×1080 30-minute FPS/P95/
  PSS/thermal/draw-calls + no GL page leaks. Target avg >=55FPS,
  P95 <=25ms and <=10% regression against **equivalent measured baseline**.
- [ ] Parent reviews screenshots, character likeness, gentle motion and
  child comfort. Record exactly which P0 rows are APPROVED.
- [ ] Publish evidence on tested merged SHA, updating STATUS/TASKS;
  no M10 DONE if physical/age-quality/visual gates not observed.

## P0/P1 contract

| Group | P0 (make reusable first) | P1 (defer) |
|---|---:|---:|
| Capybara/Sâu/Ong | 24 | 8 |
| Vehicles | 12 | 0 |
| Biome props | 48 | 48 |
| VFX | 16 | 0 |
| UI | 8 | 8 |
| **Total** | **108** | **64** |

Source inventory status for **all 172 remains PLANNED**.
Do not count palette tint/frame transform as a new source. First release
requires >=100/108 rendered+reviewed; a future follow-up may adopt P1
once memory/attention/fps issues are verified.

## Explicit execution

For each task: start a branch from main; preserve base SHA, run tests
locally, write `.agent/evidence/T-0XX-...md` with commands/SHA/actual
results, update TASKS/STATUS and merge only after review. In this plan
no art/code has been created or runtime tests run. Historical performance
claims remain separate from the new M10 baseline.

**Next action:** implement **T-039.A** source/atlas pipeline and registry.
Do not start by generating 172 pictures in a branch that cannot pack,
validate or display them.

## T-041.B current Main execution — 2026-10-13

Dependency T-047 checkpoints through2b1b61b. Main-only per user; preserve inherited work. Audit:92P0 source +16missing child vectors; approved child references are raster cartoon sheets; no face replacement or embedded raster allowed. Author16 distinct gesture SVGs using vector traces of approved heads and newly drawn body/hand shapes. Main preview detects/removes crop fragments. Source remains UNREVIEWED; runtime retains original approved child sheets until style/likeness acceptance. P1deferred.

Milestones: P0source/gallery →26data profiles+8one-shots in existing registry/timeline →runtime CAP/VEH/FX/UI atlas with bounded pages →source-gated scenic coverage/anti-repeat →device QA. No second engine; no false child grading, geographic/weather or traffic claims. Each milestone local suite, core, debug/testAPK, scoped physical tests where available, review/commit/push/evidence. Parent likeness/comfort gate remains explicit.

### Profile checkpoint — 2026-10-13

Registry defines26 bounded profiles and8 one-shots. Two original-portrait
breathing profiles drawn. Other rig channels and all new FX drawing still open;
semantic clock wired for welcome/question/language-answer/POI/surprise only.
175core/local PASS; both APKs built; actual Fold3 Desktop2 30s smoke PASS,
58.96129FPS/P95upper19ms. Short smoke not full acceptance. See profile evidence.
