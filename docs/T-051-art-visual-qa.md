# T-051 — FullHD Android art QA and verified corrections

Source base: fetched `origin/main` at `1ab1f42dbb9fe017c9e2e8df2e6260452491c062`.
Windows emulator API35, 1920×1080, existing AVD; no new CI workflows or physical
Samsung DeX claims. Evidence: [T-051](../.agent/evidence/T-051-art-visual-qa/SUMMARY.md).

The baseline has83 Storybook overrides. All83 were rasterized and inspected,
then16 individually authored Coast/Mountain overrides bring the total to99.
These use the existing region names/native160×160 contract. Production still
loads only URB/VIL/FOR/FRM M10 prop families; Coast/Mountain screenshots distinguish
existing named POI scenery from the new test-only Android atlas catalog.

Corrections address actual clipped source edges (irrigation, ox-cart, rabbit,
banana, palm, rice), penguin head identity, curved water-buffalo horns and the
water-scene duck's shortened neck/submerged feet/ripples. The V2 sparrow artwork
includes a branch and claws, so it now sits at the shared ground plane rather
than using the old flying-bird placement. Classic keeps its original flight
presentation. M10 cropped silhouettes fit their existing render envelopes
with their actual pixel aspect, rather than stretching to170×158.

No source edits to Sâu/Ong or the original five T-048 PNGs. Their33 inherited
protected hashes are checked. Traffic model/movement/order, narrative, GPS,
AI, voice, Android permissions and device controls are unchanged.

## Reproduce

Use CairoSVG/Pillow and the existing Java17/Linux Android SDK for local builds;
use Windows `adb.exe` for the running Windows emulator. Repository-specific
NDK override and exact commands/results are recorded in the evidence.

```sh
python3 tools/test_t049_scenery.py
python3 tools/test_t050_animal_art.py
python3 tools/test_t051_coast_mountain.py
python3 tools/build_sprites.py
python3 tools/test_sprite_atlas.py
python3 tools/test_m10_art.py
bash scripts/test-local.sh
./gradlew :core:test :android:assembleDebug :android:assembleDebugAndroidTest
# Catalog atlas is deliberately excluded from the production APK.
# Copy the locally built atlas to this test-only external-files directory first.
adb -s emulator-5554 push assets/generated/m10 /sdcard/Android/data/com.khuongnd.dexkids/files/qa-t051-catalog
adb -s emulator-5554 shell am instrument -w -e mode t051_art -e display_id 0 \
  com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
adb -s emulator-5554 shell am instrument -w -e mode t051_soak -e display_id 0 \
  com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
```

`t051_art` freezes an offline BOTH-child DEMO with production renderer/atlases
and controlled scene/animal choices; screenshots require independent review.
It also renders new CST/MTN sources in an explicitly labelled Android catalog.
`t051_soak` runs normal DEMO continuously for at least300s, checks advancing
frames, records cumulative frame cadence/P95 histogram upper bounds, sampled
previous-frame draw calls/managed textures/PSS, then checks DEMO pause/resume.
All preferences are restored, microphone/cloud/speech disabled in the fixture.
No production GPS fixture or imagery loading is introduced.

Classic rollback requires `assets/art-style.txt=classic`, atlas rebuild,
APK rebuild/reinstall and actual capture. T-048 legacy uses independently
packaged `assets/art-mode.txt=legacy`. Restore both packaged switches to
storybook-v2/png before final build. Fixture reflection is not rollback proof.

## Outstanding scope

Detailed remaining inventory is in [art-review.md](../.agent/evidence/T-051-art-visual-qa/art-review.md):
FX, UI except protected child artwork, Capybara gestures and22 named POI
backdrops still lack bespoke Storybook redraws.64 M10 planned source rows
remain absent. Real-landmark architecture must be verified before any redraw;
no real location facts were authored here. Coast/Mountain production scene
integration, per-region Android appearance of every catalog sprite, parent
visual signoff, long-run flicker/video and physical hardware remain open.
This is a verified implementation slice, not automatic full-task DONE.
