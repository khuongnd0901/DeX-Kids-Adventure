# Host Android execution

Windows ADB: /mnt/d/Android/Sdk/platform-tools/adb.exe, serial emulator-5554.
Install debug/testAPK via `adb install -r` Windows D:\DeX-Kids-Adventure paths.
Provision catalog: `adb push assets/generated/m10 /sdcard/Android/data/com.khuongnd.dexkids/files/qa-t051-catalog`.
Execute: `adb shell am instrument -w -e mode MODE -e display_id 0 com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner`.
Modes/outputs:

- t051_art initial14scenes: android-fixtures-initial.txt
- t051_art stale testAPK failed missing internal catalog: android-fixtures-stale-testapk.txt
- corrected t051_art final16captures: android-fixtures-final.txt
- t051_soak >=300s: android-soak.txt
- classic/png actual APK then t051_art: android-classic.txt; retainedTC06 asTC15
- V2/legacy actual APK then t051_art: android-legacy.txt; retainedTC02 asTC16
- restored finalV2/png then t049_traffic8cases: android-traffic-regression.txt

Screenshot originals pulled with ADB from external-files qa-t051/qa-t049/qa-t051-soak.
adb-initial.png additionally uses `adb shell screencap -p /sdcard/t051-initial.png` then `adb pull`.
See source-dimension/hash manifest screenshots.json; no host raster is saved in screenshots/.
