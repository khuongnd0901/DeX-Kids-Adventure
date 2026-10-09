# Simulator capture tools

Run from any directory with an explicit ADB serial. Existing device/AVD configuration is preserved. `ADB_BIN` can be a Linux ADB binary or the absolute Windows `adb.exe` path; Windows file transfers use `wslpath`. Scripts need Bash, `rg`, Java and the relevant SDK tools.

```bash
export ADB_BIN=/path/to/adb
scripts/simulator/env-check.sh --serial emulator-5554
AAPT_BIN=/path/to/sdk/build-tools/35.0.0/aapt scripts/simulator/build-install.sh --serial emulator-5554
scripts/simulator/visual-capture.sh --serial emulator-5554
```

Build uses executable Gradle wrapper, otherwise global `gradle`; `GRADLE_BIN` overrides this choice. Build/install verifies APK package and launcher using `aapt` and opens only ParentActivity. Start the child preview through the parent consent UI before sampling. It never grants permissions or bypasses child routing.

```bash
scripts/simulator/smoke-test.sh --serial emulator-5554
scripts/simulator/stability-test.sh --serial emulator-5554 --duration 600
scripts/simulator/stability-test.sh --serial emulator-5554 --duration 1800
scripts/simulator/stability-test.sh --serial emulator-5554 --duration 3600
```

Durations are actual wall-clock seconds. Sampling intervals default to 15 seconds and cannot exceed 30 seconds. Each sample requires the original process PID and a resumed KidsActivity. PID-scoped logcat captures only the tested app process, and only that collector is terminated when the script exits. Fatal signatures, a missing process, a restart or leaving the child activity cause a failure. Script success means capture completed; it does not automatically declare visual, memory, FPS or crash/ANR acceptance.

Timestamped artifacts live under `build/simulator-artifacts/`, including screenshots, logcat, per-sample raw gfxinfo and meminfo, activity state, and UTC/elapsed timestamps. Review screenshots and app logs, calculate metrics from available samples, and report metrics as NOT_VERIFIED when the renderer exposes insufficient frame data. A sampler can detect an absent/restarted process or a logged fatal error; it cannot prove complete system ANR coverage. Emulator runs never close physical Fold3/DeX acceptance gates.

`--help` works without a connected device. Invalid/missing arguments return 2; absent tools/device return 3; capture/runtime assertion failures return 4. SDK/ADB/Gradle failures propagate nonzero. No APK/cache or raw capture artifacts should be committed automatically.

Verified locally during implementation: `bash -n` on all scripts; help for every entry point; synthetic fake-ADB assertions for missing serial (exit 2), oversized interval (exit 2), and resumed ParentActivity instead of child (exit 4). These fixture checks validate script guards, not Android runtime behavior.

For this WSL + Windows SDK environment:

```bash
export JAVA_HOME=/home/khuongnd/.local/opt/jdk17
export ANDROID_HOME=/home/khuongnd/Android/Sdk
bash scripts/simulator/bootstrap-python.sh --venv /home/khuongnd/.local/share/dexkids-qa-venv
export PATH=/home/khuongnd/.local/share/dexkids-qa-venv/bin:$PATH
export ADB_BIN=/mnt/d/Android/Sdk/platform-tools/adb.exe
AAPT_BIN=$ANDROID_HOME/build-tools/35.0.0/aapt bash scripts/simulator/build-install.sh --serial emulator-5580
python3 scripts/simulator/functional-test.py --serial emulator-5580
bash scripts/simulator/security-probe.sh --serial emulator-5580
bash scripts/simulator/gpx-replay-test.sh --core
```

Functional UI checks temporarily resize the emulator and reset those overrides. The separately signed
probe verifies different UID/certificate and child survival; it requests visibility of only this
project's package. ADB broadcast result alone is inconclusive. Source model narration tests never
claim that speech is actually wired or playing.

`prepare-soak.py --serial SERIAL` is restricted to `ro.kernel.qemu=1`; it installs debug APK,
backs up parent preferences and loads a clearly synthetic60-minute/quietON/TTSOFF fixture.
It starts child only through the explicit parent-preview button. Use its original setup directory
with `restore-soak.py --serial SERIAL --setup DIRECTORY` after collection. Repeated setup directories
may contain an earlier fixture; restore the **first original backup**. The parent limit remains active;
expected timeout can cause the strict stability script to fail when child stops at60minutes. Analyze
actual session-start/limit logs separately; never hide an early or unexpected stop.

`activity-recreation.sh --serial SERIAL` installs framework instrumentation (build
`:android:assembleDebugAndroidTest` first). It uses parent preview, actual `Activity.recreate()`,
real screenshots and preserved absolute deadline assertions. Run it only after any active soak ends.

`visual-series.sh --serial SERIAL --duration SECONDS --interval 30` captures real screenshots
without changing app state. `collect-evidence.sh --serial SERIAL` snapshots PID-scoped logs and
resources. Artifacts remain ignored/local; they are not automatically uploaded or committed.
Desktop WSLg native shutdown initially crashed; an actual software-GL retry succeeded using
`LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe`. That desktop result is separate from Android.

`security-probe.sh --serial SERIAL --trusted` uses only the local debug signing key to test authorized STOP; it does not establish production Assistant signing compatibility. Restart an explicit preview before each probe. `analyze-evidence.py --run DIRECTORY` calculates recorded-prefix metrics/PSS without automatically declaring acceptance.


`privacy-test.py --serial SERIAL` is emulator-only and needs the built Android test APK. It starts
only this app through framework parent Button.performClick, clicks only the denial node, and
never grants a permission. Existing grants cause the denial case to be BLOCKED. It tests actual
airplane/Wi-Fi-off/no-active-default-network preview and recreation, waits at most15seconds for
network teardown, and restores radio state even on failure. No coordinate dialog taps. A grant
that unexpectedly changes an initially denied state is reported FAIL and revoked. Do not run
while a soak/session is active. USER_SET/USER_FIXED denial flags may change during real tests;
record/reconcile emulator state afterward. All screenshots still require human visual review.
