# T-049 — traffic depth verification, 2026-10-14

Owner Main; dependencies T-048 and active T-049 artwork branch.
Status: implemented traffic slice; overall T-049 IN_PROGRESS, PR56 Draft.
Physical Samsung DeX, parent art approval and larger bespoke artwork gates
remain NOT VERIFIED. No CI enabled, no merge into main.

## Implementation and audit

See [render audit](audit.md). Tire-based depth dispatch now surrounds bus and
passengers, after the POI panorama. Lane ground/scale, vehicle dimensions,
source wheel anchors, speed and deterministic entry spacing are centralized.
Two reused lane slots; no new atlas, frame allocations, sorting or texture load.
Six active SVG shadows corrected; truck cargo silhouette made taller.
The12 M10 VEH sources were reviewed; they remain catalog, not runtime traffic.

## Executed checks

- Core:188 tests,0 failures/errors/skips, including10 TrafficDepthModel tests.
  `core-test-count.json`, `build-final.txt`.
- Android debug and AndroidTest APK, desktop classes: Gradle build success.
  Android JVM unit task is NO-SOURCE, not an executed Android test suite.
- Local Python contracts, art/atlas and privacy/UI guards: `local-tests.txt`.
- Explicit T-049 source/size/alpha/determinism and source hash/tire contact
  checks: `art-tests.txt`, `traffic-contract.txt`.33 protected source hashes
  unchanged, all6 active SVG tire planes match metadata and have transparent
  canvas edges. Sâu/Ong and T-048 source/copy code unchanged.
- Windows emulator DeX_Stability_API35_20261005, Android15/API35, x86_64,
  SwiftShader, actual1920x1080, density160. Metadata text files retained.
  No real GPS, microphone, provider requests or physical DeX measurements.
- Framework instrumentation `mode=t049_traffic`, `display_id=0`: actual
  production SpriteBatch/atlas rendering with frozen synthetic DEMO fixtures,
  BOTH audience;8 captures per configuration. Runner restores preferences.
  PASS in instrumentation means fixture/capture success; visual review below
  is separate. It does not certify all gameplay or real traffic behavior.

## Screenshot review

[Before/after comparison](comparison.png) · [near sedan](after/sedan-near.png) ·
[far sedan](after/sedan-far.png) · [near truck](after/truck-near.png) ·
[POI backdrop](after/poi-backdrop.png) · [source art review](art-audit.png).
24 original FullHD captures have dimensions and SHA256 in `screenshots.json`.

`before/`: original182841d renderer and6 SVG source versions built with the
new test harness. Baseline has no near lane; "near" names describe the desired
scenario and deliberately expose its old all-behind-bus behavior. Original
vehicle placement x500 for baseline; final far tests use x800 so the front
silhouette remains visible beyond the bus. These are controlled comparisons,
not pixel-identical background/time captures.

`after/`: final PNG bus mode. `legacy/`: final traffic with legacy bus mode.
Each has sedan-far, sedan-near, truck-far, truck-near, scooter-near,
bicycle-near, two-lanes and poi-backdrop (both children in all cases).

Reviewed final PNG images at full resolution: near sedan covers the bottom
of the bus front tire; near truck overlaps lower bus/tire while retaining
cabin/driver and passengers. Far sedan/truck silhouettes disappear under the
bus at intersection, with the unoccluded front visible past its edge. Scooter
and bicycle are smaller than cars, tires sit on the same near plane, no crop
or white rectangle. POI panorama is visibly present behind the traffic; alpha
blend code is unchanged. All Story companions and native HUD text/buttons
remain above traffic. Representative legacy sedan/truck/POI images reviewed
with the same layer behavior. Source geometry tests cover both bus modes.

Still open: parent aesthetic approval; physical DeX performance/lifecycle;
long-duration runtime flicker/performance acceptance. No FPS/VRAM benchmark
claim. Mathematical motion/pause/chunk continuity tests are deterministic
unit evidence, not a recorded physical driving session or full traffic simulator.

## Retained failures and fixes

Initial Gradle failed because default NDK27.2 compiler path was missing;
rerun with installed DEXKIDS_NDK_ROOT=/home/khuongnd/.local/opt/android-ndk-r27c.
First emulator fixture failed focus readiness due Android's first-use
"Viewing full screen" confirmation. Dismissed the visible system tooltip;
rerun captured all fixtures. Logs retained. An initial too-low near plane65
was raised to95 after inspecting an actual DEMO capture; truck height capped225
so its complete canvas remains below companion interaction y315. Early fixture
POI injection set only cachedScene; corrected full director state and recaptured
actual panorama. These early snapshots are not final acceptance images.

## Reproduce final verification

```sh
export JAVA_HOME=/home/khuongnd/.local/opt/jdk17
export ANDROID_HOME=/home/khuongnd/Android/Sdk
export DEXKIDS_NDK_ROOT=/home/khuongnd/.local/opt/android-ndk-r27c
export PATH="$JAVA_HOME/bin:/home/khuongnd/.local/share/dexkids-qa-venv/bin:$PATH"
bash scripts/test-local.sh
./gradlew :core:test :android:assembleDebug :android:assembleDebugAndroidTest :desktop:classes
# Install debug + AndroidTest APKs on the explicitly selected API35 emulator.
adb -s emulator-5554 shell am instrument -w -e mode t049_traffic -e display_id 0 \
  com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
# Pull /sdcard/Android/data/com.khuongnd.dexkids/files/qa-t049 only after completion.
```
