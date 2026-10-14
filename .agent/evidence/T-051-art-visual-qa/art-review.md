# T-051 offline source raster audit — 2026-10-14

Owner: Art Agent. Baseline: main `1ab1f42dbb9fe017c9e2e8df2e6260452491c062`.
Scope: **83/83 existing Storybook V2 overrides rasterized and visually
inspected**, in 17 classic/Storybook contact sheets. This establishes source
raster review, not 83 runtime appearances, parent approval or full T-051 DONE.
The sheets are CairoSVG/Pillow output, deliberately outside `screenshots/`.

Reproduce from repository root with the QA Python environment:

```sh
/home/khuongnd/.local/share/dexkids-qa-venv/bin/python tools/render_t051_art_gallery.py .agent/evidence/T-051-art-visual-qa/comparisons
/home/khuongnd/.local/share/dexkids-qa-venv/bin/python tools/render_t051_art_gallery.py .agent/evidence/T-051-art-visual-qa/comparisons --fixes
```

`comparisons/inventory.json` records every relative source, native dimensions,
alpha bounds, source SHA256 and review sheet. Runtime Storybook treatment is
applied at native size, before the preview is enlarged. Every matching classic
source exists and preserves native dimensions. No new external image source,
licence assumption, runtime network access or texture page is introduced.

## Baseline visual observations and corrections

| Family | Count reviewed | Source raster observations |
|---|---:|---|
| Companions | 5 | Clear faces and feet. Rabbit left ear outline was clipped at the upper source edge. Penguin had two tall rabbit-like ears and blue feet, weakening its species identity. Corrected ear clearance and penguin smooth crown/ochre feet. |
| Environment | 10 | Tree/building/house silhouette and linework remain readable; hills/river are horizontal scenery strips. Cloud is light on a pale background; assess contrast in actual sky. No isolated silhouette clipping identified. |
| Foreground extras | 18 | Sparrow has beak, eye, feather layers and two claws on a branch; recognizable versus the old generic blue bird. Buffalo has four leg silhouettes and hooves, but horns are steep/upward and stylized; water-buffalo horn direction was a review concern, corrected with broad lateral/back-curved crescents. Duck has a flat bill, white wing and visible feet, but an elongated upright neck; Android review by Main confirmed WATER/BRIDGE pool placement with feet floating on water. Corrected to shorter-neck swimming duck with submerged feet and surface ripples. Banana/palm trunk ends and rice-field left leaves were clipped; corrected frame clearance. |
| M10 urban | 8 | Storefront contents distinguish bakery/fruit/library/toy shop, though their building silhouettes remain similar. Mailbox/shelter/island readable. Requires scale check relative to bus. |
| M10 village | 8 | Gate/fence/hay/pump readable, grounded base shapes visible. Tile-roof home roof pattern is schematic rather than realistic roof detail. No isolated clipping identified. |
| M10 forest | 8 | Hollow trunk, fern, fungi, log, rocks and vine arch distinct. Oak and generic environment tree remain visually close; no additional shape change claimed. |
| M10 farm | 8 | Irrigation sprig and ox-cart animal were visibly clipped at right frame boundary; both corrected. Rice bundle has ties; green/gold rice use similar structure by growth-stage intent. Ox-cart ox remains a schematic small head/body depiction; restored bounds do not constitute a realistic animal redraw. |
| M10 vehicles | 12 | Different fittings distinguish ambulance/fire/camper/recycling/ice-cream/electric vehicles; several share a common rounded body construction. Tractor/train/sailboat/cable car have distinct silhouette. Catalog art is not moving runtime traffic. |
| Active traffic | 6 | Side-view wheels, windows, lights, hubs/spokes readable; tire planes and outlines coherent in isolated rasters. Depth/lane occlusion must be assessed in actual runtime captures. |

