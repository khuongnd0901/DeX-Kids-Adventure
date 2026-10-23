# T-075 — ChatGPT subscription migration

Owner Main. Status IN_PROGRESS. Dependencies T-029/030/034/054/063 and SIWC.
Baseline origin/main 81fa123ed8a89e47b2326692c7d7a868532540f3.
Read-only Assistant reference a52ddf0ecf3022922401f222f4db2d71282d3a9d.
Isolated worktree; original owner workspace and Assistant are not edited.

## Acceptance and execution

1. Audit every inference caller, persistence and consent. Record migration matrix.
2. Adapt only auth/OIDC/secure-store/catalog/SSE from Assistant. New Kids host,
   Keystore alias and client registration; no transferred credentials. Public API only.
3. Replace gateway transport and parent settings; preserve public feature schemas,
   validators, ages 5/4, saved data and offline-first behavior. Leave YouTube intact.
4. Unit/mock tests: OAuth binding/refresh/logout/OIDC, terminal SSE, failure,
   truncation, cancellation/timeout, model selection, consent and physical rate gate.
5. Run scripts/test-local.sh, core/Android unit tests, build APK/test APK and lint.
6. Boot Windows AVD, record hardware/API/serial, install in place without clearing
   data; verify parent settings/disconnected/errors, controlled feature regression,
   offline replay, lifecycle, screenshots/logcat/resources. Fixture != live inference.
7. Review diff, whitespace/secrets/artifacts; update README/decisions/task/status,
   commit verified source, push, verify remote SHA and create draft PR if gates open.

## Protocol decisions

Subscription consent defaults OFF independently of retained feature preferences.
Only catalog-provided model slugs selectable. Per-install dynamic registration
and loopback callback follow official SIWC OSS guide, with PKCE/state/nonce/RS256.
JSON/schema validation stays local. Unsupported temperature/max_output_tokens
are omitted; bounded bytes/output, cancellation and timeout remain mandatory.
Only terminal response.completed releases text. No fallback or stream replay.
Quota/ambiguous failures do not trigger acquisition retries. Rate limiter counts
physical Responses starts including the bounded pre-stream401 reissue.

## Gates / progress

- [x] Fetch exact baselines and read relevant existing plans/code/decisions.
- [x] Implementation and migration matrix verified (controlled fixtures; live compatibility open).
- [x] Build/unit/contracts/lint: core290/Android32, test-local, APK/testAPK/full lint0errors89warnings.
- [x] Actual Windows AVD evidence: API35/display2FullHD; focused fixtures and181s story smoke, with documented limits.
- [x] Publication/remote SHA/PR: source4b0cfd1 verified on origin; Draft PR66, main unmerged.
- [ ] LIVE_AUTH_NOT_VERIFIED until owner signs in and consents in Kids.
- [ ] Physical Fold3/DeX: NOT VERIFIED by simulator evidence.

Evidence directory: .agent/evidence/T-075-chatgpt-subscription/.
Keep plan active until required acceptance is met; no fabricated PASS or DONE.

## Verification checkpoint

Final evidence SUMMARY/verification.json and individual Android receipts are recorded.
Owner expressly chose to defer live login in Draft PR. No overall acceptance/DONE.
34/35original records identical after QA restoration, only ART install metadata
changed. App stopped after restoration. Initial failures retained in SUMMARY.
Publication: source4b0cfd1 pushed and independently verified; Draft PR66 created.
Next: owner-led native login/inference, real account/model validation and physical
gates. Owner explicitly deferred that work for this checkpoint.

## Follow-up — configured ages in AI prompts (2026-10-23)

Latest owner request supersedes fixed5/4 prompt instructions:5/4remain defaults,
but all generated/reviewed educational content must use configured4–7ages.
Owner Main; scope StoryAuthor/KidsAiGateway and age validation/cache boundaries.

- Snapshot configured ages once per operation. Prompts, examples and metadata agree;
  BOTH targets min(Sâu,Ong). Reject model age mismatch or config change mid-operation.
- Story generation/regeneration/review use that snapshot; old saved episodes are
  never rewritten automatically. Review input can carry current age metadata without
  mutating the saved episode. Validator accepts supported4–7, rejects out-of-range.
- Trip-pack prompt/validation/store use configured ages and card age ranges together;
  keep existing JSON/cache formats and public overloads. Creative already receives
  configured session ages; make them explicit in generation/review prompts too.
- Keep privacy/consent/subscription boundaries unchanged. No live account request.
- Verify reversed/asymmetric ages, defaults, malformed model ages, mid-flight change,
  old saved data, trip card eligibility and story author/regenerate/review with mocks;
  run core/Android unit/build/lint and focused Windows AVD fixtures. Append evidence,
  update memory and existing Draft PR66; do not merge or claim live/hardware success.

Follow-up verification: configured-age slice PASS; core293/Android35, test-local,
app/testAPK/full lint and actual Windows configured-age+regression fixtures.
37/38original follow-up records exact after restoration; only ART metadata differs.
No live inference. Evidence configured-ages/SUMMARY.md. Publish to same Draft PR66;
overall live/physical gates remain open, so plan remains active.
