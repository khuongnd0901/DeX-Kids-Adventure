# T-053 — Windows Child Costume QA

State: **DONE — REQUESTED WINDOWS EMULATOR ACCEPTANCE**.
Owner Main. Dependencies T-048/T-051/T-052. Base latest fetched main8ee864d.
Branch feat/T-053-windows-child-outfits-qa; PR61. No CI enabled.

| Milestone | Result | Evidence |
| --- | --- | --- |
| M0 latest source | VERIFIED: ff-only latest main, reference ancestor, unrelated untracked work preserved | Git checkpoint7a24fc2 |
| M1 Windows emulator | API35/x86_64, existing DeX_Stability_API35_20261005,1920×1080/density160 | test-results/windows-environment.txt |
| M2 source/build | Required Python/local suite and194core tests PASS; debug/testAPK build/install PASS | baseline-results.json, release-local.log, pet-palm-final-build.log, core-counts.json |
| M3/M4 art |96pose-aware layers;12styles×4poses checked in story and bus; same palette/index; crowns fit window; no face/HUD obstruction by garment | visual-review.csv, real screenshots/sau,ong,both,bus |
| M5 rotation |240s unchanged; six unique choices; independent starts; stable atlas; audience switch, pause/resume and actual recreation PASS | OutfitRotationTest5tests; pet-palm-final-emulator.log |
| M6 ages | Android fresh/swapped/correct/custom/restart and focus mapping PASS; Sâu5/Ong4/BOTHmin | final/fallback emulator logs; test_t052_child_ages.py |
| M7 actual screenshots |72baseline+72final variants,12fallback variants, parent/recreation extras; Android UiAutomation and additional ADB screenshot/pull | screenshot-manifest.json; before/,sau/,ong/,both/,fallback/; comparisons/adb-live.png |
| M8 performance/lifecycle | Atlas1page16MiB; no reload on rotation; optional absent atlas and GL recreation PASS; short real measurements retained | performance/*.json; fallback-apk-integrity.json |
| M9 GitHub | Seven verified feature slices committed/pushed; PR61 merged atb0f5dd7; local main synchronized | Git/PR receipts |

## Correction and visual review

Original fixed overlays FAIL: Sâu headband crossed eyes, torso/legs displaced,
full-body garments stretched onto cropped bus portraits, standing skirt used
for crouch. New build samples original fabric geometry/shading without editing
source PNGs. Explicit eye/mouth exclusion; six distinct motifs perchild;
pose-specific skirt and original-hat crown; exact passenger alpha>=128 crop
and base aspect ratio. All96story/passenger combinations reviewed by Main
from actual Android captures. Derived crop grids are comparisons, not proof
of a desktop render being Android. All6final PET variants re-reviewed after
polygon hand mask removed knee-colored holes while retaining the whole palm.
Existing explorer accessories and nearby PET animal remain; no new cape or
face-crossing headband. Original child source bytes match8ee864d(10files).

## Measurements and limits

Final normal gameplay217frames/9.999529s=21.701FPS,P95upper56ms,
18drawcalls,10managedtextures,PSS64457KiB. Nearby fallback sample219frames/
10.039998s=21.813FPS,P95upper58ms,14calls,9textures,PSS65513KiB.
These short sequential windows are descriptive, not controlled causal A/B.
Four extra garment quads for BOTH; observed+4calls and+1texture. One raw
RGBA2048×2048 atlas=16MiB(previous8MiB); processPSS is not isolated GPU memory.
Seven0.75s rotation windows cover0/240/480/720/960/1200/1440simulated seconds;
samples and variation retained without claiming precise change-event latency.
Smooth30/60FPS, long-run flicker and physical Samsung Fold3 external DeX
performance remain **NOT VERIFIED**; T-053 Windows scope does not close those
separate project performance/hardware gates. No child/cloud/audio/GPS data
sent during fixtures; local parent/AI settings restored.

## Failed attempts retained

SystemPython lackedPIL: reused existing /tmp/t051-art-venv, no SDK reinstall.
Generated-script indentation error failed build and was corrected. Initial
face-alpha assertion found garment pixels; explicit protection fixed it.
A full-size PET review found knee mask holes; polygon mask + regression
anchors fixed them, then all required checks/build/captures reran. No
assertion removed, fake screenshot, fake benchmark or hidden failed run.

## Reproduction

Use existing venv PATH, JAVA_HOME=/home/khuongnd/.local/opt/jdk17,
ANDROID_HOME=/home/khuongnd/Android/Sdk,
DEXKIDS_NDK_ROOT=/home/khuongnd/.local/opt/android-ndk-r27c.
Run scripts/test-local.sh and ./gradlew :core:test :android:assembleDebug
:android:assembleDebugAndroidTest. Windows adb install -r both APKs, then:
`adb -s emulator-5554 shell am instrument -w -e mode t053_outfits -e display_id 0 com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner`.
Device captures: /sdcard/Android/data/com.khuongnd.dexkids/files/qa-t053.
Fallback mode t053_fallback uses ignored signed APK with only child-outfits
atlas/png removed; all other APK payload bytes match final release.
Production APK remains normal and parent launcher is restored after QA.

Additional process-death migration check: test APK prepare marks migration, stores parent-selected4/5, then adb force-stop and a separate instrumentation process verify4/5 is retained. Actual PASS; original age keys/marker restored from app-private backup and backup deleted. Logs age-process-emulator.log and age-process-build-retry.log. Initial build rejected Files.readString on Android; corrected to supported readAllBytes, no production behavior change.

GitHub delivery complete: exact d46f1c0 head merged in PR61 atb0f5dd7; merge tree equals verified feature tree. See MERGE.md/MERGE.json. Physical DeX/FPS project gates unchanged.
