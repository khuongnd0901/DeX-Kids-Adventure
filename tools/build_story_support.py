#!/usr/bin/env python3
"""Pack original editable T-063 SVGs into one bounded 512 RGBA story atlas."""
from pathlib import Path
from io import BytesIO
import json, hashlib, xml.etree.ElementTree as ET
import cairosvg
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
NAMES=('book','ball','gift','watering_can','seedling','apple','basket','star','leaf','shell','umbrella','magnifying_glass','water_drop','sparkle','shadow')
def build():
    src=ROOT/'art/assets-source/story-support';out=ROOT/'assets/generated/story-support';out.mkdir(parents=True,exist_ok=True)
    assert {p.stem for p in src.glob('*.svg')}==set(NAMES)
    canvas=Image.new('RGBA',(512,512));regions=[]
    for i,name in enumerate(NAMES):
        path=src/(name+'.svg');data=path.read_bytes();root=ET.fromstring(data)
        assert root.attrib['viewBox']=='0 0 180 160'
        assert not any(p.tag.split('}')[-1] in ('script','image','foreignObject') for p in root.iter())
        bitmap=Image.open(BytesIO(cairosvg.svg2png(bytestring=data,output_width=120,output_height=107))).convert('RGBA')
        assert bitmap.getbbox() and bitmap.getchannel('A').getextrema()==(0,255) if name!='shadow' else bitmap.getbbox()
        x,y=4+(i%4)*128,4+(i//4)*128;canvas.paste(bitmap,(x,y))
        regions.append(dict(name=name,x=x,y=y,width=120,height=107,sha256=hashlib.sha256(data).hexdigest()))
    canvas.save(out/'story-support.png',optimize=True)
    lines=['story-support.png','size: 512, 512','format: RGBA8888','filter: Linear, Linear','repeat: none']
    for r in regions:lines += [r['name'],'  rotate: false',f"  xy: {r['x']}, {r['y']}",'  size: 120, 107','  orig: 120, 107','  offset: 0, 0','  index: -1']
    (out/'story-support.atlas').write_text('\n'.join(lines)+'\n')
    (out/'manifest.json').write_text(json.dumps(dict(version=2,page=512,rgbaBytes=1048576,regions=regions),indent=2)+'\n')
    print('T-063: 12 original props + 3 effects, one 512 RGBA atlas (1 MiB backing)')
if __name__=='__main__':build()
