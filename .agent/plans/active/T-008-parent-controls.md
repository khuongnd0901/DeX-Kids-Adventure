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

## Current completion slice — 2026-10-12
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
