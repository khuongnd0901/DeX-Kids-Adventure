#!/usr/bin/env python3
"""Small resident scenery atlas from actual M10 props (<=2 RGBA pages expected).

Copies crop pixels from already rasterized M10 pages, avoiding SVG re-render.
The new album is an optional, controlled 8 MiB visual upgrade.
"""
from pathlib import Path
from PIL import Image

HERE=Path(__file__).resolve().parents[1]/"assets/generated/m10"
SIZE=1024
PAD=4
PROP_GROUPS={"URB","VIL","FOR","CST","MTN","FRM"}

def build(manifest):
    props=[rec for rec in manifest["regions"] if rec["family"] in PROP_GROUPS]
    pages=[]; current=Image.new("RGBA",(SIZE,SIZE))
    x=y=PAD; row=0; regions=[]
    for rec in props:
        px,py=rec["xy"];w,h=rec["size"]
        with Image.open(HERE/f"m10-{rec['page']:02d}.png") as full:
            pic=full.crop((px,py,px+w,py+h))
        if x+w+PAD>SIZE:
            x=PAD;y+=row+PAD;row=0
        if y+h+PAD>SIZE:
            pages.append((current,regions))
            current=Image.new("RGBA",(SIZE,SIZE))
            regions=[];x=y=PAD;row=0
        current.alpha_composite(pic,(x,y))
        regions.append((rec,x,y))
        x+=w+PAD;row=max(row,h)
    if regions:
        pages.append((current,regions))
    for stale in HERE.glob("props-*.png"):
        stale.unlink()
    atlas=[]
    for n,(bitmap,entries) in enumerate(pages):
        filename=f"props-{n:02d}.png"
        bitmap.save(HERE/filename,format="PNG",optimize=True)
        if atlas:atlas.append("")
        atlas.extend([filename,f"size: {SIZE}, {SIZE}","format: RGBA8888",
                     "filter: Linear, Linear","repeat: none"])
        for rec,x,y in entries:
            w,h=rec["size"];ow,oh=rec["orig"];ox,oy=rec["offset"]
            atlas.extend([rec["region"],"  rotate: false",
                          f"  xy: {x}, {y}",f"  size: {w}, {h}",
                          f"  orig: {ow}, {oh}",f"  offset: {ox}, {oy}",
                          "  index: -1"])
    (HERE/"props.atlas").write_text("\n".join(atlas)+"\n",encoding="utf-8")
    (HERE/"props-map.tsv").write_text(
        "".join(f"{rec['family']}\t{rec['region']}\n" for rec in props),
        encoding="utf-8")
    if len(pages)>3:
        raise ValueError("M10 scenery resident atlas exceeds 3-page budget")
    print(f"M10 runtime scenery: {len(props)} actual props, {len(pages)} pages")
    return len(pages)
