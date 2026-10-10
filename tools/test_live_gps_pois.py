#!/usr/bin/env python3
"""OSM-derived LIVE place labels: coordinate/source integrity, no production promotion."""
from pathlib import Path
import csv, io, json
BASE=Path(__file__).resolve().parents[1]
def rows(path):
    text=(BASE / path).read_text(encoding="utf-8")
    assert '\ufffd' not in text and '\x00' not in text
    return list(csv.DictReader(io.StringIO("\n".join(text.splitlines()[1:])),delimiter="\t"))
pois=rows("assets/poi/live-landmarks.tsv")
stories=rows("assets/narration/live-landmarks.tsv")
dialogues=rows("assets/poi/live-dialogue.tsv")
audit=json.loads((BASE/"assets/poi/live-landmarks-source-audit.json").read_text(encoding="utf-8"))
assert audit["status"]=="SOURCE_CROSS_CHECKED_NOT_FIELD_APPROVED"
assert len(pois)==len(stories)==len(dialogues)==len(audit["data"])==40
ids={p["id"] for p in pois}
assert len(ids)==40 and {n["poi_id"] for n in stories}==ids and {q["poi_id"] for q in dialogues}==ids
assert {p["id"] for p in audit["data"]}==ids
from collections import Counter
assert Counter(p["corridor"] for p in audit["data"])=={"dong-nai":7,"vung-tau":6,"phan-thiet":6,"bao-loc":5,"da-lat":9,"nha-trang":7}
for p in pois:
    t,code=p["id"].split(":")[1:]
    assert p["osm_url"]==f"https://www.openstreetmap.org/{t}/{code}"
    assert -90<=float(p["lat"])<=90 and -180<=float(p["lon"])<=180
    assert p["reviewer"]=="source-audit-20261010"
for p in audit["data"]:
    assert p["humanReviewed"] is False
    assert p["source_url"].startswith("https://mapcarta.com/")
    assert p["osm_url"] == "https://www.openstreetmap.org/" + p["id"].split(":")[1] + "/" + p["id"].split(":")[2]
    assert -90 <= float(p["lat"]) <= 90 and -180 <= float(p["lon"]) <= 180
for n in stories:
    assert n["fact_url"].startswith("https://mapcarta.com/")
    assert 2<=int(n["min_age"])<=int(n["max_age"])<=6
for q in dialogues:
    assert 8<=min(len(q[x]) for x in ["quiz_vi","answer_vi","chat_vi"])
    assert all(len(q[x])<=190 for x in ["quiz_vi","answer_vi","chat_vi"])
# LIVE mode must use actual location+freshness gate; not a repeating timer.
screen=(BASE/"core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java").read_text(encoding="utf-8")
kids=(BASE/"android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt").read_text(encoding="utf-8")
parent=(BASE/"android/src/main/java/com/khuongnd/dexkids/ParentActivity.kt").read_text(encoding="utf-8")
settings=(BASE/"android/src/main/java/com/khuongnd/dexkids/ParentSettings.kt").read_text(encoding="utf-8")
assert "liveFixGate.accept(position.get())" in screen and "LiveJourneyFeed" in screen
assert "poi/live-landmarks.tsv" in screen and "narration/live-landmarks.tsv" in screen
assert "GPS GẦN ĐỊA DANH" in kids and "queueLiveConversation" in kids
assert "liveFeed == null && cue == null" in kids, "Timed geo stories must not run in LIVE GPS"
assert "settings.quiet" not in kids and "settings.quiet" not in parent and "var quiet:" not in settings
assert "Quiet mode: ON" not in parent
assert "settings.allowOfflineSpeech" not in kids, "Speech consent is read via ParentSettings before every utterance"
assert "ParentSettings(this).allowOfflineSpeech" in kids
assert len((BASE/"assets/poi/reviewed.tsv").read_text().strip().splitlines())==2
print("PASS: GPS-derived live candidate pack (40 features), 40 stories/dialogues; Quiet UI removed, no geographic clock fiction")
