#!/usr/bin/env python3
"""Contact sheets from *actual* generated sprites; skips not-yet-drawn M10 art.

Planning inventory never counts as visual source evidence.
"""
from pathlib import Path
from PIL import Image, ImageDraw
from build_m10_atlas import ROOT, OUTPUT, build

TARGET = ROOT / "assets/generated/m10-gallery"
WIDTH, HEIGHT = 196, 196

def contact_sheet(name, paths):
    if not paths:
        return None
    TARGET.mkdir(parents=True, exist_ok=True)
    columns = 6
    rows = (len(paths)+columns-1)//columns
    out=Image.new("RGB",(columns*WIDTH,rows*HEIGHT),(245,247,250))
    painter=ImageDraw.Draw(out)
    for i, (label, image) in enumerate(paths):
        with Image.open(image) as picture:
            picture=picture.convert("RGBA")
            picture.thumbnail((180,160))
            px=(i%columns)*WIDTH+(WIDTH-picture.width)//2
            py=(i//columns)*HEIGHT+9
            out.paste(picture,(px,py),picture)
        painter.text(((i%columns)*WIDTH+8,(i//columns)*HEIGHT+172),
                     label[:28],fill=(28,38,57))
    outfile=TARGET/(name+".png")
    out.save(outfile)
    return outfile


def main():
    from build_m10_atlas import inventory
    result=build()
    # Exact generated atlas cropping uses metadata; no proposed asset thumbnails.
    grouped={}
    for record in result["regions"]:
        page=OUTPUT/f"m10-{record['page']:02d}.png"
        grouped.setdefault(record["family"],[]).append((record,page))
    for family,items in grouped.items():
        thumbs=[]
        for rec,image in items:
            with Image.open(image) as atlas:
                x,y=rec["xy"];w,h=rec["size"]
                art=atlas.crop((x,y,x+w,y+h))
                TARGET.mkdir(parents=True,exist_ok=True)
                thumb=TARGET/(rec["region"]+".png")
                art.save(thumb)
                thumbs.append((rec["region"],thumb))
        contact_sheet("m10-"+family,thumbs)
    legacy=ROOT/"assets/generated/sprites"
    original=[(p.stem,p) for p in sorted(legacy.glob("*.png"))]
    contact_sheet("baseline-original-cast",original)
    print(f"M10 gallery: {result['produced']} actual new assets; {len(original)} legacy sprites; no generated concepts counted as art")


if __name__=="__main__":
    main()
