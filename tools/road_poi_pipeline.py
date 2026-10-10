#!/usr/bin/env python3
"""Route-aware, OFFLINE OSM candidate builder. No network or automatic app publishing.

Step 1: export Overpass QL chunks from an actual detailed road GPX/GeoJSON.
Step 2: download each chunk externally into --extract-dir, then build candidates.
Output is under build/ only and has EMPTY approval fields.
"""
import argparse
import csv
import hashlib
import json
import math
import re
import unicodedata
import xml.etree.ElementTree as ET
from collections import Counter, defaultdict
from pathlib import Path

R = 6371008.8
HEADER = "id\tname\tlat\tlon\ttype\tosm_url\tverified_at\treviewer"
TAGS = [
    'nw["leisure"~"^(park|playground|garden|nature_reserve)$"]["name"]',
    'nw["tourism"~"^(attraction|museum|viewpoint|zoo|aquarium)$"]["name"]',
    'nw["historic"~"^(monument|memorial|ruins|archaeological_site)$"]["name"]',
    'nw["natural"~"^(peak|rock|waterfall|beach|water|wood|spring)$"]["name"]',
    'nw["amenity"~"^(library|arts_centre|planetarium)$"]["name"]',
    'nw["waterway"~"^(river|stream)$"]["name"]',
    'way["bridge"="yes"]["name"]',
]

def meters(a, b):
    y1, x1 = map(math.radians, a)
    y2, x2 = map(math.radians, b)
    h = math.sin((y2-y1)/2)**2 + math.cos(y1)*math.cos(y2)*math.sin((x2-x1)/2)**2
    return 2*R*math.asin(min(1, math.sqrt(h)))

def valid_point(lat, lon):
    return all(math.isfinite(v) for v in (lat, lon)) and 8 <= lat <= 24 and 102 <= lon <= 110

def load_routes(path):
    data = path.read_bytes()
    if len(data) > 5*1024*1024:
        raise ValueError("Route file >5 MiB")
    if path.suffix.lower() in (".geojson", ".json"):
        j = json.loads(data)
        if j.get("type") == "FeatureCollection":
            geoms = [f["geometry"] for f in j["features"]]
        elif j.get("type") == "Feature":
            geoms = [j["geometry"]]
        else:
            geoms = [j]
        tracks = []
        for g in geoms:
            if g["type"] == "LineString":
                tracks.append([(float(x[1]), float(x[0])) for x in g["coordinates"]])
            elif g["type"] == "MultiLineString":
                tracks.extend([[(float(x[1]),float(x[0])) for x in line] for line in g["coordinates"]])
            else:
                raise ValueError("Only LineString/MultiLineString route geometry supported")
    elif path.suffix.lower() == ".gpx":
        if b"<!DOCTYPE" in data.upper() or b"<!ENTITY" in data.upper():
            raise ValueError("GPX entities/DTD forbidden")
        root = ET.fromstring(data)
        tracks = []
        for seg in root.findall(".//{*}trkseg"):
            tracks.append([(float(pt.attrib["lat"]),float(pt.attrib["lon"])) for pt in seg.findall("{*}trkpt")])
        if not tracks:
            tracks = [[(float(pt.attrib["lat"]),float(pt.attrib["lon"]))
                       for pt in root.findall(".//{*}rtept")]]
    else:
        raise ValueError("Use .gpx or .geojson")
    tracks = [t for t in tracks if len(t) >= 2]
    if not tracks or sum(map(len, tracks)) > 50000:
        raise ValueError("Route needs 2..50000 geometry points")
    for t in tracks:
        for p in t:
            if not valid_point(*p):
                raise ValueError("Route outside Vietnam bounding box / invalid coordinates")
        for a,b in zip(t,t[1:]):
            if meters(a,b)>1500:
                raise ValueError("Route has >1.5 km straight segment; use FULL road geometry, not waypoints")
    return tracks

