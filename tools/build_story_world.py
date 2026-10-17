#!/usr/bin/env python3
"""Reproducible T-070 atlases from versioned SVG/PNG sources, never network.
Packs sprites, normalizes alpha/foot anchors, validates extraction and records hashes.
"""
from pathlib import Path
from io import BytesIO
import hashlib,json,xml.etree.ElementTree as ET
import cairosvg
from PIL import Image,ImageOps,ImageFilter,ImageChops
from collections import deque
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'art/assets-source/story-world'
OUT=ROOT/'assets/generated/story-world'
EMOTIONS=('happy','curious','calm','surprised','excited','proud','sad','worried','afraid','angry','shy','confused','tired','grateful','closed','open')
PARTS=('torso','upper_left','fore_left','upper_right','fore_right','leg_left','leg_right')
COSTUMES={'sau':('solar','tech','web','thunder','shield','shadow','classic_explorer','classic_firefighter','classic_pilot','classic_police'),'ong':('rose','ocean','sky','forest','starlight','lavender','classic_explorer','classic_firefighter','classic_pilot','classic_police'),'capybara':('explorer',)}
BACKDROPS=('home','classroom','playground','garden','forest','beach','market','library')
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def trimmed(im):
 alpha=im.getchannel('A');box=alpha.point(lambda a:255 if a>=32 else 0).getbbox()
 if not box:raise ValueError('Empty sprite')
 # Preserve soft alpha around the silhouette; do not key out colored pixels.
 box=(max(0,box[0]-2),max(0,box[1]-2),min(im.width,box[2]+2),min(im.height,box[3]+2))
 return im.crop(box)
def isolated(im):
 # Keep the main silhouette, remove disconnected fragments from neighbouring
 # generated sheet cells. Expand its mask 2px to preserve original soft alpha.
 w,h=im.size;mask=bytearray(im.getchannel('A').point(lambda a:255 if a>=100 else 0).tobytes());best=[]
 for i in range(len(mask)):
  if not mask[i]:continue
  mask[i]=0;queue=deque([i]);component=[]
  while queue:
   at=queue.popleft();component.append(at);y,x=divmod(at,w)
   for n in (at-w if y else -1,at+w if y<h-1 else -1,at-1 if x else -1,at+1 if x<w-1 else -1):
    if n>=0 and mask[n]:mask[n]=0;queue.append(n)
  if len(component)>len(best):best=component
 if not best:raise ValueError('Missing sprite silhouette')
 keep=bytearray(w*h)
 for at in best:keep[at]=255
 alpha=Image.frombytes('L',(w,h),bytes(keep)).filter(ImageFilter.MaxFilter(5))
 im=im.copy();im.putalpha(ImageChops.multiply(im.getchannel('A'),alpha));return im

def fit(im,size):return ImageOps.contain(trimmed(im),size,Image.Resampling.LANCZOS)
def atlas(name,sprites):
 width=2048 if name=='objects' else 1024
 page=Image.new('RGBA',(width,1024));regions=[];x=y=4;rowh=0
 for key,im in sorted(sprites,key=lambda s:(-s[1].height,s[0])):
  if x+im.width+4>width:x=4;y+=rowh+8;rowh=0
  if y+im.height+4>1024:raise ValueError(f'{name}: one-page budget exceeded')
  page.alpha_composite(im,(x,y))
  # Extrude 2px nearest-edge padding for linear texture filtering.
  for n in (1,2):
   page.paste(im.crop((0,0,im.width,1)),(x,y-n));page.paste(im.crop((0,im.height-1,im.width,im.height)),(x,y+im.height-1+n))
   page.paste(im.crop((0,0,1,im.height)),(x-n,y));page.paste(im.crop((im.width-1,0,im.width,im.height)),(x+im.width-1+n,y))
  regions.append(dict(name=key,x=x,y=y,width=im.width,height=im.height));x+=im.width+8;rowh=max(rowh,im.height)
 page.save(OUT/(name+'.png'),optimize=True)
 lines=[name+'.png',f'size: {width}, 1024','format: RGBA8888','filter: Linear, Linear','repeat: none']
 for r in regions:lines += [r['name'],'  rotate: false',f"  xy: {r['x']}, {r['y']}",f"  size: {r['width']}, {r['height']}",f"  orig: {r['width']}, {r['height']}",'  offset: 0, 0','  index: -1']
 (OUT/(name+'.atlas')).write_text('\n'.join(lines)+'\n')
 return dict(page=name+'.png',width=width,height=1024,rgbaBytes=width*1024*4,regions=regions)
