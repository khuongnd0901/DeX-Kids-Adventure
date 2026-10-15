# T-060 reproduction and evidence scope

Application baseline: main b5b5423ce49c41a74ceb112fc46b8945f371c4e7.
Production code/assets unchanged by this verification task. Test APK changes:
explicit external SurfaceFlinger screenshot selection; semantic parent AI action
lookup and complete AI preferences restoration.

## Host

Use existing JDK17 `/home/kunrg/.local/opt/jdk17`, SDK
`/home/kunrg/Android/Sdk`, and art packages via
`PYTHONPATH=/home/kunrg/.cache/dexkids-qa/art-python`.
`./gradlew --no-daemon :android:assembleDebug :android:assembleDebugAndroidTest`
generates ignored art before `bash scripts/test-local.sh`. Android JVM unit
tests are NO-SOURCE; 194 core tests are separate from actual instrumentation.

## Physical target and execution

`adb -s 192.168.1.3:35165 devices -l` identifies SM-F926B.
`dumpsys display` confirms active EXTERNAL_DEX display2,1920x1080.
`dumpsys SurfaceFlinger --display-id` maps that physical port to
4620382033380392964 (ARZOPA). These IDs are session-specific.
Never substitute phone display0 if either selection is unavailable.

Install both APKs with `adb -s SERIAL install -r APK`. Use:

```sh
adb -s SERIAL shell am instrument -w -r \
  -e display_id 2 -e capture_display_id 4620382033380392964 \
  -e mode MODE \
  com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
```

T-053 modes return instrumentation code0 with assertion fields rather than
`qa_status`; other modes expose `qa_status`. Do not infer PASS from adb exit
status alone. Preserve every initial failure before a retry.

T-051 catalog requires host-generated `assets/generated/m10/m10.atlas` and
`m10-*.png` pushed into app external `files/qa-t051-catalog/`; these are
test-only catalog data, excluded from production runtime APK.

For mic/location denial tests snapshot own-app permission grants, revoke only
those grants, execute the denied-permission mode, then restore and compare
original grants in finally. No global provider, navigation or accessibility
settings changed. Permission JSON receipts document restoration.

Physical original screenshots: ignored `build/qa/T-060/qa-t053/` and
`build/qa/T-060/qa-t051/`; tracked manifests give SHA256 and tracked contact
sheets/selected originals support review without publishing every raw image.
T-053 has72 pose captures+parent+recreation=74; T-051 has14 gameplay fixtures
plus2 Coast/Mountain catalogs=16. Agent visual review is not adult art signoff.

Audience audio test can require up to160+160+430 seconds plus lifecycle
cleanup. Use >=900s host timeout. In this session the initial short-mode
supervisor detached; adb/device instrumentation continued and the runner's
fresh `cache/qa-audience-audio-result.json` is authoritative evidence. The
file was absent during the active run before completion. No speech recording.

The600s offline reviewed-cache window uses:

```sh
python3 scripts/fold3-soak-resources.py SERIAL 2 OUTPUT --creative --seconds 600
```

No live-refill flag: both AI providers disabled for this fixture. Existing
reviewed fixture is replayed, not new provider/editorial acceptance. This
window includes F10 Continue and Full→Audio-only→Full checks; resource rows
therefore describe mixed render modes. P95 values are histogram upper bounds.

Additional one-hour QA uses physical_soak through the same resource script,
without --creative and with --seconds 3600. Original own-app parent/AI prefs
are backed up privately, test disables mic/providers and enables offline TTS,
then original XML/grants are restored and ParentActivity reopens on DeX2.
The one-hour result completed: functional/performance PASS,120checks,86
resolutions,voiceUnavailable0;see hour/summary.json. Visual semantic acceptance
FAIL and Native RAM finding OPEN;see SUMMARY.md.

T-051 result retains a historical Windows wording in its scope string; this
session uses actual SM-F926B/API35 identified in environment.json, with
external capture explicitly selected. No Windows result is being substituted.

Independent30min, real-road GPS, navigation audio coexistence, human listening,
child appeal, release signing/license and design-only T-054–T-059 remain open.
