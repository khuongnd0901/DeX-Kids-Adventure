# T-066 measurement protocol

Baseline product APK: source bcd832c / SHA256 accab9e2af3329bb961d6be11ed9815f93d5307308db5dec8fdcb415ea884056.
Host uses existing JDK17 at /home/kunrg/.cache/dexkids-qa/jdk17, SDK
/home/kunrg/Android/Sdk, PYTHONPATH=/home/kunrg/.cache/dexkids-qa/art-python.

```sh
./gradlew :core:test :core:checkStoryPack :android:assembleDebug :android:assembleDebugAndroidTest :desktop:classes --console=plain
bash scripts/test-local.sh
adb install -r android/build/outputs/apk/debug/android-debug.apk
adb install -r android/build/outputs/apk/androidTest/debug/android-debug-androidTest.apk
adb shell am force-stop com.khuongnd.dexkids
adb shell am instrument -w -r -e display_id 2 -e capture_display_id 4620382033380392964 -e mode t066_resources com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
python3 .agent/evidence/T-066-resources/run-regressions.py
```

Only the new test APK was installed for the baseline; baseline app hash verified.
Same test probe for final before/after: cold process, BOTH, same DeX2 FullHD,
DEMO, AI/TTS/mic/ambient/autoplay temporarily disabled, no explicit GC/purge,
90sFull+15sAudio+15sFull; foreground source URL remains configured. Ten Full
snapshots at0-90s, then Audio and restored Full. Rows30-90s define steady mean;
CPU = process CPU-time delta / elapsed delta,100%=one core. Probe restores original
preferences; no real GPS/ASR/AI provider calls. Public source HTTPS may refresh
normally; its settings/cache are preserved.

Final generation succeeded in runtime-final-build.log. To remove locally generated
pages from rejected experiments, moved only those known files into the local
archive; repackaged with `:android:assembleDebug -x generateKidsArt` after successful
full generation. Verified APK contains original single kids atlas,640x960 runtime
sheets and no discarded pages before installing/testing. Product code unchanged
between the successful full build and this cleanup package.

Library probe seeds isolated40local+30remote stories, deletes1, warms cache then
measures20lists. Checks favourites/local-first/source/deleted/undo semantics and
reuse of64immutable episodes with persistent cache<=64. Cursor-factory counting
proved unavailable on this Android implementation; initial zero counter is NOT
zero actual queries. Two bulk SELECTs are established by source inspection,
while actual list latency and object reuse are device measurements.
