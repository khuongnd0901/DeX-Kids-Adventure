# DeX Kids Adventure — verified status
Updated: 2026-10-08 (Asia/Ho_Chi_Minh)
Development branch: `feat/T-001-bootstrap-libgdx`; PR #1 is DRAFT, no production release.

## Verified code / CI
| Milestone | Last verified evidence | State |
| --- | --- | --- |
| M0 bootstrap | [37795183419](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37795183419) Android+desktop compile | IN_PROGRESS (device runtime and repo visibility) |
| M1 libGDX | [37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) Xvfb OpenGL screenshots at 1280x720 & 1920x1080 | IN_PROGRESS (hardware P95, lifecycle) |
| M2 art/animation | [37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) 18 original SVG sprites, 2048 atlas, 10 sequential GL frames/GIF and Android APK | IN_PROGRESS (art approval, hardware, narration sync) |
| M3 GPX simulation | [37796769631](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37796769631) | IN_PROGRESS |
| M4 GPS/POI scaffolding | [37798067824](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37798067824) | IN_PROGRESS |
| M5 narration scaffolding | [37797120923](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797120923) | IN_PROGRESS |
| M6 dynamic journey | [37797221624](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797221624) | IN_PROGRESS |
| M7 parent UI | [37797413766](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797413766) | IN_PROGRESS |
| M8 DeX+IPC scaffolding | [37797528265](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797528265) | IN_PROGRESS |
| M9 release benchmark | [37797706168](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797706168) virtual soak only | BLOCKED |

**M2 latest verified source SHA:** `e63e21cc2374a95d36cdba837792382534daac3c`. A HUD accuracy wording fix `b9661a0c335976af05879b87e0af85abbe44e32a` is not yet included in these CI results. The original 720p/1080p screenshot artifact and animation GIF were actually rendered by LibGDX in Xvfb/Mesa software GL; they are not AI mockups.
Artifact links: [actions run 37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673), screenshots/GIF id **11563302286**, generated PNG+TextureAtlas id **11562987678**, Android debug APK id **11563282420**.
The SVG sources are in `art/assets-source/`, generated raster content in `assets/generated/` (build outputs).

## What M2 implements
- Capybara idle, blink, wave, talk-open frame, sleep, surprised; time/state-driven model and explicit narration-active gate. Does not pretend to narrate without reviewed content.
- Original yellow bus, lighting highlights, independent rolling wheels calculated from absolute traveled meters; bounded suspension/tilt animation.
- Original cloud/hills, home/building, bridge, bush/flower, street lamp, tree and glow assets; layered 2D parallax from stable journey distance.
- Biome scenery grouped into deterministic four-chunk districts to reduce abrupt geographical scene changes. Biomes are fictional until ground truth verified.
- Unit tests for animation states, vehicle movement, chunk continuity and district stability; source-atlas validator; desktop OpenGL captures and actual GIF preview.

## Unverified and release blockers
- No Android emulator, physical Z Fold3 or Samsung DeX display has run in this execution context.
- No measured Fold3 30 FPS/P95 frame latency, 60-minute actual soak, thermal, PSS or battery evidence. Render FPS shown in screenshots belongs to CI software OpenGL only.
- No production-approved assets, voice-synced talking frame or curated real-world POI/narration pack.
- No complete DeX-Assistant cross-app signing/command and navigation audio coexistence acceptance. Existing Assistant repos unchanged.
- No signed production APK, license release sign-off or validated upgrade path.
- Repo is currently **PUBLIC**; master requirements requested PRIVATE. Owner action required.

**No task/milestone is DONE.** Next gate: Z Fold3/DeX runtime smoke → manual art visual approval → narration/audio synchronization → actual 60-minute benchmark → release readiness review.