All 17 contact sheets were opened using `view_image`; this review did not infer
appearance from XML or alpha assertions alone. Corrected artwork was separately
opened in `comparisons/t051-source-fixes.png`, which compares **baseline V2 from
pinned pre-T051 Git 1ab1f42** to corrected V2, rather than confusing classic art with the defective
baseline. Raster review confirms the restored right edge of the ox-cart animal,
transparent margins around rabbit/tree/rice outlines, and the penguin smooth
head outline. Region names and viewboxes are unchanged.

## Executed correction checks

Initial `tools/test_t049_scenery.py` failed on the irrigation right border.
After the sprig correction it advanced and failed on ox-cart right border.
Both are real source clipping failures; assertions were unchanged.

After the source corrections:

```text
tools/test_t049_scenery.py:
T-049 Storybook scenery source/raster/alpha contracts PASS: 60 assets
tools/test_storybook_art.py:
T-049 source/size/alpha/protection/determinism contracts PASS
```

These executed checks use
`/home/khuongnd/.local/share/dexkids-qa-venv/bin/python`. Main Agent owns final
atlas rebuild, Android build, screenshot/in-scene acceptance and performance.
Protected Sâu/Ong and original five T-048 PNGs were outside this edit scope.

Final animal rerun: `tools/test_t050_animal_art.py` PASS for all three, with
alpha boxes buffalo `(14,42,165,153)`, bird `(17,52,170,155)`, duck
`(22,45,172,149)`. The nine-correction before/after sheet was visually
inspected after the animal edits. Main owns Android verification of the
updated swimming duck and curved-horn buffalo.

Final native plant alpha boxes: banana `(8,17,173,157)`, palm
`(24,13,164,157)`, rice `(4,43,172,157)`. The first attempted automated
plant group replacement did not match the source attribute order; it was
corrected before delivery. Initial rice inset still left one pixel clearance;
a second source inset gives four pixels. No clipping assertion was weakened.

## Baseline remaining bespoke artwork inventory

The initial 83 do not cover the whole repository. At this audit checkpoint,
individual Storybook overrides were still absent for the following families:

- Coast: 8 — beach_parasol, crab_cartoon, fishing_net_rack, foam_wave,
  sand_dune, shell_spiral, shell_white, starfish_cartoon.
- Mountain: 8 — cascade_small, highland_flower, hill_cabin, mountain_ridge,
  pine_canopy, rope_guardrail, scree_pile, viewpoint_platform.
- FX: 16 — bubble_rise, butterfly_trail, cartoon_heart_pop, cloud_puff,
  confetti_burst, correct_starburst, dust_puff_round, gentle_raindrops,
  leaf_swirl, petal_drift, question_pop, reward_halo, sparkle_cluster,
  surprise_rays, vehicle_speed_lines, water_ripple.
- UI: 8 — audience_sau, reward_heart, reward_star, sticker_correct,
  sticker_discover, sticker_english, sticker_retry, sticker_wow. The
  audience_sau artwork remains protected, not a candidate for modification.
- Capybara gesture source: 8 — cap_cheer, cap_clap, cap_laugh, cap_listening,
  cap_point_left, cap_point_right, cap_puzzled, cap_thinking. Current source
  selector explicitly excludes cap_ names, so new overrides would require
  separate integration analysis rather than simply adding a folder.
- 22 named POI backdrops: retain original identity until source-bound
  architecture/reference review is available. No landmark reconstruction or
  historical accuracy claimed by this raster audit.

Main Agent may complete additional families after this checkpoint. Use the
final SUMMARY and inventory for the integrated delivery count. Parent aesthetic
approval, actual 83-sprite in-scene appearance and remaining landmark validation
stay open unless separately evidenced.

## Integrated coast/mountain raster checkpoint

Main authored 16 coast/mountain overrides during this audit. Regenerated gallery
now contains **99 pairs in 21 sheets**, plus the nine-correction V2 baseline/fix
sheet. Opened all four new coast/mountain sheets with `view_image`:

- Coast: segmented parasol canopy, claw/eyestalk crab, draped fishing net,
  curling foam wave, sand dune grasses, spiral shell, ribbed white shell and
  spotted starfish have distinct shapes. Fine fishing-net lines may disappear
  at small runtime scale; verify actual scene. No raster clipping observed.
