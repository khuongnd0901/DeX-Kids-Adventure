# T-021 — Autonomous physical Fold3 QA

Owner: Main agent. Dependencies: T-001–T-019 source, physical SM-F926B ADB, Android SDK/JDK17.
State: COMPLETE — automatic scope; external/manual gates retained as NOT VERIFIED. User authorizes sequential autonomous checks, commit all verified changes and push main; no manual acceptance available.

Acceptance: build current source and run local contracts/unit tests; install debug and test APKs without clearing user data; execute supported framework tests explicitly on DeX display 2; collect bounded GL performance and 60-minute synthetic DEMO endurance if launch succeeds; preserve settings/permissions and remove test APK. Record actual assertions and failures. No GPS injection or cloud/audio collection on real phone. Real road/voice/navigation quality and unplug/replug require equipment/operator and remain NOT VERIFIED. No other repository changes.

Evidence: .agent/evidence/T-021-fold3-autonomous-qa-2026-10-10.md; raw local artifacts /tmp/dex-fold3-qa (exclude private device dumps from Git).

Sequence: inventory → local tests/build → install → parser/lifecycle/single-display/HCMC/route/privacy/cache → performance/soak → audio playback and visible Parent action tests → fix reproduced issues and rerun affected checks → cleanup → status/tasks → commit/push.

User follow-up authorizes actual sound playback. Audio gate: local one-second tone with active media status, production offline Vietnamese narrator start/finish callbacks, same offline voice native onDone without onError, volume/mute restored; no microphone recording. If offline Vietnamese voice is missing, record the actual blocked spoken-audio gate instead of claiming PASS.

Additional Parent gate: GL-thread distance freezes while menu is open, Continue advances it without resetting deadline, actual visible list selects Audio-only and AI disable, temporary preferences restored. Capture assertion failures on the instrumentation thread so finally cleanup can run.

Outcome: FullHD real 60min PASS, tone/offline Vietnamese native onDone PASS, expanded Parent list failure reproduced and fixed, final eleven script modes and local tests/build PASS. Endurance baseline APK and final menu-fix APK hashes distinguished in evidence. Cleanup and final source regression recorded in evidence.
