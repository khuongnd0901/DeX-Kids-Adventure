#!/usr/bin/env python3
"""Verify whole-figure extraction, alpha, footprint, atlas seams and reproducibility."""
from pathlib import Path
import hashlib
import json
from PIL import Image
from build_whole_characters import build, complete_figure, SOURCE, OUT, ROOT, FRAME, PAGE

def digests():
    return {p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(OUT.iterdir()) if p.is_file()}

manifest = build()
first = digests()
build()
assert digests() == first, 'Complete-character packing changed between identical builds'
assert len(manifest['frames']) == 144 and len(manifest['pages']) == 36
assert manifest['activeCharacterPages'] == 3
assert manifest['totalStageLiveUpperBoundBytes'] <= 32*1024*1024
bounds = json.loads((SOURCE/'extraction.json').read_text())
assert len(bounds) == 9
for name, meta in bounds.items():
    image = Image.open(SOURCE/name)
    assert image.mode == 'RGBA' and image.getchannel('A').getextrema() == (0,255)
    assert len(meta['boxes']) == 16
    assert hashlib.sha256((SOURCE/name).read_bytes()).hexdigest() == meta['sha256']
    for box in meta['boxes']:
        # Dominant silhouette must be entirely within its reviewed whole-figure rectangle.
        assert 0 < box[0] < box[2] < image.width and 0 < box[1] < box[3] < image.height
        alpha = complete_figure(image,box).getchannel('A').point(lambda a:255 if a>=192 else 0)
        x,y,right,bottom = alpha.getbbox()
        assert x>=1 and y>=1 and right<alpha.width and bottom<alpha.height, (name,box)
all_hashes = {}
for page in manifest['pages']:
    image = Image.open(OUT/page['file'])
    assert image.mode == 'RGBA' and image.size == PAGE
    lines = (OUT/(Path(page['file']).stem+'.atlas')).read_text()
    hashes = all_hashes.setdefault(page['actor'],{})
    for i in range(4):
        index = page['page']*4+i
        assert f'pose_{index}\n' in lines
        x,y=i%2*FRAME[0],i//2*FRAME[1]
        figure = image.crop((x,y,x+FRAME[0],y+FRAME[1]))
        alpha = figure.getchannel('A');left,top,right,bottom=alpha.getbbox()
        assert left>=8 and top>=16 and right<=FRAME[0]-8 and FRAME[1]-24<=bottom<=FRAME[1]-16
        assert len(set(figure.getdata()))>1000
        hashes[index]=hashlib.sha256(figure.tobytes()).hexdigest()
for actor, hashes in all_hashes.items():
    assert len(hashes)==48
    for first,second in [(1,2),(6,7),(8,9),(16,17),(18,19),(24,25),(46,47)]:
        assert hashes[first]!=hashes[second], (actor,first,second,'Aliased whole-pose frames')
renderer=(ROOT/'core/src/main/java/com/khuongnd/dexkids/game/StoryWorldRenderer.java').read_text()
for forbidden in ('rigAtlases','head_','armLimb(','armPose(','bodyPoint(','StoryRigMotion.sample'):
    assert forbidden not in renderer, forbidden
protected=json.loads((ROOT/'.agent/evidence/T-070/protected-before.json').read_text())
for name,digest in protected.items():
    assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest, name
report=dict(wholeFigures=144,sourceSheets=9,atlasPages=36,activeCharacterPages=3,
            atlasPageSize=PAGE,frameSize=FRAME,distinctGesturePairs=21,deterministic=True,
            stageTextureUpperBoundBytes=manifest['totalStageLiveUpperBoundBytes'],
            protectedSourcesUnchanged=len(protected),storyOutfits='canonical explorer only; road rotation unchanged')
(ROOT/'.agent/evidence/T-071/art-verification.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report))
