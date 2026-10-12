# T-008 / T-011 parent gate and audio-only
Owner Main; dependencies T-004/T-006/T-011. API35 emulator-5580; no physical DeX.

Implemented optional salted PBKDF2 PIN setup/change/removal, dashboard re-lock
on leaving/recreation, protected in-game End/menu, cancel/resume, three-attempt
30-second retry delay. Credential derivation runs off the main thread. Audio-only
keeps foreground journey/captions/deadline and hides moving scenery without
changing speech consent, quiet mode or background audio behavior.

86 core tests / zero failures/errors; desktop, Android debug and instrumentation
builds SUCCESS (`build/simulator-artifacts/20261012/parent-build.log`). Six Python
source/provenance/art audits PASS (`source-audits.log`).
Actual `parent-gate.txt`: wrong/correct PIN, cooldown, cancel, protected end,
dashboard/recreation re-lock and audio-only distance advance PASS; synthetic
settings restored. `parent-setup-gate-2.txt` additionally verifies mismatch
rejection, real setup/hashed persistence/removal and audio toggle PASS. Actual
`audio-only.png` inspected: no moving scenery, readable label and parent button,
Vietnamese offline-data status visible.

Retained QA failures: first setup check waited only 1.3s for asynchronous PBKDF2;
a full-suite retry likewise checked removal too early. Both superseded by bounded
completion polling (10s), not weakening credential assertions. Files
`parent-setup-gate.txt` and `parent-full-regression.txt` retain these FAIL results.
Final whole-suite result is appended after execution. No hardware, actual offline
voice playback, kiosk confinement or production signing acceptance inferred.

Final whole suite `runtime-suite-20261012T014728Z-5423`: all five modes PASS
(parent setup/gate/audio-only, Android secure XML, GPX controls/recreation,
demo recreation, same-display F10/end). Test APK removed and dashboard restored.
Slice accepted on emulator; T-008 milestone remains open for physical DeX and
actual offline speech/navigation coexistence.