def build():
 OUT.mkdir(parents=True,exist_ok=True);pages=[];extraction=[]
 specs=json.loads((SOURCE/'objects.json').read_text())
 head_bounds=json.loads((SOURCE/'head-bounds.json').read_text())
 sprites=[]
 for obj in specs:
  p=SOURCE/'objects'/(obj['id'].lower()+'.svg');data=p.read_bytes();root=ET.fromstring(data)
  if any(el.tag.split('}')[-1] in ('script','image','foreignObject','use') for el in root.iter()):raise ValueError('External SVG content')
  raster=384 if obj['kind']=='LARGE' else 192 if obj['kind']=='SKY' else 160
  im=Image.open(BytesIO(cairosvg.svg2png(bytestring=data,output_width=raster,output_height=round(raster*176/192)))).convert('RGBA')
  sprites.append((obj['id'].lower(),trimmed(im)))
 for effect in ('water_drop','shadow'):
  data=(ROOT/'art/assets-source/story-support'/(effect+'.svg')).read_bytes()
  sprites.append((effect,Image.open(BytesIO(cairosvg.svg2png(bytestring=data,output_width=64,output_height=48))).convert('RGBA')))
 pages.append(atlas('objects',sprites))
 for child,costumes in COSTUMES.items():
  heads=Image.open(SOURCE/(child+'-expressions.png')).convert('RGBA');rig=Image.open(SOURCE/(child+'-rig.png')).convert('RGBA');sprites=[]
  for i,name in enumerate(EMOTIONS):
   assert sha(SOURCE/(child+'-expressions.png'))==head_bounds[child]['sha256'], 'Revalidate expression extraction after changing source'
   box=head_bounds[child]['boxes'][i];cell=isolated(heads.crop(box))
   extraction.append(dict(child=child,expression=name,box=box))
   sprites.append(('head_'+name,fit(cell,(128,128))))
  if child=='capybara':
   boxes=((0,80,525,680),(525,140,730,660),(730,140,945,680),(970,140,1200,660),(1220,140,1430,680),(1450,80,1755,680),(1755,80,2048,680))
   for part,box in zip(PARTS,boxes):
    sprites.append(('explorer_'+part,fit(rig.crop(box),(120,155) if part=='torso' else (64,106))))
    extraction.append(dict(child=child,costume='explorer',part=part,box=box))
  else:
   # Human-reviewed column boundaries: generated sheets have wider torsos.
   xs=(0,235,393,552,720,880,1060,1254) if child=='sau' else (0,275,430,570,720,868,1045,1254)
   ys=(0,210,415,620,827,1030,1254) if child=='sau' else (0,203,405,610,813,1010,1254)
   for row,costume in enumerate(costumes[:6]):
    for col,part in enumerate(PARTS):
     box=(xs[col],ys[row],xs[col+1],ys[row+1]);cell=isolated(rig.crop(box))
     if part.startswith('upper_') and child=='sau':cell=cell.crop((0,0,cell.width,round(cell.height*.80)))
     if part.startswith('fore_') and child=='sau':cell=cell.crop((0,round(cell.height*.27),cell.width,cell.height))
     if child=='ong' and part.startswith('upper_'):
      fore_col=2 if part=='upper_left' else 4
      clean=isolated(rig.crop((xs[fore_col],ys[row],xs[fore_col+1],ys[row+1])))
      cell=clean.crop((0,0,clean.width,round(clean.height*.54)))
     if child=='ong' and part.startswith('leg_'):
      clean=isolated(Image.open(SOURCE/'ong-classic-rig.png').convert('RGBA').crop((868 if part=='leg_left' else 1045,0,1045 if part=='leg_left' else 1254,320)))
      cell=clean.crop((0,round(clean.height*.28),clean.width,clean.height))
     sprites.append((costume+'_'+part,fit(cell,(110,142) if part=='torso' else (54,96))))
     extraction.append(dict(child=child,costume=costume,part=part,box=box))
   classic=Image.open(SOURCE/(child+'-classic-rig.png')).convert('RGBA')
   cxs=(0,268,415,565,720,875,1060,1254) if child=='sau' else (0,277,429,573,721,880,1060,1254)
   cys=(0,324,620,920,1254) if child=='sau' else (0,320,608,910,1254)
   for row,costume in enumerate(costumes[6:]):
    for col,part in enumerate(PARTS):
     box=(cxs[col],cys[row],cxs[col+1],cys[row+1]);cell=isolated(classic.crop(box))
     # Generated arm cells still include a sleeve; retain only elbow-to-hand.
     if part.startswith('fore_'):cell=cell.crop((0,round(cell.height*.46),cell.width,cell.height))
     if child=='ong' and part.startswith('leg_'):cell=cell.crop((0,round(cell.height*.20),cell.width,cell.height))
     sprites.append((costume+'_'+part,fit(cell,(110,142) if part=='torso' else (54,96))))
     extraction.append(dict(child=child,costume=costume,part=part,box=box))
  pages.append(atlas(child,sprites))
 for backdrop in BACKDROPS:
  im=Image.open(SOURCE/'backdrops'/(backdrop+'.png')).convert('RGB')
  ImageOps.fit(im,(1280,720),Image.Resampling.LANCZOS).save(OUT/(backdrop+'.jpg'),quality=90,subsampling=0,optimize=True)
 manifest=dict(version=3,objects=specs,emotions=EMOTIONS,costumes=COSTUMES,parts=PARTS,backdrops=BACKDROPS,pages=pages,
  extraction=extraction,sourceHashes={str(p.relative_to(SOURCE)):sha(p) for p in sorted(SOURCE.rglob('*')) if p.is_file()},
  liveTextureUpperBoundBytes=5*4194304+2*1280*720*4,budgetBytes=32*1024*1024)
 assert manifest['liveTextureUpperBoundBytes']<=manifest['budgetBytes']
 (OUT/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
 print('T-070: 60 props, 3 rigs, 14 emotions + speech heads, 8 backdrops; active textures <=28 MiB')
if __name__=='__main__':build()
