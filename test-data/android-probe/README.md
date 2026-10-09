# Isolated synthetic Android IPC probe

This fixture requests no permissions, network, microphone, or location. It is a
separate package/UID and each build uses a new QA-only signing certificate under
ignored `build/simulator-artifacts/`; it never shares the app's debug keystore.
The local QA-only keystore password is intentionally non-secret and is not a
production signing credential.

Build only (no emulator interaction):

```sh
scripts/simulator/security-probe.sh --build-only
```

Runtime requires a selected emulator with an already resumed child activity:

```sh
ADB_BIN=/path/to/adb.exe scripts/simulator/security-probe.sh --serial emulator-5580
```

The script verifies different UIDs and signing certificate digests, submits an
explicit STOP to the signature-protected receiver, attempts launching the
nonexported child activity, captures the probe screen and PID-scoped logs,
presses BACK, and verifies the original child activity remains resumed. The
broadcast may return normally even when Android rejects delivery; that return
value alone is never reported as a security PASS. Direct launch must throw
SecurityException and the child must survive STOP for the overall probe to PASS.
A new probe install may uninstall only the previous QA probe package.

Artifacts are timestamped and local only: `build/simulator-artifacts/security-probe-*`.
`--build-only` validates APK packaging/signing, not runtime IPC protection.
Overrides: `ANDROID_SDK_ROOT`, `BUILD_TOOLS_VERSION`, `JAVA_HOME`, `ADB_BIN`.
Requires Linux aapt/aapt2/d8/apksigner/zipalign and JDK17; WSL adb.exe paths are
converted with wslpath. No existing app/AVD permissions are changed.
