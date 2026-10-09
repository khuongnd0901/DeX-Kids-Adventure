# T-011 / T-004 — Android GPX loader correction

Owner: Main agent. Dependencies: M3 parser, M5 bundled sample, Android QA runner.
Base source: 6c8a9dfa25f4c9e6836462a50e6ad78cb8a26390 plus this local patch.
Target: ZFold3_API35, emulator-5580. All paths below are relative to
`build/simulator-artifacts/gpx-fix-20261009/` (ignored, local raw evidence).

## Reproduced cause
`baseline-parser.txt`: actual Android instrumentation FAIL with
ParserConfigurationException for `http://apache.org/xml/features/disallow-doctype-decl`
from Android Harmony DocumentBuilderFactoryImpl.setFeature. No XML content was
parsed. Both local import and bundled HCMC use this shared parser.

## Correction and acceptance
Replace unsupported factory security flags with a 4MiB-bounded, strict UTF8/UTF16
decode, rejecting DTD/entity declarations before DOM sees the same characters;
external entity resolver also rejects resolution. No network/storage permission
added. Trackpoint/time/coordinate validation remains in place. Conservative
limitation: literal declaration text in comments/CDATA is also rejected; other
encodings are not supported.

- `build.log`: core tests82, failures0/errors0/skipped0; desktop classes and both
  Android APK builds SUCCESS.
- `fixed-parser.txt`: PASS; bundled route duration900s; unsafe entity/DTD rejected
  on Android for UTF8/UTF16/UTF16LE/UTF16BE.
- `fixed-sample.txt`: actual parent sample button → parsed replay installed,
  timeline1.423s, no live GPS, PASS.
- `fixed-picker.txt`: actual parent picker button → Android DocumentsUI Downloads
  → local `dexkids-qa-gpx.gpx` copied from bundled synthetic fixture → replay
  installed, timeline1.493s, no live GPS, PASS. Selection used screenshot-guided
  ADB taps on the actual file picker; no fabricated URI/callback or grants.
- `picker-ui.png`, `picker-drawer.png`, `picker-downloads.png` retain selection
  evidence. `sample/loaded.png` and `picker/loaded.png` visually reviewed: actual
  rendered game and GPX PLAYING1/900s controls, no import-error dialog.

APK SHA256: c33bf5e12f75be736c564a8928ed43c9a26ed230de3a63cbe9031ac4e296b365.
Fixed APK installed on emulator. Test fixture and test APK cleaned up; parent
dashboard reopened. No radio/permission change required for this regression.

Observed bug acceptance met on emulator. T-011/T-004 milestones remain IN_PROGRESS:
physical DeX, other document providers, process death, long GPX playback, controls
and full narration/theme acceptance are not implied by this short run.

## Repeat
Build/install app and AndroidTest APK. Run framework runner with `-e mode gpx_parser`
or `gpx_sample`. For `gpx_picker`, supply a local synthetic GPX via Downloads and
select it in the real system picker within60s; instrumentation asserts installation
and timeline advancement. Modes do not grant location or start LIVE tracking.