def point_segment(p, a, b):
    """Distance and projection parameter in metres; local tangent plane anchored at p."""
    lat = math.radians(p[0])
    scale = math.cos(lat)
    ax = math.radians(a[1]-p[1])*R*scale
    ay = math.radians(a[0]-p[0])*R
    bx = math.radians(b[1]-p[1])*R*scale
    by = math.radians(b[0]-p[0])*R
    dx,dy = bx-ax,by-ay
    t = max(0.,min(1.,-(ax*dx+ay*dy)/max(1.e-20,dx*dx+dy*dy)))
    return math.hypot(ax+t*dx,ay+t*dy),t

def closest_edges(road_a, road_b, feat_a, feat_b):
    """Distance between line segments; representative stays ON the OSM feature geometry."""
    # Detect crossing in a local metre plane. Otherwise check four endpoint projections.
    origin = road_a
    c = math.cos(math.radians(origin[0]))
    def xy(p):
        return ((p[1]-origin[1])*math.pi/180*R*c,(p[0]-origin[0])*math.pi/180*R)
    a,b,d,e=map(xy,(road_a,road_b,feat_a,feat_b))
    cross=lambda u,v:u[0]*v[1]-u[1]*v[0]
    r=(b[0]-a[0],b[1]-a[1])
    s=(e[0]-d[0],e[1]-d[1])
    den=cross(r,s)
    if abs(den)>1.e-8:
        da=(d[0]-a[0],d[1]-a[1])
        t=cross(da,s)/den
        u=cross(da,r)/den
        if 0<=t<=1 and 0<=u<=1:
            return 0.,(feat_a[0]+u*(feat_b[0]-feat_a[0]),
                        feat_a[1]+u*(feat_b[1]-feat_a[1]))
    choices=[]
    for p in (feat_a, feat_b):
        choices.append((point_segment(p,road_a,road_b)[0],p))
    for p in (road_a,road_b):
        dist,t=point_segment(p,feat_a,feat_b)
        choices.append((dist,(feat_a[0]+t*(feat_b[0]-feat_a[0]),
                              feat_a[1]+t*(feat_b[1]-feat_a[1]))))
    return min(choices,key=lambda x:x[0])

def classify(tags):
    if tags.get("access") in ("private","no") or "disused" in tags or "abandoned" in tags:
        return None
    if tags.get("leisure") in ("park","playground","garden","nature_reserve"):
        return "PARK"
    if tags.get("tourism") == "museum":
        return "MUSEUM"
    if tags.get("bridge") == "yes" and tags.get("highway"):
        return "BRIDGE"
    if tags.get("waterway") in ("river","stream"):
        return "RIVER"
    if tags.get("natural") in ("peak","rock","waterfall","beach","water","wood","spring"):
        return "NATURE"
    if tags.get("historic") in ("monument","memorial","ruins","archaeological_site"):
        return "LANDMARK"
    if tags.get("tourism") in ("attraction","viewpoint","zoo","aquarium") or tags.get("amenity") in ("library","arts_centre","planetarium"):
        return "LANDMARK"
    return None

def simplify(points, tolerance=20.):
    # Iterative Ramer-Douglas-Peucker, avoids recursion on long routes.
    keep={0,len(points)-1}
    stack=[(0,len(points)-1)]
    while stack:
        lo,hi=stack.pop()
        if hi-lo<=1: continue
        d,i=max((point_segment(points[k],points[lo],points[hi])[0],k)
                for k in range(lo+1,hi))
        if d>tolerance:
            keep.add(i);stack.extend(((lo,i),(i,hi)))
    return [points[i] for i in sorted(keep)]

def query_chunks(routes, radius=350):
    if not 50<=radius<=1000: raise ValueError("Query radius must be 50..1000 m")
    pieces=[]
    for track in routes:
        simplified=simplify(track)
        start=0
        while start<len(simplified)-1:
            end=min(start+29,len(simplified)-1)
            # Keep each query on a short road section even for straight highways.
            while end>start+1 and sum(meters(a,b) for a,b in
                   zip(simplified[start:end+1],simplified[start+1:end+1]))>12000:
                end-=1
            coords=",".join(f"{a:.6f},{b:.6f}" for a,b in simplified[start:end+1])
            lines="\n  ".join(f"{tag}(around:{radius},{coords});" for tag in TAGS)
            pieces.append("[out:json][timeout:90];\n(\n  "+lines+"\n);\nout center geom;\n")
            start=end
    return pieces

