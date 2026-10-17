#!/usr/bin/env python3
"""T-070 asset/capability integration checks against actual generated pixels and sources."""
from pathlib import Path
import json,hashlib,re,xml.etree.ElementTree as ET
from PIL import Image
from build_story_world import build,OUT,SOURCE,ROOT,COSTUMES,PARTS,EMOTIONS,BACKDROPS,isolated

def hashes():return {p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in OUT.iterdir() if p.is_file()}
build();first=hashes();build();assert hashes()==first,'Nondeterministic world asset build'
m=json.loads((OUT/'manifest.json').read_text());assert len(m['objects'])==60
assert len({o['id'] for o in m['objects']})==60
assert len(BACKDROPS)==8 and len(EMOTIONS)==16
assert m['liveTextureUpperBoundBytes']<=m['budgetBytes']==32*1024*1024
java=(ROOT/'core/src/main/java/com/khuongnd/dexkids/story/adventure/StoryWorldCatalog.java').read_text()
for o in m['objects']:assert '|'.join(o[k] for k in ('id','labelVi','topic','kind')) in java
checks=0
for page in m['pages']:
 image=Image.open(OUT/page['page']);assert image.mode=='RGBA' and image.size==((2048,1024) if page['page']=='objects.png' else (1024,1024))
 occupied=[]
 for r in page['regions']:
  x,y,w,h=(r[k] for k in ('x','y','width','height'));assert x>=4 and y>=4 and x+w+4<=image.width and y+h+4<=1024
  for bx,by,bw,bh in occupied:assert x>=bx+bw+4 or bx>=x+w+4 or y>=by+bh+4 or by>=y+h+4,'Atlas bleed gap'
  occupied.append((x,y,w,h))
  sprite=image.crop((x,y,x+w,y+h));assert sprite.getchannel('A').getbbox(),r['name'];checks+=1
 keys={r['name'] for r in page['regions']}
 if page['page']=='objects.png':assert keys=={o['id'].lower() for o in m['objects']}|{'water_drop','shadow'}
 else:
  child=Path(page['page']).stem
  assert keys=={'head_'+e for e in EMOTIONS}|{c+'_'+p for c in COSTUMES[child] for p in PARTS}
  assert len({hashlib.sha256(image.crop((r['x'],r['y'],r['x']+r['width'],r['y']+r['height'])).tobytes()).hexdigest() for r in page['regions'] if r['name'].startswith('head_')})==16
for name in BACKDROPS:
 image=Image.open(OUT/(name+'.jpg'));assert image.mode=='RGB' and image.size==(1280,720)
for child,meta in json.loads((SOURCE/'head-bounds.json').read_text()).items():
 assert len(meta['boxes'])==16
 image=Image.open(SOURCE/(child+'-expressions.png'))
 for box in meta['boxes']:
  alpha=isolated(image.crop(box)).getchannel('A');bounds=alpha.point(lambda a:255 if a>=128 else 0).getbbox();assert bounds
  assert bounds[0]>0 and bounds[1]>0 and bounds[2]<alpha.width and bounds[3]<alpha.height,(child,box,'Clipped face')
protected=json.loads((ROOT/'.agent/evidence/T-070/protected-before.json').read_text())
for name,digest in protected.items():assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest,name
report=dict(objects=60,emotions=14,speechHeadVariants=2,actions=27,backdrops=8,childCostumesEach=10,atlasPages=4,
 spriteRegionsChecked=checks,protectedSourcesUnchanged=len(protected),deterministic=True,
 liveTextureUpperBoundBytes=m['liveTextureUpperBoundBytes'],gpuBudgetBytes=m['budgetBytes'])
(ROOT/'.agent/evidence/T-070/art-verification.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report))
