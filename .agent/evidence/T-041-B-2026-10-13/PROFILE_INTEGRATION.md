# T-041.B profile/timeline milestone — 2026-10-13

Owner Main; dependency P0 draft checkpoint 9bb8ac3. Main only.

## Audit and implementation

Reuse AnimationRegistry / AnimationTimeline, CharacterAnimationController and
VehicleMotionModel. No second engine or wheel-distance implementation.
Registry now defines 26 allowlisted profile IDs and authored bounded channels.
Head/hand channels are explicitly rig channels, not whole-avatar rotations.
Deterministic profile sampling is allocation-free. Wheel sampling is delegated
to the existing distance-based VehicleMotionModel. Blend/render integration of
all channels remains an acceptance requirement, not a completed source count.

Existing approved Sâu/Ong portraits gain gentle +/-1.5% breathing at 3.3/3.4s,
with foot-anchored scaling and the existing paused render clock. Draft vector
portraits remain outside gameplay. Existing TALK still follows actual narration.

Existing timeline owns one bounded semantic-effect slot, eight allowlisted
one-shot definitions, a two-second start cooldown, and at most eight retained
latest identity strings (each <=160 chars). No effect event requests network,
TTS or microphone. Prompt/answer/interrupt remain beat-bound. Welcome/question
and language-answer events are wired; sourced POI introduction and decorative
SURPRISE may signal discovery. A stale answer cannot reveal an English word.
A quiz answer revelation is not evidence of child correctness: no automatic
ANSWER_CORRECT_STAR / TRY_AGAIN judgment. Confetti and scene-transition contexts,
actual FX drawing, other profile rigs and CAP/VEH/UI atlas integration remain OPEN.

## Verification

Local script contracts + core JUnit: 175 tests, 0 failures/errors.
Three added behavioral tests exercise finite/bounded profiles through long
elapsed times and extreme seeds, return-to-rest one-shots, pause, cooldown,
bounded identities, duplicate suppression, interruptions and stale answer
rejection. These tests do not assert physical graphics quality or all-profile
runtime coverage. Android unit target: NO-SOURCE.
Final debug/test APK BUILD SUCCESSFUL; git diff --check PASS.
Actual Samsung SM-F926B logical Desktop display2,1920x1080: existing
creative_endurance harness, 30s, providers disabled, PASS (wall30s,total34.747s).
Average58.96129FPS,P95upper19ms,3 external-display checks,1 reviewed cache loaded,
0 observed creative beats,1 resolution,voiceUnavailable0. The short run does
not establish full creative-playback coverage or long-run performance.
Installed debug APK SHA256:
323685447e9e528310c6086ff247d970ba28a8316c75044786f61b128f8674de.
Main inspected the captured physical1920x1080 frame: BOTH original approved
child portraits and HUD are visible, no vector-draft replacement. A static frame
cannot prove all animation channels, modes or comfort acceptance.
Device log: /home/kunrg/.cache/dexkids-qa/t041-profiles-device.txt.
Screenshot stays local: /home/kunrg/.cache/dexkids-qa/t041-profiles-dex.png.
Local final build log: /home/kunrg/.cache/dexkids-qa/t041-profiles-final-build.log.

## Acceptance

PARTIAL INTEGRATION, not DONE. All 26 profile definitions exist; only the two
breathing profiles are newly drawn in this milestone. Eight effect definitions
exist; effect rendering is not implemented in this checkpoint. P0 likeness and
>=100 rendered/human-reviewed assets, hardware visual comfort, resource budget
and final 10/30/60m integrated QA remain NOT VERIFIED. P1 stays deferred.