def road_index(routes):
    tiles=defaultdict(list)
    total=0.
    for track in routes:
        for a,b in zip(track,track[1:]):
            seg=(a,b,total)
            # The cell index is overapproximate, ensuring no eligible segment is skipped.
            y0,y1=sorted((int(math.floor(a[0]/.02)),int(math.floor(b[0]/.02))))
            x0,x1=sorted((int(math.floor(a[1]/.02)),int(math.floor(b[1]/.02))))
            for y in range(y0,y1+1):
                for x in range(x0,x1+1):
                    tiles[y,x].append(seg)
            total+=meters(a,b)
    return tiles,total

def near_segments(index, p):
    cy=int(math.floor(p[0]/.02));cx=int(math.floor(p[1]/.02))
    for y in range(cy-1,cy+2):
        for x in range(cx-1,cx+2):
            yield from index.get((y,x),())

def geometry(raw):
    if raw["type"]=="node":
        return [[(float(raw["lat"]),float(raw["lon"]))]]
    if raw["type"]=="way":
        g=raw.get("geometry")
        return [[(float(x["lat"]),float(x["lon"])) for x in g]] if isinstance(g,list) and len(g)>=2 else []
    return []  # Relations and geometry-free ways rejected (centroid may be misleading).

def candidate_distance(raw, index):
    best=None
    for track in geometry(raw):
        if any(not valid_point(*p) for p in track): continue
        for i,p in enumerate(track):
            for ra,rb,progress in near_segments(index,p):
                dist,t=point_segment(p,ra,rb)
                item=(dist,p,progress+t*meters(ra,rb))
                if best is None or dist<best[0]: best=item
            if i:
                a=track[i-1]
                # Edges near the route are considered even if their endpoints are far apart.
                midpoint=((a[0]+p[0])/2,(a[1]+p[1])/2)
                possibilities={id(x):x for pt in (a,p,midpoint) for x in near_segments(index,pt)}
                for ra,rb,progress in possibilities.values():
                    dist,feature_point=closest_edges(ra,rb,a,p)
                    t=point_segment(feature_point,ra,rb)[1]
                    item=(dist,feature_point,progress+t*meters(ra,rb))
                    if best is None or dist<best[0]: best=item
    return best

def normalize(name):
    plain=unicodedata.normalize("NFKD",name)
    return re.sub(r"\W+","", "".join(c for c in plain if not unicodedata.combining(c)).casefold())

def extract_candidates(routes, extracts, max_distance=180, min_separation=70):
    if not 20<=max_distance<=500: raise ValueError("Road distance must be 20..500 m")
    index,total=road_index(routes)
    found={}
    for filepath in extracts:
        raw=filepath.read_bytes()
        if len(raw)>30*1024*1024: raise ValueError("Overpass extract >30 MiB: "+str(filepath))
        j=json.loads(raw)
        if not isinstance(j,dict) or not isinstance(j.get("elements"),list) or len(j["elements"])>250000:
            raise ValueError("Invalid/oversized Overpass response: "+str(filepath))
        for x in j["elements"]:
            if not isinstance(x,dict) or x.get("type") not in ("node","way"):continue
            oid=x.get("id")
            if not isinstance(oid,int) or oid<=0:continue
            tags=x.get("tags",{})
            if not isinstance(tags,dict):continue
            category=classify(tags)
            name=tags.get("name:vi") or tags.get("name")
            if not category or not isinstance(name,str):continue
            name=name.strip()
            if not 2<=len(name)<=90 or any(ord(c)<32 for c in name):continue
            score=candidate_distance(x,index)
            if not score or score[0]>max_distance:continue
            dist,p,progress=score
            osm_id=f"osm:{x['type']}:{oid}"
            item=dict(id=osm_id,name=name,lat=round(p[0],7),lon=round(p[1],7),
                      type=category,osm_url=f"https://www.openstreetmap.org/{x['type']}/{oid}",
                      road_distance_m=round(dist,1),route_km=round(progress/1000,2),
                      geometry="node" if x["type"]=="node" else "way-nearest-boundary",
                      route_validated=False)
            if osm_id not in found or item["road_distance_m"]<found[osm_id]["road_distance_m"]:
                found[osm_id]=item
    # Avoid many identically named POIs at nearly the same place, without discarding distinct towns.
    ordered=sorted(found.values(),key=lambda x:(x["route_km"],x["road_distance_m"],x["id"]))
    picked=[]
    seen=defaultdict(list)
    for item in ordered:
        key=(normalize(item["name"]),item["type"])
        if any(meters((item["lat"],item["lon"]),(p["lat"],p["lon"]))<min_separation
               for p in seen[key]):continue
        seen[key].append(item);picked.append(item)
    if len(picked)>5000:
        raise ValueError("Candidate count >5000; split route into smaller data packs")
    return picked,total

