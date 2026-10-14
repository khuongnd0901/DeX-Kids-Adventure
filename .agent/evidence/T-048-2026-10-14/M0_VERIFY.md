# T-048 M0 audit receipt — 2026-10-14

Repo `khuongnd0901/DeX-Kids-Adventure`, branch `feat/T-048-capybara-bus-art-import`, Draft PR #55.

The previous ChatGPT session's Library contained `DeX-Kids-Adventure-art-v1.zip` (7,294,553 bytes). Materialized locally, extracted and examined with Pillow and SHA256. Original PNG bytes are intact.

| PNG | SHA256 | RGBA size | Nontransparent alpha bounds |
| --- | --- | --- | --- |
| capybara_idle.png | 2ed7f5c147e519746c91a4702e12d406c2511f6e1753dd58a775fb8572c2e835 | 1122x1402 | (0,23,1090,1402) |
| capybara_laugh.png | f98b2f3136af6843dd56200bf2ca4660ffbeac886010401efebf51f5f86a1483 | 1122x1402 | (0,17,1115,1376) |
| capybara_ask.png | 5cde0d728b21e05528228b5a15115358fa89033e647dbbaf9fa5da4f9d46d31f | 1122x1402 | (0,17,1108,1385) |
| capybara_wave.png | f3bb006f3af57b79716e18667dc0f35271a34df9894f711558f90f1023266bb3 | 1122x1402 | (0,0,1108,1402) |
| bus_side_with_driver.png | 7f77484b78c92b426e5bd33645a2bde421b81855b4e09091b9f1c1e930acebc9 | 1448x1086 | (0,19,1432,1029) |

Local checks: real ZIP verify-only PASS; extraction/import into isolated temp folder PASS; second import idempotent PASS; tampered PNG SHA rejected; extra traversal entry rejected. All source-level; no remote PNG committed; no atlas/Gradle/build/device tests run.

Important audit: `CartoonSprites.drawVehicle` draws legacy bus, driver, two rotating wheels independently; this must change for the baked composite bus. `VehicleLayout` has a legacy 660:290 aspect. The new concept PNG has a very different aspect; a direct swap would distort it. Story companions are outside this task's modification scope.

BINARY-UPLOAD BLOCKER: connected GitHub write actions are text/base64 payload APIs and do not expose a binary-file handoff from Library/container. No physical Samsung Z Fold3 connected here. **Task NOT DONE.**
