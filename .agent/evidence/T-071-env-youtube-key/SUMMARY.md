# T-071 — env key + Windows simulator, 2026-10-19

Owner Main; dependencies embedded setup/key vault/T-068/T-071 and existing Windows
AVD. Owner explicitly authorizes `.env` YOUTUBE_KEY for injection, then requests
Windows simulator tests. Bounded injection/API/native simulator slice verified;
overall YouTube task remains IN_PROGRESS, no live child player or physical DeX.

## Implemented / actual results

- Opt-in `-PinjectYouTubeTestKey=true` generates only a private debug asset with
  the single YOUTUBE_KEY field, never copies `.env` or other values. No secret in
  source, Gradle CLI, HTML, logs or this evidence. The private test APK DOES contain
  the key and is not a publication artifact. Runtime import requires debuggable.
- Import into existing Android Keystore ciphertext preserves contact/reference,
  consent, selection and library enablement. A handled-seed marker prevents
  silent reimport after explicit deletion. Three synthetic provisioning tests
  verify opt-in, stale cleanup, invalid/duplicate rejection and log redaction.
- Existing Windows emulator `DeX_Command_Test_API35`, no data wipe/snapshot save:
  API35, emulator-5554, 1920x1080, density160; WebView124.0.6367.219. New app/testAPK
  installed with -r. Installed SHA256 matches exact corrected debug APK. No DO,
  Accessibility, navigation, system GPS/volume or other-repository changes.
- Actual instrumentation verifies private asset -> exact key -> encrypted prefs
  -> fresh Keystore reload, plus isolated seed/delete/reinitialize stays empty.
  Native parent setup recognizes a stored key without rendering its value.
- One host adult diagnostic returned HTTP200. Actual production Android HTTPS
  client also validates owner-authorized `uWXvGSMrwXc`: explicit MFK/embeddable,
  available/public, no region/age block, duration247seconds within planned300.
  This is metadata verification, not247seconds of video playback. Test URL was
  not permanently approved or added to a child catalog.
- Actual Android WebView uses production HTML with only the external API loader
  replaced by a labelled mock; WebView network loads are BLOCKED. Exact-origin
  native message port receives PLAYING -> ENDED once, preserving generation
  9007199254740993. `INSTRUMENTATION_CODE: 0`. No Google iframe/script loaded.
- Screenshot review: native setup shows key-present hint, blank Google reference/
  contact and unchecked consent; no key value. Offline screenshot is explicitly
  labelled QA OFFLINE MOCK and is not live-player/geometry evidence.
- 290 core tests zero failures/errors/skips, actual Node page mock and three Python
  provisioning tests PASS. Corrected debug app/testAPK +desktop build PASS.
- Release build including lintVital PASS after generated producer dependencies
  were fixed. Actual unsigned release APK has no test seed and no env-key bytes
  in any decompressed ZIP member. Default preparation removed the previous seed.
  Private debug APK retains its authorized seed. Sanitized logs/evidence scanned
  for raw key absence. No commit, push, CI or release install.

## Failures retained and corrected

Initial Windows WebView run caught Android JSONObject.getLong coercing a large
numeric string through a lossy double. Native callbacks now compare the decimal
string via EmbeddedYouTubeSession.matchesGeneration. Added boundary regression;
corrected actual emulator run passes. Initial failure retained, not overwritten.

An initial release compile overlapped the generation source fix and saw a stale
core jar; sequential retry corrected it. Release also exposed existing implicit
asset/resource/JNI producer dependencies in both generate*Lint* model and lower-
case lintVitalAnalyze tasks. Declared dependencies case-insensitively; final full
release/lintVital build passed. All intermediate logs retained without secrets.

## Remaining gates / review

`child_setup_ready=false`; no Google notification reference or consent was invented
or auto-enabled. No actual YouTube video playback, visible autoplay, real ENDED,
75/25 geometry/art/audio/GPS restoration, physical DeX or vehicle/endurance PASS
is claimed. Those remain behind real configuration and separate acceptance.
Owner's Windows emulator is left running on the parent app dashboard; imported
key persists there. Existing parent/library opt-ins are preserved.

Files: verification.json, api-diagnostic.json, windows-instrumentation.txt,
initial failure, two reviewed screenshots and full build logs. Current design
and private build instructions: docs/T071_EMBEDDED_YOUTUBE.md.
