> **Current implementation reality (2026-10-12):** New source files exist
> for 92 P0 visuals on the candidate feature branch, but the 26 reusable
> motion presets below and 8 event effects are **still only metadata/design**,
> not yet integrated with runtime AnimationTimeline. T-041.B owns that work;
> Codex must check it after atlas validation. See
> `docs/M10_CODEX_IMPLEMENTATION_REVIEW.md`.

> **M10 replanning (2026-10-12):** No new animation engine is required.
> The 26 profiles below are **data/motion presets** to be mapped onto the
> existing `AnimationTimeline` / `AnimationRegistry` from T-033, with T-041
> owning the only renderer integration. T-040 supplements the *existing*
> procedural `SceneryCast` with a bounded history; it does not generate a
> second world. Use T-039.A/T-040.A/T-041.A/T-042.A before drawing P0
> batches. Inventory now **108 P0 / 64 P1**, unchanged 172 concepts.
> See [M10 ownership/dependency review](M10_REUSE_DEPENDENCY_REVIEW.md).

# M10 — Art Expansion, Animation Catalog & Anti-Repetition Spec

**Status:** DESIGN ONLY. Owner Main. Canonical files: `.agent/plans/active/T-036-T-042-art-variety-and-animation-pack.md`, `art/M10_ASSET_INVENTORY.csv`, `docs/ASSET_VARIETY_GUIDE.md`.

## Product outcome

Create a richer, child-friendly cartoon road companion for Sâu (5) and Ong (4) on one Samsung Z Fold3 DeX external FullHD display. Make repeated 30-minute trips *feel* different through (i) 172 planned original asset sources, (ii) 26 motion profiles, (iii) 8 event-triggered one-shots, (iv) six biome prop pools, (v) local deterministic anti-repetition. **The goal is minimum 100 actually integrated/approved assets in milestone M10; all 172 currently are concepts only.**

## Milestone tasks

| ID | Scope | Proposed sources | Dependencies | Acceptance |
|---|---|---:|---|---|
| T-036 | Capybara/Sâu/Ong expressions | 32 | M0 | Original art, stable face anchors, real atlas/scene acceptance |
| T-037 | Traffic and vehicles | 12 | M0 | Original art and wheel pivots, gallery |
| T-038 | Urban/Village/Forest/Coast/Mountain/Farm | 96 | M0 | 6 biome source sheets, 16 new prop IDs each |
| T-039 | VFX/UI + multi-page packer | 32 | T036–38 batches | Reproducible IDs, alpha, page budget, 26 profiles +8 events |
| T-040 | Weighted bag + cooldown variety logic | 0 | M0 | Injected seed, collision-free placement, 30m diversity metrics |
| T-041 | Runtime scene mapping & integration | 0 | T039,T040 | >=100 source+atlas+used+approved distinct new assets |
| T-042 | Gallery/synthetic + device QA | 0 | T036–41 | Real FullHD Fold3 tests and adult review; retain blockers |

## 26 reusable motion profiles (not extra source assets)

| Profile | Actor or layer | Timeline/motion | Loop? |
|---|---|---|---|
| CAP-AN01 | Capybara | idle breathe scale +/-1.5% / 3s | loop |
| CAP-AN02 | Capybara | blink two-frame / natural spacing 3–8s | loop |
| CAP-AN03 | Capybara | wave hand swing +/-12 degrees / 2s | loop |
| CAP-AN04 | Capybara | think head lean 6 degrees / 3s | loop |
| CAP-AN05 | Capybara | point left/right with hold | one-cycle |
| CAP-AN06 | Capybara | clap hands gentle 2 beats | one-cycle |
| CAP-AN07 | Capybara | nod / listen head 5 degrees | loop |
| CAP-AN08 | Capybara | praise/celebrate bounce 10px | one-cycle |
| SAU-AN01 | Sâu | breathing / 3.3s | loop |
| SAU-AN02 | Sâu | gentle wave / 2.4s | loop |
| SAU-AN03 | Sâu | think tilt / 2.8s | loop |
| SAU-AN04 | Sâu | clap / 2 beats | one-cycle |
| SAU-AN05 | Sâu | surprise scale 1.00→1.05→1.00 | one-cycle |
| SAU-AN06 | Sâu | attentive blink / 4–8s | loop |
| ONG-AN01 | Ong | breathing / 3.4s | loop |
| ONG-AN02 | Ong | gentle wave / 2.2s | loop |
| ONG-AN03 | Ong | think tilt / 2.9s | loop |
| ONG-AN04 | Ong | clap / 2 beats | one-cycle |
| ONG-AN05 | Ong | surprise scale 1.00→1.05→1.00 | one-cycle |
| ONG-AN06 | Ong | attentive blink / 3–8s | loop |
| VEH-AN01 | bus/car | wheel pivot rotation from distance | loop |
| VEH-AN02 | bus/car | suspension y +/-3px, phase offset | loop |
| VEH-AN03 | train/cable car/boat | controlled horizontal sway or bob | loop |
| ENV-AN01 | tree/flower | soft wind sway <6 degrees | loop |
| ENV-AN02 | water/foam | parallax ripple tint/offset | loop |
| ENV-AN03 | cloud/leaf | slow offset phase-lag / 10–25s | loop |

