#!/usr/bin/env python3
"""Render reviewable T-049 scenery source vs Storybook V2 contact sheets.

Output is only a local QA preview, not a Samsung DeX runtime screenshot.
"""
from __future__ import annotations
import argparse
from io import BytesIO
from pathlib import Path

import cairosvg
from PIL import Image, ImageDraw, ImageFont

from storybook_art import SOURCE_ROOT, select_source, stylize
from test_t049_scenery import specs

BACKGROUND = (247, 244, 237)
INK = (75, 71, 65)
PREVIEW_W, PREVIEW_H = 280, 228

def render(path: Path, w: int, h: int, styled: bool) -> Image.Image:
    data = cairosvg.svg2png(url=str(path), output_width=w, output_height=h)
    bitmap = Image.open(BytesIO(data)).convert("RGBA")
    if styled:
        bitmap = stylize(bitmap)
    bitmap.thumbnail((PREVIEW_W - 16, PREVIEW_H - 24), Image.Resampling.LANCZOS)
    return bitmap

def build(output_dir: Path):
    output_dir.mkdir(parents=True, exist_ok=True)
    families = {}
    for relative, dims in specs():
        category = relative.split("/")[0]
        if category == "m10":
            category = "m10-" + relative.split("/")[2]
        families.setdefault(category, []).append((relative, dims))
    for category, entries in sorted(families.items()):
        row_h, header, margin = 258, 45, 18
        result = Image.new("RGB", (780, header + row_h*len(entries)), BACKGROUND)
        d = ImageDraw.Draw(result)
        d.text((20, 15), "T-049 " + category + " | ORIGINAL / STORYBOOK V2", fill=INK)
        for index, (rel, (width, height)) in enumerate(entries):
            y = header + row_h*index
            d.rounded_rectangle((8, y+4, 771, y+row_h-3),
                                radius=11, fill=(255, 255, 255))
            d.text((22, y+17), rel.split("/")[-1], fill=INK)
            original = SOURCE_ROOT / rel
            for left, path, revised in ((115, original, False),
                                        (455, select_source(original), True)):
                bitmap = render(path, width, height, revised)
                x = left + (PREVIEW_W - bitmap.width)//2
                top = y+33+(PREVIEW_H-bitmap.height)//2
                result.paste(bitmap, (x, top), bitmap)
            d.line((390, y+34, 390, y+row_h-13), fill=(226, 221, 210), width=2)
        file = output_dir / (category + ".png")
        result.save(file, optimize=True)
        print(f"Rendered {len(entries)} comparison rows -> {file}")
    return sum(len(entries) for entries in families.values())

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("output_dir", type=Path)
    args = parser.parse_args()
    count = build(args.output_dir)
    print(f"T-049 scenery contact sheets: {count} source assets, review only")
