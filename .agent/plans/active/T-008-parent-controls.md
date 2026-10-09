# M7 T-008 Parent Controls ExecPlan

Source: parent phone Activity with age/quiet/timeout/offline-TTS consent persistence, explicit phone preview action and Stop; child Activity does not export itself, session auto-finishes at configured time.
No child UI buttons; no GPS data saved by these controls.
Hardware gate: multi-display parent/child independence and child screen limits require Fold3+DeX evidence.
Remaining: pause/resume, audio-only, true parent gate/PIN, deletion of persistent GPS history if it is ever implemented. Quiet mode settings are persisted but full audio behavior not wired (narrator is opt-in and not started).
Do not mark DONE.

## T-011 local QA checkpoint — 2026-10-09
Owner Main agent; issue #12, draft PR #1. Actual environment/build/UI/model results and
remaining acceptance gates are in .agent/evidence/simulator/. No milestone DONE.
Baseline60-minute emulator soak and post-fix Android regression are tracked separately;
no physical Fold3/DeX/content/audio acceptance inferred.

Final local validation 2026-10-09: see `.agent/evidence/simulator/final-test-summary.md` and `test-matrix.md`. Real emulator results are scoped; no physical/verified-content/release gate closed. Milestone remains unfinished.

## M7 implementation checkpoint (2026-10-09)
User explicitly excludes screen locking/PIN due damaged built-in Fold3 screen and mouse/keyboard external monitor controls.
- Normal single-click Parents menu, F10/Menu; no touch-and-hold.
- Audio-only persists in ParentSettings and skips game scenery/sprites while GPS/GPX/POI/native captions/deadline continue.
- Parent dialog can toggle Audio-only/Quiet without altering TTS consent; 60-minute option and confirmed local preference reset.
- New emulator mouse instrumentation authored; build-only until actually executed.
- See docs/M7_M8_MOUSE_IPC.md. Keep IN_PROGRESS until emulator runtime and physical DeX evidence.
