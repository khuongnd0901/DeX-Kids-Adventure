#!/usr/bin/env python3
"""Incremental reproducible M10 SVG -> multi-page LibGDX TextureAtlas.

The 172-row plan is authoritative; absent drawings are never invented.
"""
from __future__ import annotations

import csv
import hashlib
import io
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INDEX = ROOT / "art/M10_ASSET_INVENTORY.csv"
OUTPUT = ROOT / "assets/generated/m10"
SIZE = 1024
PAD = 4
FAMILIES = {"CAP":12,"SAU":10,"ONG":10,"VEH":12,
            "URB":16,"VIL":16,"FOR":16,"CST":16,"MTN":16,
            "FRM":16,"FX":16,"UI":16}
DIMENSIONS = {x:(160,160) for x in FAMILIES}
DIMENSIONS.update({"CAP":(256,320),"SAU":(256,320),
                   "ONG":(256,320),"VEH":(280,160),"UI":(160,96)})


def inventory():
    with INDEX.open(encoding="utf-8",newline="") as f:
        result = list(csv.DictReader(f))
    if len(result)!=172:
        raise ValueError("Expected 172 M10 inventory rows")
    ids=set(); names=set(); paths=set(); counts={}
    for row in result:
        group=row["family"]; key=row["asset_id"]; name=row["target_region"]
        rel=row["source_path"]
        if group not in FAMILIES or not re.fullmatch(r"[A-Z]{2,3}-[0-9]{3}",key):
            raise ValueError("Invalid M10 id/family "+key)
        if not re.fullmatch(r"[a-z][a-z0-9_]*",name) or row["name"]!=name:
            raise ValueError("Invalid region "+key)
        if not rel.startswith("art/assets-source/m10/") or rel != rel.lower() or \
                rel != str(Path(rel)) or Path(rel).stem!=name or not rel.endswith(".svg"):
            raise ValueError("Unsafe M10 source "+rel)
        if row["priority"] not in ("P0","P1") or row["format"]!="SVG":
            raise ValueError("Invalid catalog format "+key)
        if key in ids or name in names or rel in paths:
            raise ValueError("Duplicate inventory entry "+key)
        ids.add(key);names.add(name);paths.add(rel)
        counts[group]=counts.get(group,0)+1
    if counts!=FAMILIES:
        raise ValueError("M10 group count mismatch")
    if sum(r["priority"]=="P0" for r in result)!=108:
        raise ValueError("M10 priority mismatch")
    return sorted(result,key=lambda r:r["asset_id"])


def parse_svg(path,width,height):
    raw=path.read_bytes()
    lower=raw.lower()
    if len(raw)>300000 or b"<!doctype" in lower or b"<!entity" in lower:
        raise ValueError("Unsafe SVG size/doctype: "+str(path))
    root=ET.fromstring(raw)
    if root.tag.rsplit("}",1)[-1]!="svg" or \
            root.attrib.get("viewBox")!=f"0 0 {width} {height}":
        raise ValueError("Invalid SVG viewport: "+str(path))
    for node in root.iter():
        tag=node.tag.rsplit("}",1)[-1].lower()
        if tag in ("script","foreignobject","image","use","animatemotion"):
            raise ValueError("Unsafe embedded SVG node: "+str(path))
        for attr,value in node.attrib.items():
            a=attr.rsplit("}",1)[-1].lower()
            v=value.lower()
            if a in ("href","src") or a.startswith("on") or any(
                    s in v for s in ("url(","http:","https:","data:")):
                raise ValueError("External SVG asset rejected: "+str(path))
    return raw


def page():
    from PIL import Image
    return {"bitmap":Image.new("RGBA",(SIZE,SIZE)), "x":PAD,"y":PAD,
            "row":0,"regions":[]}


def build():
    try:
        import cairosvg
        from PIL import Image
    except ImportError as err:
        raise SystemExit("Install tools/requirements-art.txt") from err
    declarations=inventory()
    available=[r for r in declarations if (ROOT/r["source_path"]).is_file()]
    OUTPUT.mkdir(parents=True,exist_ok=True)
    packed=[]; pages=[]; current=page()
    for row in available:
        width,height=DIMENSIONS[row["family"]]
        path=ROOT/row["source_path"]
        source=parse_svg(path,width,height)
        bitmap=Image.open(io.BytesIO(cairosvg.svg2png(
            bytestring=source,output_width=width,output_height=height))).convert("RGBA")
        bounds=bitmap.getbbox()
        if bounds is None:
            raise ValueError("Transparent-only M10 asset: "+str(path))
        part=bitmap.crop(bounds)
        w,h=part.size
        if current["x"]+w+PAD>SIZE:
            current["x"]=PAD
            current["y"]+=current["row"]+PAD
            current["row"]=0
        if current["y"]+h+PAD>SIZE:
            pages.append(current);current=page()
        x,y=current["x"],current["y"]
        current["bitmap"].alpha_composite(part,(x,y))
        item={"asset_id":row["asset_id"],"region":row["target_region"],
              "family":row["family"],"priority":row["priority"],
              "source":row["source_path"],
              "sha256":hashlib.sha256(source).hexdigest(),
              "page":len(pages),"xy":[x,y],"size":[w,h],
              "orig":[width,height],"offset":[bounds[0],height-bounds[3]],
              "review":"SOURCE_CREATED_UNREVIEWED"}
        packed.append(item);current["regions"].append(item)
        current["x"]+=w+PAD;current["row"]=max(h,current["row"])
    if current["regions"]:
        pages.append(current)
    # Only remove names in this tool's output namespace.
    for stale in OUTPUT.glob("m10-*.png"):
        stale.unlink()
    atlas=[]
    for index,p in enumerate(pages):
        filename=f"m10-{index:02d}.png"
        p["bitmap"].save(OUTPUT/filename,format="PNG",optimize=True)
        if atlas:
            atlas.append("")  # One empty line separates atlas pages, not regions.
        atlas.extend([filename,f"size: {SIZE}, {SIZE}","format: RGBA8888",
                      "filter: Linear, Linear","repeat: none"])
        for r in p["regions"]:
            x,y=r["xy"]; w,h=r["size"]
            ow,oh=r["orig"]; ox,oy=r["offset"]
            atlas.extend([r["region"],"  rotate: false",f"  xy: {x}, {y}",
                          f"  size: {w}, {h}",f"  orig: {ow}, {oh}",
                          f"  offset: {ox}, {oy}","  index: -1"])
    (OUTPUT/"m10.atlas").write_text("\n".join(atlas)+"\n",encoding="utf-8")
    result={"schema":1,"declared":172,"produced":len(packed),
            "pages":len(pages),"regions":packed}
    from build_m10_prop_atlas import build as build_props
    result["prop_pages"] = build_props(result)
    (OUTPUT/"manifest.json").write_text(
        json.dumps(result,sort_keys=True,indent=2,ensure_ascii=False)+"\n",
        encoding="utf-8")
    print(f"M10: {len(packed)}/172 source files on {len(pages)} atlas pages (not human approved)")
    return result


if __name__=="__main__":
    build()
