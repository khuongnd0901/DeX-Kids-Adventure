#!/usr/bin/env python3
"""Offline OSM polygon raster QA (no web/device/Android)."""
import importlib.util
from pathlib import Path

src=Path(__file__).resolve().parent/"build_geo_world.py"
spec=importlib.util.spec_from_file_location("build_geo_world",src)
mod=importlib.util.module_from_spec(spec);spec.loader.exec_module(mod)
def feature(rings, tags, osm_id="42", kind="way"):
    return {"type":"Feature","properties":{"tags":tags,"osm_id":osm_id,"osm_type":kind},
            "geometry":{"type":"Polygon","coordinates":rings}}
# One cell around (106.82,10.95) center; actual polygon boundaries contain the center.
cx=(int(106.82/mod.CELL)+.5)*mod.CELL
cy=(int(10.95/mod.CELL)+.5)*mod.CELL
delta=mod.CELL*.35
outer=[[cx-delta,cy-delta],[cx+delta,cy-delta],[cx+delta,cy+delta],
       [cx-delta,cy+delta],[cx-delta,cy-delta]]
result=mod.extract({"type":"FeatureCollection","features":[feature([outer],{"landuse":"farmland"})]})
assert len(result)==1 and next(iter(result.values()))==("COUNTRYSIDE","osm:way:42")
# No OSM ID / unrecognized tag must never produce locally asserted landcover.
assert not mod.extract({"type":"FeatureCollection","features":[feature([outer],{"landuse":"farmland"},osm_id="fake")]})
assert not mod.extract({"type":"FeatureCollection","features":[feature([outer],{"shop":"shoes"})]})
assert mod.classify({"natural":"beach"})=="RIVER"
assert mod.classify({"natural":"wood"})=="PARK"
assert mod.classify({"landuse":"residential"})=="RESIDENTIAL"
print("PASS: T-033 offline geometry center-in-polygon, provenance and biome classification")
