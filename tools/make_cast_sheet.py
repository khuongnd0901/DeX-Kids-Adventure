#!/usr/bin/env python3
"""Montage of ACTUAL SVG-derived transparent PNG game assets, not AI concept art."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "assets/generated/sprites"
OUT = ROOT / "build/visual-evidence/cast-review.png"
SECTIONS = [
  ("CAPYBARA — 6 aligned expressions",
   ["capybara_idle","capybara_blink","capybara_talk","capybara_wave","capybara_sleep","capybara_surprised"],
   240, 10, 55, (220, 230)),
  ("ANIMAL COMPANIONS — 5 fictional friends",
   ["friend_rabbit","friend_fox","friend_panda","friend_cat","friend_penguin"],
   315, 18, 385, (210, 235)),
  ("BACKGROUND TRAFFIC — 6 fictional props",
   ["traffic_car","traffic_taxi","traffic_truck","traffic_minibus","traffic_scooter","traffic_bicycle"],
   258, 8, 730, (250, 135)),
]
def font(size):
    try: return ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", size)
    except OSError: return ImageFont.load_default()
def main():
    OUT.parent.mkdir(parents=True,exist_ok=True)
    canvas=Image.new("RGB",(1620,950),"#F6F4EF")
    draw=ImageDraw.Draw(canvas)
    titlefont=font(22)
    itemfont=font(18)
    for section, names, spacing, start, top, box in SECTIONS:
        draw.text((30,top-35),section,font=titlefont,fill="#254A62")
        for i,name in enumerate(names):
            x=start+i*spacing
            draw.rounded_rectangle((x,top,x+spacing-13,top+box[1]+60),radius=18,fill="#FFFFFF",outline="#E0DFD8",width=2)
            path=SRC/(name+".png")
            with Image.open(path) as obj:
                image=obj.convert("RGBA")
                image.thumbnail(box,Image.Resampling.LANCZOS)
                xx=x+(spacing-13-image.width)//2
                yy=top+6+(box[1]-image.height)//2
                canvas.paste(image,(xx,yy),image)
            draw.text((x+9,top+box[1]+17),name.replace("capybara_","").replace("traffic_","").replace("friend_","").upper(),font=itemfont,fill="#3F535A")
    canvas.save(OUT,optimize=True)
    print(f"Created {OUT}, showing 17 sprites derived from committed SVG assets")
if __name__=="__main__":
    main()
