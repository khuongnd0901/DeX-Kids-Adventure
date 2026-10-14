#!/usr/bin/env python3
"""T-049 regression: new vector sources, deterministic grade, protected child art."""
from pathlib import Path
import hashlib
import xml.etree.ElementTree as ET

from PIL import Image, ImageDraw
from storybook_art import ROOT, SOURCE_ROOT, enabled, select_source, stylize

COMPANIONS = ("rabbit","fox","panda","penguin","cat")
VEHICLES = ("ambulance_cartoon","cable_car","camper_van",
            "double_decker_tour_bus","electric_city_bus","family_van",
            "farm_tractor","fire_engine","ice_cream_van",
            "mini_train","recycling_truck","sailboat_family")

def digest(p: Path) -> str:
    return hashlib.sha256(p.read_bytes()).hexdigest()

def main() -> None:
    assert enabled(), "New pack should be active on the T-049 branch"
    for animal in COMPANIONS:
        original = SOURCE_ROOT / "characters" / "companions" / (animal + ".svg")
        updated = select_source(original)
        assert original != updated and updated.is_file()
        root = ET.parse(updated).getroot()
        assert root.attrib["viewBox"] == "0 0 220 250"
        assert root.attrib["width"] == "220" and root.attrib["height"] == "250"
    from build_m10_atlas import parse_svg
    for vehicle in VEHICLES:
        src = SOURCE_ROOT / "m10" / "vehicles" / (vehicle + ".svg")
        updated = select_source(src)
        assert updated != src and updated.is_file()
        parse_svg(updated, 280, 160)
    for child in ("sau", "ong"):
        src = SOURCE_ROOT / "m10" / "characters" / child / (child + "_clap.svg")
        assert select_source(src) == src
        assert not (SOURCE_ROOT / "storybook-v2" / "m10" / "characters" / child).exists()
    original = Image.new("RGBA", (64, 64), (0,0,0,0))
    ImageDraw.Draw(original).ellipse((10,10,54,54), fill=(127,167,184,255))
    before = original.tobytes()
    converted = stylize(original, "sprite")
    assert converted.size == original.size
    assert converted.getchannel("A").tobytes() == original.getchannel("A").tobytes()
    assert converted.tobytes() != before
    assert stylize(original,"sprite").tobytes() == converted.tobytes()
    assert stylize(original, "protected").tobytes() == before
    assert original.tobytes() == before
    assert not (ROOT / "tools" / "build_t048_atlas.py").read_text().count("storybook_art")
    print("T-049 source/size/alpha/protection/determinism contracts PASS")

if __name__ == "__main__":
    main()
