# T-047 M1 checkpoint — 2026-10-13
Owner Main. Source M1 in user checkpoint d7db908908c2d424a5ddc8a5e0786f3a8c751bc7.
Reuse existing renderer: lazy GL scenery; release on Audio-only; retain CPU variety
selection; deterministic seed; package runtime assets without unused gallery/full
M10 atlas. Runtime props limited to 32 actually selected regions (one page).

Current local verification: Python contracts PASS, core {'tests': 171, 'failures': 0, 'errors': 0, 'skipped': 0}; Android
unit NO-SOURCE; debug and AndroidTest APK builds PASS. APK size 21,770,740 →
21,229,145 bytes (-541,595). This is packaging savings, not GPU memory savings.

Fresh BEFORE run on actual Fold3 SM-F926B, Samsung Desktop display2 1920×1080:
600s app window, 604.792s instrumentation, 59.9158 FPS, P95 upper19ms, PASS.
60 display checks; one actual Gemini-reviewed cached pack; eight creative beats;
14 resolutions; three Audio-only resolutions; F10/Continue; zero voiceUnavailable.
Providers disabled during the run; no child GPS/audio sent. APK hash/source in
apk-checkpoints.json. Before samples include startup and a two-minute Audio-only
phase; report this as a mixed-mode benchmark, not pure Full-mode GPU timing.

BLOCKED/NOT VERIFIED: M1 device install failed after Wi-Fi transport stopped
responding. A bounded model query timed out; targeted reconnect then failed to
recover the transport. Thus 100-cycle scenery_residency and AFTER measurements
were not run. Do not claim resource/FPS target acceptance or texture-leak absence.
Historical emulator results and interrupted 2026-10-12 runs are excluded.
User's modified old measurements file is preserved and excluded from this commit.

Next: independent M2 renderer duplication/POI preparation; resume physical
comparison when ADB returns. No CI, permission, voice path or other repo changes.
