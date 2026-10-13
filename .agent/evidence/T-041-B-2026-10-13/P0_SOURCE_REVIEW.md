# T-041.B P0 source checkpoint — 2026-10-13

Owner Main; dependency T-047 source 2b1b61b; main-only execution.

## Audit / plan / implementation

92 existing P0 SVGs; add the missing 8 Sâu + 8 Ong named P0 gestures
from the established inventory. `tools/author_m10_avatar_vectors.py` traces
heads from approved public cartoon sheets and authors distinct body/hand paths.
No original child photographs, external SVG references or embedded raster.
All 108 source candidates remain SOURCE_CREATED_UNREVIEWED. No P1 work.

The SVG crop review removed disconnected garment/arm fragments. Main reviewed
the rendered 16-pose contact sheet locally. Faces follow the approved references;
flat vector bodies differ stylistically from the original shaded cartoon bodies.
Therefore this is a validated draft-source checkpoint, not likeness acceptance.
The approved original child sheets remain active in gameplay. These drafts are
not added to the runtime atlas. Parent/art quality review is still required.
Generated gallery assets are explicitly excluded from APK runtime assets.

## Test / review

`bash scripts/test-local.sh`: PASS, including existing atlas parser/security,
172 inventory entries, 108 actual source files, reproducible full gallery atlas
(4 pages), and the unchanged runtime prop subset (32 regions, 1 page).
Core JUnit: 172 tests, 0 failures/errors/skips.
`./gradlew --no-daemon :android:assembleDebug :android:assembleDebugAndroidTest`:
BUILD SUCCESSFUL. Android unit target remains NO-SOURCE from prior checkpoints;
no new physical graphics/likeness/animation PASS is claimed here.
`git diff --check`: PASS.

Local full log: /home/kunrg/.cache/dexkids-qa/t041-p0-build.log.
Local draft preview: /home/kunrg/.cache/dexkids-qa/t041-avatars-current.png.
Full generated atlas and preview remain local, excluded from runtime payload.

## Remaining acceptance

26 profiles / 8 semantic triggers and non-prop runtime integration remain open.
At least 100 rendered + human-reviewed P0 assets, resource budget, final DeX
visual/lifecycle tests, and parent likeness/comfort acceptance are NOT VERIFIED.
Do not mark T-041.B DONE or begin P1 based on source counts.
