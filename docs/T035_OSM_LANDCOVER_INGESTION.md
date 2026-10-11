# T-035 — Real OSM landcover ingestion (source tooling)

**State: SOURCE IMPLEMENTED; LIVE OSM DOWNLOAD AND REAL FIVE-ROUTE DATA NOT VERIFIED.**

T-033 shipped `assets/geo/landcover-cells.tsv` without production rows. The missing link was converting downloaded Overpass `out geom` way polygons to the app's coarse cell format and limiting those cells to a real routed road polyline.

`tools/import_landcover_overpass.py` now:
- Uses validated **full road** GeoJSON/GPX; refuses sparse long straight-line waypoints.
- Generates bounded, landcover-only Overpass QL chunks and reuses existing rate-limited, operator-owned `fetch_overpass_chunks.py` to download source data when external Internet is available.
- Processes **closed tagged OSM ways with real geometry and OSM IDs only**. Excludes open ways, invalid coordinates, private/unknown features and multipolygon relations whose holes are not assembled. Missing ways are a source coverage limitation, not an excuse to invent polygons.
- Rasterizes real polygon interiors at 0.002°, filters grid centers to <=350 m from route segments, and writes a SHA-256 provenance audit. No roads fabricated, no child/GPS cloud transmission and no Android network traffic.
- Keeps existing production index unchanged if all candidate cells are outside the route, or inputs are invalid.

## Exact local flow

1. Choose the actual intended start, destination, roads and alternatives; do NOT treat a city or province center as the driver's private address.
2. Obtain a full routed road polyline under `build/routes/<route>.geojson` (operator can use `tools/prepare_osrm_route.py` with explicit points, subject to demo service policy).
3. Generate OSM landcover queries:

```bash
python3 tools/import_landcover_overpass.py \
 --route build/routes/route.geojson \
 --query-dir build/osm-landcover/route/queries
```

4. Download with the existing polite shared service client (local use only; never run in Android or CI):

```bash
python3 tools/fetch_overpass_chunks.py \
 --query-dir build/osm-landcover/route/queries \
 --extract-dir build/osm-landcover/route/extracts \
 --max-requests 5 --delay 12
```

5. Convert into a staged index:

```bash
python3 tools/import_landcover_overpass.py \
 --route build/routes/route.geojson \
 --extract-dir build/osm-landcover/route/extracts \
 --output build/osm-landcover/route/landcover-cells.tsv
```

6. Inspect the generated `landcover-cells.audit.json`, compare representative map cells to OSM and actual road choice. Merge **nonoverlapping sourced** cells from all five selected routes into `assets/geo/landcover-cells.tsv` only after reviewing conflicts and provenance. OSM attribution must remain with the app.

## Critical limitations

- No actual source Overpass JSON was downloaded by this GitHub-only session: direct access to Overpass and public OSRM demo failed in the execution container, and browser retrieval did not yield the underlying JSON API.
- Current repository contains no full authenticated tracks for the five requested trips; synthetic test routes and a HCMC preview do not establish the user's actual route.
- OSM ways with geometry vertices outside the query corridor can be omitted by Overpass's `around` filter. Open relation multipolygons are conservatively excluded. If coverage is poor, use a local Vietnam OSM PBF extract and proper multipolygon assembly rather than inventing pixels.
- A road polyline estimate is not GPS lane matching. No assertion that the vehicle passed a landmark, river, farm or animal.
- For five long routes, use a local region extract rather than large numbers of public Overpass calls; honor availability, ODbL and source date.
- The importer source does not remove any of the T-033 acceptance gates (Gradle, Fold3 DeX, adult content QA and field routes).

## Local QA

```bash
python3 tools/test_landcover_overpass.py
bash scripts/test-local.sh
```

All new fixture geometries and OSM IDs in tests are synthetic and NEVER production data.
