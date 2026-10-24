# T-075: Reproducible build cleanup (2026-10-20)

Owner: Main. Dependency: owner-authorized host maintenance.
Acceptance: remove only ignored, untracked compiler intermediates; preserve source, APKs, historical captures, test reports and existing worktree changes.

Removed Android intermediates/generated/tmp/kotlin, core classes/tmp/generated/transforms, desktop classes/tmp/generated. No Java/Gradle processes were running. All target paths were verified ignored and without tracked files. Git status (including untracked paths) matched exactly before/after deletion, before adding this maintenance record. Freed available space: 200196096 bytes (about 191 MiB). Historical 3.6 GiB root build galleries and APK outputs retained because historical evidence cannot be assumed reproducible from current sources.

Detailed host log: /home/kunrg/system-maintenance-2026-10-20/build-cleanup-log.json. No code changed; build/tests were not rerun because doing so would recreate the removed outputs. Rebuild behavior NOT VERIFIED during cleanup.
