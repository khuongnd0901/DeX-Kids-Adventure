# T-003 M2 facelift + sprite cast — source checkpoint
Date: 2026-10-09

## Code implementation
- Corrected 6 Capybara expression SVGs with shared positions for eyes/cheeks/front snout/nose/philtrum/mouth; added a Python geometry assertion across six frames.
- Added 5 companion animal sprites, 6 background traffic vehicle sprites. Roster and deterministic scenario selection is in `SceneryCast`; render placements are driven by 640px chunk indices, not live road data.
- Alpha and named region verification continue through `tools/test_sprite_atlas.py`. Original SVG files in `art/assets-source/`; build generates PNG and TextureAtlas.
- Java tests cover stable/different roster selection and safe biome placement.

**CI: VERIFIED for the 29-region baseline** with [run #37879894270](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37879894270), SUCCESS: core unit tests, Capybara facial-landmark check, sprite atlas alpha check, visual cast-sheet, desktop OpenGL screenshots and Android debug APK. Renderer screenshot visual inspection, simulator and Fold3 acceptance not yet verified for this commit.
No milestone DONE. PR stays DRAFT.


## Evidence
- Commit `35ad6d56`: 6 aligned Capybara SVG expressions; CI #37879618113 SUCCESS.
- Commit `d20cb6fe`: 5 companions + 6 traffic vehicle original SVG assets.
- Commit `fd0889a3`: generator/renderer SceneryCast integration; CI #37879826175 SUCCESS.
- Commit `291770e2`: 17-sprite cast review sheet + GIF; run #37879894270 SUCCESS.
- Asset review sheet/screenshot artifact id **11594260235**, zip size 5,380,626 bytes, digest sha256 249b1ba7bcac909a47ceb0a99e9a6b4c8585360f77fa773e8e91b147a9891fbe.
- Atlas artifact id **11593323780**, APK debug artifact id **11593184361**.
- Viewed actual cast sheet and 1920x1080 desktop screenshot: Capybara nose+smile correctly aligned on muzzle; 5 friends and all six vehicles visible on source sheet; initial penguin had bear-like ears, subsequently corrected in commit `ec459c6`.
- Source fix `d6cd0dc` changes spawn cadence so the first companion should appear beyond the main bus silhouette. **Still needs rerender/review**.
- Android simulator and Samsung DeX physical test remain NOT VERIFIED by this tool session. M2 open.
