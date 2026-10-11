#!/usr/bin/env python3
"""Build the dedicated 1024px scenery prop atlas, without expanding the character atlas."""
from pathlib import Path
from io import BytesIO
import xml.etree.ElementTree as ET
import cairosvg
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"art/assets-source/props"
OUT=ROOT/"assets/generated"
SIZE=(1024,1024)
PAD=4

def build():
    sources=sorted(SRC.glob("*.svg"))
    if not (14<=len(sources)<=24):
        raise ValueError("Unexpected extra scenery artwork count")
    OUT.mkdir(parents=True,exist_ok=True)
    canvas=Image.new("RGBA",SIZE,(0,0,0,0))
    x=y=PAD
    row_h=0
    regions=[]
    for p in sources:
        root=ET.parse(p).getroot()
        if root.attrib.get("viewBox")!="0 0 180 160":
            raise ValueError(f"Invalid prop viewBox: {p.name}")
        content=p.read_text(encoding="utf-8")
        safe=content.replace('xmlns="http://www.w3.org/2000/svg"',"")
        if "<script" in safe.lower() or "http://" in safe.lower() or "xlink:href" in safe.lower():
            raise ValueError(f"Unsafe prop: {p.name}")
        pic=Image.open(BytesIO(cairosvg.svg2png(bytestring=content.encode(),output_width=180,output_height=160))).convert("RGBA")
        if not pic.getbbox():raise ValueError(f"Empty prop {p.name}")
        if x+180+PAD>SIZE[0]:
            x=PAD;y+=row_h+PAD;row_h=0
        if y+160+PAD>SIZE[1]:raise ValueError("Extras atlas overflow")
        canvas.alpha_composite(pic,(x,y))
        regions.append((p.stem,x,y))
        x+=180+PAD;row_h=max(160,row_h)
    canvas.save(OUT/"extras.png",optimize=True)
    lines=["extras.png","size: 1024, 1024","format: RGBA8888","filter: Linear, Linear","repeat: none"]
    for name,x,y in regions:
        lines.extend([name,"  rotate: false",f"  xy: {x}, {y}",
                     "  size: 180, 160","  orig: 180, 160","  offset: 0, 0","  index: -1"])
    (OUT/"extras.atlas").write_text("\n".join(lines)+"\n",encoding="utf-8")
    print(f"Packed {len(regions)} transparent POI-supporting props in extra 1024 atlas")

if __name__=="__main__":build()
