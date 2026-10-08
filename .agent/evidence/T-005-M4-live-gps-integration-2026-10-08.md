# M4 follow-up - opt-in GPS to journey motion
Parent selects LIVE GPS on external display and grants runtime fine location permission. AndroidGpsSource callbacks pass fixes to LiveJourneyFeed atomic mailbox. Render updates a smoothed distance; stale/low-quality/implausible jumps are rejected. App labels real vs demo mode distinctly. No POI claims while offline database absent.
GPS mode can start ONLY after parent action and permission grant. No implicit phone fallback or cloud sync. Real sensor output, DeX launch and P95 GPS latency: NOT VERIFIED; CI results pending.
