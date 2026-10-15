#!/usr/bin/env python3
"""Optional complete Ong art packer; call AFTER build_child_outfits.build().

Copies all existing Sâu overlay regions pixel-for-pixel into the same 2048² page.
Ong uses 24 whole-character frames; passenger regions reference their upper-body
subrectangles, so no dress overlays or extra GPU texture/page are introduced.
Protected original source art is never changed. Requires Pillow, no network.
"""
from __future__ import annotations
import argparse
import hashlib
import json
from pathlib import Path
import re
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'art/assets-source/characters/ong-complete'
OUTPUT = ROOT / 'assets/generated'
STYLES = ('rose', 'ocean', 'sky', 'forest', 'starlight', 'lavender')
PAGE = (2048, 2048)
FRAME = (224, 336)
PAD = 6

def regions(text: str) -> list[tuple[str,int,int,int,int]]:
    pattern = r'^([a-z0-9_]+)\n  rotate: false\n  xy: (\d+), (\d+)\n  size: (\d+), (\d+)'
    return [(m[0],*map(int,m[1:])) for m in re.findall(pattern,text,re.M)]

def validate_sheet(path: Path, extraction: list[dict] | None = None) -> list[Image.Image]:
    with Image.open(path) as image:
        if image.mode != 'RGBA' or image.width % 2 or image.height % 2:
            raise ValueError(f'{path.name}: expected transparent RGBA 2×2 pose sheet')
        if image.width * image.height > 16_000_000:
            raise ValueError(f'{path.name}: source image exceeds pixel budget')
        image.load()
        w,h=image.width//2,image.height//2
        if w<128 or h<192:raise ValueError(f'{path.name}: pose cells below minimum 128×192')
        # One scale for all four poses, based on nominal cells rather than each
        # silhouette. A crouching PET therefore stays shorter than standing poses.
        scale=min((FRAME[0]-16)/w,(FRAME[1]-16)/h)
        columns=[]
        for column in range(2):
            strip=image.crop((column*w,0,(column+1)*w,image.height))
            opaque=strip.getchannel('A').point(lambda a:255 if a>=128 else 0)
            low=h-round(h*78/768);high=h+round(h*72/768)
            gaps=[];start=None
            for y in range(low,high+1):
                empty=not opaque.crop((0,y,w,y+1)).getbbox()
                if empty and start is None:start=y
                if start is not None and (not empty or y==high):
                    end=y if not empty else y+1
                    if end-start>=2:gaps.append((start,end))
                    start=None
            if len(gaps)!=1:
                raise ValueError(f'{path.name}: column {column} needs one unambiguous transparent separating gap; found {gaps}')
            gap=gaps[0];split=(gap[0]+gap[1])//2
            columns.append((strip,split,gap))
        result=[]
        metadata=[]
        for pose in range(4):
            column=pose%2;row=pose//2;strip,split,gap=columns[column]
            top=0 if row==0 else split;bottom=split if row==0 else image.height
            cell=strip.crop((0,top,w,bottom))
            alpha=cell.getchannel('A');bounds=alpha.point(lambda a:255 if a>=128 else 0).getbbox()
            if not bounds or alpha.getextrema()[0] == 255:
                raise ValueError(f'{path.name}: pose {pose} empty or opaque background')
            if bounds[0]<=0 or bounds[1]<=0 or bounds[2]>=w or bounds[3]>=cell.height:
                raise ValueError(f'{path.name}: pose {pose} clipped at extraction boundary')
            # Add transparent/antialias guard pixels around the alpha>=128 bounds,
            # without crossing either neighbouring sprite or original sheet edge.
            guard=(max(0,bounds[0]-2),max(0,bounds[1]-2),min(w,bounds[2]+2),min(cell.height,bounds[3]+2))
            cut=cell.crop(guard)
            resized=cut.resize((max(1,round(cut.width*scale)),max(1,round(cut.height*scale))),Image.Resampling.LANCZOS)
            if resized.width>FRAME[0]-12 or resized.height>FRAME[1]-12:
                raise ValueError(f'{path.name}: pose {pose} exceeds fixed nominal-cell scale budget')
            tile=Image.new('RGBA',FRAME)
            destination=((FRAME[0]-resized.width)//2,FRAME[1]-8-resized.height)
            tile.alpha_composite(resized,destination)
            result.append(tile)
            metadata.append({'pose':pose,'column':column,'source_crop':[column*w,top,(column+1)*w,bottom],
                'separating_gap_y_exclusive':list(gap),'split_y':split,
                'alpha128_bbox_source':[column*w+bounds[0],top+bounds[1],column*w+bounds[2],top+bounds[3]],
                'guard_crop_source':[column*w+guard[0],top+guard[1],column*w+guard[2],top+guard[3]],
                'scale':scale,'destination_xy':list(destination),'destination_size':list(resized.size),
                'destination_alpha128_bbox':tile.getchannel('A').point(lambda a:255 if a>=128 else 0).getbbox()})
        if extraction is not None:
            extraction.append({'file':path.name,'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),
                'source_size':list(image.size),'nominal_cell':[w,h],'frame':list(FRAME),
                'alpha_threshold':128,'baseline_bottom_padding':8,'poses':metadata})
        return result

def build(source: Path=SOURCE, output: Path=OUTPUT) -> bool:
    files=[source/f'{style}.png' for style in STYLES]
    if not any(path.exists() for path in files):
        print('Ong complete sprites absent: retain original Ong art, no dress-overlay fallback')
        return False
    if not all(path.is_file() for path in files):
        raise ValueError('Ong complete pack requires all six reviewed style sheets')
    originals=regions((output/'child-outfits.atlas').read_text())
    sau=[r for r in originals if r[0].startswith('sau_')]
    if len(sau)!=48 or len({r[0] for r in sau})!=48:
        raise ValueError('Expected all 48 existing Sâu story/passenger regions')
    with Image.open(output/'child-outfits.png') as old:
        old.load();sau_pixels=[(name,old.crop((x,y,x+w,y+h))) for name,x,y,w,h in sau]
    extraction=[]
    sheets=[validate_sheet(path,extraction) for path in files] # validate EVERYTHING before replacing outputs
    canvas=Image.new('RGBA',PAGE)
    packed=[]
    for i,(name,tile) in enumerate(sau_pixels):
        if tile.size != (128,192):raise ValueError('Unexpected Sâu tile geometry')
        x=PAD+(i%14)*134;y=PAD+(i//14)*198
        canvas.paste(tile,(x,y));packed.append((name,x,y,128,192))
    for style,cells in zip(STYLES,sheets):
        for pose,tile in enumerate(cells):
            i=STYLES.index(style)*4+pose;x=PAD+(i%8)*230;y=798+(i//8)*342
            assert x+FRAME[0]<=PAGE[0] and y+FRAME[1]<=PAGE[1]
            canvas.alpha_composite(tile,(x,y))
            packed.append((f'ong_complete_{style}_story_{pose}',x,y,*FRAME))
            # Upper-body UV alias of the complete sprite: no duplicate pixels or clothing layer.
            bounds=tile.getchannel('A').point(lambda a:255 if a>=128 else 0).getbbox()
            left,top,right,bottom=bounds
            waist=min(bottom,top+round((bottom-top)*.62))
            packed.append((f'ong_complete_{style}_passenger_{pose}',x+left,y+top,right-left,waist-top))
    atlas=['child-outfits.png',f'size: {PAGE[0]}, {PAGE[1]}','format: RGBA8888','filter: Linear, Linear','repeat: none']
    for name,x,y,w,h in packed:
        atlas += [name,'  rotate: false',f'  xy: {x}, {y}',f'  size: {w}, {h}',f'  orig: {w}, {h}','  offset: 0, 0','  index: -1']
    output.mkdir(parents=True,exist_ok=True)
    temporary=output/'child-outfits.complete.tmp.png';canvas.save(temporary,optimize=True)
    (output/'child-outfits.complete.tmp.atlas').write_text('\n'.join(atlas)+'\n')
    temporary.replace(output/'child-outfits.png')
    (output/'child-outfits.complete.tmp.atlas').replace(output/'child-outfits.atlas')
    (output/'ong-complete-extraction.json').write_text(json.dumps(extraction,indent=2)+'\n')
    print('Packed 48 unchanged Sâu layers + 24 complete Ong poses (24 passenger UV aliases), one 2048² page')
    return True

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--source',type=Path,default=SOURCE);parser.add_argument('--output',type=Path,default=OUTPUT)
    args=parser.parse_args();build(args.source,args.output)
