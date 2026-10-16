#!/usr/bin/env python3
"""FullHD Sâu sheets: 320x480 per pose, above the largest 285x427 display slot.

Protected 512x768 source poses are unchanged. Retain legacy sheets for fallback.
One deterministic LANCZOS resize at build time, never during rendering.
"""
from pathlib import Path
import hashlib
import json
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'assets/generated/runtime-children'
SIZE=(640,960)

def build():
    OUT.mkdir(parents=True,exist_ok=True);rows=[];bounds=[]
    for suffix in ['story-v2','passenger-v3']:
        source=ROOT/f'art/assets-source/characters/sau-{suffix}.png'
        with Image.open(source) as original:
            if original.size!=(1024,1536) or original.mode!='RGBA':raise ValueError('Unexpected source sheet')
            image=original.resize(SIZE,Image.Resampling.LANCZOS)
            image.save(OUT/f'sau-{suffix}.png',optimize=True)
            if suffix=='passenger-v3':
                for pose in range(4):
                    x=pose%2*320;y=pose//2*480
                    alpha=image.crop((x,y,x+320,y+480)).getchannel('A')
                    left,top,right,bottom=alpha.point(lambda a:255 if a>=128 else 0).getbbox() or (0,0,320,480)
                    bounds.append('\t'.join(map(str,('sau',pose,*SIZE,left,top,right-left,bottom-top))))
        rows.append({'sheet':source.name,'source_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),
                     'source_size':[1024,1536],'runtime_size':list(SIZE),'runtime_rgba_bytes':SIZE[0]*SIZE[1]*4})
    (OUT/'passenger-bounds.tsv').write_text('\n'.join(bounds)+'\n',encoding='ascii')
    (OUT/'manifest.json').write_text(json.dumps(rows,indent=2)+'\n')
    return rows
if __name__=='__main__':build()
