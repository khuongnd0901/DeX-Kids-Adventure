# Build and installation — 2026-10-09

Actual JDK17.0.20.1 / Gradle8.11.1 / Linux SDK35. Commands ran locally, not inferred from CI.

| Command/result | Evidence under build/simulator-artifacts | Status |
| --- | --- | --- |
| Initial ./gradlew --version with CRLF shebang failed; locally normalized wrapper | environment.md | FAIL → corrected |
| :core:test :desktop:classes :android:assembleDebug | baseline-build.log, 42 executed tasks,2m39s | PASS |
| Expanded core56 tests/0fail + desktop classes + debug + instrumentation APK | recreation-continuity-build.log | PASS |
| aapt package/launcher verification, install-r and ParentActivity launch | build-install-20261009T014050Z-3802 | PASS |
| Same-version debug reinstall, retained app settings | repeated actual install-r, soak setup | PASS |
| Production signed version upgrade | No prior release artifact/signing | BLOCKED |
| SVG18-region generation and alpha/atlas validation | baseline-build.log, atlas-validation.txt | PASS |
| Desktop WSLg render then native shutdown SIGSEGV | desktop-fixed.log, hs_err_pid11263.log | FAIL |
| Desktop software GL retry, actual1080p PNG and dispose log | desktop-software.log, desktop-software-1920x1080.png | PASS |

Package/activity came from AndroidManifest and aapt: com.khuongnd.dexkids / ParentActivity.
KidsActivity is nonexported; game launched only by explicit parent preview in emulator tests.
Running60-minute APK source a4e1f34 differs from subsequent lifecycle/GPS/HUD fixes.
Artifacts local-only; environment.md gives WSL/Windows access paths. Final post-fix Android runtime pending.

Verified GitHub checkpoint `ea8597e952e53fa9fc55f3cde08c61125c97a89f` also has actual completed SUCCESS CI [37874635344](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37874635344). This does not substitute for later Android runtime regression or final-commit CI.

Final source bad299a follow-up completed: actual permission/offline/recreation PASS `privacy-20261009T032118Z/`; final post-fix smoke capture126seconds and GL disposal log PASS. See final-test-summary.md for current gates and restoration.
