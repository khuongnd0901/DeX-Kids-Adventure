#!/usr/bin/env python3
"""T-052: pack 12 transparent child clothing/accessory overlays into one atlas.

Uses only authored SVGs; does not read, modify, redraw, hash-update or overwrite
any of Sâu/Ong's original story-v2, passenger-v3 and costume PNG files.
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
SIZE = (2048, 1024)  # fixed single page; ~8 MiB RGBA on GPU
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
    regions = []
    cols = 6
    for index, name in enumerate(NAMES):
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
        col, row = index % cols, index // cols
        x, y = PAD + col * (FRAME[0] + PAD), PAD + row * (FRAME[1] + PAD)
        assert x + FRAME[0] <= SIZE[0] and y + FRAME[1] <= SIZE[1]
        canvas.alpha_composite(image, (x, y))
        regions.append((name, x, y))
    atlas = ["child-outfits.png",
             "size: 2048, 1024", "format: RGBA8888",
             "filter: Linear, Linear", "repeat: none"]
    for name, x, y in regions:
        atlas.extend([name, "  rotate: false", f"  xy: {x}, {y}",
                      "  size: 256, 384", "  orig: 256, 384",
                      "  offset: 0, 0", "  index: -1"])
    canvas.save(OUT / "child-outfits.png", optimize=True)
    (OUT / "child-outfits.atlas").write_text("\n".join(atlas) + "\n", encoding="utf-8")
    print(f"T-052 packed {len(regions)} independent child outfits on 1 atlas page")

if __name__ == "__main__":
    build()
