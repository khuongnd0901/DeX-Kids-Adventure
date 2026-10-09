# T-005 / M4 — offline POI engine, source evidence
Date: 2026-10-09

## Source and data gates
- `991f3ae8`: add JourneyFeed optional position observation, quality-gated LIVE fixes, explicitly simulated GPX coordinates (no invalid teleport), deferred feed adapter. [CI #37891960202](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37891960202) SUCCESS.
- `5933e2a3`: strict reviewed catalog and tile-indexed proximity engine. [CI #37892078068](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37892078068) FAILED because one JUnit lambda captured a reassigned local variable.
- `a64e7f56`: fix test-only effectively-final issue; [CI #37892403773](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37892403773) SUCCESS, including core unit tests and desktop OpenGL.
- `63155b91`: OSM candidate importer, fictional source fixture, empty reviewed asset pack, provenance docs.
- `b2fc5a14`: bind offline engine to LibGDX game and Android native multilingual overlay (preserves DeX single-display UX).
- `d86017d0`: require exact review ledger/provenance/ODbL checks; no auto-publishing candidate rows.
- `4a4fcaa8`: avoid GPX coordinate interpolation work for every render frame.
- `b701a0ae`: confidence threshold based on accuracy and distance.
- The integrated/latest CI is still running at this checkpoint; no claims of final integrated success yet.

## Implemented behavior (software)
- Offline-only lookups from approved TSV; strict 512 KiB file size and 5,000 record ceiling.
- Most recent accurate real fixes/GPX interpolated samples feed a bounded tile index.
- Duplicate-suppressed NEARBY, APPROACHING and PASSING_CANDIDATE (not a claimed crossing), globally cooled down 30s. Quality gate 25m, confidence threshold 0.55, query throttle.
- Runtime shows full OSM attribution whenever a sourced result is presented; no unverified POI triggers a narrator.
- Source test fixtures with synthetic coordinates and OSM-shaped IDs are NOT real named places. Shipped reviewed.tsv has header only and review-ledger.json has 0 approved entries.

## Open
No actual OSM HCMC reviewed place pack or segment map-matching; human verification, Android emulator UI runtime, route ground truth and Fold3/DeX not performed. M4 remains IN_PROGRESS. Need verified POI dataset before M5 named narration.
