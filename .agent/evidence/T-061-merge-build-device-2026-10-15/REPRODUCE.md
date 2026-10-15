# T-061 local/physical commands
Host JDK17: /home/kunrg/.local/opt/jdk17; Android SDK:
/home/kunrg/Android/Sdk. Python raster modules supplied through
PYTHONPATH=/home/kunrg/.cache/dexkids-qa/art-python.

```
bash scripts/test-local.sh
./gradlew --no-daemon :android:assembleDebug :android:assembleDebugAndroidTest
adb -s 192.168.1.3:35165 install -r android/build/outputs/apk/debug/android-debug.apk
adb -s 192.168.1.3:35165 install -r android/build/outputs/apk/androidTest/debug/android-debug-androidTest.apk
adb -s 192.168.1.3:35165 shell am instrument -w -r \
  -e display_id 2 -e capture_display_id 4620382033380392964 \
  -e mode MODE com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
```
Modes and actual results:device-results.jsonl; raw device-MODE.txt logs.
A few incoming story fixtures finish with instrumentation code0 or-1 and
mode-specific PASS/asserted result fields rather than common qa_status.
Never infer PASS from adb process exit alone. Failures remain retained.

No live AI/real GPS/real ASR instrumentation invoked. Story smoke uses local
SQLite fixtures and sourced downstream POI queue injection, not GPS arrival.
The prior T-060 hour uses b5b5423 application source; it is not retested here.
