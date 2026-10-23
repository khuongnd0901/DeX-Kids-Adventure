# T-075 initial migration checkpoint — 2026-10-23

Latest branch state adds [configured ages](configured-ages/SUMMARY.md): core293/
Android35and updated installed APK. The migration receipts below describe the
initial4b0cfd1checkpoint and its historical artifact hashes. Live gates stay open.

Status **IN_PROGRESS**, publish as Draft PR. Owner explicitly defers live account
sign-in; **LIVE_AUTH_NOT_VERIFIED**. Physical Fold3/DeX is NOT VERIFIED. No merge.
Kids origin/main `81fa123ed8a89e47b2326692c7d7a868532540f3`; read-only Assistant
reference `a52ddf0ecf3022922401f222f4db2d71282d3a9d`. Public Kids repository metadata
independently checked via connected GitHub. No Assistant edits, push or token import.

## Reproducible host verification

Use the existing QA environment (default Python lacks Pillow; default NDK discovery
misses the installed compiler):

```bash
export PATH=/home/khuongnd/.local/share/dexkids-qa-venv/bin:$PATH
export JAVA_HOME=/home/khuongnd/.local/opt/jdk17
export ANDROID_HOME=/home/khuongnd/Android/Sdk
export DEXKIDS_NDK_ROOT=/home/khuongnd/.local/opt/android-ndk-r27c
./gradlew --no-daemon --max-workers=2 :core:test :android:testDebugUnitTest \
  :android:assembleDebug :android:assembleDebugAndroidTest :android:lint
bash scripts/test-local.sh
```

Both final commands **PASS**. Core **290/290**, Android **32/32**, zero failures,
errors or skips. Full lint **0 errors / 89 warnings**. App and instrumentation APKs
built. XML-derived counts and artifact/log SHA256 receipts: [verification.json](verification.json).
Final parent help copy matches the retained10RPM gate and distinguishes content retries
from terminal subscription failures; unused key-editor imports removed. A final
app/testAPK/unit/lint rebuild and T075Windows fixture rerun PASS after that copy cleanup.
Source/art/geo/consent/contracts in test-local PASS; static all-production-source
scan finds zero Gemini/Groq provider/key/endpoint paths. YouTube sources unchanged.

Auth tests: callback state/code/client binding and duplicate rejection; OIDC nonce,
RS256 signature/issuer/audience/azp/lifetime; synchronized refresh and rotating-token
preservation; logout/revocation failure and refresh race; terminal expiry invalidation.
Transport tests: terminal completion only, finished-item reconciliation, malformed/
incomplete/failed/truncated/contradictory SSE, quota/HTTP,401 once, cancellation during
auth and active reads, timeout/no replay, two physical401 starts counted, catalog
visibility and unsupported-field omission. No real account used. Staged secret/artifact and relative Markdown link audit PASS;
two pre-existing T018memory links corrected to their existing ../docs target.

## Actual Windows simulator

Windows `DeX_Command_Test_API35`, WHPX/SwiftShader, `emulator-5580`, Android15/API35,
x86_64; external virtual display **2**, **1920×1080**. SurfaceFlinger capture ID
`11529215047770711573`; Android display ID2 is not a screencap display token.
Existing AVD data/config kept. `scripts/simulator/env-check.sh --serial emulator-5580`
PASS using Windows ADB. Native settings was opened through parent menu on display2,
never by failed child inference. The app/test APKs were installed with `install -r`.
Original and final certificate SHA256 both
`9ddb7f477828847e3908328c4cfea0bb9d5c50124a41743dd42c2443abd83d03`.
Final installed APK re-pulled and byte-hash matched final build (see verification).

Example focused fixture command:

```bash
/mnt/d/Android/Sdk/platform-tools/adb.exe -s emulator-5580 shell am instrument -w \
  -e mode t075_subscription -e display_id 2 \
  com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
```

| Actual Android gate | Result / receipt |
| --- | --- |
| New gateway + storage + UI | PASS [t075_subscription-final.txt](t075_subscription-final.txt): real Keystore roundtrip, encrypted-token absence from raw record, registration retained on logout; actual gateway parsing for POI/trip/creative/story+review+regeneration; ages5/4; malformed schema, child consent/privacy, revoke/late-return denial; UI disconnected/refused test and synthetic expired/quota rendering |
| Physical rate starts | PASS [ai_limiter.txt](ai_limiter.txt):601fake-clock reservations, persisted10RPM/6100ms/rollback; zero network |
| Creative JSON review/cache | PASS [creative_review.txt](creative_review.txt), [creative_cache.txt](creative_cache.txt): strict verdict parsing, AtomicFile/cursor/checksum/last-good/age |
| Coordinator/fallback/cancellation | PASS [creative_coordinator-final.txt](creative_coordinator-final.txt): worker/recreation/refill guards; each injected subscription quota/incomplete/network error gets exactly one attempt, no retry, last-good pack retained |
| Private story inputs / failure/late output | PASS [t054_story_safety.txt](t054_story_safety.txt), [t054_story_failures.txt](t054_story_failures.txt):42private input cases, no dispatch; cancellation at author/review cannot persist late draft |
| Saved replay without inference | PASS [t059_replay_no_network-final.txt](t059_replay_no_network-final.txt): real Activity replay/autoplay blocks ready injected gateway; zero AI calls |
| Story lifecycle/recreation | PASS [t059_story_lifecycle.txt](t059_story_lifecycle.txt): persisted cursor, stale load/revision/deletion handling |
| FullHD story smoke | [181336ms /23captures /3loads](t059_story_smoke-retry.txt), framework RESULT_OK(-1): actual GL/HUD, SQLite edit/approval invalidation/undo and simulated downstream POI pause/resume. No complete natural episode recorded (`completed_audiences=[]`); not a full-story completion gate |

