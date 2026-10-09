# T-003 M2 facelift + sprite cast — source checkpoint
Date: 2026-10-09

## Code implementation
- Corrected 6 Capybara expression SVGs with shared positions for eyes/cheeks/front snout/nose/philtrum/mouth; added a Python geometry assertion across six frames.
- Added 5 companion animal sprites, 6 background traffic vehicle sprites. Roster and deterministic scenario selection is in `SceneryCast`; render placements are driven by 640px chunk indices, not live road data.
- Alpha and named region verification continue through `tools/test_sprite_atlas.py`. Original SVG files in `art/assets-source/`; build generates PNG and TextureAtlas.
- Java tests cover stable/different roster selection and safe biome placement.

**CI: PENDING after authoring**. Renderer screenshot visual inspection, simulator and Fold3 acceptance not yet verified for this commit.
No milestone DONE. PR stays DRAFT.
