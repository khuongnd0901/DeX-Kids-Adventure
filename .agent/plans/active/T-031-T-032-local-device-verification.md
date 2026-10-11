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
