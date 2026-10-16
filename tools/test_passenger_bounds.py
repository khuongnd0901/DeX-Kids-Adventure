#!/usr/bin/env python3
"""Generated crop geometry equals the original alpha>=128 scan for all eight poses."""
import hashlib
from pathlib import Path
from PIL import Image
from build_sprites import ROOT, OUT, build_story_art

files=list((ROOT/"art/assets-source/characters").glob("*-*-v[23].png"))
before={str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in files}
build_story_art()
rows=(OUT/"passenger-bounds.tsv").read_text().splitlines()
assert len(rows)==8
seen=set()
for row in rows:
    child,*values=row.split("\t");pose,width,height,x,y,w,h=map(int,values)
    assert (child,pose) not in seen;seen.add((child,pose))
    with Image.open(ROOT/f"art/assets-source/characters/{child}-passenger-v3.png") as image:
        assert image.size==(width,height)
        cw,ch=width//2,height//2
        alpha=image.crop((pose%2*cw,pose//2*ch,pose%2*cw+cw,pose//2*ch+ch)).getchannel("A")
        bounds=alpha.point(lambda v:255 if v>=128 else 0).getbbox() or (0,0,cw,ch)
        assert bounds==(x,y,x+w,y+h)
assert before=={str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in files}
print("PASS: eight build-time passenger crops exactly match original alpha128 geometry; source hashes preserved")
