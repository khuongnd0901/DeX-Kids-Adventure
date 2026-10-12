# T-047 — measured resource optimization
Owner Main. Dependencies: T-046 checkpoint f3d048a, T-044 renderer baseline.

Audit: graphics eagerly load in Audio-only; two identical WorldWindow owners;
POI texture uploads inside SpriteBatch; unused coastal/mountain props packed;
inactive scene records allocated repeatedly. Worker ownership already fixed and
regressed by T-046; no duplicate worker rewrite needed.

Plan: same-device baseline (Fold3 DeX display 2, 1920x1080), then M1 lazy/disposed
scene resources and runtime-only packing; M2 shared WorldWindow/POI preparation;
M3 inactive scene/audio cache audit. Reuse all existing engines and source priority.
Each checkpoint runs local suite + core + Android APK/tests and device regressions.
After comparison uses identical duration/device/harness; historical emulator FPS
is context only. Long runs, thermal/GPU/visual acceptance remain open until measured.

Display correction: ID6 is HDMI underneath Samsung desktop; user could not see QA. Run invalidated. Actual Desktop logical display ID2 confirmed; rerun required.
