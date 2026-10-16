# T-062 verification — 2026-10-16

Owner Main. Base origin/main `702405f`; branch `feat/T-062-remote-story-source`.
Implementation complete; tested source remains uncommitted/unpushed. Full task
IN_PROGRESS because physical DeX acceptance is NOT VERIFIED. No user-hosted URL
was supplied: live GitHub/custom-host end-to-end is NOT VERIFIED. No content was
published, no AI calls made, and no other repository accessed.

## Implemented

Public HTTPS pack URL in the parent library; automatic foreground download with
five-minute throttle/single-flight; manual refresh; conditional GET; bounded
strict UTF-8/schema validation; cached offline playback; whole-pack activation
that waits for all playback leases. Remote provenance is explicit and immediately
playable without AI or per-item approval. Local copies are unreviewed drafts.
SQLite v1→v2 migration adds remote tables without changing local content tables.
Remote IDs are source-namespaced; content changes reset remote playback cursors,
retain favourites and create revisions; identical content does not. Removed
stories are no longer selected after idle activation. Cache retains three sources.
Unactivatable staged content does not prevent fetching a corrected pack.

Sample: `docs/story-source/stories.example.json` (three authored audience fixtures).
Guide: `docs/story-source/README.md`. No default remote URL or secret bundled.

## Commands and results

Environment: Linux JDK17 `/home/khuongnd/.local/opt/jdk17`, SDK
`/home/khuongnd/Android/Sdk`, NDK
`/home/khuongnd/.local/opt/android-ndk-r27c`. Windows API35 x86_64 emulator
`DeX_Command_Test_API35`, FullHD1920×1080, explicitly selected display0;
Windows adb on port5038 (Linux adb on5037 cannot discover this Windows emulator).

Final Gradle invocation (all three environment paths above set):

```text
./gradlew :core:test :core:checkStoryPack :android:assembleDebug :android:assembleDebugAndroidTest :desktop:classes --console=plain
```

`build-final.log`: BUILD SUCCESSFUL, 50s, 71 actionable tasks. Core: **220 tests,
0 failures, 0 errors, 0 skipped**. Example pack validates with three stories.
`python3 tools/test_child_voice_privacy.py`: PASS (static privacy contract).
`git diff --check`: clean. No asset regeneration/source-art edits.

APK install/update succeeded on the emulator. Actual framework instrumentation:

```text
adb.exe -P 5038 -s 127.0.0.1:5583 shell am instrument -w -e display_id 0 -e mode <mode> com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
```

| Mode | Actual result |
| --- | --- |
| t062_remote | PASS: actual v1→v2 SQLite migration; local approval/favourite/cursor retained; mocked HTTPS add/304/throttle/identical-content/deferred-update/removal/offline/error/source-switch/disable/copy/read-only/pending-recovery assertions |
| t062_playback | PASS: actual KidsActivity remote SAU/ONG/BOTH load; paused revision survives pending sync; stop activates revision2; next load uses revision2; removal deferred; no leaked playback leases |
| t056_library | PASS: existing55-story SQLite/100-selection/revision/undo/delete/approval/cursor suite (earlier same storage implementation; later production changes confined to sync recovery/UI refresh handling) |
| t057_manager | PASS: actual F10/library/source dialog footer + HTTP URL rejection; local draft/approval/edit/cancel/undo/replay result flows |
| t059_story_lifecycle | PASS: actual Activity recreation/cursor, stale/deleted revision refusal, stopped-load callback suppression |
| t059_replay_no_network | PASS: AI/automatic/creative enabled with ready fake provider, zero gateway calls during replay/autoplay startup |
| t057_cancellation | PASS: queued/running UI cancellation releases busy correctly; subsequent real DB operations succeed |

Remote HTTP responses in instrumentation and Java transport tests are **mocked**;
SQLite, application lifecycle callbacks, Activities and playback controllers are
real Android executions. The nine new JUnit tests include malformed JSON/UTF-8,
duplicate keys/IDs, schema/depth/count/size limits, audience fixtures, HTTPS-only,
redirect downgrade/limit, validators, HTTP404/500, stream overflow, timeout and
connection cleanup. These results do not assert actual external-host availability.

Final artifact SHA256 (see `results.json`):

- app debug,26161093bytes: `6c2b5a0ca48edd621d06f208c4693fe607b482d3ef8b13c849b2258427e04f96`
- test debug,411935bytes: `15c74c0962e1804550f7e15268e1a755dbaf4de5a7872efba0abf4ef66c8d001`

Eight manager screenshots retrieved; `01-library-draft.png` visually inspected:
new source controls visible, local draft row retained. Existing editor footer
assertions also passed. All QA preference mutations were restored by fixtures.

## Retained failures and limits

Initial `t057_manager` invocation omitted explicit `display_id 0` and failed its
FullHD/emulator routing precondition before running UI actions. Retained in
`t057_manager-missing-display-argument.txt`; corrected command passed. Initial
ADB discovery failed because Windows emulator and Linux server used different
network contexts; isolated Windows adb port5038 resolved discovery.

No physical Fold3/DeX device attached. No live user URL, real child ASR, navigation
coexistence, endurance, release signing or editorial quality certification claimed.
Migration verified with an actual isolated v1 database; cached data survives helper
close/reopen, but no new OS force-stop/crash-injection run was added in T-062.
