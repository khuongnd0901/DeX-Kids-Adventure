# T-031/T-032 technical verification — Main, 2026-10-11

Owner Main, dependency a379117. Parent authorized implementation and commit/push.
Source checks:21 contracts;148 core tests,0 failures/errors/skips. Android debug,
AndroidTest and desktop builds successful; Android unit NO-SOURCE. Latest APK
hashes:creative-timeout-apk-sha256.txt. Python QA script py_compile successful.

Production review now checks at most4 cards per request, all batches required;
original indices/ages are authoritative. Partial approvals never return a pack.
Known observed translation errors are rejected without a finite word allowlist.
Configured Gemini3.5Flash-Lite uses medium thinking for creative only. Review
output budget4096 includes thoughts/JSON. Observed12s creative timeouts led to
30s creative-only reads; POI/child reply stay12s/connect5s. Same shared5RPM,
12100ms spacing, six-attempt policy, consent/revocation and last-good persistence.

Actual medium matrix PASS317774ms:16 lessons per5/4,6/7,8/10; revocation true.
Main reviewed its48 rows; parent difficulty/translation acceptance pending.
Final30s-timeout matrix FAIL493693ms:5/4 accepted16,6/7 rejected all six
attempts,8/10 not reached. No new timeout. Failures remain original evidence.
Main flags its new5/4 fruit-identification riddle as potentially ambiguous; no
claim that AI approvals prove all generated packs correct. See content-review.md
for the earlier complete matrix, unchanged generated text, clearly labeled.

Final APK creative offline smoke PASS34601ms/30wallseconds:3external checks,
one complete resolution,voiceUnavailable0,58.988FPS/P95upper19ms. Zero new
pending creative IDs observed in this short sample. No endurance/network claim.
Earlier1805s natural creative run has refresh/refill and resource measurements;
renderer/coordinator unchanged, newer reviewer/guards explicitly out of scope.
Latest26HUDcaptures,14visual cues/11theme transitions pass; eight tracked GL
textures retain handles/size/format (44.50MiB backing, not all driver VRAM).
Latest creative cache/coordinator device tests PASS1893ms/5244ms.

Audio: nine BOTH beats4SAU/4ONG/1shared/zero missing voices. Parent confirmed
changing volume; original volume failure retained. Independent audio_volume
PASS13238ms with10→10, native offline narrator/tone PASS12845ms. Human listening,
real navigation/road and physical power loss remain unverified. Do not reset
parent-selected volume. No keys, GPS, children's audio/transcripts in publication.

Both tasks stay IN_PROGRESS pending content/age, field and quota acceptance.

Canonical parent/AI/encrypted-key preference hashes match before/after. Parent
again deferred content review; neither approval nor task completion inferred.
