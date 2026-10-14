#!/usr/bin/env python3
"""Geometry regression: face/hand alpha, pose-specific passenger crop, page bounds.
This does not substitute for reviewing actual Android screenshots.
"""
from pathlib import Path
from PIL import Image
from build_child_outfits import ROOT,SRC,NAMES,SIZE
from child_outfit_geometry import garment
import hashlib

# Tight eyes/mouth regions exclude hats/collars; all coordinates in 256x384 cells.
FACE_CORE={
 ('sau','story'): [(106,78,183,133),(70,98,150,145),(107,76,192,129),(84,108,173,151)],
 ('sau','passenger'): [(99,158,192,224),(64,171,162,241),(107,128,202,192),(69,140,174,204)],
 ('ong','story'): [(109,104,189,162),(105,138,185,185),(113,101,196,152),(100,140,182,181)],
 ('ong','passenger'): [(108,158,194,211),(102,193,179,237),(107,148,191,195),(97,164,179,212)],
}
checks=0
for child in ('sau','ong'):
 for kind,suffix in [('story','story-v2'),('passenger','passenger-v3')]:
  file=ROOT/'art/assets-source/characters'/f'{child}-{suffix}.png'
  before=hashlib.sha256(file.read_bytes()).hexdigest();sheet=Image.open(file)
  for style,name in enumerate(n for n in NAMES if n.startswith(child+'_')):
   for pose in range(4):
    cell=sheet.crop((pose%2*512,pose//2*768,pose%2*512+512,pose//2*768+768))
    layer=garment(cell,(SRC/(name+'.svg')).read_bytes(),child,kind,pose,style)
    assert layer.getchannel('A').getbbox(),(name,kind,pose)
    assert layer.getchannel('A').crop(FACE_CORE[child,kind][pose]).getbbox() is None,(name,kind,pose,'face overlay')
    if child=='ong' and kind=='story' and pose==3:
     assert layer.getpixel((110,230))[3]==0, (name,'pet palm must stay exposed')
     assert layer.getpixel((85,260))[3]>240, (name,'skirt must cover knee below exposed palm')
    checks+=1
  assert hashlib.sha256(file.read_bytes()).hexdigest()==before
atlas=(ROOT/'assets/generated/child-outfits.atlas').read_text()
assert SIZE==(2048,2048)
assert len([l for l in atlas.splitlines() if '_story_' in l or '_passenger_' in l])==96
print(f'T-053: {checks} pose-aware layers preserve eyes/mouth; original sheets intact;96regions/1page')
