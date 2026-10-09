#!/usr/bin/env python3
"""T-015 security/static contract; audio end-to-end remains device test."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
a=(root/"android/src/main/java/com/khuongnd/dexkids")
manifest=(root/"android/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
prefs=(a/"ParentSettings.kt").read_text(encoding="utf-8")
parent=(a/"ParentActivity.kt").read_text(encoding="utf-8")
kids=(a/"KidsActivity.kt").read_text(encoding="utf-8")
asr=(a/"OnDeviceChildSpeech.kt").read_text(encoding="utf-8")
core=(root/"core/src/main/java/com/khuongnd/dexkids/story/ChildAnswerInterpreter.java").read_text(encoding="utf-8")
assert 'android.permission.RECORD_AUDIO' in manifest
assert 'android.hardware.microphone' in manifest and 'android:required="false"' in manifest
assert 'android.speech.RecognitionService' in manifest
assert 'var allowChildMicrophone' in prefs and 'getBoolean("child_mic_optin", false)' in prefs
assert 'requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 4003)' in parent
assert 'settings.allowChildMicrophone = false' in parent
assert 'OnDeviceChildSpeech.available(this)' in parent
assert 'SpeechRecognizer.createOnDeviceSpeechRecognizer(context)' in asr
assert 'SpeechRecognizer.createSpeechRecognizer(' not in asr
assert 'RecognizerIntent.EXTRA_PREFER_OFFLINE' in asr
assert 'installedOnDeviceLanguages' in asr
assert 'getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)' in asr
assert '9_000L' in asr and 'recognizer?.let' in asr and 'it.destroy()' in asr
assert 'RECORD_AUDIO' in asr and 'context.checkSelfPermission' in asr
assert 'ChildAnswerInterpreter.quiz' in kids and 'ChildAnswerInterpreter.chat' in kids
assert 'childSpeech?.cancel()' in kids and 'clearTalkQueue()' in kids
assert 'if (canListenToChild())' in kids
assert 'lastNearbyStoryAt' in kids and 'GPS GẦN ĐỊA DANH' in kids
assert 'microphoneStatus' in kids and 'ParentSettings(this).allowChildMicrophone' in kids
assert 'android.permission.INTERNET' not in manifest
assert 'Log.' not in asr and 'Log.' not in core
assert all(x not in asr for x in ("MediaRecorder", "AudioRecord", "java.net", "okhttp", "openFileOutput"))
print("PASS: explicit opt-in, real mic runtime permission, on-device-only one-shot ASR, no child audio/transcript persistence")
