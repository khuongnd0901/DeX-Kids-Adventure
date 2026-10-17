# T-068 — Field feedback: question pacing, variety and external learning

Owner Main. Dependencies T-030/T-053/T-062/T-067. Branch
feat/T-068-question-flow-external-learning. User authorized implementation.

Acceptance: wait 3–5 seconds after spoken prompt before revealing answer; next
activity 2–5 seconds after completed answer, with varied attention cues inside this gap; stale/watchdog callbacks cannot reveal
or complete twice. Shuffle eligible cards without repeating until exhausted,
keep audience/age constraints. Parent can import UTF-8 learning TSV or configure
public HTTPS source, validate whole file and atomically retain last-good cache on
failure, no child data upload. Supply validated example and Vietnamese authoring
prompt. User clarified: add story interleaving after five completed questions, with bundled fallback; preserve
explicit library playback. No navigation/voice-provider changes or publication.

Verify core behavior tests, external malformed/duplicate/bounds fixtures, Android
app/test APK and desktop builds, relevant regressions. Record actual evidence;
physical car/DeX and audible timing NOT VERIFIED unless exercised on equipment.
Update STATUS/TASKS and keep plan active if physical gates remain unverified.

Progress: source implementation and final local verification complete.240core
checks0failures/errors/skips; Android app/testAPK and desktop build SUCCESS;
public9-story production codec and8relevant contracts PASS. Final timing3–5s
response/2–5s transitions with attention cues. Parent clarified add story
interleaving, implemented after5questions with3original bundled fallbacks.
Sample/prompt and isolated Android storage test mode supplied (compiled only).
No ADB equipment; physical/SAF/liveHTTPS/acoustic behavior NOT VERIFIED; plan
remains active. Evidence .agent/evidence/T-068/SUMMARY.md and verification.json.
Original failed fixture/static checks retained; final checks pass. No publication.

2026-10-17 physical follow-up authorized: install on connected Fold3 DeX2, run
isolated storage/native audio and measured production pacing/interleaving checks.
Initial97.626s physical pacing PASS (five unique cards, five cues,3.056–5.054s waits,
3.111–4.117s gaps; library narration after exactly5answers), but user observed the
English sample in the wrong phase. Latest required order: Vietnamese invitation →
English native sample →3–5s imitation wait →Vietnamese answer. Correct and rerun
native-language/order assertions before accepting installed final APK. Verify story
wait bounds on the real controller; preserve failures and restore parent settings.

Physical follow-up completed for bounded install/retest/correction scope. See
.agent/evidence/T-068/device/SUMMARY.md/results.json/install-receipt.json. Final
APK hash matches installed Fold3/API35 package on externalDeX2.240core/9contracts/
Android app+testAPK/desktop PASS. Actual native English order (model before3–5s
window),5unique cards/5attention cues/library interlude after5answers PASS. Real
Android AtomicFile/source controls/publicHTTPS176-card download and lifecycle
regression PASS. Exactly one caption through5content phases in Full and Audio-only
verified on exact final APK. Original story5.634s deadline FAIL fixed;3final recall
observations5.082–5.093s including observation overhead. User preferences restored;
real labelled DEMO left running via exported parent/real button onDeX2.
Remaining actualSAFselection/humanacoustic/childASR/movingcar/endurance/full-long-
episode gates remain NOT VERIFIED; keep overall plan active, no release DONE claim.
No commit/push/other-repository work. Original compiler/native failures preserved.
