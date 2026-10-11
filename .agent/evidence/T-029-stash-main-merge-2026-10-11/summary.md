# T-029 local stash / latest-main integration — 2026-10-11
Owner Main; dependencies local T-029 fixes, upstream T-030–T-032.
User explicitly requested merging stash with the latest pulled origin/main.
Working tree now on main, HEAD b75d825; changes remain uncommitted/unpushed.
Original stash retained: pre-pull-latest-2026-10-11-T029.

Resolved 10 conflicted files, keeping both documentation histories. Preserved
new upstream creative/session packs, parent-selected AI ages 4–10 and safe
VisualCue mapping alongside local day-period/cache, scenery, ambient and
Sâu reminder changes. Catalog now 96 quizzes + 80 words; 24 age-3 quizzes
removed and all authored learning cards minimum age4. AI POI generation/cache
and replies retain parent ages4–10. Dynamic age HUD stays intact. Enlarged
props apply to both VisualCue and legacy rendering; AI counting guard retained.

Initial validation: 20/21 contracts; upstream HUD test asserted Kotlin switch
syntax against Java ternary; corrected expression check without dropping gate.
Initial build: 139 tests / 3 failures and Android compilation error. Two
fixtures retained age3, corrected to age4. Upstream VisualCue interpreted
Vietnamese generic “điều” as kite; now requires explicit con/cánh/thả diều
or English kite, with a new regression test. Android prefetch referenced
shadowing KidsAiSettings.audienceMode; now uses this.settings.audienceMode.
Original failure log retained in build.txt.

Final validation: 21/21 source/data contracts PASS (contracts.txt).
140 core tests, 0 failures/errors/skips (core-test-totals.txt).
Gradle :core:test :android:testDebugUnitTest :android:assembleDebug
:android:assembleDebugAndroidTest :desktop:classes BUILD SUCCESSFUL in1m24s.
Android unit task NO-SOURCE; desktop classes compiled, no runtime claim.
JDK17 and CairoSVG/Pillow prepared under /tmp; Android SDK existing local.
No remaining unmerged paths; git diff --check clean.
No device install, instrumentation, provider/API calls, precise GPS/audio or
child-data transmission. Hardware/visual/audio/endurance acceptance remains
NOT VERIFIED. Overall T-029 and existing milestones remain IN_PROGRESS.
