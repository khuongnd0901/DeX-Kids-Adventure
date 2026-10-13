# T-047 M2 — renderer duplication
Owner Main; dependency M1 checkpoint 566df5d.

One screen-owned WorldWindow is shared by WorldPainter/CartoonSprites and retained
across Audio-only transitions. Existing seeded ring cache and rewind unchanged.
POI preparation moves decode/upload/disposal before SpriteBatch.begin; draw only
uses the prepared image. Keep at most one resident panorama.
At exact alpha=1 with an actually loaded RGB888 panorama, render road instead of
hidden sky/far/primitive terrain. Retain all layers for fades and alpha textures.
All 22 current authored PNGs inspected locally: RGB, alpha extrema 255/255.
No removal of visible procedural layers when a panorama is absent.

Local Python contracts/core regression PASS (171 tests); Android unit NO-SOURCE;
debug and AndroidTest APK builds PASS. Current source/APK hashes recorded.
Device deployment NOT VERIFIED: streaming install on fresh ADB port returned an
empty failure, while model queries worked; no-streaming retry was pending when
this checkpoint was prepared. Do not claim FPS/PSS savings, GPU leak absence or
physical image equivalence. Before 600s measurements are valid; after is pending.
Continue M3 independently; physical QA remains open.
