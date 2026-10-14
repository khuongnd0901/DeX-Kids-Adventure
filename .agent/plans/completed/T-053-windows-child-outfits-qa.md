# T-053 Child Costume Integration & Visual QA
Owner Main. Dependencies T-048/T-051/T-052. State DONE for requested Windows Emulator scope.
Base fetched origin/main 8ee864de4ec675dd3ae8be2f7138b0ab81e738f3.

## Execution and acceptance
1. M0/M1 preserve unrelated untracked evidence; sync latest main and inspect existing Windows AVD.
2. M2 run all requested Python/local/Gradle checks; retain failures and fixes.
3. M3/M4 inspect original story/passenger crops; implement pose-specific clothing geometry without modifying protected source PNGs. Verify twelve costumes × four poses in story and bus.
4. M5/M6 instrumentation-only deterministic costume/time fixture; runtime rotation, lifecycle, audience and five age migration cases.
5. M7/M8 capture actual Android screenshots; inspect face/limb preservation and HUD; measure emulator performance and texture lifecycle.
6. M9 commit/push verified slices regularly; PR to main; merge only after all requested gates pass. No CI.

## Evidence
.agent/evidence/T-053-child-outfits/SUMMARY.md. Static compositions are comparisons only, never emulator evidence. Physical Fold3 unavailable; no equivalent performance claim.

## Progress
M0 verified: no tracked modifications, historical untracked files preserved, fast-forward main, reference ancestor check succeeds, requested branch created.
M1 verified: Windows adb.exe/emulator.exe D:\Android\Sdk; existing emulator-5554 DeX_Stability_API35_20261005 API35, physical1920x1080 density160. No additional ADB server or AVD created.
M2–M8 verified; see final SUMMARY/visual-review.csv/194core tests and actual runtime logs. M9 seven verified feature pushes complete; PR61 merged atb0f5dd7, exact source headd46f1c0 and full tree equality verified. Physical DeX/smoothFPS NOT VERIFIED, outside Windows task acceptance.

Acceptance complete; final evidence/merge receipts under T-053-child-outfits. Original local untracked historical work preserved. No CI enabled; separate physicalDeX/performance gates NOT VERIFIED.
