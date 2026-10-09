#!/usr/bin/env python3
"""Check source fixture conversion never generates auto-approved named places."""
from import_osm_pois import convert, HEADER
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
lines=convert((root/"test-data/poi/synthetic-overpass.json").read_bytes())
assert len(lines)==2 and all(line.endswith("\t\t") for line in lines)
assert any("\tBRIDGE\t" in x for x in lines)
assert any("\tPARK\t" in x for x in lines)
approved=(root/"assets/poi/reviewed.tsv").read_text(encoding="utf-8")
assert approved.startswith("# dexkids-poi-v1\n"+HEADER+"\n")
assert approved.count("\n")==2, "Production POIs changed without documented approval"
print("PASS: deterministic OSM candidate extraction, no auto-approved catalog records")
