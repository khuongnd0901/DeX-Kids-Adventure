#!/usr/bin/env python3
"""Pack complete painted characters; never cut faces, garments or limbs.

Input rectangles are reviewed whole-figure bounds, pinned to source SHA256.
Extraction separates the complete connected figure from adjacent-sheet fragments;
then uniformly resizes and packs that whole figure. Original source pixels stay intact.
Four 512x512 frames per page; only one page per visible actor is live in the game.
"""
from pathlib import Path
import hashlib
import json
from collections import deque
from PIL import Image, ImageOps, ImageFilter, ImageChops

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'art/assets-source/story-characters-v3'
OUT = ROOT / 'assets/generated/story-characters-v3'
FRAME = (512, 512)
PAGE = (1024, 1024)
ACTORS = ('sau', 'ong', 'capybara')
SHEETS = ('actions', 'secondary', 'expressions')
# Sitting/crouching paintings have a lower head, not a miniaturized character.
HEIGHT = {5: .72, 6: .88, 7: .86, 10: .87, 15: .78,
          26: .72, 27: .80, 29: .66}

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def complete_figure(image, box):
    """Keep one WHOLE figure, not a head/body part; discard neighbouring-cell flecks."""
    image=image.crop(box)
    w,h=image.size
    original=image.getchannel('A')
    opaque=original.point(lambda a:255 if a>=192 else 0)
    mask=bytearray(opaque.tobytes());largest=[]
    for start in range(len(mask)):
        if not mask[start]:continue
        mask[start]=0;queue=deque([start]);component=[]
        while queue:
            at=queue.popleft();component.append(at);y,x=divmod(at,w)
            for n in (at-w if y else -1,at+w if y<h-1 else -1,
                      at-1 if x else -1,at+1 if x<w-1 else -1):
                if n>=0 and mask[n]:mask[n]=0;queue.append(n)
        if len(component)>len(largest):largest=component
    if len(largest)<1500:raise ValueError('Missing complete figure')
    pixels=bytearray(w*h)
    for at in largest:pixels[at]=255
    whole=Image.frombytes('L',(w,h),bytes(pixels))
    soft=whole.filter(ImageFilter.MaxFilter(5))
    # Preserve two pixels of the original soft alpha, excluding opaque neighbours.
    allowed=ImageChops.subtract(soft,ImageChops.subtract(opaque,whole))
    image.putalpha(ImageChops.multiply(original,allowed))
    return image

def build():
    extraction = json.loads((SOURCE / 'extraction.json').read_text())
    OUT.mkdir(parents=True, exist_ok=True)
    frames = []
    pages = []
    for actor in ACTORS:
        actor_frames = []
        for kind in SHEETS:
            path = SOURCE / f'{actor}-{kind}.png'
            meta = extraction[path.name]
            if sha(path) != meta['sha256']:
                raise ValueError(f'Review whole-figure extraction after changing {path.name}')
            with Image.open(path) as image:
                if image.mode != 'RGBA' or image.getchannel('A').getextrema() != (0, 255):
                    raise ValueError(f'Real alpha required: {path.name}')
                assert len(meta['boxes']) == 16
                for local, box in enumerate(meta['boxes']):
                    index = SHEETS.index(kind) * 16 + local
                    complete = complete_figure(image, box)
                    assert complete.getchannel('A').getbbox(), (path.name, index)
                    # No head swap, limb extraction, recolor or cloth layer.
                    ratio = .66 if actor == 'ong' and index in (30,31) else HEIGHT.get(index, 1)
                    target_h = round(480 * ratio)
                    fitted = ImageOps.contain(complete, (488, target_h), Image.Resampling.LANCZOS)
                    tile = Image.new('RGBA', FRAME)
                    x = (FRAME[0] - fitted.width) // 2
                    y = FRAME[1] - 16 - fitted.height
                    tile.alpha_composite(fitted, (x, y))
                    actor_frames.append(tile)
                    frames.append(dict(actor=actor, index=index, source=path.name, box=box,
                                       fitted=[x,y,fitted.width,fitted.height]))
        assert len(actor_frames) == 48
        for page_index in range(12):
            name = f'{actor}-{page_index}'
            atlas = Image.new('RGBA', PAGE)
            lines = [name+'.png', f'size: {PAGE[0]}, {PAGE[1]}',
                     'format: RGBA8888', 'filter: Linear, Linear', 'repeat: none']
            for local in range(4):
                x = local % 2 * FRAME[0]
                y = local // 2 * FRAME[1]
                index = page_index * 4 + local
                atlas.alpha_composite(actor_frames[index], (x, y))
                lines += [f'pose_{index}', '  rotate: false', f'  xy: {x}, {y}',
                          f'  size: {FRAME[0]}, {FRAME[1]}', f'  orig: {FRAME[0]}, {FRAME[1]}',
                          '  offset: 0, 0', '  index: -1']
            atlas.save(OUT/(name+'.png'), optimize=True)
            (OUT/(name+'.atlas')).write_text('\n'.join(lines)+'\n')
            pages.append(dict(actor=actor, page=page_index, file=name+'.png',
                              rgbaBytes=PAGE[0]*PAGE[1]*4))
    manifest = dict(version=1, technique='complete-painted-character', actors=ACTORS,
                    canonicalOutfits=dict(sau='blue-explorer',ong='sky-explorer',capybara='blue-explorer'),
                    frameSize=FRAME, pageSize=PAGE, frames=frames, pages=pages,
                    activeCharacterPages=3, activeCharacterBytes=3*PAGE[0]*PAGE[1]*4,
                    totalStageLiveUpperBoundBytes=2048*1024*4+3*PAGE[0]*PAGE[1]*4+2*1280*720*4,
                    sourceHashes={p.name:sha(p) for p in sorted(SOURCE.glob('*.png'))})
    assert manifest['totalStageLiveUpperBoundBytes'] <= 32*1024*1024
    (OUT/'manifest.json').write_text(json.dumps(manifest, indent=2)+'\n')
    print('T-071: 144 complete figures, 36 four-pose pages; <=3 character pages live')
    return manifest

if __name__ == '__main__':
    build()
