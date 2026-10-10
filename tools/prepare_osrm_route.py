#!/usr/bin/env python3
"""Operator-only: fetch a complete OSRM driving route for explicit lon,lat waypoints."""
import argparse
import hashlib
import json
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import quote, urlencode
from urllib.request import Request, urlopen

def parse_point(raw):
    lon,lat=map(float,raw.split(","))
    if not (102<=lon<=110 and 8<=lat<=24):raise ValueError("Expected lon,lat within Vietnam")
    return [lon,lat]

def make_url(points, endpoint):
    if endpoint not in ("https://router.project-osrm.org",):
        raise ValueError("Only the documented OSRM demo host is allowed")
    path=";".join(f"{lon:.7f},{lat:.7f}" for lon,lat in points)
    return endpoint+"/route/v1/driving/"+quote(path,safe=",;.") +"?"+urlencode(
        {"overview":"full","geometries":"geojson","steps":"false","alternatives":"false"})

def extract_geometry(payload):
    if payload.get("code")!="Ok" or not payload.get("routes"):
        raise ValueError("OSRM failed to return an available driving route")
    r=payload["routes"][0]
    g=r.get("geometry")
    if not isinstance(g,dict) or g.get("type")!="LineString" or len(g.get("coordinates",[]))<2:
        raise ValueError("OSRM full road geometry missing")
    from road_poi_pipeline import load_routes
    # Verify real-road detail before returning; never accept just endpoints.
    import tempfile
    with tempfile.TemporaryDirectory() as td:
        tmp=Path(td)/"route.geojson"
        tmp.write_text(json.dumps(g),encoding="utf-8")
        load_routes(tmp)
    return g,float(r["distance"])

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument("--point",action="append",required=True,
                   help="lon,lat; provide >=2 in actual road-trip order; use --point repeatedly")
    p.add_argument("--output",required=True,type=Path,help="Output GeoJSON under build/")
    args=p.parse_args()
    if len(args.point)<2 or len(args.point)>12:p.error("Need 2..12 exact road waypoints")
    try:
        points=[parse_point(x) for x in args.point]
        out=args.output.resolve().relative_to(Path.cwd().resolve())
        if "build" not in out.parts:p.error("--output must be under build/")
        request=Request(make_url(points,"https://router.project-osrm.org"),
                        headers={"User-Agent":"DeX-Kids-Adventure/0.1 (manual OSM route candidate research)",
                                 "Accept":"application/json"})
        with urlopen(request,timeout=45) as response:
            body=response.read(8*1024*1024+1)
        if len(body)>8*1024*1024:raise ValueError("OSRM response too large")
        g,distance=extract_geometry(json.loads(body))
        args.output.parent.mkdir(parents=True,exist_ok=True)
        args.output.write_text(json.dumps(g,separators=(",",":"))+"\n",encoding="utf-8")
        metadata={"status":"ROUTED_ESTIMATE_NOT_DRIVEN_TRACE",
                  "provider":"OSRM demo; OSM-derived road network",
                  "waypoints_lonlat":points,"distance_km":round(distance/1000,1),
                  "route_sha256":hashlib.sha256(args.output.read_bytes()).hexdigest(),
                  "license_notice":"© OpenStreetMap contributors; ODbL 1.0",
                  "warning":"Route choice may differ from real driver route. Not navigation, map matching or proof of passage."}
        args.output.with_suffix(".source.json").write_text(json.dumps(metadata,indent=2,ensure_ascii=False)+"\n")
        print(f"Saved {distance/1000:.1f} km road-network geometry; compare with actual driving route before filtering POIs.")
    except (ValueError,HTTPError,URLError,TimeoutError) as exc:
        p.error(str(exc))
if __name__=="__main__":main()
