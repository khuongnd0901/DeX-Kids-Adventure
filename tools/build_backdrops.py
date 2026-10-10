#!/usr/bin/env python3
"""Compile editable, original POI SVGs to a bounded set of texture files."""
from pathlib import Path
import xml.etree.ElementTree as ET
import cairosvg

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "art/assets-source/backdrops"
DEST = ROOT / "assets/generated/backdrops"
RASTER_W, RASTER_H = 960, 520

def build() -> None:
    DEST.mkdir(parents=True, exist_ok=True)
    artwork = sorted(SRC.glob("*.svg"))
    if not artwork or len(artwork) > 40:
        raise ValueError("Expected 1–40 original POI backdrops")
    names = {p.stem for p in artwork}
    mapping = ROOT / "assets/poi/backdrop-map.tsv"
    for raw in mapping.read_text(encoding="utf-8").splitlines():
        if raw.startswith("#") or not raw.strip() or raw.startswith("poi_id\t"):
            continue
        poi_id, name = raw.split("\t")
        if name not in names:
            raise ValueError(f"Mapped POI {poi_id} has no artwork: {name}")
    for svg in artwork:
        root = ET.parse(svg).getroot()
        if root.attrib.get("viewBox") != "0 0 1200 650":
            raise ValueError(f"Invalid native aspect: {svg.name}")
        # Avoid executable SVG references or external fetches.
        content = svg.read_text(encoding="utf-8")
        if "<script" in content.lower() or "http://" in content.lower() or "xlink:href" in content.lower():
            raise ValueError(f"Disallowed external reference: {svg.name}")
        cairosvg.svg2png(bytestring=content.encode("utf-8"),
                         write_to=str(DEST / (svg.stem + ".png")),
                         output_width=RASTER_W, output_height=RASTER_H)
    print(f"Rasterized {len(artwork)} source-controlled POI background scenes")

if __name__ == "__main__":
    build()