Four committed [screenshots](screenshots/settings-disconnected.png) were visually
reviewed: native settings, disconnected test refusal, Sâu5 story fixture and BOTH
post-POI replay. Text/buttons fit and rendering is present; broad aesthetic approval
or real-road correctness is not inferred. All23story captures remain local.

[Measured story samples](story-performance.json):16samples; final rolling cumulative
average31.015FPS, P95upper48ms, PSS81018KiB,8managed textures. Final settings PID5382
PSS55683KiB. Not a matched baseline/candidate benchmark; no performance/memory
improvement, stable60FPS or physical-device performance claim. No crash/ANR signature
observed in collected final-PID logcat; this is not exhaustive OS/long-soak ANR coverage.
Raw PID logcat, meminfo and private backups are local only; sanitized logcat receipt
is [logcat-summary.txt](logcat-summary.txt). TTS-active counters do not prove audible
Vietnamese speech or navigation audio coexistence.

## Preservation and retained failures

Before installation, original APK and35private data records backed up locally.
Post-QA comparison:31/35identical,0missing. Fixture/application use advanced an
opening counter, source-success timestamp and remote-story playback metadata;
original two preference files plus SQLite DB/journal restored with target stopped.
After restoration **34/35 original files byte-identical**, including all legacy AI
credentials and saved settings/content/DB. Only ART `files/profileInstalled` differs
legitimately after APK installation. Three new test/empty-subscription records are
not replacements of original data. No uninstall, app-data clear, permission grant,
account/session import or removal of old credential aliases. Final target is stopped
after restoration, avoiding another auto-sync/write. AVD remains booted.

Initial failures retained rather than labelled PASS:
- Linux SSH fetch failed host verification; existing Windows OpenSSH identity fetched
  and is used for publication, with normal host verification.
- Initial build lacked Pillow, then NDK path; corrected to installed venv/NDK above.
- An Assistant-specific proof test was initially copied with auth tests, failed compile
  and was removed because Kids has no device-command proof contract.
- First overlapping art-build/contract execution failed determinism; discarded and
  rerun serially. Final full local suite PASS; no source art change.
- Lint exposed2existing diagnostics: explicit equivalent API31 guard makes the existing
  on-device ASR availability boundary visible; equivalent LineBreaker constant fixes
  HUD annotation type. No speech or rendering behavior change. Final lint PASS.
- First requested T075 invocation used an older test APK lacking its mode and entered
  generic recreation. It is not T075 evidence. Latest APK's explicit fixture receipts
  above replace it.
- First story smoke failed missing external capture token; [failure](t059_story_smoke.txt)
  retained. Correct virtual token and180s rerun succeeded as described above.
- Streaming stdin restoration with Windows adb/dd did not close EOF; killed that
  specific QA dd and restored by bounded file copies, then verified34/35exact hashes.

## Open acceptance

Owner chose to defer live login. **LIVE_AUTH_NOT_VERIFIED** includes actual Kids
registration/redirect/browser roundtrip, real entitlement/model catalog/refresh/
quota, real model JSON/schema/content compatibility and live logout/reconnect.
Synthetic tests never establish live success. One saved account only; subject must
match reconnect. JSON schema is prompt guidance plus local validation, not a claimed
provider structured-output guarantee. Unsupported token caps may reject long output.

Physical Fold3/DeX, real Vietnamese TTS/ASR, vehicle/navigation audio coexistence,
full natural story completion, matched performance/memory regression and long soak
remain NOT VERIFIED. Existing broader content/art/hardware gates remain open.
Implementation and simulator slice are verified; overall T075 is not DONE.

Rollback: review/revert only this feature branch's source commits; preserve all
app data. Leave subscription consent OFF and use existing saved/bundled offline
content. Never resurrect a silent legacy-provider fallback or uninstall/clear data.

## Publication

Verified source **4b0cfd1a658effc325ad0286f7b58465c5fcc510** committed/pushed on
`feat/T-075-chatgpt-subscription-migration`. Independent `git ls-remote` and
GitHub head_sha both match. [Draft PR66](https://github.com/khuongnd0901/DeX-Kids-Adventure/pull/66)
targets main, unmerged. [Publication receipt](publication.json). Documentation-only
receipt commit follows; no source changes after the verified implementation.
Final post-install/post-QA data comparison again confirms34/35original records
byte-identical, with only ART profileInstalled differing. Overall IN_PROGRESS.
