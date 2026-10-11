# T-031/T-032 verification — Main, 2026-10-11

Dependencies: integrated T-029–T-032 at a379117; connected SM-F926B/external DeX.
Scope: verify current source, add test-only Android coverage where existing
POI/provider tests do not cover creative caches or dynamic HUD ages.

Acceptance gates:
- Execute all local contracts/core tests and Android debug/test + desktop compile.
- Isolated on-device creative AtomicFile roundtrip, cursor/history, corrupted
  checksum and changed-age rejection; preserve user packs/settings.
- Synthetic long prompt/answer HUD on external DeX, SAU/ONG/BOTH, ages 4/5/7/10;
  inspect real TextView layout and save screenshots for visual review.
- Run relevant existing lifecycle/parent/single-display regression.
- Real creative provider/reviewer, quota/error/refill/revocation, adult factual
  and age QA, navigation audio and natural 30–60 minute endurance remain open
  unless actually executed. Never reuse old-build metrics as acceptance.

Evidence: `.agent/evidence/T-031-T-032-verify-2026-10-11/`.
Do not change parent opt-in, keys, mic/GPS permissions, or release status.

Verification slice executed: 21 contracts, 140 fresh core tests, target builds,
isolated creative cache and 12 age/HUD sessions PASS. Five relevant physical
regressions PASS after correcting obsolete audience test labels. Initial failed
build/test logs retained. T-032 composed visual acceptance FAIL: provenance
panel covers first answer line. Remaining provider/human/endurance gates above
NOT VERIFIED; plan remains active. STATUS/TASKS/evidence updated. Test APK
removed; ParentActivity restored to external DeX2.

## Authorized completion work — 2026-10-11

User requests T-031/T-032 through DONE and provides Gemini .env credentials.
Owner Main; branch feat/T-031-T-032-completion; dependencies prior integrated source.
Preserve existing local edits/evidence. Fix measured composed HUD collision,
assert global overlay rectangles, run production Gemini generation/review at
4/5,6/7,8/10 ages, retain generated-only lessons for adult review, verify
cache/coordinator/refill/revocation and external-display endurance. Secrets
remain app-private, ephemeral, redacted and excluded from evidence. Temporary
QA opt-ins must be restored. DONE still requires actual human/device gates;
no acceptance criteria are waived by this request.
Evidence: .agent/evidence/T-031-T-032-completion-2026-10-11/.

### Completion checkpoints (Main)

- 143 core tests PASS; Android debug/test + desktop compile PASS.
- Current source includes measured answer-space reservation/global overlap QA;
  spoken fiction labels, diacritics/low-age/privacy guards; locally owned age/focus
  schedule; complete vocabulary resolution; bounded indexed reviewer feedback.
- Physical production coordinator with simulated faults PASS: six attempts,
  Retry-After/backoff, malformed/timeout/review rejection, last-good, cursor,
  pending IDs, mixed-focus rotation, refill, pause/revoke and terminal refusal.
- Initial real 47-card generation passed machine gates but content review FAILED
  (romanized/abstract/ambiguous output), preserved as initial-content-review-failed.
  After safeguards, 4/5 and 6/7 real packs PASS; 8/10 rejected all six attempts.
  Failures retained. New named-field reviewer/enum feedback is awaiting live QA.
- Parent explicitly says review content at the end; do not repeat that question
  before technical work is complete. Human acceptance is not yet provided.
- Add a >=30 minute actual creative DeX run with authorized .env Gemini initial
  refresh and exhaustion refill, FPS/P95/PSS/thermal/dispatch reservations.
  Restore keys/settings/history/caches afterward; no child mic/text/GPS upload.

### Actual 30-minute creative runtime (Main)

- SM-F926B FullHD external DeX2; natural 1805 wall seconds PASS; 181 checks,
  24 observed creative pending IDs, three distinct reviewed snapshots (baseline,
  new-trip refresh, exhaustion refill), 43 resolutions, voiceUnavailable=0.
- Average 59.936 FPS / P95 upper20ms, battery30.6–31.1°C, thermal0.
  Shared persisted reservations sampled: >=12100ms spacing, <=5 rolling60s.
- Provider age matrix 6/7 and8/10 each16 rows approved after bounded rejection
  retries. Real human content review still finds imprecise vocabulary and
  inconsistent riddle premises; samples retained as NOT final accepted content.
- Final prompt-only vocabulary/premise guards built; no rendering/coordinator
  changes after runtime. Final live regeneration + corrected screenshot capture
  awaiting execution. No driving/navigation audio/physical power-loss proof.
