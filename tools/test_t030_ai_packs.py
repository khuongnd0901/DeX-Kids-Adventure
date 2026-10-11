#!/usr/bin/env python3
"""T-030 offline source/data contract (not a substitute for Android/runtime/HTTP tests)."""
from pathlib import Path
r = Path(__file__).resolve().parents[1]
root = r / "android/src/main/java/com/khuongnd/dexkids"
gateway = (root/"ai/KidsAiGateway.kt").read_text()
budget = (root/"ai/KidsAiDispatchLimiter.kt").read_text()
prefs = (root/"ai/KidsAiStore.kt").read_text()
cache = (root/"ai/TripPackStore.kt").read_text()
coordinator = (root/"ai/TripPackCoordinator.kt").read_text()
activity = (root/"KidsActivity.kt").read_text()
parent = (root/"ParentActivity.kt").read_text()
core = (r/"core/src/main/java/com/khuongnd/dexkids/story/TripQuestionPack.java").read_text()
lines=(r/"assets/learning/offline-learning.tsv").read_text(encoding="utf-8").splitlines()
rows=[row.split("\t") for row in lines[2:] if row.strip()]
assert len(rows)==200 and len({row[0] for row in rows})==200
assert sum(x[1]=="QUIZ" for x in rows)==120
assert sum(x[1]=="WORD" for x in rows)==80
for row in rows:
    assert len(row)==8 and int(row[3])<=int(row[4])
    assert 4<=int(row[4]) and len(row[5])<=230 and len(row[6])<=230
    assert row[2] in ("sea","city","road","nature","weather","animal","garden","safety")
    if row[1]=="QUIZ":
        assert row[7]==""
    else:
        assert row[7].isascii() and all(c.islower() or c in " -" for c in row[7])
assert 'getBoolean("automatic_trip_packs", false)' in prefs
assert 'getBoolean("child_text_cloud_explicit", false)' in prefs
assert "KidsAiDispatchLimiter.acquire(appContext)" in gateway
assert "maxCallsPerInstance" not in gateway
assert 'MAX_REQUESTS = 5' in budget and 'WINDOW_MS = 60_000L' in budget
assert 'MIN_GAP_MS = 12_100L' in budget and '.commit()' in budget
assert 'status == 429' in gateway and 'Retry-After' in gateway
assert 'fun generateTripPack' in gateway and 'TripQuestionPack.validate(' in gateway
assert 'quizOptions' in gateway and 'wordOptions' in gateway and 'avoidQuizIds' in gateway
assert 'put("items"' in gateway and 'responseJsonSchema' in gateway
assert 'proposed.size() < 15 || proposed.size() > 20' in core
assert 'topics.size() < 4' in core and 'question.topic().equals(word.topic())' in core
assert 'item.focus() != expectedFocus' in core and 'ids.add(question.id())' in core
assert 'AtomicFile' in cache and 'finishWrite' in cache and 'failWrite' in cache
assert 'TripQuestionPack.validate' in cache and 'sha256' in cache and '16_384' in cache
assert 'for (attempt in 0..5)' in coordinator and 'Thread.sleep(' in coordinator
assert 'KidsAiRetryAfterException' in coordinator
assert 'if (pendingId!=beatId) return' in coordinator
assert 'if (cursor>=deck.size)' in coordinator and 'runWorker(false,true)' in coordinator
assert 'fun pause()' in coordinator and 'epoch.incrementAndGet()' in coordinator
assert 'tripPackCoordinator?.nextBeat()' in activity
assert 'tripPackCoordinator?.complete(beat.id())' in activity
assert 'tripPackCoordinator?.pause()' in activity
assert 'firstTripPackResume = savedInstanceState == null' in activity
assert 'Tắt tự tạo 15–20 câu/chuyến' in parent
assert 'aiSettings.automaticPacks = true' in parent
assert 'trip-packs-v1-' in parent
manifest=(r/"android/src/main/AndroidManifest.xml").read_text()
assert 'android.permission.ACCESS_NETWORK_STATE' in manifest
print("PASS: T-030 static contract, 120 curated quizzes / 80 English words, separate opt-in, bounded request and retry contracts")
