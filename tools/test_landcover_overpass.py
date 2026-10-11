#!/usr/bin/env python3
"""T-035 deterministic converter tests; all OSM IDs/geometries are SYNTHETIC."""
import json
import tempfile
from pathlib import Path
from import_landcover_overpass import as_polygon, convert, make_queries, load_features, filter_route
from road_poi_pipeline import load_routes
from build_geo_world import CELL

with tempfile.TemporaryDirectory() as temp:
    d=Path(temp)
    route=d/"route.geojson"
    # Dense fixture rather than sparse straight-line endpoint waypoints.
    route.write_text(json.dumps({"type":"LineString","coordinates":[
        [106.800,10.951], [106.801,10.951], [106.802,10.951], [106.803,10.951]
    ]}), encoding="utf-8")
    tracks=load_routes(route)
    q=make_queries(tracks)
    assert q and all('out geom;' in chunk for chunk in q)
    assert all('way["landuse"' in chunk and 'way["natural"' in chunk for chunk in q)
    assert all('around:350,' in chunk for chunk in q)

    def ring(lon, lat):
        w=.0008
        return [{"lon":x,"lat":y} for x,y in [
            (lon-w,lat-w),(lon+w,lat-w),(lon+w,lat+w),
            (lon-w,lat+w),(lon-w,lat-w)]]
    near_center=(int(106.801/CELL)+.5)*CELL, (int(10.951/CELL)+.5)*CELL
    close={"type":"way","id":123,"tags":{"landuse":"farmland"},
           "geometry":ring(*near_center)}
    far={"type":"way","id":124,"tags":{"natural":"water"},
         "geometry":ring(107.04,11.0)}
    open_way={"type":"way","id":125,"tags":{"landuse":"farmland"},
              "geometry":ring(*near_center)[:-1]}
    assert as_polygon(close)["properties"]["osm_id"]=="123"
    assert as_polygon(open_way) is None
    assert as_polygon({"type":"way","id":126,"tags":{"landuse":"farmland"},
                       "geometry":ring(111.0,11.0)}) is None

    ext=d/"extracts";ext.mkdir()
    (ext/"chunk-0000.json").write_text(json.dumps({"elements":[close,far,open_way]}))
    (ext/"chunk-0001.json").write_text(json.dumps({"elements":[close]}))
    features, receipts=load_features(ext)
    assert len(features)==2 and len(receipts)==2
    output=d/"landcover-cells.tsv"
    summary=convert(route,ext,output)
    lines=[line for line in output.read_text().splitlines() if not line.startswith("#")]
    assert summary["closed_sourced_polygons"]==2
    assert summary["route_near_cells"]>=1
    assert all(line.endswith("osm:way:123") for line in lines)
    assert summary["route_near_cells"]==len(lines)
    assert output.with_suffix(".audit.json").is_file()
    assert summary["status"]=="OSM_SOURCED_ROUTE_ESTIMATE_NOT_FIELD_VERIFIED"
    assert filter_route({(100,200):("PARK","osm:way:123")},tracks)=={}

    # Invalid Overpass content must not be written as a new production-looking pack.
    (ext/"chunk-0002.json").write_text(json.dumps({"bad":"response"}))
    bad=d/"bad-output.tsv"
    try:
        convert(route,ext,bad)
    except ValueError:
        assert not bad.exists()
    else:
        raise AssertionError("Invalid chunk unexpectedly accepted")
print("PASS: T-035 deterministic Overpass way geometry, provenance, corridor proximity and fail-closed")
