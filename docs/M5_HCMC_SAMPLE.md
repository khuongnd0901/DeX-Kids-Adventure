# HCMC location and narration sample (preview only)

As of 2026-10-09, four OSM-feature coordinates and names have been **cross-checked through an OSM-derived Mapcarta view**, with separately sourced factual descriptions for young children. The OSM primary feature endpoint did not respond in the research environment. This is NOT field survey, road map matching, public editorial approval, or authorization to ship factual narration as production content.

Files:
- `assets/poi/sample-hcm.tsv`: **preview** catalog, distinct from `assets/poi/reviewed.tsv`, which stays empty until human editorial/geometry approval.
- `assets/poi/sample-hcm-source-audit.json`: per-place OSM IDs, approximate coordinates, secondary map URLs and independent factual sources.
- `assets/narration/sample-hcm.tsv`: original short Vietnamese narration drafts backed by the source URL named on each row; no verbatim copyrighted passage.
- `test-data/gps/synthetic-hcm-poi-loop.gpx`: **SIMULATED** 16-point path with monotonic 60-second timestamps; straight-line segments are illustrative and NOT surveyed drivable roads.

| Landmark | OSM feature | Geopoint (approx center) | Fact source |
|---|---|---|---|
| Công viên Tao Đàn | [osm:way:500888743](https://www.openstreetmap.org/way/500888743) | 10.77479, 106.69309 | [Website](https://visithcmc.net/en/news/kham-pha-cong-vien-tao-dan-oc-dao-xanh-giua-long-thanh-pho) |
| Dinh Độc Lập | [osm:way:39598493](https://www.openstreetmap.org/way/39598493) | 10.77702, 106.69540 | [Website](https://dinhdoclap.gov.vn/) |
| Bảo tàng Chứng tích Chiến tranh | [osm:way:186249226](https://www.openstreetmap.org/way/186249226) | 10.77938, 106.69219 | [Website](https://baotangchungtichchientranh.vn/en/) |
| Bưu điện Trung tâm Sài Gòn | [osm:way:39514793](https://www.openstreetmap.org/way/39514793) | 10.77998, 106.70002 | [Website](https://visithcmc.net/en/news/buu-dien-trung-tam-sai-gon-net-dep-hai-hoa-giua-long-sai-gon) |

## Licensing and semantics
Geometry and names: © OpenStreetMap contributors under ODbL 1.0; https://www.openstreetmap.org/copyright. Read `assets/poi/NOTICE.txt`.
This sample is explicitly for opt-in **PREVIEW** mode; position-triggered narration is **not equivalent to a verified passage**. The actor should say only “Đây là…” or “Có thể ở gần…” with a sample disclaimer, never “Chúng ta vừa đi qua”. The preview must not become the default live child experience.

## To graduate content to production
A human maintainer must check the canonical OSM feature and actual road-adjacent geometry (not simply centroids), exact labels, historical claims, independent source and age suitability. Update human-reviewed `review-ledger.json`, populate `reviewed.tsv`, and migrate editorially approved narration separately. Re-run CI, emulator replay and actual DeX test.