- Runtime credentials/settings/cache/history restored in instrumentation finally;
  private fixture removed. Verify final cleanup after remaining QA.

### Verified publication and latest checks

User authorized commit/push; f755f5c published on feat/T-031-T-032-completion.
Latest26HUDframes PASS with11theme transitions and identical tracked sprite
GLtextures. Native offline audio/tone and restored volume PASS. Audience QA
firstthree BOTHbeats are SAU/ONG/SAU because everythird slot is a learning
card; update test to observe allninebeats before requiring shared focus.
Real provider reasons matrix failed at8/10 aftersixattempts; retained.
Updated full-sentence English answers, numeric retry length constraints and
Unicode spelling corrections compile; live rerun pending actual outcome.
No human/field gates waived and no DONE/release claim.

### Measured reviewer false approvals: four-card batches

Main found incorrect acorn/marigold/glasses translations and ambiguous desk
identification after full-pack reviewer approval. Tighten the independent
review stage to at most4cards per request, with original indices/authoritative
ages; require every batch approved before returning a pack. Reject indices
outside the current batch and keep bounded corrections on rejection.
16cards require1generation+4reviews;20cards require1generation+5reviews,
all under the existing shared5RPM/12100ms limiter and unchanged six-attempt
policy. Partial approvals never persist/play. No model/key/consent change.
Compile/runcontracts and actual5/4,6/7,8/10 batch review; inspect all returned
translations/answers and keep failures. Audio BOTH durable result still pending.

Add deterministic rejection only for observed wrong word/meaning pairs (not
an allowed vocabulary catalog); test correct/alternate/new words stay allowed.
BOTH completed9beats,4SAU/4ONG/1shared,0missingvoices, but external Android
volume-down events invalidated volume gate. RetainFAIL,record numeric volumes
and run targeted narration/music/F10 volume smoke without changing user volume.

First4-cardreviewmatrixFAILED411404ms at5/4 aftersixattempts (negative
qualitychecks, wordcounts andfeedbackverbosity); preserved. Normalizeinternal
negativefeedback tobounded160chars, shorten low-age targetwordcounts and
use documented medium thinking forcurrent3.5Flash-Lite creative requests
only. Reviewer4096-token ceiling includes thinking; authoring7500unchanged.
Verify actual same-model API latency/JSON/allbatches beforeacceptance.

### Parent volume confirmation and measured creative timeouts

Parent confirmed changing DeX volume during the nine-beat BOTH run. Retain the
failed global-volume assertion as externally invalidated; functional nine-beat
coverage and separate production audio_volume PASS (10→10) are independent
evidence. No reset of user volume.

Medium-thinking live generation observed multiple SocketTimeoutException at
the previous 12s read budget. Increase only explicitly consented background
creative author/review reads to30s; other requests stay12s/connect5s. Keep
shared5RPM, six attempts, review rejection and last-good unchanged. Compile,
contracts/core tests, then actual provider rerun after previous cleanup.

### Actual medium-thinking matrix and final APK smoke

Live matrix PASS317774ms:16 fully reviewed lessons each for5/4,6/7,8/10;
consent revocation true. Failed12s requests, safety/format and age rejection
retained; six-attempt policy respected. Main read all48 generated lessons and
exported unchanged to content-review.md. Parent age/difficulty gate still open.
After creative-only30s timeout change:21 contracts/148 core tests and Android
debug/test + desktop builds PASS. Final installed APK offline creative smoke
PASS34601ms,30 wall seconds,3external checks,one resolution/zero unavailable
voice,58.988FPS/P95upper19ms. Short sample observed zero new pending creative
IDs; do not call it endurance or refresh/refill proof. Live30s matrix pending.

### Final live outcome and parent deferral

Final30s creative matrix FAIL493693ms:5/4 accepted16;6/7 rejected six attempts
(FACT_ERROR/AMBIGUOUS_RIDDLE/ANSWER_MISMATCH);8/10 not reached. No new read
timeout observed. Preserve prior complete317774ms PASS as separate run, not
latest acceptance. Generation quality remains inconsistent; do not weaken
review, fabricate acceptance or switch billable models silently.
Canonical parent_settings/kids_ai_settings_v1/kids_ai_keys_v1 hashes match
before/after. Parent again answered “Để xem lại sau” to final content review.
Source verification/publication can proceed, both tasks stay IN_PROGRESS.

### User-authorized main integration

Fetched origin/main a379117; fast-forward merged f755f5c and1b1493b on main
without conflicts/source edits. Prior verified tests apply to identical source.
User explicitly requested this merge; pending acceptance gates and IN_PROGRESS
remain unchanged. Publish main with this integration record; plan stays active.
