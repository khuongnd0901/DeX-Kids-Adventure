#!/usr/bin/env python3
"""Synthetic fixtures check route geometry, point/edge distances and no approval."""
import json
import tempfile
from pathlib import Path
from road_poi_pipeline import (load_routes, query_chunks, point_segment,
    closest_edges, extract_candidates, write_output, meters)

with tempfile.TemporaryDirectory() as td:
    d=Path(td)
    # Detailed 280m shape approximating real-road geometry; fixture, NOT a real route.
    route=d/"route.geojson"
    route.write_text(json.dumps({"type":"LineString","coordinates":[
        [106.80000,10.95000],[106.80100,10.95000],[106.80200,10.95000],
        [106.80300,10.95000]]}),encoding="utf-8")
    tracks=load_routes(route)
    assert len(query_chunks(tracks))==1
    # A 25-km truly straight road still splits into bounded Overpass chunks.
    long_track=[[(10.95,106.80+i*.01) for i in range(27)]]
    chunks=query_chunks(long_track)
    assert len(chunks)>=2, len(chunks)
    assert all(q.count('around:350,')==7 for q in chunks)
    assert "around:350," in query_chunks(tracks)[0]
    assert "out center geom;" in query_chunks(tracks)[0]
    assert point_segment((10.95000,106.80150),tracks[0][1],tracks[0][2])[0]<1.
    assert meters((10.95,106.80),(10.95,106.803))>300
    # A long way intersects the road even when its endpoints/centroid are distant.
    distance,nearest=closest_edges(
        (10.95,106.801),(10.95,106.802),
        (10.949,106.8015),(10.951,106.8015))
    assert distance<0.01 and abs(nearest[1]-106.8015)<1.e-7
    elements=[
        {"type":"node","id":100,"lat":10.9502,"lon":106.8008,
         "tags":{"name":"Công viên Thử Nghiệm","leisure":"park"}},
        {"type":"node","id":101,"lat":10.960,"lon":106.801,
         "tags":{"name":"Quá xa","leisure":"park"}},
        {"type":"node","id":102,"lat":10.95021,"lon":106.80081,
         "tags":{"name":"Công viên Thử Nghiệm","leisure":"park"}},
        {"type":"way","id":200,"center":{"lat":10.99,"lon":106.83},
         "geometry":[{"lat":10.949,"lon":106.8015},{"lat":10.951,"lon":106.8015}],
         "tags":{"name":"Suối Cắt Ngang","waterway":"stream"}},
        {"type":"way","id":201,"center":{"lat":10.95001,"lon":106.801},
         "tags":{"name":"Thiếu hình học","leisure":"park"}},
        {"type":"node","id":105,"lat":10.95001,"lon":106.803,
         "tags":{"name":"Khu vực riêng tư","access":"private","leisure":"park"}}
    ]
    extract=d/"overpass.json"
    extract.write_text(json.dumps({"elements":elements}),encoding="utf-8")
    points,total=extract_candidates(tracks,[extract],180)
    assert {p["id"] for p in points}=={"osm:node:100","osm:way:200"},points
    assert [p for p in points if p["id"]=="osm:way:200"][0]["road_distance_m"]<1
    assert all(not p["route_validated"] for p in points)
    build=Path.cwd()/"build"/"route-pipeline-unit"
    try:
        out=build/"candidates.tsv"
        write_output(out,route,tracks,[extract],points,total,350,180)
        lines=out.read_text(encoding="utf-8").splitlines()
        assert len(lines)==4 and all(x.endswith("\t\t") for x in lines[2:])
        audit=json.loads(out.with_suffix(".audit.json").read_text(encoding="utf-8"))
        assert audit["candidate_count"]==2 and audit["status"].startswith("UNREVIEWED")
    finally:
        import shutil
        shutil.rmtree(build,ignore_errors=True)
    # Anti-waypoint shortcut: insufficiently detailed GPX/GeoJSON must reject.
    sparse=d/"sparse.geojson"
    sparse.write_text(json.dumps({"type":"LineString","coordinates":[[106.8,10.95],[106.9,10.95]]}))
    try:
        load_routes(sparse)
        raise AssertionError("Rejected sparse waypoints must not be routed as a straight road")
    except ValueError as ex:
        assert ">1.5 km" in str(ex)
print("PASS: OSM road-corridor candidate extraction, edge crossing, source audit, no auto approval")
