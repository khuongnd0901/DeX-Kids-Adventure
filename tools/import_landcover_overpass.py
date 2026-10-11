#!/usr/bin/env python3
"""Offline, source-preserving OSM Overpass landcover -> route-filtered GeoWorld.

Input is the response from operator-downloaded Overpass 'way ... out geom;'
queries. No network, private GPS upload, remote calls or invented OSM IDs.
A complete road geometry is required; endpoints-only tracks are rejected.
"""
import argparse
import hashlib
import json
import math
import re
from pathlib import Path

from build_geo_world import CELL, MAX_CELLS, classify, extract
from road_poi_pipeline import (
    load_routes, query_chunks, road_index, near_segments, point_segment
)

TAGS = (
    'way["landuse"~"^(forest|farmland|residential|commercial|industrial|meadow)$"]',
    'way["natural"~"^(wood|scrub|water|beach|grassland)$"]',
    'way["leisure"="park"]',
    'way["waterway"="riverbank"]',
)
MAX_EXTRACT_BYTES = 30 * 1024 * 1024


def make_queries(tracks):
    """Reuse the validated road route splitting, not straight-line endpoints."""
    out = []
    for base in query_chunks(tracks, radius=350):
        match = re.search(r"\\(around:350,([0-9.,-]+)\\)", base)
        if match is None:
            raise ValueError("No road polyline found in bounded query")
        argument = match.group(1)
        out.append("[out:json][timeout:90];\n(\n  " +
                   "\n  ".join(tag + "(" + argument + ");" for tag in TAGS) +
                   "\n);\nout geom;\n")
    return out


def as_polygon(element):
    """Only complete closed OSM ways; multipolygon relations need proper assembly."""
    if not isinstance(element, dict) or element.get("type") != "way":
        return None
    ident = element.get("id")
    tags = element.get("tags")
    geometry = element.get("geometry")
    if not isinstance(ident, int) or ident <= 0 or not isinstance(tags, dict):
        return None
    if tags.get("access") in ("private", "no") or classify(tags) is None:
        return None
    if not isinstance(geometry, list) or len(geometry) < 4 or len(geometry) > 30000:
        return None
    try:
        points = [[float(x["lon"]), float(x["lat"])] for x in geometry]
    except (KeyError, TypeError, ValueError, OverflowError):
        return None
    if any(not math.isfinite(x) or not math.isfinite(y) or
           x < 102 or x > 110 or y < 8 or y > 24 for x, y in points):
        return None
    if points[0] != points[-1]:
        return None
    return {"type": "Feature",
            "properties": {"osm_type": "way", "osm_id": str(ident), "tags": tags},
            "geometry": {"type": "Polygon", "coordinates": [points]}}


def load_features(extract_dir):
    """Read bounded operator-owned JSON, skip invalid shapes and duplicate OSM IDs."""
    extracts = sorted(extract_dir.glob("chunk-*.json"))
    if not extracts:
        raise ValueError("No downloaded chunk-*.json Overpass extracts found")
    features = []
    seen = set()
    receipts = []
    for file in extracts:
        if file.stat().st_size > MAX_EXTRACT_BYTES:
            raise ValueError("Overpass JSON file exceeds 30 MiB: " + file.name)
        body = file.read_bytes()
        payload = json.loads(body)
        if not isinstance(payload, dict) or not isinstance(payload.get("elements"), list):
            raise ValueError("Not an Overpass JSON elements response: " + file.name)
        receipts.append({"file": file.name, "sha256": hashlib.sha256(body).hexdigest()})
        for raw in payload["elements"]:
            feature = as_polygon(raw)
            if feature is None:
                continue
            ident = feature["properties"]["osm_id"]
            if ident not in seen:
                seen.add(ident)
                features.append(feature)
    return features, receipts


def filter_route(cells, tracks, distance_m=350.0):
    """Keep only cells whose centers lie within 350 m of a *road polyline*."""
    if not 50 <= distance_m <= 1000:
        raise ValueError("Road-proximity threshold must be 50..1000 m")
    index, _ = road_index(tracks)
    result = {}
    for (row, col), value in cells.items():
        center = ((row + .5) * CELL, (col + .5) * CELL)
        if any(point_segment(center, a, b)[0] <= distance_m
               for a, b, _ in near_segments(index, center)):
            result[row, col] = value
    return result


def convert(route_file, extract_dir, output, proximity_m=350):
    tracks = load_routes(route_file)
    features, receipts = load_features(extract_dir)
    if not features:
        raise ValueError("No closed, tagged, sourced OSM way polygons found")
    candidates = extract({"type": "FeatureCollection", "features": features},
                         maximum=MAX_CELLS)
    cells = filter_route(candidates, tracks, proximity_m)
    if not cells:
        raise ValueError("No sourced landcover cells near this route; existing output kept")
    content = "# dexkids-geo-cells-v1\n" + (
        "# Coarse OSM illustrative landcover; not road matching or passage evidence\n"
    )
    content += "".join(
        f"{lat}\t{lon}\t{biome}\t{source}\n"
        for (lat, lon), (biome, source) in sorted(cells.items())
    )
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(content, encoding="utf-8")
    audit = {
        "status": "OSM_SOURCED_ROUTE_ESTIMATE_NOT_FIELD_VERIFIED",
        "route_sha256": hashlib.sha256(route_file.read_bytes()).hexdigest(),
        "extracts": receipts,
        "closed_sourced_polygons": len(features),
        "raster_candidates": len(candidates),
        "route_near_cells": len(cells),
        "proximity_m": proximity_m,
        "license": "© OpenStreetMap contributors, ODbL 1.0",
        "warning": "Rasterized scenery is illustrative; route and actual drive are unverified."
    }
    output.with_suffix(".audit.json").write_text(
        json.dumps(audit, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    return audit


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--route", type=Path, required=True, help="Detailed road GeoJSON or GPX")
    p.add_argument("--query-dir", type=Path, help="Create bounded Overpass query chunks")
    p.add_argument("--extract-dir", type=Path, help="Locally downloaded Overpass JSON chunks")
    p.add_argument("--output", type=Path, help="GeoWorld TSV path (normally under build/)")
    p.add_argument("--proximity-m", type=float, default=350)
    args = p.parse_args()
    if bool(args.query_dir) == bool(args.extract_dir):
        p.error("Supply exactly one of --query-dir or --extract-dir")
    try:
        tracks = load_routes(args.route)
        if args.query_dir:
            q = make_queries(tracks)
            if len(q) > 1000:
                raise ValueError("Too many public Overpass chunks; use a local OSM PBF extract")
            args.query_dir.mkdir(parents=True, exist_ok=True)
            if list(args.query_dir.glob("chunk-*.ql")):
                raise ValueError("Query directory already contains chunks; refuse overwrite")
            for i, content in enumerate(q):
                (args.query_dir / f"chunk-{i:04d}.ql").write_text(content, encoding="utf-8")
            print(f"Prepared {len(q)} source-only Overpass queries. Not downloaded.")
        else:
            if args.output is None:
                p.error("--output is required with --extract-dir")
            info = convert(args.route, args.extract_dir, args.output, args.proximity_m)
            print(f"Imported {info['route_near_cells']} sourced cells; field QA NOT VERIFIED")
    except (OSError, ValueError, OverflowError) as exc:
        p.error(str(exc))


if __name__ == "__main__":
    main()
