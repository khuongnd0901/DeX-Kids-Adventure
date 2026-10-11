#!/usr/bin/env python3
"""Build conservative offline 0.002° OSM landcover cells from *locally downloaded* GeoJSON.

No network, no child GPS upload, no route inference. Only polygon centers inside
actual OSM geometry are admitted. This is a stylized visual hint, NOT map matching.
"""
import argparse
import json
import math
from pathlib import Path

CELL = .002
MAX_CELLS = 100000
# Reuse existing authored scenery families; do not claim photoreal geography.
TAGS = {
    ("natural", "wood"): "PARK",
    ("landuse", "forest"): "PARK",
    ("natural", "scrub"): "COUNTRYSIDE",
    ("natural", "beach"): "RIVER",
    ("natural", "water"): "RIVER",
    ("waterway", "riverbank"): "RIVER",
    ("landuse", "farmland"): "COUNTRYSIDE",
    ("landuse", "meadow"): "COUNTRYSIDE",
    ("natural", "grassland"): "COUNTRYSIDE",
    ("leisure", "park"): "PARK",
    ("landuse", "residential"): "RESIDENTIAL",
    ("landuse", "commercial"): "URBAN",
    ("landuse", "industrial"): "URBAN",
}

def inside_ring(x, y, ring):
    inside = False
    for p, q in zip(ring, ring[1:] + ring[:1]):
        x1, y1 = p
        x2, y2 = q
        if (y1 > y) != (y2 > y):
            at = x1 + (y - y1) * (x2 - x1) / (y2 - y1)
            if x < at:
                inside = not inside
    return inside

def inside_polygon(x, y, polygon):
    return inside_ring(x, y, polygon[0]) and not any(
        inside_ring(x, y, hole) for hole in polygon[1:]
    )

def classify(properties):
    tags = properties.get("tags", properties)
    if not isinstance(tags, dict):
        return None
    for pair, biome in TAGS.items():
        if tags.get(pair[0]) == pair[1]:
            return biome
    return None

def source(properties):
    kind = str(properties.get("osm_type", properties.get("type", ""))).lower()
    ident = str(properties.get("osm_id", properties.get("id", "")))
    if kind in ("way", "relation") and ident.isascii() and ident.isdecimal() and int(ident) > 0 and len(ident) <= 16:
        return f"osm:{kind}:{ident}"
    return None

def extract(data, maximum=MAX_CELLS):
    if data.get("type") != "FeatureCollection" or not isinstance(data.get("features"), list):
        raise ValueError("GeoJSON FeatureCollection required")
    result = {}
    for feature in data["features"]:
        if not isinstance(feature, dict) or feature.get("type") != "Feature":
            continue
        props = feature.get("properties")
        shape = feature.get("geometry")
        if not isinstance(props, dict) or not isinstance(shape, dict):
            continue
        biome, origin = classify(props), source(props)
        if not biome or not origin:
            continue
        kind = shape.get("type")
        if kind not in ("Polygon", "MultiPolygon"):
            continue
        polygons = [shape.get("coordinates")] if kind == "Polygon" else shape.get("coordinates")
        if not isinstance(polygons, list):
            continue
        for polygon in polygons:
            if not isinstance(polygon, list) or not polygon:
                continue
            try:
                points = [(float(x), float(y)) for ring in polygon for x,y in ring]
                if len(points) > 30000 or len(points) < 3 or not all(
                    math.isfinite(x) and math.isfinite(y) and -180<=x<=180 and -90<=y<=90
                    for x,y in points
                ):
                    continue
                xmin, xmax = min(p[0] for p in points), max(p[0] for p in points)
                ymin, ymax = min(p[1] for p in points), max(p[1] for p in points)
                cols = range(math.floor(xmin/CELL), math.floor(xmax/CELL)+1)
                rows = range(math.floor(ymin/CELL), math.floor(ymax/CELL)+1)
                if len(cols) * len(rows) > maximum:
                    # Do not silently accept partial (oversized) geographic coverage.
                    raise OverflowError("Single OSM polygon exceeds cell budget")
                for row in rows:
                    y=(row+.5)*CELL
                    for col in cols:
                        x=(col+.5)*CELL
                        if inside_polygon(x,y,polygon):
                            result.setdefault((row,col),(biome,origin))
                            if len(result)>maximum:
                                raise OverflowError("Geo world exceeds maximum offline size")
            except (ValueError, TypeError):
                # Malformed source polygon is ignored; no guessed points are emitted.
                continue
    return result

def build(input_path, output_path):
    with input_path.open(encoding="utf-8") as stream:
        data=json.load(stream)
    rows=extract(data)
    output_path.parent.mkdir(parents=True,exist_ok=True)
    with output_path.open("w",encoding="utf-8",newline="\n") as stream:
        stream.write("# dexkids-geo-cells-v1\n")
        stream.write("# OSM raster: latitude_cell TAB longitude_cell TAB biome TAB source\n")
        for (lat,lon),(biome,origin) in sorted(rows.items()):
            stream.write(f"{lat}\t{lon}\t{biome}\t{origin}\n")
    return len(rows)

if __name__ == "__main__":
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument("--input",required=True,type=Path,help="Offline OSM polygon GeoJSON")
    p.add_argument("--output",required=True,type=Path,help="Generated offline cell TSV")
    args=p.parse_args()
    print(f"GeoWorld: {build(args.input,args.output)} sourced cells; manual route QA still required")
