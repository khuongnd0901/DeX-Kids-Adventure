#!/usr/bin/env python3
"""T-016 contract: provider/key path, ground-truth isolation, opt-ins and no child raw audio."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
mobile=r/"android/src/main/java/com/khuongnd/dexkids"
store=(mobile/"ai/KidsAiStore.kt").read_text(encoding="utf-8")
gateway=(mobile/"ai/KidsAiGateway.kt").read_text(encoding="utf-8")
cache=(mobile/"ai/KidsAiQuizCache.kt").read_text(encoding="utf-8")
parent=(mobile/"ParentActivity.kt").read_text(encoding="utf-8")
kids=(mobile/"KidsActivity.kt").read_text(encoding="utf-8")
manifest=(r/"android/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
assert "android.permission.INTERNET" in manifest and 'android:allowBackup="false"' in manifest
assert "GEMINI" in store and "GROQ" in store and "AndroidKeyStore" in store
assert "AES/GCM/NoPadding" in store and "Base64" in store
assert 'getBoolean("ai_quizzes", false)' in store
assert 'getBoolean("child_text_cloud_explicit", false)' in store
assert "freeTierAcknowledged" in store and "setFreeTierAcknowledged" in store
assert "setModel(p" in parent and "keys.save(p" in parent and "keys.delete(p)" in parent
assert "AI Kids" in parent and "Cho phép AI phản hồi từ lời bé" in parent
assert "Tạo cache 10" in parent and "prepareAiQuestionCache" in parent
assert 'https://generativelanguage.googleapis.com/v1beta/models/' in gateway
assert 'https://api.groq.com/openai/v1/chat/completions' in gateway
assert 'HttpsURLConnection' in gateway and 'requestMethod = "POST"' in gateway
assert "x-goog-api-key" in gateway and 'Bearer ' in gateway
assert "check(Looper.myLooper() != Looper.getMainLooper())" in gateway
assert "maxCallsPerInstance = 20" in gateway
assert "KidsAiSafety.childCloudInput(spoken)" in gateway
assert "prefs.enabled && prefs.cloudChildReply" in gateway
assert "val facts = (listOf(base.answer)" in gateway
assert 'row.getInt("factIndex")' in gateway
assert 'KidsQuiz(q,facts[idx],follow)' in gateway, "AI answers must come ONLY from source-fact index, never generated"
assert 'questions list' in gateway
assert "get(id: String, age: Int, fact: String, base: KidsQuiz)" in cache
assert 'val ttlMillis = 14L' in cache and "MessageDigest.getInstance" in cache
assert 'it.answer in allowed' in cache and '"created"' in cache
assert "quiz" not in (r/"assets/poi/reviewed.tsv").read_text().lower()
assert "aiCache.next(card.poiId(),age,fact,base)" in kids
assert 'if (cfg.enabled)' in kids and 'queueLiveConversation(it,cue.textVi())' in kids
assert "KidsAiSafety.childCloudInput(heard)" in kids
assert "settings.enabled && settings.cloudChildReply" in kids
assert "ChildAnswerInterpreter.quiz" in kids and "return@runOnUiThread" in kids
assert "aiAnswerThread?.interrupt()" in kids and "aiWarmThread?.interrupt()" in kids
assert "GPS GẦN ĐỊA DANH" in kids
assert "AudioRecord" not in gateway and "MediaRecorder" not in gateway
assert "rawAudio" not in gateway and "GPS_PROVIDER" not in gateway
assert "openFileOutput" not in gateway and "Log." not in gateway
assert 'https://www.openstreetmap.org/' not in gateway, "No implicit geo lookup"
assert "KidsAiGateway" not in (mobile/"OnDeviceChildSpeech.kt").read_text()
print("PASS: T-016 direct opt-in Gemini/Groq, encrypted BYOK, fact-bound AI quizzes, TTL/age/source offline cache, separate child cloud opt-in")
