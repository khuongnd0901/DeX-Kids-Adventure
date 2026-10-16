# T-062 — source publication candidate — 2026-10-16

Owner Main. User explicitly authorized committing, publishing and merging all
T-062 source to main. Source branch feat/T-062-remote-story-source, base25b679c.
Acceptance: verified source/tests/docs + scoped evidence published; main includes
that exact source tree. Physical DeX gate remains NOT VERIFIED, not marked DONE.

Pre-publication command:

```text
JAVA_HOME=/home/khuongnd/.local/opt/jdk17 ANDROID_HOME=/home/khuongnd/Android/Sdk DEXKIDS_NDK_ROOT=/home/khuongnd/.local/opt/android-ndk-r27c ./gradlew :core:test :core:checkStoryPack :android:assembleDebug :android:assembleDebugAndroidTest :desktop:classes --console=plain
```

BUILD SUCCESSFUL in12s,71tasks,1executed/70up-to-date. Core results contain220tests,
0failures/0errors/0skips; source and previous real Windows live-host/offline QA
unchanged. Pack checker validates3stories. `git diff --check` clean. Prior evidence
under this directory retains scope and initial failures.

Publication includes implementation, meaningful unit/framework tests, architecture
and authoring guides, active plan, task/status updates and textual/JSON evidence.
Raw screenshots/XML, duplicate adb-pull directories and ignored build logs remain
local; no APK, credential store or unrelated historical QA artifacts are included.
No private preference backup is present in exported live-test results.

Git CLI push dry-run failed because its configured credential helper points to
missing `/tmp/dex-gh-cli/bin/gh`. Use the existing GitHub connector for Git objects,
branch, PR and merge; verify the published Git tree equals the verified local
commit tree. Preserve the original local commit in a backup ref if the connector
uses a different author/commit SHA. No force push or branch protection bypass.

Final publication outcome will be recorded in PUBLICATION_RECEIPT.md and GitHub PR.
