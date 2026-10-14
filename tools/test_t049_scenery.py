#!/usr/bin/env python3
"""T-049 QA for individually authored Storybook V2 non-child scenery.

This is a SOURCE/raster check; emulator visual acceptance remains separate.
"""
from __future__ import annotations

from io import BytesIO
from pathlib import Path
import xml.etree.ElementTree as ET

from PIL import Image
import cairosvg
from storybook_art import SOURCE_ROOT, select_source, enabled
from build_m10_atlas import parse_svg

ENV = ("tree", "bush", "cloud", "flower", "house", "building",
       "lamp", "bridge", "hills", "river_water")
EXTRAS = ("banana_tree", "bird", "buffalo", "butterfly", "duck", "flower_bed",
          "kite", "market_stall", "mountain_rocks", "palm", "pine",
          "rice_field", "road_sign", "street_tree", "tea_bush",
          "traffic_light", "water_boat", "water_lily")
M10 = {
    "urban": ("apartment_corner", "bakery_front", "bus_shelter",
              "fruit_shop", "library_front", "mailbox_blue",
              "pedestrian_island", "toy_store"),
    "village": ("bamboo_grove", "farm_pump", "haystack_round",
                "poultry_house", "scarecrow_smile", "tile_roof_home",
                "village_gate", "wooden_fence"),
    "forest": ("fern_cluster", "forest_rock_pair", "hollow_tree",
               "moss_log", "mushroom_brown", "mushroom_red",
               "oak_fictional", "vine_arch"),
    "farm": ("irrigation_channel", "ox_cart", "rice_bundle",
             "rice_gold_stage", "rice_green_stage", "sunflower_row",
             "tractor_field", "waterwheel_small"),
}
ENV_SIZE = {
    "tree": (200, 260), "bush": (190, 116),
    "cloud": (300, 145), "flower": (125, 120),
    "house": (275, 275), "building": (230, 340),
    "lamp": (115, 300), "bridge": (440, 240),
    "hills": (640, 260), "river_water": (620, 120),
}

def specs():
    for name in ENV:
        yield "environment/" + name + ".svg", ENV_SIZE[name]
    for name in EXTRAS:
        yield "props/" + name + ".svg", (180, 160)
    for family, names in M10.items():
        for name in names:
            yield "m10/props/" + family + "/" + name + ".svg", (160, 160)

def main():
    assert enabled(), "T-049 Storybook V2 mode required"
    all_specs = list(specs())
    assert len(all_specs) == 60 and len({path for path, _ in all_specs}) == 60
    for relative, (width, height) in all_specs:
        original = SOURCE_ROOT / relative
        replacement = select_source(original)
        assert original.is_file(), f"Missing original: {relative}"
        assert replacement.is_file() and replacement != original, relative
        assert replacement.parent != original.parent
        raw = replacement.read_bytes()
        low = raw.lower().replace(b'xmlns="http://www.w3.org/2000/svg"', b"")
        assert b"<!doctype" not in low and b"<!entity" not in low
        assert b"<script" not in low and b"<image" not in low
        assert b"http://" not in low and b"https://" not in low
        assert b"url(" not in low and b"href=" not in low
        root = ET.fromstring(raw)
        assert root.attrib.get("width") == str(width), relative
        assert root.attrib.get("height") == str(height), relative
        assert root.attrib.get("viewBox") == f"0 0 {width} {height}", relative
        if relative.startswith("m10/"):
            assert parse_svg(replacement, width, height) == raw
        raster = cairosvg.svg2png(bytestring=raw,
                                  output_width=width, output_height=height)
        bitmap = Image.open(BytesIO(raster)).convert("RGBA")
        alpha = bitmap.getchannel("A")
        assert bitmap.size == (width, height) and alpha.getbbox(), relative
        assert alpha.getpixel((0, 0)) == 0, f"Source has opaque corner: {relative}"
        # M10 atlas packs the alpha-cropped region. A visible border on the
        # outermost pixel indicates cropped/clipped illustration source.
        if relative.startswith("m10/"):
            assert alpha.crop((0, 0, width, 1)).getbbox() is None, relative
            assert alpha.crop((0, height-1, width, height)).getbbox() is None, relative
            assert alpha.crop((0, 0, 1, height)).getbbox() is None, relative
            assert alpha.crop((width-1, 0, width, height)).getbbox() is None, relative
    for protected in ("sau", "ong"):
        assert not (SOURCE_ROOT / "storybook-v2/m10/characters" / protected).exists()
        assert not (SOURCE_ROOT / "storybook-v2/characters" / protected).exists()
    print(f"T-049 Storybook scenery source/raster/alpha contracts PASS: {len(all_specs)} assets")

if __name__ == "__main__":
    main()
