# M5 T-006 Vietnamese Tour Guide ExecPlan

Implemented source: cue with source/provenance enforcement, age gating, session cooldown, generic fallback, Android offline TTS voice selection guard, parent opt-in and transient audio focus.
NOT yet implemented: editorially verified named POI content packs, prerecorded Vietnamese audio for devices without offline voice, talking animation synchronization, actual child audio UX, navigation coexistence.
No unsolicited network TTS or cloud calls. No automatic narrator launch in child activity.
Unit tests authored; CI result pending. Device audio focus: NOT VERIFIED.

## T-011 local QA checkpoint — 2026-10-09
Owner Main agent; issue #12, draft PR #1. Actual environment/build/UI/model results and
remaining acceptance gates are in .agent/evidence/simulator/. No milestone DONE.
Baseline60-minute emulator soak and post-fix Android regression are tracked separately;
no physical Fold3/DeX/content/audio acceptance inferred.

Final local validation 2026-10-09: see `.agent/evidence/simulator/final-test-summary.md` and `test-matrix.md`. Real emulator results are scoped; no physical/verified-content/release gate closed. Milestone remains unfinished.
