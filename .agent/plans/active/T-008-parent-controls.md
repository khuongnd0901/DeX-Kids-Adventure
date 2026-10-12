## Scope update — 2026-10-12: NO PIN (supersedes previous PIN work)

User explicitly removed the optional PIN feature. ParentActivity and KidsActivity
provide immediate same-display parent dashboard/menu access using mouse, F10,
Menu or Back. No PIN setup, unlock, retry lockout, persistence or device-locking
UI remains. ParentSettings purges older PIN hashes/counters on initialization.
Retain Audio-only, session deadline, pause/resume, emergency AI disable and
OS permission gates. No phone-screen interaction is required.

Acceptance for this scope: remove PIN classes/tests, ensure no live source
references, verify a migrated PIN preference is deleted and the dashboard/menu
are accessible without authentication; update `docs/PARENT_CONTROLS.md`,
runtime suite and current STATUS/TASKS. Physical Fold3 DeX remains open.
Source-level checks are not a substitute for a new Gradle build/device run.

---
# M7 T-008 Parent Controls ExecPlan

Source: parent phone Activity with age/quiet/timeout/offline-TTS consent persistence, explicit phone preview action and Stop; child Activity does not export itself, session auto-finishes at configured time.
No child UI buttons; no GPS data saved by these controls.
Hardware gate: multi-display parent/child independence and child screen limits require Fold3+DeX evidence.
Remaining: physical DeX/voice acceptance, and deletion of persistent GPS history if it is ever implemented. No PIN gate is in scope. Quiet mode settings are persisted but full audio behavior not wired (narrator is opt-in and not started).
Do not mark DONE.

## T-011 local QA checkpoint — 2026-10-09
Owner Main agent; issue #12, draft PR #1. Actual environment/build/UI/model results and
remaining acceptance gates are in .agent/evidence/simulator/. No milestone DONE.
Baseline60-minute emulator soak and post-fix Android regression are tracked separately;
no physical Fold3/DeX/content/audio acceptance inferred.

Final local validation 2026-10-09: see `.agent/evidence/simulator/final-test-summary.md` and `test-matrix.md`. Real emulator results are scoped; no physical/verified-content/release gate closed. Milestone remains unfinished.

## Superseded historical PIN implementation — 2026-10-12 (removed by later scope update)
Owner Main; dependencies T-004/T-006/T-011; same display supersedes the obsolete
phone/dual-display description above. Implement optional local parent PIN for
both dashboard entry and child End/settings controls, including cancellation,
failed-attempt delay and hashed storage. Add explicit audio-only setting that
keeps foreground journey/captions/deadline and existing speech opt-in rules.
No background audio or automatic speech permission. Gate: core hash tests,
Android build, actual PIN wrong/correct/cancel/recreation and audio-only runtime.
Persist no raw PIN, GPS or audio. Hardware/navigation audio remains unverified.

Slice accepted on API35: 86 core PASS; five ADB modes PASS. Evidence:
`.agent/evidence/T-008-parent-controls-2026-10-12.md`. Full hardware/audio gate remains open.

## M7 implementation checkpoint (2026-10-09)
User explicitly excludes screen locking/PIN due damaged built-in Fold3 screen and mouse/keyboard external monitor controls.
- Normal single-click Parents menu, F10/Menu; no touch-and-hold.
- Audio-only persists in ParentSettings and skips game scenery/sprites while GPS/GPX/POI/native captions/deadline continue.
- Parent dialog can toggle Audio-only/Quiet without altering TTS consent; 60-minute option and confirmed local preference reset.
- New emulator mouse instrumentation authored; build-only until actually executed.
- See docs/M7_M8_MOUSE_IPC.md. Keep IN_PROGRESS until emulator runtime and physical DeX evidence.
