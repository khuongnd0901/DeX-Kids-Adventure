#!/usr/bin/env python3
"""Source contract only; NOT Android, provider, model-output or actual child acceptance."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
mobile=root/"android/src/main/java/com/khuongnd/dexkids"
core=root/"core/src/main/java/com/khuongnd/dexkids/story/CreativeLessonPack.java"
validator=core.read_text()
gateway=(mobile/"ai/KidsAiGateway.kt").read_text()
coordinator=(mobile/"ai/TripPackCoordinator.kt").read_text()
store=(mobile/"ai/CreativePackStore.kt").read_text()
settings=(mobile/"ParentSettings.kt").read_text()
ai=(mobile/"ai/KidsAiStore.kt").read_text()
parent=(mobile/"ParentActivity.kt").read_text()
activity=(mobile/"KidsActivity.kt").read_text()
assert "Genre { QUIZ, STORY, RIDDLE, ENGLISH }" in validator
# Age boundaries are exercised by CreativeLessonPackTest, independent of Java syntax.
assert "items.size()<16 || items.size()>20" in validator
assert "topics.size()<4 || genres.size()<3" in validator
assert "recentFingerprints.contains(hash)" in validator
assert "đang đi qua" in validator and "địa chỉ nhà" in validator
assert "fun generateCreativePack" in gateway and "Generate 10-12" in gateway
assert "Invent exactly 20 completely NEW lessons" in gateway
assert "creativePack" in gateway and "requireCreativeConsent=true" in gateway
assert "reviewedPool.merge" in gateway and "reviewSchema" in gateway
assert "KidsAiDispatchLimiter.acquire(appContext)" in gateway
assert "AtomicFile" in store and "finishWrite" in store and "failWrite" in store
assert "creative-pack-state-v1" in store and "sha256" in store
assert "creativeStore.load()" in coordinator and "creativeStore.swap(proposed)" in coordinator
assert "creativeStore.recent()" in coordinator and "creativeStore.remember" in coordinator
assert "creativeBeat()" in coordinator and "runWorker(false,true)" in coordinator
assert "getBoolean(\"creative_trip_packs\", false)" in ai
assert "val activeAge: Int get() = minOf(6" in settings
assert "fun ageForFocus" in settings and "4,7" in settings
assert "showAgePicker()" in parent and "Bật AI tự sáng tạo nội dung mới" in parent
assert "aiSettings.creativePacks = true" in parent and "aiSettings.automaticPacks = true" in parent
assert "creative-packs-v1-" in parent
assert "beat.id().startsWith(\"creative-\")" in activity
assert "tripPackCoordinator?.nextBeat()" in activity
assert "entertainmentDirector.nextQuestion(" in activity
assert 'LearningSource.mode(this@KidsActivity) != "builtin"' in activity
assert "if (!external) tripPackCoordinator?.nextBeat()" in activity
assert "reviewLessons.chunked(4)" not in gateway
assert "JSONArray(reviewLessons)" in gateway
assert "MAX_REQUESTS = 10" in (mobile/"ai/KidsAiDispatchLimiter.kt").read_text()
assert "6_100L" in (mobile/"ai/KidsAiDispatchLimiter.kt").read_text()
print("PASS: source contracts for opt-in creative AI ages 4–7, one full-pack review, cache, retry, shared 10RPM, offline/consent")
