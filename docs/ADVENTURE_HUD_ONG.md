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

The single indexed, transparent `art/assets-source/characters/ong-costumes.png` is a 128×192 sheet (2×2; each quadrant 64×96). Order: explorer, firefighter, pilot, police. It was derived from the previously generated Ong illustration set and optimized for small memory and a public GitHub repo. Only the **reduced cartoon**, not the original family photographs or full-resolution generated artwork, is included.

`python3 tools/build_sprites.py` (the `generateKidsArt` Gradle pipeline) verifies its SHA-256, alpha and all four quadrants, then copies the asset to `assets/characters/ong-costumes.png` for Android packaging. Existing Sâu sprites are unchanged.

## New in-game HUD

- Top band: project title and selected audience; DEMO, GPX, or GPS/OSM **estimated** source.
- Left side: read-only discovery, quiz and listening activity indicators (not interactive screen buttons).
- Speech panel: current `EntertainmentDirector.Beat.introduction()` caption, already narrated by existing offline Vietnamese TTS.
- Bottom panel: real `poiStatusText` when present, otherwise generic cartoon-world text.
- Journey summary: distance and speed **already supplied by the JourneyFeed**. No invented upcoming stops, route lengths, ETAs or verified road matching.

The current `BitmapFont` only supports basic Latin; new HUD labels are deliberately ASCII/transliterated until a reviewed Vietnamese Unicode font solution is introduced. Original Vietnamese dialogue/audio and backend data are not modified.

In audio-only mode no new sprite or HUD is drawn. This update adds no GPS, microphone, AI or network permissions, no CI, no new textures per frame, and no touchscreen dependency.

## Verification

```bash
git pull origin main
python3 tools/test_adventure_hud_ong.py
./scripts/test-local.sh
./gradlew --no-daemon :android:assembleDebug
adb install -r android/build/outputs/apk/debug/android-debug.apk
```

Check all three audience settings on a single external DeX display. Confirm correct child visibility, outfits changing per narrative topic, speech caption, and non-overlap of parent controls, subtitles, bus and HUD. Test DEMO, GPX and LIVE GPS separately, ensuring estimated POIs are not displayed as facts. Record FPS/P95/ANR/PSS from **actual Fold3** runs. No complete local Gradle build, install or physical-device measurement was performed via the GitHub connector.
