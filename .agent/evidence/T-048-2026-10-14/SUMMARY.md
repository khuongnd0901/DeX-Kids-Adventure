# T-048 local + Windows emulator acceptance — 2026-10-14

Owner Main. Branch `feat/T-048-capybara-bus-art-import`, Draft PR #55.
Source: original binary commit faba918; runtime/pipeline/test commit df955fa.
Main checkout's unrelated modifications preserved; isolated worktree
`/mnt/d/DeX-Kids-T048`. No source from other repositories read or modified.

## Results

| Gate | Actual result | Evidence |
|---|---|---|
| M0 immutable import and remote PNG blobs | PASS, five originals SHA/RGBA/size/alpha match ZIP and repository | M0_BINARY_IMPORT.md, local/source-verify.txt |
| M1 atlas/source integrity | PASS; repeat-build bytes match; corrupt source rejected; alpha/gutters/baseline verified | local/art-test.txt |
| Legacy atlas preservation | PASS, 30 original regions | local/legacy-atlas-test.txt |
| Local contracts + core JUnit | PASS; 178 tests, 0 failures/errors/skips | local/test-local.txt, local/core-test-count.json |
| Android debug + test APK, desktop classes | BUILD SUCCESSFUL, 51s; Android unit NO-SOURCE | local/gradle-build-retry.txt |
| Windows install + launch/render | Emulator PASS; both APK installs Success | emulator/install.txt |
| M2 four actual rendered poses | Emulator PASS, 8,900ms, unchanged texture snapshot | emulator/t048-art.txt, emulator/qa-t048/*.png |
| Same-display parent/menu/pause/stop regression | Emulator PASS, 2,759ms | emulator/single_display_mouse.txt |
| Route story selection/7 cards/Vietnamese overlay | Emulator PASS, 9,589ms | emulator/p0_route.txt, emulator/p0-route.png |
| Full/Audio-only scenery lifecycle, 100 cycles | Emulator PASS, 51,527ms; managed texture count one in Audio-only | emulator/scenery_residency.txt |
| Normal DEMO after test cycling | 12 sampled graphics frames nonblack; natural answer/scene visible; KidsActivity resumed | emulator/normal-demo-samples.json, normal-demo-{00,11}.png |
| Physical Samsung DeX / Fold3 / FPS / thermal / GPU leak | NOT VERIFIED | No attached physical equipment tested |
| Human/child appeal and release licensing | OPEN | Parent/redistribution signoff not implied by generation |

## Implementation and limits

New PNG atlas is one 1024 RGBA page with 5 regions, **4 MiB texture backing**
(not total GPU/driver VRAM). Strict allowlist, SHA256 and source bounds precede
packing. Shared 224×288 pose canvas aligns perceptible feet without stretching;
bus uses alpha crop, 850×600 derived pixels and actual trimmed source ratio in
VehicleLayout. Linear filtering and extruded gutters. No original art deleted;
all generated runtime outputs are reproducible and ignored.

Default `assets/art-mode.txt=png`; set `legacy`, rebuild/reinstall for rollback.
Legacy atlas/source retained and validated. Legacy mode was source-reviewed,
but a separate legacy APK runtime was NOT RUN in this session.

Composite bus preserves bob/pitch; bypasses independent legacy driver/wheels.
Passenger source and gesture logic unchanged, render rectangles move to cabin.
Standing Capybara beside bus presents source IDLE/ASK/LAUGH/WAVE. Existing
question/answer phases drive ASK/cheerful LAUGH; greetings/wave retain WAVE;
interrupted/no active conversation maps IDLE. Cheer is not correctness grading.
Baked driver is static; PNG pack has no independent blink/talk/sleep frames.
Existing motion/narration controller untouched. Fade is bounded 180ms and
freezes on zero paused delta. No GPS/voice/AI/story/content/business-flow changes.

Fixed four-pose QA temporarily suppresses natural wave timing and native prompt
timers, using existing production prompt/phase APIs and GL renderer. These are
controlled visual fixtures, not actual provider/content or child acceptance.
Source gallery `local/runtime-art-gallery.png` is **not a device screenshot**.
Natural DEMO screenshot and sourced route regression are separate runtime flows.

## Screenshot review

Main inspected ASK/IDLE/LAUGH/WAVE, p0-route and normal-demo-11 screenshots:
new right-facing bus visibly replaces old bus; no independent old driver/wheel
layers visible; cabin children fit windows; Capybara poses visibly distinct and
share footprint/baseline. No art clipping, obvious dark alpha halo or stretched
bus observed in these screenshots. This scoped review is not parent art signoff.

User observed continuous black flashes during the deliberate 100 Full/Audio-only
cycles. That test disables/enables GL scenery intentionally and has ended.
Normal DEMO then restarted through the visible parent-preview button. Twelve
screenshots over roughly 12 seconds had nonblack graphics; first/last retained.
This does not prove absence of every-frame flicker over unlimited playback.
App left running in normal DEMO; no further cycling test active.

## Failures retained and corrected

Earlier missing ZIP/Pillow failures are in LOCAL_M0_BLOCKER.md. User then supplied
local ZIP path; existing QA venv resolves Pillow. First target build failed:
NDK missing and concurrent Gradle invocations raced on CartoonSprites$1.class.
Retain `local/gradle-build.txt`; sequential final build PASS after installing NDK.
First pose baseline check failed; isolated near-zero alpha tails caused drift.
Align visible alpha>=16 feet with shared canvas/soft-edge padding; final art
contract PASS. No fake PASS markers or failure deletion.

Official Android NDK r27c archive 663,987,688 bytes, SHA1
`090e8083a715fdb1a3e402d0763c388abb03fb4e`, matched SDK repository metadata.
Installed at `/home/khuongnd/.local/opt/android-ndk-r27c`, Pkg.Revision
27.2.12479018. Built unchanged native_heap_trim.c for all three existing ABIs.
SDK Manager download was too slow; ranged download assembled the same official
archive and full checksum validated before extraction. No native source edit.
Gradle native strip warnings are retained in successful build log.

## Reproduction

```bash
export JAVA_HOME=/home/khuongnd/.local/opt/jdk17
export ANDROID_HOME=/home/khuongnd/Android/Sdk
export DEXKIDS_NDK_ROOT=/home/khuongnd/.local/opt/android-ndk-r27c
export PATH=/home/khuongnd/.local/share/dexkids-qa-venv/bin:$JAVA_HOME/bin:$PATH
python3 tools/test_t048_art.py
python3 tools/test_sprite_atlas.py
bash scripts/test-local.sh
./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug :android:assembleDebugAndroidTest :desktop:classes
```

Windows emulator launched with `-avd DeX_Command_Test_API35 -port 5580
-no-snapshot -gpu swiftshader -no-boot-anim`; actual API35/Android15 x86_64,
1920×1080, density160. Run each instrument mode serially with Windows adb.exe:

```bash
/mnt/d/Android/Sdk/platform-tools/adb.exe -s emulator-5580 shell am instrument -w -e mode t048_art -e display_id 0 com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
```

Other executed modes: single_display_mouse, p0_route, scenery_residency. The last
intentionally flashes graphics during 100 Full/Audio-only transitions; use normal
DEMO afterward. Preferences restored by QA harness. No CI/manual workflow run,
provider test, child audio/GPS upload, physical-device or release acceptance claim.

M0–M2/local M3 delivered; Windows emulator functional slice PASS. Overall T-048
remains IN_PROGRESS / PARTIAL for physical and human/release gates. Plan stays
active; PR stays Draft, no main merge.
