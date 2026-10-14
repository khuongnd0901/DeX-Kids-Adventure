#!/usr/bin/env python3
"""T-053: pack 96 pose-aware transparent child clothing layers into one atlas.

Reads original story/passenger PNGs for geometry and fabric shading only;
never modifies, redraws, hash-updates or overwrites protected original PNGs.
Runtime loads the output once, then keeps regions in memory for the session.
"""
from __future__ import annotations

from io import BytesIO
from pathlib import Path
import xml.etree.ElementTree as ET
import re

import cairosvg
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "art/assets-source/child-outfits"
OUT = ROOT / "assets/generated"
SIZE = (2048, 2048)  # one page; 16 MiB RGBA; pose-aware story and passenger overlays
FRAME = (256, 384)
PAD = 6
NAMES = (
    "sau_solar", "sau_tech", "sau_web", "sau_thunder", "sau_shield", "sau_shadow",
    "ong_rose", "ong_ocean", "ong_sky", "ong_forest", "ong_starlight", "ong_lavender",
)

def validate(raw: bytes, name: str) -> None:
    if len(raw) > 300_000:
        raise ValueError(f"Oversized costume SVG: {name}")
    lowered = raw.lower().replace(b'xmlns="http://www.w3.org/2000/svg"', b"")
    for unsafe in (b"<!doctype", b"<!entity", b"<script", b"<image",
                   b"<foreignobject", b"<use", b"href=", b"onload=",
                   b"http://", b"https://", b"data:"):
        if unsafe in lowered:
            raise ValueError(f"Unsafe costume SVG {name}: {unsafe!r}")
    # Only allow paint references to locally declared gradients (never external URLs).
    without_local_paint = re.sub(rb"url\(#[a-z0-9_-]{1,32}\)", b"", lowered)
    if b"url(" in without_local_paint:
        raise ValueError(f"External paint reference in {name}")
    root = ET.fromstring(raw)
    if root.tag.rsplit("}", 1)[-1] != "svg":
        raise ValueError(f"Not an SVG: {name}")
    if (root.attrib.get("width"), root.attrib.get("height"), root.attrib.get("viewBox")) != (
            "256", "384", "0 0 256 384"):
        raise ValueError(f"Invalid 256x384 costume frame: {name}")

def build() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    canvas = Image.new("RGBA", SIZE, (0, 0, 0, 0))
    for name in NAMES:
        file = SRC / (name + ".svg")
        data = file.read_bytes()
        validate(data, name)
        png = cairosvg.svg2png(bytestring=data, output_width=FRAME[0],
                               output_height=FRAME[1])
        image = Image.open(BytesIO(png)).convert("RGBA")
        alpha = image.getchannel("A")
        if not alpha.getbbox() or alpha.getpixel((0, 0)):
            raise ValueError(f"Empty or opaque costume: {name}")
        if alpha.crop((0, 0, 256, 1)).getbbox() or alpha.crop((0, 383, 256, 384)).getbbox():
            raise ValueError(f"Top/bottom clipped outfit: {name}")
    # Validate original SVG palette sources, then produce independent pose frames.
    from child_outfit_geometry import garment
    pose_regions = []
    tile = (128, 192)
    for child in ("sau", "ong"):
        for kind, suffix in (("story", "story-v2"), ("passenger", "passenger-v3")):
            sheet = Image.open(ROOT / "art/assets-source/characters" / f"{child}-{suffix}.png").convert("RGBA")
            for style, name in enumerate(n for n in NAMES if n.startswith(child + "_")):
                raw = (SRC / (name + ".svg")).read_bytes()
                for pose in range(4):
                    cell = sheet.crop((pose % 2 * 512, pose // 2 * 768,
                                       pose % 2 * 512 + 512, pose // 2 * 768 + 768))
                    overlay = garment(cell, raw, child, kind, pose, style)
                    if kind == "passenger":
                        # Same alpha>=128 bounds as CartoonSprites.loadPassengerSheet.
                        bounds = cell.getchannel("A").point(lambda a: 255 if a >= 128 else 0).getbbox()
                        overlay = overlay.resize((512,768), Image.Resampling.LANCZOS).crop(bounds)
                    overlay = overlay.resize(tile, Image.Resampling.LANCZOS)
                    i = len(pose_regions)
                    x = PAD + (i % 15) * (tile[0] + PAD)
                    y = PAD + (i // 15) * (tile[1] + PAD)
                    assert x + tile[0] <= SIZE[0] and y + tile[1] <= SIZE[1]
                    canvas.alpha_composite(overlay, (x,y))
                    # Passenger overlay has the same aspect ratio as its original crop;
                    # frame storage may be rectangular, runtime explicitly uses base geometry.
                    pose_regions.append((f"{name}_{kind}_{pose}", x, y, *tile))
    atlas = ["child-outfits.png", f"size: {SIZE[0]}, {SIZE[1]}", "format: RGBA8888",
             "filter: Linear, Linear", "repeat: none"]
    for name, x, y, width, height in pose_regions:
        atlas.extend([name, "  rotate: false", f"  xy: {x}, {y}",
                      f"  size: {width}, {height}", f"  orig: {width}, {height}",
                      "  offset: 0, 0", "  index: -1"])
    canvas.save(OUT / "child-outfits.png", optimize=True)
    (OUT / "child-outfits.atlas").write_text("\n".join(atlas) + "\n", encoding="utf-8")
    print(f"T-053 packed {len(pose_regions)} pose-aware child overlays on 1 atlas page")

if __name__ == "__main__":
    build()
