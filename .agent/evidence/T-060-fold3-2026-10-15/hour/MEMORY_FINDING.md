# T-060 / T-047 memory observation — acceptance OPEN

Cold Full-only run mid-window app dump: TOTAL PSS231942KiB;
Native PSS71158KiB; native heap Size32904KiB / Alloc16729KiB / Free10902KiB.
GL mtrack72364KiB; EGL41052KiB. NativeHeapTrim logs skip at about10.5MiB free,
below existing32MiB threshold. No manual purge/GC or render-mode intervention
was performed during this uninterrupted run.

Earlier600s test includes Full→Audio-only→Full and its steady restored Full
PSS ranged172126–188061KiB. This is a different sequence, not a controlled
causal A/B. Native PSS exceeds allocator-reported current allocation;
retained/cache/page accounting needs investigation on the new asset build.
Do not claim a leak, a measured cause, or completed RAM optimization from
these samples. The hour run continues to establish actual stability and
performance; T-047 RAM/retention acceptance remains OPEN.

Evidence: memory-midrun.txt, native-budget-midrun.txt, resource prefixes and
cache600/summary.json. No production optimization changes in T-060.

Late same-process dump (near final minute): Native PSS71658KiB,
Alloc17197KiB, Free11088KiB. Native PSS increased500KiB between these
two snapshots; no leak or successful reclaim conclusion follows from this.
The stationary one-hour functional/performance result subsequently passed;
Native retention/accounting investigation remains OPEN.
