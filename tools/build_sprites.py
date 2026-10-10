#!/usr/bin/env python3
"""Build deterministic LibGDX sprite atlas from original editable SVG artwork."""
from __future__ import annotations

import io
import base64
import hashlib
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
SIZE = (2048, 2048)  # single page, 16 MB RGBA max; release review required
PAD = 4
SOURCES = {
    "bus": SRC / "vehicles" / "bus.svg",
    "wheel": SRC / "vehicles" / "wheel.svg",
    "capybara_idle": SRC / "characters" / "capybara_idle.svg",
    "capybara_blink": SRC / "characters" / "capybara_blink.svg",
    "capybara_talk": SRC / "characters" / "capybara_talk.svg",
    "capybara_wave": SRC / "characters" / "capybara_wave.svg",
    "tree": SRC / "environment" / "tree.svg",
    "capybara_sleep": SRC / "characters" / "capybara_sleep.svg",
    "capybara_surprised": SRC / "characters" / "capybara_surprised.svg",
    "building": SRC / "environment" / "building.svg",
    "bridge": SRC / "environment" / "bridge.svg",
    "bush": SRC / "environment" / "bush.svg",
    "cloud": SRC / "environment" / "cloud.svg",
    "hills": SRC / "environment" / "hills.svg",
    "house": SRC / "environment" / "house.svg",
    "lamp": SRC / "environment" / "lamp.svg",
    "flower": SRC / "environment" / "flower.svg",
    "headlight_glow": SRC / "effects" / "headlight_glow.svg",
    "river_water": SRC / "environment" / "river_water.svg",
    "friend_rabbit": SRC / "characters/companions/rabbit.svg",
    "friend_fox": SRC / "characters/companions/fox.svg",
    "friend_panda": SRC / "characters/companions/panda.svg",
    "friend_cat": SRC / "characters/companions/cat.svg",
    "friend_penguin": SRC / "characters/companions/penguin.svg",
    "traffic_car": SRC / "vehicles/traffic_car.svg",
    "traffic_taxi": SRC / "vehicles/traffic_taxi.svg",
    "traffic_truck": SRC / "vehicles/traffic_truck.svg",
    "traffic_minibus": SRC / "vehicles/traffic_minibus.svg",
    "traffic_scooter": SRC / "vehicles/traffic_scooter.svg",
    "traffic_bicycle": SRC / "vehicles/traffic_bicycle.svg",
}

# Compact authored cartoon render of Sâu in explorer/firefighter/pilot/police
# costumes. Text-encoded chunks make the binary sheet reproducible with the
# existing UTF-8 GitHub connector; no original child photos are in this repo.
SAU_PARTS = [SRC / "characters" / f"sau-costumes.b64.part{i:02d}" for i in range(1, 6)]
SAU_SHA256 = "3cd623f54e37cf7c3d6e9e6a371f5dd280ba2f0d9df5c9575917125ac8427f6d"
SAU_SHEET_SIZE = (224, 336)  # quadrants 112x168, 4 outfits; ~150 KB GL texture


def build_sau_art() -> None:
    chunks = [p.read_text(encoding="ascii").strip() for p in SAU_PARTS]
    binary = base64.b64decode("".join(chunks), validate=True)
    if hashlib.sha256(binary).hexdigest() != SAU_SHA256:
        raise ValueError("Sâu sprite SHA256 mismatch — refusing broken or incomplete art")
    with Image.open(io.BytesIO(binary)) as sheet:
        if sheet.size != SAU_SHEET_SIZE:
            raise ValueError("Unexpected Sâu sprite sheet dimensions")
        rgba = sheet.convert("RGBA")
        if rgba.getpixel((0, 0))[3] != 0:
            raise ValueError("Sâu sprite should have a transparent backdrop")
        for i in range(4):
            x, y = i % 2 * 112, i // 2 * 168
            if not rgba.crop((x, y, x + 112, y + 168)).getbbox():
                raise ValueError(f"Empty Sâu costume {i}")
    dest = OUT.parent / "characters" / "sau-costumes.png"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_bytes(binary)


# A small, transparent illustration derived from the approved Ong chibi art.
# Only this low-resolution cartoon is versioned; no original family photographs.
ONG_SOURCE = SRC / "characters" / "ong-costumes.png"
ONG_SHA256 = "deef87a482965d5f5c8fe4c692e0891416869c43eb9c4170d47a24987d987bfd"
ONG_SHEET_SIZE = (192, 288)  # 4 quadrants, each 96x144


def build_ong_art() -> None:
    binary = ONG_SOURCE.read_bytes()
    if hashlib.sha256(binary).hexdigest() != ONG_SHA256:
        raise ValueError("Ong sprite SHA256 mismatch")
    with Image.open(io.BytesIO(binary)) as sheet:
        if sheet.size != ONG_SHEET_SIZE:
            raise ValueError("Unexpected Ong sprite dimensions")
        rgba = sheet.convert("RGBA")
        if rgba.getpixel((0, 0))[3] != 0:
            raise ValueError("Ong backdrop must be transparent")
        for i in range(4):
            x, y = (i % 2) * 96, (i // 2) * 144
            if not rgba.crop((x, y, x + 96, y + 144)).getbbox():
                raise ValueError(f"Empty Ong costume {i}")
    target = OUT.parent / "characters" / "ong-costumes.png"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(binary)


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
    build_sau_art()
    build_ong_art()
    print(f"Built {len(regions)} original sprites + 4 Sâu and 4 Ong costumes -> {OUT / 'kids.png'}")

if __name__ == "__main__":
    build()
