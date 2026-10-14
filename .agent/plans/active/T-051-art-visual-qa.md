# T-051 — Art Visual QA, Fix & Completion

Owner Main; Art/Render/QA agents have distinct scopes. Dependencies T-048,
T-049, T-050; base fetched origin/main 1ab1f42dbb9fe017c9e2e8df2e6260452491c062.
Branch feat/T-051-art-visual-qa. Status IN_PROGRESS; no merge/CI authorized.

## Boundaries and environment

Preserve Sâu/Ong, five original T-048 PNGs, gameplay, GPS, AI, dialogue,
voice, Android permissions and device controls. Existing tracked work saved
in stash pre-T051-tracked-work-20261014; unrelated untracked evidence retained.
Windows adb.exe connects emulator-5554: API35, physical1920x1080, density160.
Use existing emulator; do not create/reset AVD or run a Linux ADB server.

## Execution and acceptance gates

1. M0: fetch/main fast-forward/feature branch, read relevant source/plans,
   record actual emulator identity and protected hashes.
2. M1: required Python/local tests then core tests and Android/debug/test APK.
   Retain initial failures; fix causes without weakening assertions.
3. M2/M3: enumerate actual V2 sources, build grouped classic/V2 contact sheets,
   inspect raster and Android frozen DEMO fixtures. Fix evidenced art/layout
   defects, preserve atlas names/sizes/page budget and depth behavior.
4. M4: individually improve missing Coast then Mountain/FX/UI/gestures as
   time and verified integration permit. Named POI drawings require sourced
   architecture; list unverified work explicitly. Catalog is not runtime.
5. M5: capture TC01–TC16 on actual API35 FullHD emulator where implemented;
   label synthetic fixtures/catalog/rollback configurations accurately.
6. M6: >=300s normal DEMO, actual cumulative frame metrics/P95/draw calls and
   memory samples, pause/resume. Emulator data never certifies Fold3.
7. Commit verified scope, push, PR to main, update STATUS/TASKS/SUMMARY.
   Keep active/IN_PROGRESS unless ALL task acceptance gates are satisfied.

Evidence root: .agent/evidence/T-051-art-visual-qa/. screenshots contains only
actual Android captures; raster contact sheets live in comparisons/atlas-review.

## First defect

Required test_t049_scenery fails on irrigation_channel.svg right alpha edge.
Art agent owns its source correction and raster verification. No assertions
relaxed. Coast/Mountain are catalog-only in current production scene model.

## Executed checkpoint

M0 PASS: latest main fetched/pulled and branch created, existing Windows API35
FullHD emulator reused,33 protected hashes match. M1 PASS after retained source
clipping/API/catalog-fixture failures: required art/local checks,189core tests,
debug/testAPK build. M2 original83 +16new =99SVG raster review, nine source
corrections. M3 shared aspect-fit/perched bird fixes,8actual traffic regression
shots reviewed. M4 adds16Coast/Mountain sources; FX/UI/gestures/POI open. M5
TC01–TC16 actual captures, classic/legacy APKs installed, labelled catalog.
M6 actual301.005s normalDEMO + pause/resume,33.073FPS/P95upper44ms at300.158s;
9 managed textures,10–14draw-call samples, measured PSS. Smoothness/flicker,
physical performance/landmark identity and full artwork runtime still OPEN.
No DONE; no merge/CI. SUMMARY.md contains commands, limitations and artifacts.