def write_output(output, route, routes, extract_paths, candidates, total, radius, max_distance):
    output.parent.mkdir(parents=True,exist_ok=True)
    if "build" not in output.resolve().relative_to(Path.cwd().resolve()).parts:
        raise ValueError("Output must reside in a build/ directory, not APK assets")
    rows=["# dexkids-poi-v1",HEADER]
    for p in candidates:
        rows.append("\t".join([p["id"],p["name"],str(p["lat"]),str(p["lon"]),p["type"],
                              p["osm_url"],"",""]))
    output.write_text("\n".join(rows)+"\n",encoding="utf-8")
    meta=dict(status="UNREVIEWED_ROUTE_FILTERED_CANDIDATES",route_file=str(route),
        route_sha256=hashlib.sha256(route.read_bytes()).hexdigest(),
        extract_sha256={f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in extract_paths},
        route_length_km=round(total/1000,2),query_radius_m=radius,max_road_distance_m=max_distance,
        candidate_count=len(candidates),by_type=dict(Counter(p["type"] for p in candidates)),
        license="ODbL-1.0",attribution="© OpenStreetMap contributors",
        limitations="Polyline proximity only: NOT lane matching, vehicle passage, a routable entrance, public access or field approval.",
        candidates=candidates)
    output.with_suffix(".audit.json").write_text(json.dumps(meta,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")

def main():
    ap=argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--route",required=True,type=Path,help="Detailed real-road GPX or OSRM GeoJSON LineString")
    ap.add_argument("--query-dir",type=Path,help="Write chunked Overpass QL queries here")
    ap.add_argument("--extract-dir",type=Path,help="Saved Overpass .json responses (offline input)")
    ap.add_argument("--output",type=Path,help="Unreviewed TSV under build/ only")
    ap.add_argument("--query-radius",type=int,default=350)
    ap.add_argument("--max-road-distance",type=int,default=180)
    args=ap.parse_args()
    routes=load_routes(args.route)
    queries=query_chunks(routes,args.query_radius)
    if args.query_dir:
        args.query_dir.mkdir(parents=True,exist_ok=True)
        if "build" not in args.query_dir.resolve().relative_to(Path.cwd().resolve()).parts:
            ap.error("--query-dir must be under build/")
        for i,q in enumerate(queries,1):
            (args.query_dir/f"chunk-{i:04d}.ql").write_text(q,encoding="utf-8")
    if args.extract_dir:
        if not args.output:ap.error("--output is required with --extract-dir")
        extracts=sorted(args.extract_dir.glob("*.json"))
        if not extracts:ap.error("No saved Overpass JSON chunks found")
        candidates,total=extract_candidates(routes,extracts,args.max_road_distance)
        write_output(args.output,args.route,routes,extracts,candidates,total,args.query_radius,args.max_road_distance)
        print(f"UNREVIEWED: {len(candidates)} points along {total/1000:.1f} km, within {args.max_road_distance}m of route polyline.")
    else:
        print(f"Prepared {len(queries)} Overpass query chunks. Download JSON externally and rerun with --extract-dir.")
if __name__=="__main__":main()