**8 one-shot semantic FX triggers:** `WELCOME_WAVE`, `QUESTION_POP`, `DISCOVERY_SPARKLE`, `ANSWER_CORRECT_STAR`, `ANSWER_TRY_AGAIN_SOFT`, `ENGLISH_WORD_REVEAL`, `MILESTONE_CONFETTI`, `GOODBYE_FADE`. Use `FX-001..016` and `UI-001..016` IDs only through an allowlisted mapping; no creative JSON may specify a path or unreviewed count of real-world objects. No sound/voice added without consent. At most one prominent effect at a time; graceful fallback `NONE`.

## Mood, variation and sampling

- **Day, dusk, night** use color+tint variants for existing source sprites. These are not duplicate art files or extra counted assets.
- **Layered composition:** background distant (L0), structures/cityscape (L1), road/characters (L2), brief FX/UI (L3). Respect safe areas around child subtitles and parent controls.
- **Scene key:** `tripSeed`, `chunkIndex`, `biome`, `timeBucket`, `audience`, `VisualCue`, `POI priority`. Random once per scene/beat, **never every frame**.
- **Selection:** deterministic shuffle-bag over available IDs, weighted by biome + novelty; max 2 visually dense props per 640px foreground chunk, reserve low-motion background. Track last 8 exact scene signatures in memory; cooldown for rare poses/reward FX; if all choices exhausted, reuse non-distracting neutral pose rather than spin up new network/content.
- **Anti-repeat QA:** 10m/30m deterministic offline synthetic traces, report exact scene repeats within 8 scenes, max streaks of same prop/pose, biome distribution and event rarity, paused/resumed/restarted history. Success: no duplicate within 8 when >=9 eligible distinct layouts, max 20% *identical* full scene signatures within 10m, no same event one-shot more than twice consecutively.
- **No static true-weather claims:** `gentle_raindrops` and real-place architecture are only fictional/comic cues unless separately sourced and reviewed; do not pretend it is raining now.

## Technical delivery

```text
art/assets-source/m10/
  characters/{capybara,sau,ong}/
  vehicles/
  props/{urban,village,forest,coast,mountain,farm}/
  fx/
  ui/
art/M10_ASSET_INVENTORY.csv    # 172 planned source contracts
tools/build_m10_atlas.py       # NEW, future; no change to fixed extras cap
assets/generated/m10/          # generated and ignored at repo level
core/game/{M10AssetRegistry,SceneVariantSelector} # proposed, not yet implemented
```

Build source integrity and alpha bounds, deterministic 1024px page packing, per-scene on-demand page loading; target incremental **resident <=12 MiB GPU**, package total backing **<=24 MiB** (provisional until measured). Existing `kids` 2048², `extras` 1024² and authored Sâu/Ong sheets remain backward compatible. Never load/unload textures inside `render()` and never draw a parent menu under a new lock/PIN dialog.

## Acceptance matrix (run locally, NOT yet executed)

1. `python3 tools/build_sprites.py` and `python3 tools/test_sprite_atlas.py` remain PASS after implementing new M10 packer.
2. New future `python3 tools/test_m10_art.py`: assert exact manifest IDs/paths, 172 planned targets vs physically produced counts, source metadata/layers, atlas IDs, alpha, no missing regions/unsafe SVG.
3. `bash scripts/test-local.sh`, `./gradlew --no-daemon :core:test :android:assembleDebug :android:assembleDebugAndroidTest`; Android API35 instrumentation recheck F10, parent without PIN, audio-only, GPX and AI.
4. 6 biome contact sheets + 3 audience galleries with generated real textures; 10-minute/30-minute seeded replay report.
5. FullHD Fold3 physical DeX 30-min benchmark target >=55 FPS average, P95 <=25 ms, no >10% regression versus *measured current build*, no crash/GPU texture leak; 30–60min adult acceptance with quiet variations.
6. T-042 documentation/evidence: source/pack/used/approved counts distinguished. Do **not** mark M10 DONE for documentation or sketches alone.

**No new generated art, implemented animation logic or device benchmark exists from this design-only task.**
