# T-051 local verification

Environment: WSL Linux; JAVA_HOME `/home/khuongnd/.local/opt/jdk17`; ANDROID_HOME `/home/khuongnd/Android/Sdk`; DEXKIDS_NDK_ROOT `/home/khuongnd/.local/opt/android-ndk-r27c`; QA venv Python with CairoSVG 2.8.2 / Pillow 12.3.0.

Final local/source/build gates PASS. No existing assertions were changed or relaxed; local script only adds coast/mountain test. Source/raster contracts do not establish visual acceptance. Emulator evidence is recorded separately by Main.

| Command | Exit | Seconds | Evidence |
| --- | ---: | ---: | --- |
| `python3 tools/test_t049_scenery.py` | 1 | 6.539 | [01-test_t049_scenery.log](01-test_t049_scenery.log) |
| `python3 tools/test_t050_animal_art.py` | 0 | 2.093 | [02-test_t050_animal_art.log](02-test_t050_animal_art.log) |
| `python3 tools/build_sprites.py` | 0 | 29.566 | [03-build_sprites.log](03-build_sprites.log) |
| `python3 tools/test_sprite_atlas.py` | 0 | 2.636 | [04-test_sprite_atlas.log](04-test_sprite_atlas.log) |
| `python3 tools/test_m10_art.py` | 0 | 26.8 | [05-test_m10_art.log](05-test_m10_art.log) |
| `bash scripts/test-local.sh` | 1 | 11.511 | [06-test-local.log](06-test-local.log) |
| `./gradlew :core:test :android:assembleDebug` | 0 | 60.672 | [07-_core_test.log](07-_core_test.log) |
| `./gradlew :core:test :android:assembleDebug :android:assembleDebugAndroidTest` | 1 | 16.964 | [08-debug-testapk.log](08-debug-testapk.log) |
| `python3 tools/test_t049_scenery.py` | 1 | 2.665 | [09-scenery-first-fix.log](09-scenery-first-fix.log) |
| `./gradlew :core:test :android:assembleDebug :android:assembleDebugAndroidTest` | 0 | 18.304 | [10-debug-testapk-fixed.log](10-debug-testapk-fixed.log) |
| `python3 tools/test_t049_scenery.py` | 0 | 3.195 | [11-final-test_t049_scenery.log](11-final-test_t049_scenery.log) |
| `python3 tools/test_t050_animal_art.py` | 0 | 0.449 | [12-final-test_t050_animal_art.log](12-final-test_t050_animal_art.log) |
| `python3 tools/test_t051_coast_mountain.py` | 0 | 0.596 | [13-final-test_t051_coast_mountain.log](13-final-test_t051_coast_mountain.log) |
| `python3 tools/build_sprites.py` | 0 | 14.864 | [14-final-build_sprites.log](14-final-build_sprites.log) |
| `python3 tools/test_sprite_atlas.py` | 0 | 0.807 | [15-final-test_sprite_atlas.log](15-final-test_sprite_atlas.log) |
| `python3 tools/test_m10_art.py` | 0 | 15.577 | [16-final-test_m10_art.log](16-final-test_m10_art.log) |
| `bash scripts/test-local.sh` | 0 | 107.142 | [17-final-test-local.log](17-final-test-local.log) |
| `./gradlew :core:test :android:assembleDebug :android:assembleDebugAndroidTest` | 0 | 36.707 | [18-final-_core_test.log](18-final-_core_test.log) |
| `./gradlew :android:assembleDebugAndroidTest` | 0 | 10.767 | [19-final-testapk-external-catalog.log](19-final-testapk-external-catalog.log) |

Final core result: **189 tests / 66 suites, zero failures, errors or skips**. Android unit tests are NO-SOURCE; no Android unit PASS invented.

Final asset checks:60 T049 scenery,3 T050 animals,16 T051 coast/mountain editable sources; atlas regeneration;30 primary atlas metadata regions;108/172 M10 catalog sources/4 pages;32 production M10 props/1 page. T048 exact5 PNG checksum/determinism/rollback and Sâu/Ong checks passed within full local suite.

Retained failures: irrigation-channel right border clipping; follow-up ox-cart right border clipping; Android test harness `Files.writeString` unavailable on Android compile API (Main fixed to `Files.write` UTF-8 bytes).

Instrumentation fixture catalog originally targeted excluded internal catalog atlas. QA review caught this; an intermediate installed fixture failed after14 scene captures at the missing catalog, retained separately by Main. Main corrected it to a separately host-provisioned test-only external bundle. Production atlas exclusions retained; final testAPK compiled after correction.

Final debug and instrumentation APK builds passed. This does not establish Android instrumentation execution or aesthetic acceptance.

Final APK hashes and production M10 package inspection: [apk-artifacts.json](apk-artifacts.json). Production catalog atlas/pages absent; runtime bundle retained.

## Packaged rollback validation

Built actual classic/png and storybook-v2/legacy debug APK variants, then restored both source switches byte-for-byte and rebuilt storybook-v2/png. All3 builds exit0; ZIP contents verify each packaged switch. APKs remain local/ignored under `build/t051-apks`; [rollback-artifacts.json](rollback-artifacts.json) records sizes/SHA256. Main owns actual emulatorTC15/TC16 execution; these build results do not prove runtime acceptance.
