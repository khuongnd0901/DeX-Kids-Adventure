#!/usr/bin/env python3
"""Local-only editorial + source gate for trip props and age-banded offline cards."""
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
sprites=sorted((ROOT/"art/assets-source/props").glob("*.svg"))
assert len(sprites)==18, f"18 component sprites expected, found {len(sprites)}"
for p in sprites:
    root=ET.parse(p).getroot()
    assert root.attrib.get("viewBox")=="0 0 180 160", p
    assert len(list(root))>0, p

pack=(ROOT/"assets/learning/offline-learning.tsv").read_text(encoding="utf-8")
lines=pack.splitlines()
assert lines[0]=="# dexkids-offline-learning-v1"
assert lines[1]=="id\tkind\ttopic\tmin_age\tmax_age\tprompt_vi\tanswer_vi\tenglish"
entries=[row.split("\t") for row in lines[2:] if row.strip()]
assert len(entries)==200,len(entries)
assert len({r[0] for r in entries})==len(entries)
assert sum(r[1]=="QUIZ" for r in entries)==120
assert sum(r[1]=="WORD" for r in entries)==80
for r in entries:
    assert len(r)==8,r
    assert 3<=int(r[3])<=int(r[4])<=6,r
    assert len(r[5])<230 and len(r[6])<230,r
    assert r[1] in ("QUIZ","WORD")
    if r[1]=="WORD":assert r[7] and r[7].isascii()
    else:assert not r[7]
topics={r[2] for r in entries}
assert topics=={"sea","city","road","nature","weather","animal","garden","safety"}
for age in range(3,7):
    for kind in ("WORD","QUIZ"):
        assert len([r for r in entries if r[1]==kind and int(r[3])<=age<=int(r[4])])>=24
scene=(ROOT/"core/src/main/java/com/khuongnd/dexkids/game/CartoonSprites.java").read_text()
screen=(ROOT/"core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java").read_text()
kids=(ROOT/"android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt").read_text()
voice=(ROOT/"android/src/main/java/com/khuongnd/dexkids/OfflineVietnameseNarrator.kt").read_text()
builder=(ROOT/"tools/build_sprites.py").read_text()
assert "extrasAtlas.dispose()" in scene and "drawExtras(" in scene
assert "cartoonSprites.drawExtras(" in screen
assert "build_extras()" in builder
assert 'assets.open("learning/offline-learning.tsv")' in kids
assert "entertainmentDirector.nextJourney(" in kids
assert "speakEnglishWord" in voice and "!it.isNetworkConnectionRequired" in voice
assert not list((ROOT/".github/workflows").glob("*.yml"))
print(f"PASS: {len(sprites)} source SVG props, 120 offline quizzes, 80 offline EN words, 8 journey topics")
