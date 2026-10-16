#!/usr/bin/env python3
"""Runtime resampling/crops remain reproducible, above FullHD slot size, sources intact."""
from pathlib import Path
import hashlib
from PIL import Image
from build_runtime_children import ROOT,OUT,SIZE,build
source=ROOT/'art/assets-source/characters'
before={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in source.glob('sau-*-v[23].png')}
build();first={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in OUT.iterdir()}
build();assert first=={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in OUT.iterdir()}
assert SIZE[0]//2>=285 and SIZE[1]//2>=427
for suffix in ['story-v2','passenger-v3']:
    with Image.open(source/f'sau-{suffix}.png') as original,Image.open(OUT/f'sau-{suffix}.png') as actual:
        expected=original.resize(SIZE,Image.Resampling.LANCZOS)
        assert actual.size==SIZE and expected.tobytes()==actual.tobytes()
        assert actual.getchannel('A').getextrema()==(0,255)
rows=(OUT/'passenger-bounds.tsv').read_text().splitlines();assert len(rows)==4
with Image.open(OUT/'sau-passenger-v3.png') as image:
    for row in rows:
        child,*values=row.split('\t');pose,w,h,x,y,rw,rh=map(int,values)
        cx=pose%2*(w//2);cy=pose//2*(h//2)
        alpha=image.crop((cx,cy,cx+w//2,cy+h//2)).getchannel('A')
        assert alpha.point(lambda a:255 if a>=128 else 0).getbbox()==(x,y,x+rw,y+rh)
assert before=={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in source.glob('sau-*-v[23].png')}
print('PASS: protected sources unchanged; deterministic 320x480 runtime poses/crops above FullHD display size')
