# T-049 — cohesive non-child art redesign

Status: IN_PROGRESS / PARTIAL. Owner: Main. Branch: feat/T-049-storybook-art-v2.
Reference: T-048 original 5 PNGs and PR #55, merged to main.
Protected: ALL Sâu and Ong art. No story/voice/GPS/animation logic changes.

## Milestones
- M0 inventory & protected boundary: DONE (source inventory examined).
- M1 original V2 source shapes: PARTIAL (5 companions + 12 M10 vehicles + 6 detailed road vehicles).
- M2 deterministic non-child source finishing & atlas integration: IMPLEMENTED,
  code review pending; runtime/unittest checks NOT RUN.
- M3 extended non-child redraw: PARTIAL (+60 authored scenery SVGs: 10 environment, 18 extras, 32 M10 active-biome props); 22 named POI backdrops, other M10 and FX/UI not redrawn.
- M4 build, local tests, Android emulator before-after QA: NOT VERIFIED.
- M5 physical Samsung DeX, art acceptance, licensing: NOT VERIFIED.

## Secondary vehicle slice (2026-10-14)
Six active road SVGs redrawn; twelve M10 catalog SVGs detail-enriched and enlarged within unchanged native viewports. Active traffic now 176px high for 4-wheel vehicles and 158px for two-wheelers (was 140px), with aspect-based width and deterministic chunk anchor. Story, GPS, Sâu/Ong, primary bus T-048 files unchanged. Added gallery/test logic; build/emulator/physical DeX NOT RUN. M10 VEH is not currently used by the road renderer, do not imply otherwise.

## 2026-10-14 scenery continuation

- Committed 60 new source-controlled SVGs in storybook-v2 override folders.
- Existing atlas region names, 160×160 M10, 180×160 extras, and environment native dimensions remain stable.
- No source changes to Sâu/Ong or original T-048 five PNG images.
- Static structural/inventory inspection to be recorded; Python raster/atlas + Android emulator NOT EXECUTED.
- Test: tools/test_t049_scenery.py; gallery: tools/render_t049_scenery_gallery.py.
- Merge blocked on genuine build, clipping review, screenshots and user art approval.

## Acceptance
New art should retain the original slot/pose/silhouette intention, landmark
identity, atlas region names and existing gameplay. No modification to
Sâu/Ong source, image bytes, sprite copy logic or their face geometry. T-048
5 original PNG bytes and driver compositing remain unchanged. Include visual
before/after sheets, hashes and reproducible local build logs before claiming
the full redesign is done. Do not merge unfinished art based only on tests.

See docs/STORYBOOK_ART_V2.md.

## Ground-depth slice — 2026-10-14 (Main)
Dependencies: T-048 bus geometry; T-049 six active SVG/atlas pipeline.
Audit: fixed 1920x1080 Y-up camera; road y=0..268, curb263..278;
legacy tire ground≈198, PNG trimmed tire ground≈188. Environment traffic
was drawn before POI panorama AND bus; canvas y110 ignored tire padding.
Plan: central source wheel anchors and per-kind sizes; stable far/near ground
planes around bus; bounded two-lane deterministic timed entries independent
of chunk membership; layer dispatch by ground Y; preserve panorama alpha,
passenger/UI ordering and protected art. One vehicle per lane with offscreen
entry/exit and slot spacing; per-kind speed; use existing paused render delta.
Gates: ten requested regression categories, art/atlas/protected hashes,
core + Android + desktop build, actual API35 FullHD before/after review.
Keep IN_PROGRESS if any screenshot or runtime acceptance remains unverified.

### Ground-depth evidence checkpoint
Implemented TrafficDepthModel/Layout/RenderOrder and6 active SVG corrections.
188 core tests (10 new depth/motion tests, obsolete chunk-size test replaced),
local art/contracts and Gradle Android/testAPK/desktop successful. Actual
Windows Android15/API35 FullHD DEMO BOTH fixtures captured for baseline,
PNG and legacy bus modes; reviewed silhouette/tire/POI/HUD results separately
from instrumentation status.33 protected source hashes unchanged. See
`.agent/evidence/T-049-traffic-depth/summary.md` for exact scope and failures.
Overall plan stays active/IN_PROGRESS; do not interpret local slice evidence
as full bespoke art, physical DeX performance or parent aesthetic approval.
