# T-065 local and physical validation

Candidate source: 76595b6, includes T-063/T-064 over main a0d447b.
Two source-contract assertions updated for the intentional `!guideActs()` pose
condition. These Python-only changes do not change the installed APK.

Build environment (existing local tools):

```sh
export JAVA_HOME=/home/kunrg/.cache/dexkids-qa/jdk17
export ANDROID_HOME=/home/kunrg/Android/Sdk
export PYTHONPATH=/home/kunrg/.cache/dexkids-qa/art-python
./gradlew :core:test :core:checkStoryPack :android:assembleDebug :android:assembleDebugAndroidTest :desktop:classes --console=plain
bash scripts/test-local.sh
adb -s 192.168.1.3:36373 install -r android/build/outputs/apk/debug/android-debug.apk
adb -s 192.168.1.3:36373 install -r android/build/outputs/apk/androidTest/debug/android-debug-androidTest.apk
python3 .agent/evidence/T-065-fold3/run-device.py
```

Explicit external logical display2; physical SurfaceFlinger4620382033380392964
(ARZOPA). Internal displays off; no phone fallback. Model SM-F926B, API35.
APK SHA256 verified against installed package in verification.json.

Actual initial failures retained locally: missing JAVA_HOME, missing art Python
module path; old T-052 and complete-Ong pose string assertions. Final local suite
and build pass. Server restart interrupted the initial host visual supervisor:
88 device screenshots existed but no terminal result was retained, so the full
visual mode was repeated with direct-to-file logging before functional modes.
Raw screenshots/logs remain local; tracked numeric/text receipts are scoped.
