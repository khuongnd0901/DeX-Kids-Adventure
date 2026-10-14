#!/usr/bin/env python3
"""Verified original PNG -> deterministic, bounded T-048 runtime atlas.

Keep the four source canvases together for stable pose scale/baseline. Only
trim bus alpha padding. Pillow RGBA resize uses premultiplied alpha internally.
"""
import hashlib
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'art/assets-source/ai-capybara-v1'
OUT = ROOT / 'assets/generated/t048'
ALPHA = {'capybara_idle': (0,23,1090,1402), 'capybara_laugh': (0,17,1115,1376),
         'capybara_ask': (0,17,1108,1385), 'capybara_wave': (0,0,1108,1402),
         'bus_side_with_driver': (0,19,1432,1029)}
PAGE = 1024
PAD = 4


def build():
    declarations = json.loads((SOURCE/'manifest.json').read_text())['assets']
    if len(declarations) != 5 or {Path(a['file']).stem for a in declarations} != set(ALPHA):
        raise ValueError('Expected exactly five T-048 sources')
    page = Image.new('RGBA', (PAGE,PAGE))
    regions = []
    # Explicit placements avoid iteration/order-dependent packing.
    poses = ['capybara_idle','capybara_laugh','capybara_ask','capybara_wave']
    for item in sorted(declarations, key=lambda a:a['file']):
        name = Path(item['file']).stem
        data = (SOURCE/item['file']).read_bytes()
        if hashlib.sha256(data).hexdigest() != item['sha256']:
            raise ValueError('Source SHA256 mismatch: '+name)
        with Image.open(SOURCE/item['file']) as source:
            source.load()
            if source.format!='PNG' or source.mode!='RGBA' or source.size!=(item['width'],item['height']):
                raise ValueError('Source PNG format/size mismatch: '+name)
            if source.getchannel('A').getbbox()!=ALPHA[name] or source.getchannel('A').getextrema()!=(0,255):
                raise ValueError('Source alpha mismatch: '+name)
            if name in poses:
                # Same full source canvas and scale for all four expressions.
                resized=source.resize((224,280),Image.Resampling.LANCZOS)
                visible=resized.getchannel('A').point(lambda value: 255 if value>=16 else 0).getbbox()
                bitmap=Image.new('RGBA',(224,288))
                # Align perceptible feet rather than isolated near-zero alpha
                # tails. Extra baseline padding retains soft resampling edges.
                bitmap.paste(resized,(112-(visible[0]+visible[2])//2,280-visible[3]))
                x,y=PAD+poses.index(name)*(224+2*PAD),PAD
            else:
                bitmap=source.crop(ALPHA[name]).resize((850,600),Image.Resampling.LANCZOS)
                x,y=PAD,304
        w,h=bitmap.size
        if x+w+PAD>PAGE or y+h+PAD>PAGE: raise ValueError('Atlas overflow')
        # Copy RGBA exactly; no alpha-over accumulation. Extrude two edge pixels
        # into the gutter to isolate Linear sampling from neighboring sprites.
        page.paste(bitmap,(x,y))
        for distance in (1,2):
            page.paste(bitmap.crop((0,0,1,h)),(x-distance,y))
            page.paste(bitmap.crop((w-1,0,w,h)),(x+w-1+distance,y))
            page.paste(bitmap.crop((0,0,w,1)),(x,y-distance))
            page.paste(bitmap.crop((0,h-1,w,h)),(x,y+h-1+distance))
        regions.append({'name':name,'xy':[x,y],'size':[w,h],
                        'source_sha256':item['sha256'],'source_alpha_bbox':ALPHA[name]})
    OUT.mkdir(parents=True,exist_ok=True)
    page.save(OUT/'t048.png',optimize=True)
    lines=['t048.png',f'size: {PAGE}, {PAGE}','format: RGBA8888','filter: Linear, Linear','repeat: none']
    for r in regions:
        x,y=r['xy'];w,h=r['size']
        lines += [r['name'],'  rotate: false',f'  xy: {x}, {y}',f'  size: {w}, {h}',
                  f'  orig: {w}, {h}','  offset: 0, 0','  index: -1']
    (OUT/'t048.atlas').write_text('\n'.join(lines)+'\n')
    (OUT/'manifest.json').write_text(json.dumps({'schema':1,'page_size':PAGE,'rgba_bytes':PAGE*PAGE*4,
        'regions':regions},sort_keys=True,indent=2)+'\n')
    print('T-048: five verified PNG sources -> one 1024 RGBA page (4 MiB backing)')

if __name__=='__main__': build()
