# LIVE GPS candidate POI expansion — 2026-10-10

This change expands 13 to 40 offline candidate POIs. Added 27 unique OSM-ID points, each with Vietnamese original narrative, quiz/answer/conversation prompt and individual Mapcarta source URL.

| Region | Before | Added | Total |
| --- | ---: | ---: | ---: |
| Đồng Nai | 3 | 4 | 7 |
| Vũng Tàu | 2 | 4 | 6 |
| Phan Thiết | 2 | 4 | 6 |
| Bảo Lộc | 2 | 3 | 5 |
| Đà Lạt | 2 | 7 | 9 |
| Nha Trang corridor | 2 | 5 | 7 |
| **Total** | **13** | **27** | **40** |

Source: OpenStreetMap IDs and OSM-derived Mapcarta pages (© OpenStreetMap contributors, ODbL 1.0). Full provenance and representative coordinates: `assets/poi/live-landmarks-source-audit.json`.

**Important:** These are approximate candidates, not verified roadside/entrance locations. They are NOT production-approved; `assets/poi/reviewed.tsv` remains empty. Some sites (islands, waterfalls, mountains, polygon centroids) are not reachable by car. Do not infer the vehicle passed, entered or traveled along any of these sites. Only trigger a GPS-nearby estimate after the existing two-fix accuracy/freshness gate. Never fabricate a nearby site when no point qualifies. 40 points over five long road corridors remain sparse.

## Offline and AI
The app loads these existing TSV schemas unchanged. Narration/TTS, local question fallback and optional Gemini/Groq cache for all 40 POIs continue to follow existing parent permissions. No API key, backend, third-party service call, extra Android permission or CI workflow was added by this data expansion.

## Test / release
Local only: `./scripts/test-local.sh`. The Python contract and Java JUnit data-link tests now require 40 entries, original source URLs and unique IDs, and six corridor counts. Device QA must still verify realistic GPX/live GPS near new sites, no false roadway claims, Vietnamese TTS/ASR, audio focus and sustained Fold3 DeX FPS. Before production: obtain primary OSM extracts, source/fact review, road geometry matching and travel-route coverage assessment.
