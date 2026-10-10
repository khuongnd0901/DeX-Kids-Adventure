# M11 — Road corridor POI pipeline (offline OSM)

**State: tooling implemented; no verified 5-route OSM extracts or authentic driving tracks available in the repository.** This does NOT mean the production catalogue has been expanded, or that the app knows which road the driver is on. No API, CI, runtime location upload, GPS permissions or backend added.

## Inputs required
For each of the five trips — Đồng Nai to Vũng Tàu, Phan Thiết, Bảo Lộc, Đà Lạt and Nha Trang — obtain a **full-detail routed road polyline** as GPX track (not sparse straight-line waypoints) or GeoJSON LineString from a lawful routing source (e.g. an OSRM route with geometries=geojson&overview=full). Origin is not fixed: select the actual starting point and expected alternate road route rather than assuming a single route. A road routing provider supplies an estimate, not proof the vehicle traveled there. Avoid uploading private GPS trip history. Use local saved files.

The OSRM [route service](https://project-osrm.org/docs/v5.24.0/api/) returns a full road-network polyline; an example **only** after selecting exact longitudes/latitudes is:
\`\`\`
# Browser/API download on operator machine, respecting routing host usage limits:
# GET /route/v1/driving/LON1,LAT1;LON2,LAT2?overview=full&geometries=geojson
# Save routes[0].geometry as {"type":"LineString","coordinates":[[lon,lat],...]}
\`\`\`
Never create a "road" by drawing a straight line between Đồng Nai and a city.


### Optional: generate route and download small query batches on local developer machine
Use **explicit coordinates** for your true planned origin, destination and optional via-points. Never assume "Đồng Nai" is a precise GPS origin:
\`\`\`bash
python3 tools/prepare_osrm_route.py \
  --point 106.82,10.95 --point 107.08,10.35 \
  --output build/routes/dong-nai-vung-tau.geojson
python3 tools/road_poi_pipeline.py \
  --route build/routes/dong-nai-vung-tau.geojson \
  --query-dir build/road-poi/vung-tau/queries
python3 tools/fetch_overpass_chunks.py \
  --query-dir build/road-poi/vung-tau/queries \
  --extract-dir build/road-poi/vung-tau/extracts \
  --max-requests 5 --delay 12
\`\`\`
Coordinates above are **illustrative Biên Hòa/Vũng Tàu anchors**, not verified user home or a promise of an exact route. The first command uses OSRM's public demo and must be run sparingly per host policy; compare its result to the route you really intend to drive. The last command downloads at most five sequential queries in one invocation (>=12s apart); rerun later until all generated queries have valid local responses, then perform the offline extraction step. For many routes / hundreds of queries use a local OSM PBF or your own Overpass instance instead of stressing public services. The downloader will **never overwrite an existing response** without manual inspection. No network calls are made by the Android application.


## For each named route (run locally)
\`\`\`bash
mkdir -p build/road-poi/vung-tau/{queries,extracts}
python3 tools/road_poi_pipeline.py \
  --route build/routes/dong-nai-vung-tau.geojson \
  --query-dir build/road-poi/vung-tau/queries

# Download each numbered .ql from Overpass using a polite, rate-limited
# operator-managed process; save the matching JSON as extracts/chunk-####.json.
# Public shared Overpass is not for bulk automated scraping; keep requests
# small, retry with backoff, and use a local extract for larger datasets.

python3 tools/road_poi_pipeline.py \
  --route build/routes/dong-nai-vung-tau.geojson \
  --extract-dir build/road-poi/vung-tau/extracts \
  --output build/road-poi/vung-tau/candidates.tsv
python3 tools/test_road_poi_pipeline.py
./scripts/test-local.sh
\`\`\`

### How distance filtering works
1. Generate Overpass QL **around the full route polyline**, split into bounded query chunks; radius defaults to 350m to capture geometry, not determine eligibility.
2. Parse named OSM **nodes and ways with real geometry**; reject ways without geometry (center-only map points are unreliable) and relations not yet supported.
3. Compute the shortest geometric distance to individual **road segments**: a node's actual coordinates or a way's closest point on its OSM geometry, including crossings where endpoints are distant. Do NOT compute distance from city centers, endpoints or way centroids.
4. Include candidates only within **180m** of a supplied route segment. This is a default *review threshold*, not an assertion that the entrance is drivable; adjust to e.g. 80–120m in urban areas after manual checks. Suppress same-name duplicates within 70m and sort by progress along the route.
5. Write **unreviewed** candidates TSV plus JSON audit with OSM URLs, on-feature representative points, road distance, relative route progress, route/extract SHA-256, source attribution, and explicit \`route_validated=false\`.
6. Review before production: true road/motorway access, building/park area, safe stops, duplicate feature names, freshness, age-appropriate place facts and independent factual citations. Only then author matching child-safe narration/dialogue. Never feed OSM names alone to LLM as verified historical facts.

**Important:** Polyline proximity does not prove a car passed the POI; a 60m distant riverbank, inaccessible waterfall or opposite highway can still be flagged. The Android \`LiveFixGate\` remains a second, independent GPS quality check; it does not perform road map matching. Better future accuracy requires road-edge/heading matching (and correct route selection) plus entry/driveway geometry.

The outputs are **staged under ignored build/** and deliberately not bundled into the Android assets. The 40 legacy preview POIs remain unchanged. \`assets/poi/reviewed.tsv\` remains empty until reviewed; do not silently auto-promote candidates.

## Quotas, licenses, tests
- OpenStreetMap geographical feature data: © OpenStreetMap contributors, ODbL 1.0. Obey attribution/share-alike rules when redistributing a substantial derived database.
- [Overpass QL around polyline syntax](https://dev.overpass-api.de/overpass-doc/en/full_data/polygon.html). For heavy corridor extractions, acquire region .osm.pbf extracts or self-host Overpass instead of overloading public endpoints.
- Local test: \`python3 tools/test_road_poi_pipeline.py\` plus \`./scripts/test-local.sh\`, no CI. Synthetic crossing and centroid tests verify geometry logic. Full Android/Fold3 GPS, ASR/TTS, road matching and five actual-route dataset sourcing are **pending**.
