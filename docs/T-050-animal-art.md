# T-050 — Realistic animal redraw (buffalo, bird, duck)

Three T-049 Storybook V2 source sprites were redrawn from scratch with
anatomically recognizable details and inspected visually at 3x zoom.
Their existing original `art/assets-source/props/{name}.svg` files are
unchanged; the optional overrides are at:

- [buffalo](../../art/assets-source/storybook-v2/props/buffalo.svg):
  Vietnamese swamp/water buffalo with large barrel body, grey/dark coat,
  back-sweeping horns, low natural head/muzzle, ears, nostril, four legs,
  visible black hooves and restrained fur highlights.
- [bird](../../art/assets-source/storybook-v2/props/bird.svg):
  Eurasian tree sparrow, brown crown, black cheek patch and bib, dark tapered
  tail, feather groups, short triangular seed-eating bill, two clawed feet
  clasping a supporting branch.
- [duck](../../art/assets-source/storybook-v2/props/duck.svg):
  domestic white duck, cream feather mantle, layered wings and tail, upright
  neck, flat orange bill with nostril, dark eye and separate webbed feet.

Each file remains **180×160 SVG with transparent background**, same region
name, identical props atlas slot and build-time `select_source()` lookup.
No new GL asset, atlas page, Java renderer, gameplay/story/GPS/voice change.
All faces oriented right (consistent with previous three SVGs). Ground/contact
art stays inside the original frame at y≃147–155.

## Verification

The three authored SVGs were locally converted with CairoSVG to
540×480 PNG previews, and also rendered at native 180×160. The resulting
SHA blob IDs were verified against the GitHub branch files to ensure the
commits contain *exactly the visually inspected artwork*.

| Asset | Native RGBA nonempty bounding box | Filled paths | Edge crop |
|---|---|---:|---|
| buffalo | (14,23)–(165,153) | 27 | none |
| bird | (17,52)–(170,155) | 22 | none |
| duck | (23,29)–(172,155) | 19 | none |

Before release execute:
```bash
python3 tools/test_t050_animal_art.py
python3 tools/test_t049_scenery.py
python3 tools/build_sprites.py
python3 tools/test_sprite_atlas.py
bash scripts/test-local.sh
./gradlew :core:test :android:assembleDebug
```

Then capture Android 15 API35/FullHD 1920×1080 previews with each animal
in its applicable biome, including when passing the main bus, to verify
apparent size, placement, depth and silhouette.

**Only individual SVG rasterization, edge and Git blob identity were
executed in this assistant session.** No full Gradle build, full atlas
or Windows emulator/physical Samsung DeX run; do not mark these as PASS.
Sâu/Ong, primary T-048 Capybara/bus PNGs, traffic depth and HUD are
intentionally unmodified.

This is an art quality improvement, not a photograph or biological asset
identification in the live GPS world.
