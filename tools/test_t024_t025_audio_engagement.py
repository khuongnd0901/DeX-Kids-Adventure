#!/usr/bin/env python3
"""T-024/T-025: static safety + integration checks; does not claim acoustic/device QA."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
mobile = root / "android/src/main/java/com/khuongnd/dexkids"
core = root / "core/src/main/java/com/khuongnd/dexkids/story"
pref = (mobile / "ParentSettings.kt").read_text(encoding="utf-8")
parent = (mobile / "ParentActivity.kt").read_text(encoding="utf-8")
kids = (mobile / "KidsActivity.kt").read_text(encoding="utf-8")
narrator = (mobile / "OfflineVietnameseNarrator.kt").read_text(encoding="utf-8")
sound = (mobile / "KidSoundscape.kt").read_text(encoding="utf-8")
metrics = (core / "ChildEngagementMetrics.java").read_text(encoding="utf-8")
manifest = (root / "android/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
qa = (root / "docs/T024_T025_AUDIO_CHILD_QA.md").read_text(encoding="utf-8")

# Silence-by-default ambient; parent controls and an offline-only synthesis path.
assert 'getBoolean("audio_effects", true)' in pref
assert 'getBoolean("ambient_music", false)' in pref
assert "settings.audioEffects = !settings.audioEffects" in parent
assert "settings.ambientMusic = !settings.ambientMusic" in parent
assert "SoundPool.Builder()" in sound and 'synthesize("ambient")' not in sound
assert "synthesize(type)" in sound and 'listOf("chime", "bird", "cat", "rabbit", "bus", "ambient")' in sound
assert "speechActive" in sound and "updateAmbient()" in sound
assert "0.035f" in sound and "0.14f" in sound
assert "AudioAttributes.USAGE_GAME" in sound
for forbidden in ("MediaRecorder", "AudioRecord", "java.net", "HttpURLConnection", "requestAudioFocus(", "setStreamVolume("):
    assert forbidden not in sound, forbidden
assert "soundscape?.setSpeechActive(true)" in kids
assert "soundscape?.setSpeechActive(false)" in kids
assert "soundscape?.playForBeat(beat.id())" in kids
assert "soundscape?.setPaused(true)" in kids
assert "soundscape?.shutdown()" in kids
assert "setOnAudioFocusChangeListener" in narrator
assert "currentUtteranceId" in narrator and "completeIfCurrent" in narrator
assert "Never invoke normal completion after cancellation" in narrator
assert "engine.setSpeechRate(if (narrationAge <= 3) 0.88f else 0.94f)" in narrator
assert 'it.locale.language == "vi" && !it.isNetworkConnectionRequired' in narrator

# Objective-only in-memory metrics: no child voice transcripts/location/analytics.
assert "engagementMetrics.recordBeatStart" in kids
assert "engagementMetrics.completeBeat()" in kids
assert "engagementMetrics.interruptForPoi()" in kids
assert "engagementMetrics.recordParentPause()" in kids
assert "Phiên này:" in kids and "không phải thước đo hai bé có thích" in kids
assert "recordVoiceUnavailable()" in metrics and "otherCancellations" in metrics
for forbidden in ("Location", "latitude", "longitude", "transcript", "Http", "Gdx", "Log.", "System.out"):
    assert forbidden not in metrics, forbidden
assert "không ghi âm" in qa
assert "Sâu (4 tuổi)" in qa and "Ong (3 tuổi)" in qa
assert "Maps" in qa and "VietMap" in qa
assert 'android.permission.RECORD_AUDIO' in manifest  # existing permission only
assert not list((root / ".github/workflows").glob("*.yml"))
assert not list((root / ".github/workflows").glob("*.yaml"))
print("PASS: T-024/T-025 audio prefs, synthesized effects, narrator cancellation, privacy metrics, same-screen QA contract")
