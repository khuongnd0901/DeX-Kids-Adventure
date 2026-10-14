#!/usr/bin/env python3
"""Render integration and protected-art checks complement pure JUnit geometry tests."""
from pathlib import Path
import json
import hashlib
ROOT=Path(__file__).resolve().parents[1]
screen=(ROOT/'core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java').read_text()
sprites=(ROOT/'core/src/main/java/com/khuongnd/dexkids/game/CartoonSprites.java').read_text()
steps=['backdropRenderer.draw(batch, themed)', 'cartoonSprites.drawExtras(batch,',
       'cartoonSprites.drawTraffic(batch, TrafficRenderOrder.Plane.FAR_TRAFFIC)',
       'cartoonSprites.drawVehicle(batch,', 'cartoonSprites.drawBusPassengers(batch,',
       'cartoonSprites.drawTraffic(batch, TrafficRenderOrder.Plane.NEAR_TRAFFIC)',
       'cartoonSprites.drawSauCompanion(batch,', 'cartoonSprites.drawOngCompanion(batch,',
       'painter.paintHud(batch)']
positions=[screen.index(s) for s in steps]
assert positions==sorted(positions), 'POI/traffic/bus/passengers/story/HUD render order'
environment=sprites[sprites.index('void drawEnvironment'):sprites.index('void advanceTraffic')]
assert 'trafficSprites[' not in environment
traffic=sprites[sprites.index('void advanceTraffic'):sprites.index('private void drawBiomeDecorations')]
assert 'new Texture' not in traffic and '.sort(' not in traffic and 'setColor' not in traffic
# Pinned from inherited T-049 base182841d; works with source archives/shallow clones.
protected=json.loads((ROOT/'tools/t049-protected-art.json').read_text())
for rel,expected in protected.items():
 assert hashlib.sha256((ROOT/rel).read_bytes()).hexdigest()==expected,rel
print(f'PASS render dispatch, no traffic transparency/allocation, {len(protected)} protected source hashes unchanged')
# Validate Java ground metadata against actual SVG tire geometry, including stroke.
import re, io, xml.etree.ElementTree as ET
import cairosvg
from PIL import Image
java=(ROOT/'core/src/main/java/com/khuongnd/dexkids/game/TrafficLayout.java').read_text()
contacts=[float(x.strip().rstrip('f')) for x in re.search(r'CONTACT = \{([^}]+)',java)[1].split(',')]
for kind,name in enumerate(('car','taxi','truck','minibus','scooter','bicycle')):
 p=ROOT/f'art/assets-source/storybook-v2/vehicles/traffic_{name}.svg'
 r=ET.parse(p).getroot()
 tires=[n for n in r.iter('{http://www.w3.org/2000/svg}circle') if float(n.get('r','0'))>=25]
 parents={child:parent for parent in r.iter() for child in parent}
 def inherited(node,key):
  if key in node.attrib: return node.get(key)
  return inherited(parents[node],key) if node in parents else '0'
 bottoms={float(n.get('cy'))+float(n.get('r'))+float(inherited(n,'stroke-width'))/2 for n in tires}
 assert bottoms=={contacts[kind]},(name,bottoms,contacts[kind])
 im=Image.open(io.BytesIO(cairosvg.svg2png(url=str(p)))).convert('RGBA')
 alpha=im.getchannel('A');w,h=im.size
 for box in [(0,0,w,1),(0,h-1,w,h),(0,0,1,h),(w-1,0,w,h)]:
  assert alpha.crop(box).getbbox() is None,('clipped',name)
 assert im.getpixel((0,0))[3]==0
print('PASS all six SVG tire planes match layout anchors; transparent canvas edges, no atlas clipping')
