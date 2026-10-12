# Unlimited journey source implementation — 2026-10-12

**Scope:** remove duration-based automatic stopping and goodbye narration
in DeX-Kids-Adventure; maintain explicit parent end control.

**Static code changes:** Deadline and Runnable deleted from KidsActivity;
duration selector/setting removed; legacy preference migrated; unused
SessionTimeLimit class/test deleted. Parent menu and service stop remain.
Two authored farewell beats replaced by ongoing activities. 30/60-minute
Android QA now tests indefinite app lifetime, not auto expiry. Static
regression tool and local runner updated. Future M10 planned goodbye
visual changed to a non-ending transition.

**Execution evidence:** GitHub structural checks only. No actual Python,
Gradle, JUnit, Android, emulator or Fold3 device execution performed.
**Result:** SOURCE IMPLEMENTED; RELEASE ACCEPTANCE PENDING.
See `docs/UNLIMITED_JOURNEY_ACCEPTANCE.md` for commands and report format.
