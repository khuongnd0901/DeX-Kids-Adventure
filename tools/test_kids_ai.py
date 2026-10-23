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
subscription=(mobile/"ai/KidsSubscriptionAiClient.kt").read_text()
secure=(mobile/"auth/ChatGptSessionStore.kt").read_text()
transport=(mobile/"ai/ChatGptResponsesClient.kt").read_text()
assert "AndroidKeyStore" in secure and "AES/GCM/NoPadding" in secure
assert "noBackupFilesDir" in secure
assert 'getBoolean("child_text_cloud_explicit", false)' in store
assert 'getBoolean("automatic_trip_packs", false)' in store
assert "ChatGPT Subscription" in parent and "prepareAiQuestionCache" in parent
assert "KidsAiKeys" not in store and "KidsAiProvider" not in store
assert "subscription.request" in gateway
assert "KidsAiDispatchLimiter.acquire(app, waitMillis" in subscription
assert 'https://api.openai.com/v1/responses' in transport
assert 'put("store", false).put("stream", true)' in transport
assert 'checkConsent()' in subscription
assert "KidsAiSafety.childCloudInput(spoken)" in gateway
assert "prefs.enabled && prefs.cloudChildReply" in gateway
assert "val facts = (listOf(base.answer)" in gateway
assert 'row.getInt("factIndex")' in gateway
assert 'KidsQuiz(q,facts[idx],follow)' in gateway, "AI answers must come ONLY from source-fact index, never generated"
assert 'questions list' in gateway
assert "get(id: String, age: Int, fact: String, base: KidsQuiz," in cache
assert 'val ttlMillis = 14L' in cache and "MessageDigest.getInstance" in cache
assert 'it.answer in allowed' in cache and '"created"' in cache
assert "quiz" not in (r/"assets/poi/reviewed.tsv").read_text().lower()
assert "aiCache.next(card.poiId(),age,fact,base,dayPeriod)" in kids
assert 'if (cfg.enabled)' in kids and 'queueLiveConversation(it,cue.textVi())' in kids
assert "KidsAiSafety.childCloudInput(heard)" in kids
assert "settings.enabled && settings.cloudChildReply" in kids
assert "ChildAnswerInterpreter.quiz" in kids and "return@runOnUiThread" in kids
assert "aiAnswerThread?.interrupt()" in kids and "aiWarmThread?.interrupt()" in kids
assert "GPS GẦN ĐỊA DANH" in kids
assert "AudioRecord" not in gateway and "MediaRecorder" not in gateway
assert "rawAudio" not in gateway and "GPS_PROVIDER" not in gateway
assert "openFileOutput" not in gateway
assert "Log." not in gateway
assert "KidsAiGateway" not in (mobile/"OnDeviceChildSpeech.kt").read_text()
assert "Required JSON schema:" in subscription
assert 'put("required", JSONArray(listOf("questions")))' in gateway
assert 'put("minItems", 10).put("maxItems", 12)' in gateway
print("PASS: T-075 subscription-only transport static contract; retained fact-bound quizzes, consent and offline cache (not live inference)")

assert 'put("dayPeriod",dayPeriod.label())' in gateway
assert 'dayPeriod.name' in cache
assert 'gateway.generate(card.poiId(),fact,base,age,dayPeriod)' in kids
assert "age in 4..7" in gateway and "age in 4..7" in cache

assert "gateway.generate(id,cue.textVi(),base,age,dayPeriod)" in parent
assert "cache.put(id,age,cue.textVi(),base,questions,dayPeriod)" in parent

# T-075: scan every production Android/core source, including non-gateway callers.
import re
legacy = re.compile(r"generativelanguage|api\.groq|KidsAiKeys|KidsAiProvider|gemini|groq", re.I)
for source_root in (r/"android/src/main", r/"core/src/main"):
    for source in source_root.rglob("*"):
        if source.suffix in (".kt", ".java", ".xml"):
            assert not legacy.search(source.read_text()), f"Legacy inference path: {source.relative_to(r)}"
assert 'dexkids-chatgpt-subscription-v1' in secure
assert 'dynamic_agent_client' in (mobile/"auth/ChatGptAuthManager.kt").read_text()
assert 'DeX Kids Adventure' in (mobile/"auth/ChatGptAuthManager.kt").read_text()
assert 'getBoolean("consent", false)' in (mobile/"ai/ChatGptSettings.kt").read_text()
print("PASS: all production Android/core sources contain no legacy AI provider, key adapter or endpoint; independent Kids identity and OFF subscription consent")
