# M1 T-002: Source-level foundation checkpoint
Date: 2026-10-08

Implemented: fixed-capacity FrameProfiler with average FPS and P95 frame time, invalid sample guard, cyclic overwrite and reset on resume; on-screen *measured-at-runtime* dev profiler (not a benchmark claim), display resize from show(), text refresh throttled to 2 Hz. JUnit test source added.

Actual execution evidence: NOT VERIFIED for this commit until its GitHub Actions run completes.
Hardware: Fold3/DeX, 30 FPS stable, 60-minute soak: NOT VERIFIED.
Desktop graphical run/emulator launch: NOT VERIFIED.

Next: CI build/test and runtime device smoke; leave task IN_PROGRESS.
