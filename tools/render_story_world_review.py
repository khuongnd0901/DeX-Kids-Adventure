#!/usr/bin/env python3
"""Contact sheets from actual GL captures plus the runtime object atlas; no invented frames."""
from pathlib import Path
import json,math,sys
from PIL import Image,ImageDraw,ImageFont,ImageOps
ROOT=Path(__file__).resolve().parents[1]
CAP=ROOT/(sys.argv[1] if len(sys.argv)>1 else 'build/story-world-gallery');OUT=ROOT/(sys.argv[2] if len(sys.argv)>2 else 'build/story-world-review')
FONT=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',11)
def sheet(name,files,cols,tile=(160,190),actor=None):
 if not files:return
 w,h=tile;canvas=Image.new('RGB',(cols*w,math.ceil(len(files)/cols)*(h+32)),'#f6f2e9');d=ImageDraw.Draw(canvas)
 for i,p in enumerate(files):
  image=Image.open(p).convert('RGB')
  if actor:
   center={'CAPYBARA':400,'SAU':850,'ONG':1300}[actor];scale=image.width/1920
   image=image.crop(tuple(round(v*scale) for v in (center-255,240,center+285,900)))
  image=ImageOps.contain(image,tile,Image.Resampling.LANCZOS);x=i%cols*w;y=i//cols*(h+32)
  canvas.paste(image,(x+(w-image.width)//2,y+(h-image.height)//2))
  label=p.stem.replace('action-','').replace('emotion-','').replace('outfit-','').replace(actor+'-' if actor else '','')
  d.text((x+4,y+h+4),label,fill='#253e4c',font=FONT)
 canvas.save(OUT/(name+'.png'),optimize=True)
def main():
 OUT.mkdir(parents=True,exist_ok=True)
 for actor in ('CAPYBARA','SAU','ONG'):
  sheet('actions-'+actor,sorted(CAP.glob('action-'+actor+'-*.png')),9,actor=actor)
  sheet('emotions-'+actor,sorted(CAP.glob('emotion-'+actor+'-*.png')),7,actor=actor)
  if actor!='CAPYBARA':sheet('outfits-'+actor,sorted(CAP.glob('outfit-'+actor+'-*.png')),5,actor=actor)
 sheet('scenes',sorted(CAP.glob('scene-*.png')),4,tile=(384,216))
 sheet('backdrops',sorted(CAP.glob('background-*.png')),4,tile=(320,180))
 m=json.loads((ROOT/'assets/generated/story-world/manifest.json').read_text());page=next(p for p in m['pages'] if p['page']=='objects.png')
 atlas=Image.open(ROOT/'assets/generated/story-world/objects.png');regions={r['name']:r for r in page['regions']}
 canvas=Image.new('RGB',(1200,10*145),'#f6f2e9');d=ImageDraw.Draw(canvas)
 for i,obj in enumerate(m['objects']):
  r=regions[obj['id'].lower()];tile=ImageOps.contain(atlas.crop((r['x'],r['y'],r['x']+r['width'],r['y']+r['height'])),(180,108),Image.Resampling.LANCZOS)
  x=i%6*200;y=i//6*145;canvas.paste(tile,(x+(200-tile.width)//2,y),tile)
  d.text((x+6,y+112),obj['id'],font=FONT,fill='#253e4c');d.text((x+6,y+126),obj['labelVi'],font=FONT,fill='#253e4c')
 canvas.save(OUT/'objects.png',optimize=True)
 print('Review sheets:',len(list(OUT.glob('*.png'))))
if __name__=='__main__':main()
