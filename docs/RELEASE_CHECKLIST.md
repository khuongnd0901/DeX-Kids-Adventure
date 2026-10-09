# M9 release gates — no release-ready claim

| Gate | Source | Actual |
| --- | --- | --- |
| Java core tests + desktop classes + Android debug assemble | Implemented | CI result per commit required |
| Desktop OpenGL runtime and actual child window | Implemented | NOT VERIFIED |
| 1920×1080 DeX on Z Fold3 | External display routing authored | NOT VERIFIED |
| 30 FPS stable and 60-minute on-device soak | Profiler + script authored | NOT VERIFIED |
| Multi-chunk infinite world with no render seams | Chunk-cache source | NOT VERIFIED visually |
| Final layered character animation assets / TextureAtlas | 18 original SVG regions and atlas implemented | Production art/license/hardware approval OPEN |
| GPX replay & jump guard | Implemented | Unit tests only; real GPS NOT VERIFIED |
| Real GPS and OSM offline POI index | Adapter + classifier source | DB + hardware BLOCKED |
| Reviewed geographic knowledge and Vietnamese narration | Cue+offline voice adapter | Curated pack + speech hardware BLOCKED |
| Parent controls & emergency stop | Basic phone UI | DeX cross-screen/parent protection BLOCKED |
| DeX Assistant IPC signing and commands | STOP receiver source | Cross-APK trust and coexistence BLOCKED |
| Privacy/license/retention audits | Docs authored | OPEN |
| Production-signed upgradeable APK & release checksum | Debug CI only | OPEN |

Do not merge a draft PR or tag a release as production without the gates above.
