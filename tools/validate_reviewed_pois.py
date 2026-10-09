#!/usr/bin/env python3
"""Fail release/CI on any mismatched OSM review-ledger or license attribution."""
import datetime as dt
import json
from pathlib import Path
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / "assets/poi/reviewed.tsv"
LEDGER = ROOT / "assets/poi/review-ledger.json"
NOTICE = ROOT / "assets/poi/NOTICE.txt"
HEADER = "id\tname\tlat\tlon\ttype\tosm_url\tverified_at\treviewer"
TYPES = {"PARK", "BRIDGE", "RIVER", "LANDMARK", "MUSEUM", "NATURE"}
def validate():
    payload=PATH.read_bytes()
    assert len(payload)<=512*1024
    rows=payload.decode("utf-8").splitlines()
    assert rows[:2]==["# dexkids-poi-v1", HEADER]
    ledger=json.loads(LEDGER.read_text(encoding="utf-8"))
    assert ledger.get("schema")=="dexkids-osm-review-v1"
    records=ledger.get("reviewed")
    assert isinstance(records,list)
    by_id={r["id"]:r for r in records}
    assert len(by_id)==len(records)
    active=set()
    for raw in rows[2:]:
        if not raw: continue
        p=raw.split("\t")
        assert len(p)==8
        ident,name,lat,lon,kind,link,when,reviewer=p
        bits=ident.split(":")
        assert len(bits)==3 and bits[0]=="osm" and bits[1] in ("node","way","relation")
        assert bits[2].isdigit() and int(bits[2])>0 and ident not in active
        assert name.strip()==name and 2<=len(name)<=90 and not any(ord(c)<32 for c in name)
        assert 8<=float(lat)<=24 and 102<=float(lon)<=110
        assert kind in TYPES
        assert link==f"https://www.openstreetmap.org/{bits[1]}/{bits[2]}"
        timestamp=dt.datetime.fromisoformat(when.replace("Z","+00:00"))
        assert timestamp.tzinfo is not None and timestamp <= dt.datetime.now(dt.timezone.utc)
        assert reviewer and reviewer.replace("_","").replace("-","").replace(".","").isalnum()
        approval=by_id.get(ident)
        assert approval is not None and approval.get("reviewer")==reviewer
        assert approval.get("osm_url")==link and approval.get("verified_at")==when
        active.add(ident)
    assert active==set(by_id), "Review ledger and shipped data must have exactly same IDs"
    assert len(active)<=5000
    license_text=NOTICE.read_text(encoding="utf-8")
    assert "OpenStreetMap contributors" in license_text
    assert "ODbL" in license_text and "https://www.openstreetmap.org/copyright" in license_text
    print(f"PASS: {len(active)} strictly reviewed OSM records, provenance ledger and ODbL notice")
if __name__=="__main__":
    validate()
