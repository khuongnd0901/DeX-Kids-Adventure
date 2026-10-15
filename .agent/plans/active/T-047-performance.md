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

M1 local verification and fresh BEFORE recorded in `.agent/evidence/T-047-2026-10-13/`; 100 cycles/AFTER blocked by lost Wi-Fi ADB. Source inherited from user d7db908. Continue independent M2.

M2 source/local verified; shared chunks, POI preparation and opaque-only culling. Evidence `T-047-2026-10-13/m2/`; physical deployment/comparison pending.

M3 local + scoped hardware checks pass. M1/M2 before/after recorded; FPS stable, Audio residency improves, Full Native PSS retention remains OPEN. Fresh M3 allocated/free and long-run measurements required.

M3b audit: current M3 measured native heap ~93MiB size/~16MiB allocated/~71MiB free, but Native PSS ~75MiB after Full reload. Add a small public-Bionic mallopt adapter, NDK27.2 for the existing three ABIs, one coalesced worker after scenery initialization, 32MiB free threshold, API34+ M_PURGE_ALL / API30–33 M_PURGE. No frame-loop GC, no global allocator-policy changes, no new engine. Verify ABI builds, 100 cycles, current 600s before/after and actual heap/PSS; retain only if benefit measured without frame regression. Public contract: https://android.googlesource.com/platform/bionic/+/master/libc/include/malloc.h .

Refinement: scene initialization alone sees little free memory; actual retention arrives later. Reuse the foreground 1s UI ticker to request a coalesced budget check at most every30s; free threshold32MiB. No additional scheduler/frame-loop/native policy change. Record skipped checks and actual purge results.

Current native-budget600s Fold3 run PASS; steady Full PSS no longer exceeds baseline. 30/60m and final integrated visual/field gates remain open; proceed T-041.B independent source work after this checkpoint.

2026-10-15 T-060 latest-art stationary3600s scope PASS:
59.945FPS/P95upper20ms;Native PSS71158→71658KiB vs allocation16729→17197KiB,
free~11MiB below32MiB trim threshold. PSS226–236MiB in full-only sequence;
restoredFull after Audio sequence is lower but not controlled causal A/B.
No forced purge or production change. Retention/accounting acceptance OPEN;
no leak determination or whole-task DONE. Evidence T-060 hour/MEMORY_FINDING.md.
