#!/usr/bin/env python3
"""Author P0 vector poses with heads traced from approved cartoon sheets.

No new faces, photos, embedded bitmaps or palette-only source variants. Body/hand
geometry is newly authored per gesture. Outputs remain SOURCE_CREATED_UNREVIEWED.
Run build_sprites.py first to prepare the established transparent reference sheets.
"""
from pathlib import Path
import csv
import hashlib
from collections import defaultdict
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
POSES=['high_five','curious','think','clap','celebrate','look_left','look_right','listen']
# Crops in the already approved 512x768 cartoon cells, excluding raised hands.
CROPS={'SAU':(0,(174,9,426,307),(67,12,122,143)),
       'ONG':(1,(64,24,409,345),(53,14,150,139))}

def head_paths(family, point=False):
    cell,box,(x,y,w,h)=CROPS[family]
    if family=="SAU" and point:cell,box=1,(68,20,324,336)
    ref=ROOT/'assets/characters'/f'{family.lower()}-story-v2.png'
    im=Image.open(ref).convert('RGBA');cw,ch=im.width//2,im.height//2
    crop=im.crop((cell%2*cw+box[0],cell//2*ch+box[1],cell%2*cw+box[2],cell//2*ch+box[3]))
    crop=crop.resize((w,h),Image.Resampling.LANCZOS)
    palette=crop.quantize(colors=256,method=Image.Quantize.FASTOCTREE).convert('RGBA')
    # Keep only the head's connected silhouette, removing original shoulder/hand
    # fragments at crop edges. Never redraw facial features.
    pixels={}
    for py in range(h):
        for px in range(w):
            r,g,b,a=palette.getpixel((px,py))
            garment=py>h*.66 and b>r*1.1 and b>g*1.03
            outside_head=family=="ONG" and px>w*.88 and py>h*.55
            lower_edge=py>h*.94 and (px<w*.25 or px>w*.75)
            if a>=24 and not (garment or outside_head or lower_edge):pixels[px,py]=(r,g,b,a)
    pending=set(pixels);largest=set()
    while pending:
        first=pending.pop();group={first};queue=[first]
        while queue:
            cx,cy=queue.pop()
            for dx,dy in ((-1,0),(1,0),(0,-1),(0,1),(-1,-1),(1,1),(-1,1),(1,-1)):
                neighbor=(cx+dx,cy+dy)
                if neighbor in pending:pending.remove(neighbor);group.add(neighbor);queue.append(neighbor)
        if len(group)>len(largest):largest=group
    rows=defaultdict(list)
    for py in range(h):
        px=0
        while px<w:
            color=pixels.get((px,py),(0,0,0,0)) if (px,py) in largest else (0,0,0,0);end=px+1
            while end<w and (pixels.get((end,py),(0,0,0,0)) if (end,py) in largest else (0,0,0,0))==color:end+=1
            if color[3]>=24:
                # Exclude collar/arm blue below the original head; retain face/hair/hat.
                r,g,b,a=color
                if not (py>h*.66 and b>r*1.1 and b>g*1.03):
                    rows[color].append(f'M{x+px} {y+py}h{end-px}v1h-{end-px}z')
            px=end
    paths=[]
    for (r,g,b,a),segments in sorted(rows.items()):
        paths.append(f'<path fill="#{r:02x}{g:02x}{b:02x}" fill-opacity="{a/255:.3f}" d="'+''.join(segments)+'"/>')
    return ''.join(paths),hashlib.sha256(ref.read_bytes()).hexdigest()

def hand(x,y,kind='open'):
    # Distinct authored palms, not transformed duplicate source poses.
    if kind=='point':
        return f'<path d="M{x-7} {y+9}q-5-8 1-12v-16q4-7 8 0v14q12-4 13 7q-1 15-13 17z" fill="#ffc28f" stroke="#794536" stroke-width="2"/>'
    if kind=='fist':
        return f'<path d="M{x-9} {y+9}v-12q0-12 8-8q7-7 10 1q10 0 9 10q1 14-10 16z" fill="#ffc28f" stroke="#794536" stroke-width="2"/>'
    return f'<path d="M{x-8} {y+12}q-14-9-17-16q0-6 5-3l8 7l-5-21q0-7 5-4l7 19l-1-23q3-7 6 0l1 21l6-19q5-4 6 2l-3 20l7-9q6-3 7 3l-10 19q-7 14-22 4z" fill="#ffc28f" stroke="#794536" stroke-width="2"/>'

def arm(points):
    a,b,c=points
    return f'<path d="M{a[0]} {a[1]}Q{b[0]} {b[1]} {c[0]} {c[1]}" fill="none" stroke="#123f8d" stroke-width="19" stroke-linecap="round"/><path d="M{a[0]} {a[1]}Q{b[0]} {b[1]} {c[0]} {c[1]}" fill="none" stroke="#3386e9" stroke-width="13" stroke-linecap="round"/>'

GESTURES={
 'high_five':(((91,164),(68,110),(58,48)),((164,166),(182,188),(161,204)),(58,35,'open'),(158,204,'fist')),
 'curious':(((92,165),(70,199),(105,210)),((164,165),(187,184),(181,137)),(107,211,'fist'),(181,130,'point')),
 'think':(((92,167),(102,197),(144,193)),((165,163),(183,192),(175,119)),(145,193,'fist'),(175,109,'point')),
 'clap':(((91,163),(72,188),(121,171)),((165,164),(185,189),(137,171)),(119,167,'open'),(139,166,'open')),
 'celebrate':(((91,164),(74,123),(60,102)),((165,164),(186,123),(200,100)),(56,92,'open'),(200,91,'open')),
 'look_left':(((91,166),(70,180),(70,216)),((166,166),(191,198),(175,221)),(69,221,'fist'),(177,222,'open')),
 'look_right':(((91,166),(69,194),(95,209)),((165,166),(196,147),(174,105)),(95,210,'fist'),(174,101,'open')),
 'listen':(((91,165),(54,158),(54,135)),((165,166),(191,208),(160,219)),(54,126,'open'),(156,221,'open'))}

def body(family,pose):
    a,b,h1,h2=GESTURES[pose]
    brown=family=='ONG';shoe='#aa702c' if brown else '#276ec1'
    stance={'celebrate':9,'clap':-4,'look_left':-7,'look_right':7}.get(pose,0)
    legs=f'<path d="M102 231l-3 41h19l7-38m13 0l5 38h20l-6-43" fill="#f7b585" stroke="#794536" stroke-width="3"/>'
    feet=f'<path d="M{95+stance} 265q-16 10-17 29q17 15 49 1v-27zM144 267l-2 27q25 13 49-1q-1-17-26-27z" fill="{shoe}" stroke="#29455c" stroke-width="4"/><path d="M79 295q20 7 46 0M144 295q26 7 44-2" stroke="#eef7ff" stroke-width="5" fill="none"/>'
    bag=f'<path d="M78 145q-14 3-16 32l6 51l24 5V154m74-9q18 1 23 23l-4 58l-19 7" fill="'+('#e7ad35' if brown else '#2259ac')+'" stroke="#203e74" stroke-width="4"/>'
    torso='<path d="M99 139h57l17 73l-17 26h-59l-16-26z" fill="#f7fcff" stroke="#294a79" stroke-width="3"/><path d="M100 139l-15 12l-6 61l29 12l8-82m36-3l20 12l7 61l-24 12l-11-82" fill="#3182e3" stroke="#173f8c" stroke-width="3"/>'
    pants='<path d="M94 223h66l11 34l-30 6l-12-28l-7 28l-35-6z" fill="#216fcb" stroke="#174485" stroke-width="3"/>'
    straps='<path d="M92 150l12 65m55-65l-8 65" stroke="#172e4f" stroke-width="8"/><path d="M94 153l9 15m54-15l-3 15" stroke="#f2c44d" stroke-width="7"/>'
    if brown:
        chest='<path d="M109 179l-3 25q0 14 14 15l9-5l8 5q15-1 13-15l-5-25z" fill="#213743" stroke="#edb437" stroke-width="4"/><ellipse cx="118" cy="207" rx="11" ry="8" fill="#294e67"/><ellipse cx="140" cy="207" rx="11" ry="8" fill="#294e67"/>'
    else:
        chest='<circle cx="128" cy="181" r="12" fill="#0575f3"/><ellipse cx="128" cy="181" rx="20" ry="4" transform="rotate(-22 128 181)" fill="none" stroke="#e8ba25" stroke-width="4"/>'
    neck='<path d="M116 131h24l3 25q-14 11-28-1z" fill="#f7b585" stroke="#794536" stroke-width="2"/>'
    return bag+legs+feet+torso+pants+straps+chest+neck,arm(a)+arm(b)+hand(*h1)+hand(*h2)

def main():
    rows=list(csv.DictReader((ROOT/'art/M10_ASSET_INVENTORY.csv').open()))
    for family in ('SAU','ONG'):
        head,sha=head_paths(family)
        gaze,_=head_paths(family,point=True)
        for row in rows:
            if row['family']!=family or row['priority']!='P0':continue
            pose=row['name'].removeprefix(family.lower()+'_')
            head_group=gaze if pose.startswith("look_") else head
            if pose=='look_left':head_group='<g transform="translate(256 0) scale(-1 1)">'+gaze+'</g>'
            back,front=body(family,pose)
            source=f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 320"><title>{row["name"]}</title><desc>New authored gesture; face traced from approved cartoon sheet, SHA256 {sha}. No photograph or embedded raster. Human likeness approval pending.</desc>'+back+head_group+front+'</svg>\n'
            if len(source.encode())>300000:raise ValueError('SVG source budget exceeded')
            path=ROOT/row['source_path'];path.parent.mkdir(parents=True,exist_ok=True);path.write_text(source)
            print(row['asset_id'],len(source.encode()),'SOURCE_CREATED_UNREVIEWED')

if __name__=='__main__':main()
