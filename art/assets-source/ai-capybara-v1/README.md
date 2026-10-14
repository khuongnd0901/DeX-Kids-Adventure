# T-048 — original Capybara and composite bus PNG sources

The five original PNG binaries were imported from the user-supplied
`D:\DeX-Kids-Adventure\DeX-Kids-Adventure-art-v1.zip` on 2026-10-14,
verified against both ZIP/repository manifests, committed and pushed at faba918.
All five remote Git blobs were fetched and SHA256 checked. Earlier missing-ZIP
and connector receipts are historical. Sources remain byte-exact, immutable.

| File | Source size | Visual |
|---|---|---|
| capybara_idle.png | 1122×1402 RGBA | standing neutral guide |
| capybara_laugh.png | 1122×1402 RGBA | laughing guide |
| capybara_ask.png | 1122×1402 RGBA | questioning guide |
| capybara_wave.png | 1122×1402 RGBA | waving guide |
| bus_side_with_driver.png | 1448×1086 RGBA | right-facing bus, baked driver and wheels |

## Reproducible runtime pipeline

`tools/build_t048_atlas.py` validates source SHA256, RGBA size and alpha bounds
before producing one 1024×1024 RGBA LibGDX atlas (4 MiB backing; not total GPU
VRAM). Original PNGs are not packaged at full source resolution. Poses resize
uniformly to 224×280 then share a 224×288 canvas, centered on visible content
with the same perceptible foot baseline and extra soft-edge padding. The bus
trims only its transparent outer bounds and resizes to 850×600. Linear texture
filtering, two extruded gutter pixels and four-pixel separation prevent adjacent
region sampling. Render bus rectangle uses the actual 1432:1010 trimmed ratio.
No new illustration, recoloring, driver removal or wheel reconstruction.

Run with the art environment in PATH:

```bash
python3 tools/import_t048_art.py /path/DeX-Kids-Adventure-art-v1.zip --verify-only
python3 tools/build_sprites.py
python3 tools/test_t048_art.py
python3 tools/test_sprite_atlas.py
python3 tools/render_t048_gallery.py /path/runtime-art-gallery.png
```

`generateKidsArt` invokes this pipeline; Android asset Sync packages the new
atlas. Generated assets stay ignored and are rebuilt from committed sources.

## Runtime mapping and rollback

`assets/art-mode.txt` defaults to `png`. Change it to `legacy`, rebuild and
reinstall to restore the original bus/driver/wheels. All legacy SVGs and atlas
regions remain. If the optional PNG atlas is absent, renderer logs legacy use.
No runtime preference, parent flow or GPS/voice/AI switch is added.

The new bus is a static composite under existing vehicle bob/pitch. Its baked
driver and wheels get no legacy overlays. Passenger avatar source/gesture logic
is retained; their render bounds align with the new cabin windows. A standing
Capybara guide beside the bus presents the supplied expression images; the
baked bus driver is not independently animated. This visual placement uses
existing source art, with no new story/persona or generated content.

`CapybaraArtPose` maps existing greeting/wave to WAVE, question phase to ASK,
answer phase to cheerful LAUGH, and interrupted/no active lesson to IDLE.
Answer cheer is not a correctness grade. Existing narration/motion state machine
is unchanged; unsupported PNG blink/talk/sleep frames use the neutral guide
outside a conversation. A bounded 180ms smooth fade uses paused render delta.

Provenance: ChatGPT-generated concept art at user request, original hashes in
manifest.json. Generation is not licensing/redistribution signoff. Parent art
acceptance and physical Samsung DeX/FPS/thermal/GPU lifecycle gates remain open.
Current acceptance/evidence: `.agent/evidence/T-048-2026-10-14/` and active plan.
