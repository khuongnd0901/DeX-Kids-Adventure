#!/usr/bin/env python3
"""T-052: enforce original child PNG byte integrity and 12 authored outfit overlays."""
from __future__ import annotations
from pathlib import Path
import sys
from io import BytesIO
import xml.etree.ElementTree as ET

from PIL import Image
import cairosvg

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
from build_child_outfits import SRC, NAMES, FRAME, SIZE, validate, build

assert len(NAMES) == len(set(NAMES)) == 12
assert len([n for n in NAMES if n.startswith("sau_")]) == 6
assert len([n for n in NAMES if n.startswith("ong_")]) == 6
for name in NAMES:
    source = SRC / (name + ".svg")
    raw = source.read_bytes()
    validate(raw, name)
    png = cairosvg.svg2png(bytestring=raw, output_width=256, output_height=384)
    with Image.open(BytesIO(png)) as im:
        im = im.convert("RGBA")
        assert im.size == FRAME
        alpha = im.getchannel("A")
        assert alpha.getbbox(), name
        assert alpha.getpixel((0, 0)) == 0, name
        assert alpha.crop((0, 0, 256, 1)).getbbox() is None, name
        assert alpha.crop((0, 383, 256, 384)).getbbox() is None, name
        assert alpha.crop((0, 0, 1, 384)).getbbox() is None, name
        assert alpha.crop((255, 0, 256, 384)).getbbox() is None, name
        # Whole outfit overlay is transparent outside the clothes and accessories.
        assert alpha.histogram()[0] > 18000, name

original = ROOT / "art/assets-source/characters"
assert (original / "sau-story-v2.png").is_file()
assert (original / "ong-story-v2.png").is_file()
assert (original / "sau-passenger-v3.png").is_file()
assert (original / "ong-passenger-v3.png").is_file()
builder = (ROOT / "tools/build_sprites.py").read_text(encoding="utf8")
runtime = (ROOT / "core/src/main/java/com/khuongnd/dexkids/game/CartoonSprites.java").read_text(encoding="utf8")
assert "build_child_outfits()" in builder
assert 'generated/child-outfits.atlas' in runtime
assert "outfitRotation.sau(elapsedSeconds)" in runtime
assert "outfitRotation.ong(elapsedSeconds)" in runtime
assert "childOutfitsAtlas.dispose()" in runtime
assert "sauStory[active && !guideActs() ? storyPose.ordinal()" in runtime
assert "ongStory[active && !guideActs() ? storyPose.ordinal()" in runtime
assert not list((ROOT / ".github/workflows").glob("*.yml"))
print("T-052: 12 costume SVGs, protection, alpha, atlas and runtime wiring PASS")
