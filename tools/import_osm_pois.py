#!/usr/bin/env python3
"""Offline ONLY: transform a saved Overpass JSON extract into UNREVIEWED candidate TSV.
Never publishes or auto-approves POIs. Requires manual verification and reviewer ID.
No network requests are made by this script or by the Android app.
"""
import argparse, datetime as dt, hashlib, json, pathlib, re, sys
HEADER = "id\tname\tlat\tlon\ttype\tosm_url\tverified_at\treviewer"
def classify(tags):
    if tags.get("leisure") == "park": return "PARK"
    if tags.get("bridge") in ("yes", "viaduct") and "highway" in tags: return "BRIDGE"
    if tags.get("waterway") == "river": return "RIVER"
    if tags.get("tourism") == "museum": return "MUSEUM"
    if tags.get("tourism") == "attraction" or tags.get("historic") in ("monument", "memorial"): return "LANDMARK"
    if tags.get("natural") in ("wood", "water") or tags.get("landuse") == "forest": return "NATURE"
    return None

def convert(blob):
    parsed = json.loads(blob)
    if not isinstance(parsed, dict) or not isinstance(parsed.get("elements"), list):
        raise ValueError("Not an Overpass JSON extract")
    if len(parsed["elements"]) > 200_000: raise ValueError("Extract too large")
    items = {}
    for raw in parsed["elements"]:
        if not isinstance(raw, dict) or raw.get("type") not in ("node","way","relation"): continue
        typ, ident = raw["type"], raw.get("id")
        if not isinstance(ident,int) or ident<=0: continue
        tags = raw.get("tags") or {}
        if not isinstance(tags,dict): continue
        name = tags.get("name:vi") or tags.get("name")
        category = classify(tags)
        if not isinstance(name,str) or len(name.strip())<2 or len(name)>90 or category is None: continue
        name = name.strip()
        if any(ch in name for ch in ("\t","\r","\n")): continue
        loc = raw if typ=="node" else raw.get("center")
        if not isinstance(loc,dict): continue
        lat, lon = loc.get("lat"), loc.get("lon")
        if not isinstance(lat,(int,float)) or not isinstance(lon,(int,float)): continue
        if not 8 <= lat <= 24 or not 102 <= lon <= 110: continue # Vietnam bounding box; review against source
        oid = f"osm:{typ}:{ident}"
        # The two trailing empty fields are INTENTIONAL: a mapper timestamp
        # does not establish manual verification or human reviewer approval.
        items[oid] = f"{oid}\t{name}\t{lat:.7f}\t{lon:.7f}\t{category}\thttps://www.openstreetmap.org/{typ}/{ident}\t\t"
    return [items[k] for k in sorted(items)]

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--input",required=True,type=pathlib.Path,help="Previously downloaded Overpass JSON")
    parser.add_argument("--output",required=True,type=pathlib.Path,help="UNREVIEWED TSV under build/ (never auto-shipped)")
    a=parser.parse_args()
    if not str(a.output.resolve()).startswith(str(pathlib.Path("build").resolve())+"/"):
        parser.error("--output must be within repository build/; never overwrite approved assets")
    blob=a.input.read_bytes()
    if len(blob)>12*1024*1024: parser.error("Input larger than 12 MiB")
    lines=convert(blob)
    a.output.parent.mkdir(parents=True,exist_ok=True)
    a.output.write_text("# dexkids-poi-v1\n"+HEADER+"\n"+"\n".join(lines)+("\n" if lines else ""),encoding="utf-8")
    receipt={"status":"CANDIDATES_NOT_APPROVED","input_sha256":hashlib.sha256(blob).hexdigest(),
       "converted_utc":dt.datetime.now(dt.timezone.utc).isoformat(),
       "source":"OpenStreetMap contributors, Overpass extract (user supplied)",
       "license":"ODbL-1.0","attribution":"https://www.openstreetmap.org/copyright",
       "candidate_count":len(lines),"input_path":str(a.input)}
    a.output.with_suffix(".provenance.json").write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(f"Prepared {len(lines)} UNREVIEWED candidates. Must independently check OSM IDs/geometry and enter verified_at + reviewer before app can use them.")
if __name__=="__main__": main()