- Mountain: cascade ends in a pool, cabin has boards/chimney/doorstep, pine
  group has visible trunks, ropes connect posts, viewpoint platform includes
  deck/rail/legs, and ridge/scree silhouettes are distinct. No raster clipping
  observed. Color contrast and grounded presentation require Android review.

The 16 new assets were visually inspected as actual raster output, independently
of source/alpha assertions. This closes the missing source-shape inventory for
coast/mountain; it does not automatically close full Android runtime acceptance.
FX, UI, gestures and verified named POI revisions remain pending as listed above.

## Remaining-family read-only raster audit

At Main's request, reviewed another **54 raster images**: all 16 M10 FX,
all 8 M10 UI, all 8 M10 Capybara gesture SVGs and all 22 generated POI PNGs.
Eight contact sheets are in `atlas-review/remaining-*.png`; every sheet was
opened with `view_image`. Thus 99 override-source pairs plus these 54 remaining
images were actually viewed. This is not a claim that every catalog asset has
been displayed on Android, or that protected character pose families have all
been reviewed individually in this slice.

FX/UI/CAP sheets use CairoSVG rasterization at original dimensions and the
same `stylize` family as `tools/build_m10_atlas.py` (`effect`, `ui`, `sprite`).
The POI sheets read existing **actual generated 960×520 PNG files** from
`assets/generated/backdrops/`, without rebuilding or modifying application
assets. Read-only previews are fit within 510×266 contact-sheet panels. These
remain offline asset reviews, not Android evidence. No protected source or
generated app output was edited during this follow-up.

| Reviewed family | Observations / open defects |
|---|---|
| FX 16 | Stars/hearts/bubbles/question/rays are recognizable, intentionally simple effect motifs. `cloud_puff`, `vehicle_speed_lines` and fine confetti have low contrast on pale backgrounds. `leaf_swirl` uses curved strands/yellow ovals rather than clear leaf shapes; `butterfly_trail` is colored discs plus a star rather than a butterfly silhouette. `confetti_burst` bottom particle is visibly cut at the native lower edge, confirmed alpha bbox `(2,27,148,160)` in 160×160. |
| UI 8 | Heart/star/check/retry readable but remain simple icon shapes. Native top-edge contact is visible in `reward_star`, `sticker_correct`, `sticker_discover`, `sticker_retry`, `sticker_wow`; these cannot be accepted as fully unclipped icons. `sticker_english` contains the glyph-like symbol **MF**, whose meaning is unclear for an English reward. `audience_sau` was inspected read-only and remains protected. |
| Cap gestures 8 | Consistent legacy brown Capybara with blue hat/backpack and recognizable muzzle; body proportions/shading differ visibly from T-048 PNG style. Cheer/laugh share raised-arm construction; puzzled/thinking differ mainly in face and hand position. No source-edge clipping identified in these eight. They have not been individually shown as runtime poses in this audit. |
| POI 22 | All are visibly simple, shared-template scenery with sparse building details. `hcm_palace.png` and `war_museum.png` are **byte-identical PNGs**, verified by direct byte comparison and RGB image-difference bbox `None`; two different named POIs therefore lack distinct visual identity. Ke Ga/Vung Tau lighthouse use very similar tower profiles; flower garden/city park share much of their layout. Architectural/historical correctness cannot be established from these generic drawings and has not been certified. Retain until verified references support distinct source-bound revisions. |

Current `CartoonSprites` owns `M10PropAtlas` and selects biome prop regions;
there is no corresponding observed FX/UI/CAP catalog rendering call in the
reviewed renderer. Consequently these 32 source previews document available
catalog quality, not proof of live gameplay usage. POI rendering is a live
path, but the Android TC13 checkpoint exercises a selected backdrop, not all
22. Main owns the exact runtime coverage matrix and acceptance state.

The confirmed remaining clipping, unclear UI mark, legacy gesture style and
duplicate/generic POIs are recorded as **open**, not silently converted to
PASS. This follow-up performed no source fixes, no benchmark and no factual
landmark reconstruction.
