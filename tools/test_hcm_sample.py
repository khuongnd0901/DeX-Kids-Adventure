#!/usr/bin/env python3
"""Evidence and contract tests for 4 OSM-derived preview-only HCMC landmarks."""
from pathlib import Path
import csv, json, io, xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
text=(root/"assets/poi/sample-hcm.tsv").read_text(encoding="utf-8")
rows=list(csv.DictReader(io.StringIO("\n".join(text.splitlines()[1:])),delimiter="\t"))
assert len(rows)==4
proof=json.loads((root/"assets/poi/sample-hcm-source-audit.json").read_text(encoding="utf-8"))
assert proof["status"]=="SOURCE_CROSS_CHECKED_NOT_HUMAN_APPROVED"
assert proof["osmApiDirectVerification"] is False
assert {row["id"] for row in rows}=={entry["id"] for entry in proof["data"]}
for row in rows:
    assert row["osm_url"].startswith("https://www.openstreetmap.org/way/")
    assert row["name"] and row["reviewer"]=="source-audit-20261009"
content=(root/"assets/narration/sample-hcm.tsv").read_text(encoding="utf-8")
cues=list(csv.DictReader(io.StringIO("\n".join(content.splitlines()[1:])),delimiter="\t"))
assert len(cues)==len(rows)
assert {cue["poi_id"] for cue in cues}=={row["id"] for row in rows}
assert all(cue["fact_url"].startswith("https://") and 2<=int(cue["min_age"])<=int(cue["max_age"])<=6 for cue in cues)
assert len(ET.parse(root/"test-data/gps/synthetic-hcm-poi-loop.gpx").findall(".//{*}trkpt"))==16
prod=(root/"assets/poi/reviewed.tsv").read_text(encoding="utf-8")
assert len(prod.strip().splitlines())==2, "Do not auto-upgrade source-cross-checked sample to human-approved production catalog"
print("PASS: 4 source-cross-checked sample-only POIs, 4 attributed cues and 16-point synthetic GPX")
