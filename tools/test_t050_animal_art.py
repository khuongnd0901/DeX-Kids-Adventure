#!/usr/bin/env python3
"""T-050 detailed three-animal art contracts (no emulator/real-device claims).

The sprites are hand-authored vectors. Check transparency, size, scene bounds,
and stable source selection instead of claiming aesthetic approval.
"""
from __future__ import annotations
from io import BytesIO
import xml.etree.ElementTree as ET
import cairosvg
from PIL import Image
from storybook_art import SOURCE_ROOT, select_source, enabled

DETAIL_PATHS = {
    "buffalo": 25,  # anatomy: legs, horns, neck folds, muzzle/nostril, hooves
    "bird": 20,     # sparrow: separate tail/wing feathers, bill, claws, perch
    "duck": 17,     # domestic duck: wing feathers, neck, flat bill, webbed feet
}
NS = {"svg": "http://www.w3.org/2000/svg"}

def test() -> None:
    assert enabled(), "T-050 check expects assets/art-style.txt=storybook-v2"
    for name, min_paths in DETAIL_PATHS.items():
        original = SOURCE_ROOT / "props" / f"{name}.svg"
        override = select_source(original)
        assert override.is_file() and override != original, name
        assert override == SOURCE_ROOT / "storybook-v2/props" / f"{name}.svg"
        data = override.read_bytes()
        lowered = data.lower().replace(b'xmlns="http://www.w3.org/2000/svg"', b"")
        for unsafe in (b"<!doctype", b"<!entity", b"<script", b"<image", b"url(",
                       b"xlink:href", b"http://", b"https://"):
            assert unsafe not in lowered, f"{name} contains {unsafe!r}"
        root = ET.fromstring(data)
        assert root.attrib.get("viewBox") == "0 0 180 160"
        assert root.attrib.get("width") == "180"
        assert root.attrib.get("height") == "160"
        assert len(root.findall(".//svg:path", NS)) >= min_paths, name
        bitmap = Image.open(BytesIO(cairosvg.svg2png(
            bytestring=data, output_width=180, output_height=160))).convert("RGBA")
        alpha = bitmap.getchannel("A")
        bbox = alpha.getbbox()
        assert bbox, name
        assert bbox[0] >= 2 and bbox[1] >= 2, (name, bbox)
        assert bbox[2] <= 178 and bbox[3] <= 158, (name, bbox)
        for edge in ((0, 0, 180, 1), (0, 159, 180, 160),
                     (0, 0, 1, 160), (179, 0, 180, 160)):
            assert alpha.crop(edge).getbbox() is None, (name, edge)
        # Strong transparency guarantees: no opaque matte or frame rectangle.
        transparent_fraction = alpha.histogram()[0] / (180 * 160)
        assert transparent_fraction >= .35, (name, transparent_fraction)
        print(f"T-050 {name}: raster 180x160 alpha bbox={bbox} OK")
    for name in ("sau", "ong"):
        assert not (SOURCE_ROOT / "storybook-v2/props" / f"{name}.svg").exists()
    print("T-050 animal art: 3 source/raster/alpha contracts PASS")

if __name__ == "__main__":
    test()
