#!/usr/bin/env python3
"""POI asset mapping, SVG syntax and composition regressions. Local-only; not a GPU test."""
from pathlib import Path
import xml.etree.ElementTree as ET
import re

root = Path(__file__).resolve().parents[1]
art = root / "art/assets-source/backdrops"
svgs = sorted(art.glob("*.svg"))
assert len(svgs) >= 20, f"Expected landmark scenery variety, got {len(svgs)}"
names = {p.stem for p in svgs}
for path in svgs:
    element = ET.parse(path).getroot()
    assert element.attrib["viewBox"] == "0 0 1200 650", path
    assert element.attrib["width"] == "1200", path
    assert element.attrib["height"] == "650", path
    assert len(list(element)) >= 10, f"Scene missing detail: {path}"
    content = path.read_text()
    assert "<script" not in content and "xlink:href" not in content, path

rows = {}
for line in (root / "assets/poi/backdrop-map.tsv").read_text().splitlines():
    if line.startswith("#") or not line.strip() or line.startswith("poi_id\t"):
        continue
    id, key = line.split("\t")
    assert re.fullmatch(r"osm:(?:node|way|relation):[0-9]+", id), id
    assert id not in rows, id
    assert key in names, (id, key)
    rows[id] = key
assert len(rows) >= 44, len(rows)
for filename in ("sample-hcm.tsv", "live-landmarks.tsv"):
    for line in (root / "assets/poi" / filename).read_text().splitlines():
        if line.startswith("#") or line.startswith("id\t") or not line.strip():
            continue
        assert line.split("\t")[0] in rows, line

layout = (root / "core/src/main/java/com/khuongnd/dexkids/game/VehicleLayout.java").read_text()
screen = (root / "core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java").read_text()
renderer = (root / "core/src/main/java/com/khuongnd/dexkids/game/PoiBackdropRenderer.java").read_text()
build = (root / "tools/build_sprites.py").read_text()
assert "VehicleLayout.WHEEL_RADIUS" in (root / "core/src/main/java/com/khuongnd/dexkids/game/VehicleMotionModel.java").read_text()
assert "backdropRenderer.draw(batch, themed);" in screen
assert "backdropRenderer.dispose();" in screen
assert "batch.draw(image,0,268,1920,1040)" in renderer  # preserve original 1200:650 aspect
assert "build_backdrops()" in build
for field in ("WIDTH=950f", "HEIGHT=417f", "WHEEL_RADIUS=66f"):
    assert field in layout
assert not list((root / ".github/workflows").glob("*.yml")), "No CI requested"
print(f"PASS: {len(svgs)} authored SVG backdrops cover {len(rows)} POIs, fixed aspect/wheels, local-only pipeline")
