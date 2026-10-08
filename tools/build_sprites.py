#!/usr/bin/env python3
"""Build deterministic LibGDX sprite atlas from original editable SVG artwork."""
from __future__ import annotations

import io
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

try:
    import cairosvg
    from PIL import Image
except ImportError as exc:
    raise SystemExit(
        "Missing art tools. Install cairosvg==2.8.2 Pillow==12.3.0 "
        "in a Python virtualenv before running Gradle."
    ) from exc

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "art" / "assets-source"
OUT = ROOT / "assets" / "generated"
SIZE = (1024, 1024)
PAD = 4
SOURCES = {
    "bus": SRC / "vehicles" / "bus.svg",
    "wheel": SRC / "vehicles" / "wheel.svg",
    "capybara_idle": SRC / "characters" / "capybara_idle.svg",
    "capybara_blink": SRC / "characters" / "capybara_blink.svg",
    "capybara_talk": SRC / "characters" / "capybara_talk.svg",
    "capybara_wave": SRC / "characters" / "capybara_wave.svg",
    "tree": SRC / "environment" / "tree.svg",
}

def build() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / "sprites").mkdir(parents=True, exist_ok=True)
    canvas = Image.new("RGBA", SIZE, (0, 0, 0, 0))
    x = PAD
    y = PAD
    row_height = 0
    regions = []
    for name, source in sorted(SOURCES.items()):
        if not source.is_file():
            raise FileNotFoundError(source)
        root = ET.parse(source).getroot()
        width = int(root.attrib["width"])
        height = int(root.attrib["height"])
        if width < 1 or height < 1 or width > 900 or height > 900:
            raise ValueError(f"Unsafe asset size: {name}")
        png = cairosvg.svg2png(url=str(source), output_width=width, output_height=height)
        sprite = Image.open(io.BytesIO(png)).convert("RGBA")
        if sprite.getbbox() is None:
            raise ValueError(f"Transparent-only sprite: {name}")
        if x + width + PAD > SIZE[0]:
            x = PAD
            y += row_height + PAD
            row_height = 0
        if y + height + PAD > SIZE[1]:
            raise ValueError(f"Atlas overflow: {name}")
        canvas.alpha_composite(sprite, (x, y))
        sprite.save(OUT / "sprites" / (name + ".png"), optimize=True)
        regions.append((name, x, y, width, height))
        x += width + PAD
        row_height = max(row_height, height)
    canvas.save(OUT / "kids.png", optimize=True)
    text = [
        "kids.png",
        f"size: {SIZE[0]}, {SIZE[1]}",
        "format: RGBA8888",
        "filter: Linear, Linear",
        "repeat: none",
    ]
    for name, px, py, width, height in regions:
        text.extend([
            name,
            "  rotate: false",
            f"  xy: {px}, {py}",
            f"  size: {width}, {height}",
            f"  orig: {width}, {height}",
            "  offset: 0, 0",
            "  index: -1",
        ])
    (OUT / "kids.atlas").write_text("\n".join(text) + "\n", encoding="utf-8")
    print(f"Built {len(regions)} original sprites -> {OUT / 'kids.png'}")

if __name__ == "__main__":
    build()
