# DeX Kids Adventure: Sâu/Ong illustrated journey HUD

Updated 2026-10-10. Target: **one external 1920×1080 Samsung DeX screen**; children do not touch the monitor. Parent selection remains in native Parent settings (mouse/F10). This is a renderer enhancement on top of the existing GPS, POI and Vietnamese speech pipelines.

## Three audiences
| Parent audience | On-screen companions | Style |
| --- | --- | --- |
| `SAU` (4 years) | Sâu + Capybara | green label |
| `ONG` (3 years) | Ong + Capybara | pink label |
| `BOTH` (3–4 years) | Sâu + Ong + Capybara | yellow label; separate character staging |

Both characters dynamically wear the same **explorer, firefighter, pilot or police** costume appropriate to the current offline entertainment beat. Costume and bounce are UI fiction, not evidence of what is next to the vehicle. Companion drawing shares the existing SpriteBatch; both optional textures are loaded once per scene and explicitly disposed.

## Art

The single indexed, transparent `art/assets-source/characters/ong-costumes.png` is a 192×288 sheet (2×2; each quadrant 96×144). Order: explorer, firefighter, pilot, police. It was derived from the previously generated Ong illustration set and optimized for small memory and a public GitHub repo. Only the **reduced cartoon**, not the original family photographs or full-resolution generated artwork, is included.

`python3 tools/build_sprites.py` (the `generateKidsArt` Gradle pipeline) verifies its SHA-256, alpha and all four quadrants, then copies the asset to `assets/characters/ong-costumes.png` for Android packaging. Existing Sâu sprites are unchanged.

## New in-game HUD

- Native Android mode badge: Sâu (green), Ong (pink), or both; DEMO/GPX/LIVE remains explicitly distinguished.
- Top-left Android cards: current topic (sky, traffic, animals, colours, shapes, nature), and real activity narration.
- Speech panel: current `EntertainmentDirector.Beat.introduction()` caption, already narrated by existing offline Vietnamese TTS.
- Lower left: passive exploration / quiz / story progression; lower right retains existing real `poiStatusText` and source attribution.
- Journey summary: distance and speed **already supplied by the JourneyFeed**. No invented upcoming stops, route lengths, ETAs or verified road matching.

The Android game now uses native `AdventureDashboard` text panels with full Vietnamese Unicode for audience, topics, 66 offline activities, quiz dialogue and estimated POI status. The separate desktop/LibGDX HUD remains ASCII-only because the default `BitmapFont` lacks Vietnamese glyph coverage. Panels are read-only (no child touch dependency); existing native parent controls use F10/mouse.

In audio-only mode no new sprite or HUD is drawn. This update adds no GPS, microphone, AI or network permissions, no CI, no new textures per frame, and no touchscreen dependency.

## Verification

```bash
git pull origin main
python3 tools/test_adventure_dashboard.py
python3 tools/test_ong_character_art.py
python3 tools/test_adventure_hud_ong.py
./scripts/test-local.sh
./gradlew --no-daemon :android:assembleDebug
adb install -r android/build/outputs/apk/debug/android-debug.apk
```

Check all three audience settings on a single external DeX display. Confirm correct child visibility, outfits changing per narrative topic, speech caption, and non-overlap of parent controls, subtitles, bus and HUD. Test DEMO, GPX and LIVE GPS separately, ensuring estimated POIs are not displayed as facts. Record FPS/P95/ANR/PSS from **actual Fold3** runs. No complete local Gradle build, install or physical-device measurement was performed via the GitHub connector.

## Current acceptance boundary

GitHub source/contract checks validate wiring and the original low-resolution child-cartoon asset. **Physical Fold3 testing, full Gradle/JUnit build, DeX layout validation at multiple DPIs, and post-change FPS/PSS measurements remain pending.** Never claim verified route ETAs or actual location from simulated scenery. CI stays disabled; tests are local-only.
