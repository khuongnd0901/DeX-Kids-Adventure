# T-048 — Capybara PNG/composite bus runtime replacement

Owner Main; branch `feat/T-048-capybara-bus-art-import`; PR #55 merged into main at 8260fe1 by explicit user request.
Dependency: immutable original source ZIP + existing LibGDX/story pipeline.
Status IN_PROGRESS, local/emulator scope delivered; physical/human/release gates open.
Keep this plan active for the remaining acceptance gates. User explicitly authorized
merging the verified local/emulator implementation into main on 2026-10-14;
this supersedes the earlier Draft/no-merge instruction, without physical/release signoff.

| Milestone | Current state | Acceptance/evidence |
|---|---|---|
| M0 original binary import/push | DONE for source gate | faba918, five remote SHA/RGBA/size/alpha matches; M0_BINARY_IMPORT.md |
| M1 PNG pipeline/renderer | IMPLEMENTED + LOCAL/EMULATOR VERIFIED | one 1024 RGBA page, guarded composite path, no distortion/duplicate layers, preserved legacy |
| M2 four emotions | IMPLEMENTED + LOCAL/EMULATOR VERIFIED | existing phases/wave mapped, stable feet/canvas, 180ms paused fade, 178 core tests and four screenshots |
| M3 local tests/build | PASS | art/legacy/local suite; Android debug/test APK, desktop classes |
| M3 Windows emulator | PASS functional/visual slice | four poses, route story/menu,100 Full/Audio cycles; actual screenshots |
| M3 physical Samsung DeX/FPS/GPU | NOT VERIFIED | no physical equipment tested; emulator is not DeX |
| M4 docs/evidence/PR publication | delivered with this continuation | SUMMARY.md, STATUS/TASKS/licenses/source README; merge now user-authorized |
| Parent/child/art/redistribution signoff | OPEN | no generation-as-license or engineering-as-parent approval |

## Final design

- Strict original SHA256/RGBA/alpha allowlist; reproducible `build_t048_atlas.py`.
- Original PNGs unchanged. Common pose scale, centered visible content/perceptible
  foot baseline with soft padding; alpha-cropped bus; Linear sampling/gutters.
- Preserve old SVGs/atlas. `assets/art-mode.txt=legacy` + rebuild/reinstall restores
  legacy renderer. Source guard/old atlas verified; separate legacy APK not run.
- New bus uses trimmed 1432:1010 ratio and existing road/body motion. Driver/wheels
  baked in; no legacy overlay. Passenger geometry aligns cabin; assets/logic unchanged.
- Standing Capybara beside bus shows four poses; baked driver stays static. Existing
  controller unchanged; ASK/question, LAUGH/answer cheer, WAVE/greeting, IDLE/inactive.
  Missing independent blink/talk/sleep art uses neutral pose; no new illustration.
- One resident1024 RGBA page adds 4 MiB backing, disposed with renderer. No GL/IO
  construction per draw. Existing privacy/flow/source rules remain.

## Execution/failure history

Initial source ZIP was missing locally; current user supplied its path on D:.
Importer now PASS and binaries pushed. Earlier connector/missing-ZIP receipts
remain evidence, superseded by M0_BINARY_IMPORT.md. Initial baseline alignment
failed and was corrected; initial target build failed on missing NDK/concurrent
Gradle output race, retained in local/gradle-build.txt. Final sequential build
PASS with verified official NDK27.2 and unchanged native source.

100 Full/Audio-only cycles intentionally caused user-observed black flashes;
that test ended. Normal DEMO resumed; 12 sampled nonblack graphics frames. No
continuous/physical flicker or unlimited endurance proof.

## Remaining acceptance

Physical Samsung DeX placement/input/long session/FPS/thermal/resource testing,
parent/child visual review and release redistribution signoff. No invented
measurements or licenses. Current local/emulator acceptance and reproducible
commands: `.agent/evidence/T-048-2026-10-14/SUMMARY.md`.

## Integration publication receipt

2026-10-14: PR #55 successfully merged with expected head 79969e9 via GitHub
merge commit 8260fe16af3cbf8d29d801260415909fb161ec47. Merge tree matches feature
head exactly; prior verified source remains applicable. This is integration,
not physical/human/release acceptance. Task/plan remain active. See MERGE.md.
