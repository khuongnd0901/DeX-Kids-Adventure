#!/usr/bin/env python3
"""Offline corridor library static editorial/packaging contract; no GPS or route claims."""
from pathlib import Path
from urllib.parse import urlparse
import csv, io
base = Path(__file__).resolve().parents[1]
asset = base / "assets/routes/knowledge.tsv"
text = asset.read_text(encoding="utf-8")
assert text.startswith("# dexkids-route-knowledge-v1\n")
rows = list(csv.DictReader(io.StringIO("\n".join(text.splitlines()[1:])), delimiter="\t"))
routes = {
    "dong-nai-vung-tau", "dong-nai-phan-thiet", "dong-nai-bao-loc",
    "dong-nai-da-lat", "dong-nai-nha-trang",
}
assert len(rows) == 35, f"Expected 35 source-bound cards, got {len(rows)}"
assert {r["route_id"] for r in rows} == routes
assert len({r["id"] for r in rows}) == len(rows)
topics = {"dia-ly", "thien-nhien", "van-hoa", "khoa-hoc", "moi-truong"}
for route in routes:
    part = [r for r in rows if r["route_id"] == route]
    assert len(part) == 7, (route, len(part))
    assert len({r["topic"] for r in part}) >= 3
for row in rows:
    assert row["topic"] in topics
    assert row["title"].strip() and row["text_vi"].strip()
    assert 8 <= len(row["text_vi"]) <= 220
    assert 2 <= int(row["min_age"]) <= int(row["max_age"]) <= 6
    assert "đang đi qua" not in row["text_vi"].lower()
    assert "vừa đi qua" not in row["text_vi"].lower()
    assert "chúng ta đang ở" not in row["text_vi"].lower()
    url = urlparse(row["fact_url"])
    assert url.scheme == "https" and url.netloc and not url.username
# No source-cross-checked content is auto-promoted into human-reviewed OSM GPS pack.
approved = (base / "assets/poi/reviewed.tsv").read_text(encoding="utf-8")
assert len(approved.strip().splitlines()) == 2
android = (base / "android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt").read_text(encoding="utf-8")
assert 'EXTRA_ROUTE_ID' in android and 'KHÔNG ĐỊNH VỊ' in android
assert 'settings.allowOfflineSpeech && !settings.quiet' in android
assert 'routeNextAtElapsed' in android
print("PASS: 5 offline knowledge corridors, 35 diverse sourced previews, no promoted GPS POIs")
