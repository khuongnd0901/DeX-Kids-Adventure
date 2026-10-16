# T-065 — latest integration and autonomous Fold3 validation
Owner Main. Dependencies T-062/T-063/T-064; user authorizes merge latest into main,
build and autonomous ADB testing without user participation.
Branch feat/T-065-merge-fold3-validation. Base a0d447b; incoming 76595b6.

1. Merge newest branch preserving existing evidence and source.
2. Run core/pack checks, Android app/test APK and desktop build; local contracts.
3. Install matching APKs on SM-F926B; external DeX display only. Run expanded
   visual/lifecycle, remote cache/playback and parent/replay/cancellation regressions.
4. Preserve actual failures/results and screenshot evidence; restore fixture settings.
5. Update trackers, integrate verified branch into main and publish if credentials permit.
Acceptance: tested source integrated in main, build/core pass, physical test results
recorded truthfully, final app on DeX. Human artwork approval and broad release/
field/endurance gates are outside this slice; no automatic phone fallback.
