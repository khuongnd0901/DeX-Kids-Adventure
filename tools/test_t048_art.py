#!/usr/bin/env python3
"""Identity, deterministic packing, transparent edges and composite draw guard."""
import hashlib
import json
import tempfile
import shutil
import build_t048_atlas as pack
from pathlib import Path
from PIL import Image
from build_t048_atlas import build, OUT, ROOT


def digests():
    return {p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in OUT.iterdir()}

build();first=digests();build();assert first==digests(), 'Atlas is not reproducible'
manifest=json.loads((OUT/'manifest.json').read_text())
assert manifest['rgba_bytes']==4*1024*1024
assert len(manifest['regions'])==5
with Image.open(OUT/'t048.png') as page:
    assert page.mode=='RGBA' and page.size==(1024,1024)
    boxes=[];baselines=[]
    for region in manifest['regions']:
        x,y=region['xy'];w,h=region['size'];box=(x,y,x+w,y+h)
        assert 2<=x and 2<=y and x+w+2<=1024 and y+h+2<=1024
        for previous in boxes:
            assert box[2]<=previous[0] or box[0]>=previous[2] or box[3]<=previous[1] or box[1]>=previous[3]
        boxes.append(box)
        sprite=page.crop(box);alpha=sprite.getchannel('A')
        assert alpha.getextrema()==(0,255) and alpha.getbbox()
        if region['name'].startswith('capybara_'):
            assert (w,h)==(224,288)
            baselines.append(alpha.point(lambda value:255 if value>=16 else 0).getbbox()[3])
        assert page.crop((x-1,y,x,y+h)).tobytes()==sprite.crop((0,0,1,h)).tobytes()
    assert max(baselines)-min(baselines)<=1, 'Pose baseline drift'
code=(ROOT/'core/src/main/java/com/khuongnd/dexkids/game/CartoonSprites.java').read_text()
composite=code.split('if (compositeBus != null) {',1)[1].split('return;',1)[0]
assert 'drawWheel' not in composite and 'batch.draw(face' not in composite
assert 'pngAtlas.dispose()' in code
assert all((ROOT/'art/assets-source'/p).exists() for p in ['vehicles/bus.svg','vehicles/wheel.svg','characters/capybara_idle.svg'])
with tempfile.TemporaryDirectory() as folder:
    source=Path(folder)/'source'
    shutil.copytree(pack.SOURCE,source)
    original=pack.SOURCE
    pack.SOURCE=source
    try:
        damaged=source/'capybara_idle.png'
        damaged.write_bytes(damaged.read_bytes()+b'CORRUPT')
        try: pack.build()
        except ValueError as error: assert 'SHA256' in str(error)
        else: raise AssertionError('Corrupted source accepted')
    finally: pack.SOURCE=original
print('T-048 identity/alpha/baseline/gutters/determinism/composite rollback contracts PASS')
