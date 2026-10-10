#!/usr/bin/env python3
"""Local-only source-level contract for the compact single-DeX parent UX."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
activity=(r/"android/src/main/java/com/khuongnd/dexkids/ParentActivity.kt").read_text()
prefs=(r/"android/src/main/java/com/khuongnd/dexkids/ParentSettings.kt").read_text()
ai=(r/"android/src/main/java/com/khuongnd/dexkids/ai/KidsAiStore.kt").read_text()
manifest=(r/"android/src/main/AndroidManifest.xml").read_text()
assert not list((r/".github/workflows").glob("*.yml")), "No GitHub Actions requested"
assert not list((r/".github/workflows").glob("*.yaml")), "No GitHub Actions requested"
render=activity.split("    private fun render() {",1)[1].split("    private fun showSetupMenu()",1)[0]
assert render.count("primary(") == 5, "Four menu actions + helper function expected"
for label in ("BẮT ĐẦU · GPS thật","XEM THỬ · hoạt hình DEMO",
              "Cài đặt · tuổi","Dừng hành trình"):
    assert label in render, f"Missing compact control {label}"
assert "showSetupMenu()" in render and "showAiSettings()" in activity
assert "showVoiceSettings()" in activity and "chooseRoute()" in activity
assert "showAdvancedTools()" in activity and "Intent.ACTION_OPEN_DOCUMENT" in activity
assert 'getBoolean("offline_tts", true)' in prefs
assert 'getBoolean("child_mic_optin", true)' in prefs
assert 'getBoolean("ai_quizzes", true)' in ai
assert 'getBoolean("child_text_cloud_explicit", false)' in ai, "Child cloud sharing must remain separately consented"
assert 'requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION' in activity
assert 'requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 4003)' in activity
assert 'PackageManager.PERMISSION_GRANTED' in activity
assert 'if (pendingLiveRoute)' in activity, "Denied mic must still start live GPS"
assert 'OnDeviceChildSpeech.available(this)' in activity
assert 'android.permission.RECORD_AUDIO' in manifest and 'android.permission.ACCESS_FINE_LOCATION' in manifest
print("PASS: compact 4-control parent UI; offline defaults ON; runtime GPS/mic consent; cloud child speech sharing OFF; no CI")
