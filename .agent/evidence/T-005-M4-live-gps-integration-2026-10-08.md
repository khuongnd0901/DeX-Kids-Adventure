# M4 follow-up - opt-in GPS to journey motion
Parent selects LIVE GPS on external display and grants runtime fine location permission. AndroidGpsSource callbacks pass fixes to LiveJourneyFeed atomic mailbox. Render updates a smoothed distance; stale/low-quality/implausible jumps are rejected. App labels real vs demo mode distinctly. No POI claims while offline database absent.
GPS mode can start ONLY after parent action and permission grant. No implicit phone fallback or cloud sync. Real sensor output, DeX launch and P95 GPS latency: NOT VERIFIED; CI results pending.

## Verified CI evidence (added 2026-10-08)
- Source commit: `8d0e4580`
- GitHub Actions [run #37798067824](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37798067824): **SUCCESS**.
- Steps: Gradle `:core:test`, `:desktop:classes`, `:android:assembleDebug` and debug APK artifact upload: **SUCCESS**.
- No GL window, full display, real POI, real-time voice or device-performance acceptance was executed. Hardware requirements remain **NOT VERIFIED**.
