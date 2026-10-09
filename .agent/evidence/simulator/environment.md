# Local environment — T-011, 2026-10-09

Source audited at baseline `d5796c246f3b78f1d5b893dcc0d0fd89854114ba`.
Actual OS: Ubuntu22.04.5 on WSL2 kernel6.6.87.2; Windows host10.0.26100 reported by WHPX.
Host WSL RAM15835MiB/swap4096MiB (not emulator app memory).
Default JDK25.0.4.1 is not the project build JDK. Existing JDK17.0.20.1 at
`/home/khuongnd/.local/opt/jdk17`; actual Gradle8.11.1 wrapper ran with that JAVA_HOME.
Linux build SDK `/home/khuongnd/Android/Sdk`, platform35, build-tools35.0.0.
Windows runtime SDK `D:\Android\Sdk`; ADB accessed from WSL as
`/mnt/d/Android/Sdk/platform-tools/adb.exe`. Every device command selected `-s emulator-5580`.

AVDs discovered: DeX_Command_Test_API35, DeX_Stability_API35_20261005, ZFold3_API35.
No device initially connected. Started existing ZFold3_API35 using port5580,
`-no-snapshot-save -no-boot-anim -gpu auto`; no AVD deletion/wipe/security change.
Emulator37.1.11/build15917651; WHPX usable; gfxstream/GLES3.0 translator on NVIDIA
RTX4060 Laptop GPU, host NVIDIA driver556.12. Intel integrated GPU also detected.
Guest Android15/API35, x86_64 Google APIs; initial1768×2208/density420, configuredRAM3072MiB,
2 virtual CPUs, GPS simulation supported by AVD config. This is a generic foldable,
not Samsung software/physical Fold3, and not real DeX.

Tests temporarily use wm1920×1080 and1280×720/density160/240; override restoration is explicit.
No dangerous permission grants; no host security changes. Synthetic parent settings fixture
for soak uses the source's existing60-minute limit, quietON/TTSOFF, originals retained.

Commands actually executed: git status/branch/diff, java-version, wrapper-version,
ADB version/devices/getprop/wm size/wm density/dumpsys display/meminfo/SurfaceFlinger,
emulator-list-avds/version/accel-check. Full raw local capture:
`build/simulator-artifacts/env-check-20261009T013308Z-2278/` and
`build/simulator-artifacts/emulator-start.log` (not committed: raw emulator log includes ADB public key).
Artifacts are local at `/mnt/d/DeX-Kids-Adventure/build/simulator-artifacts/`
(Windows `D:\DeX-Kids-Adventure\build\simulator-artifacts`); copy locally to review.
No shared artifact link is claimed.

Initial failure: CRLF gradlew shebang causes `/bin/sh^M: bad interpreter`; normalized only
that file locally. Most tracked files arrived as CRLF-only differences, left preserved.
Python art tooling installed into isolated `/tmp/dexkids-qa-venv`, not system Python.

Persistent reproducible QA venv subsequently installed at `/home/khuongnd/.local/share/dexkids-qa-venv` using `scripts/simulator/bootstrap-python.sh` and pinned `tools/requirements-art.txt`. Initial enabled accessibility-service list was null; instrumentation explicitly preserves any services via FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES.
