# M4 — OpenStreetMap offline POI provenance and release gate

POIs may only be shown by name from `assets/poi/reviewed.tsv` once a reviewer has checked each feature's OSM URL, location, local name, feature type and verification date. **The bundled catalog is intentionally empty as of 2026-10-09**. This is not a claim that HCMC has no POIs: it means no reviewed source extract was provided/verified yet.

## Extract / reproduce without app network access
1. Save an Overpass JSON extract for a small consented area (do NOT upload user driving history). Query only OSM objects with name and selected feature tags. Example query near central HCMC:
```ql
[out:json][timeout:45];
(
  nwr["leisure"="park"]["name"](10.75,106.65,10.80,106.73);
  nwr["tourism"="museum"]["name"](10.75,106.65,10.80,106.73);
  nwr["tourism"="attraction"]["name"](10.75,106.65,10.80,106.73);
  nwr["highway"]["bridge"="yes"]["name"](10.75,106.65,10.80,106.73);
  nwr["waterway"="river"]["name"](10.75,106.65,10.80,106.73);
);
out center tags;
```
2. Export result to `build/overpass.json` locally using a compliant, rate-limited Overpass endpoint under its terms. Run:
```bash
python3 tools/import_osm_pois.py --input build/overpass.json --output build/poi-candidates.tsv
python3 tools/test_osm_pipeline.py
```
3. Keep `build/poi-candidates.provenance.json`, especially source SHA-256, import date and attribution. **Candidates have EMPTY `verified_at` and `reviewer` fields** by design. They are never included in the APK automatically.
4. Reviewer must independently check original OSM feature URL/type/name/coordinate (centroids of ways may be far from road route), remove sensitive/inaccurate entries, then write UTC ISO 8601 `verified_at` and a stable reviewer ID, and copy approved rows into `assets/poi/reviewed.tsv` explicitly. Maintain the tracked `assets/poi/review-ledger.json` with the reviewer ID, OSM feature URL and manually verified date. CI validates that every approved POI matches its ledger; the importer can never silently approve candidates. Tests require exact source URL and review marker; no extrapolated claims.
5. Verify app package and attribution `© OpenStreetMap contributors`, ODbL, https://www.openstreetmap.org/copyright. Any published derivative dataset must comply with ODbL including its applicable share-alike obligations. Do not mix with Google Maps proprietary data.
6. Do not state historical facts from OSM point tags; M5 narration will require separate verified reference materials and age-appropriate editorial review.

Only point/centroid proximity is supported. **PASSING_CANDIDATE is not route map matching and must never generate a spoken "we passed this bridge" claim.** GPX import counts as SIMULATED. Real GPS requires valid accurate recent user-consented fixes. No GPS location is uploaded, persisted or logged by the POI engine.

License reference: https://www.openstreetmap.org/copyright and https://osmfoundation.org/wiki/Licence/Attribution_Guidelines .

## Package validation

```bash
python3 tools/validate_reviewed_pois.py
```

The application currently ships no approved named entries. A human reviewer must populate `assets/poi/reviewed.tsv` and the matching `assets/poi/review-ledger.json`; all approved entries require genuine source provenance, UTC review timestamp and exact canonical OSM URL. Automated checks cannot replace field/geometry verification. Android uses the Unicode TextView overlay to display names with attribution, never calls network endpoints at runtime.
