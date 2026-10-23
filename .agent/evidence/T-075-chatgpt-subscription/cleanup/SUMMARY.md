# T-075 — refactor cleanup checkpoint (2026-10-23)

Owner Main; requested cleanup slice verified. Same feature branch/Draft PR66,
main unmerged. Live sign-in/inference and physical acceptance remain unverified.

## Removed / retained

- Delete scripts/fold3-configure-ai.py and scripts/fold3-gemini-test.py: obsolete
  host .env/key injection and retired live-provider probes.
- Delete unused StoryAuthorValidation live-placeholder stub/mode. Remove runner
  key-provisioning/provider modes, unreachable creative live/matrix threads,
  generateReviewedMatrix, dead flags/age parameters, key-pref snapshot/restore and
  secret-file cleanup. QA no longer touches old credentials to clean itself up.
- Keep fold3-soak-resources offline collector; remove .env reads, secret fixture,
  live-refill/matrix flags and unused XML import. Offline creative fixture now
  explicitly disables automatic generation, including with an authenticated owner.
- Remove KidsAiRetryAfterException and provider-specific coordinator error strings.
  All IO failures are terminal; only malformed/schema/content rejection retains
  bounded acquisition retries. Replace obsolete429Retry-After/5xx-retry assertions
  with subscription quota/no-replay and content-retry coverage.
- Remove unused accountEmail getter, clearSavedPacks coordinator facade and unused
  unit-test imports. Parent's explicit clear-pack UI still uses its existing path.
- Unknown instrumentation modes now fail closed rather than silently performing
  recreation. Existing registered modes remain; default mode is still recreation.
- Update README/current T029QA instructions and durable decisions; preserve old
  historical evidence. Broaden static scan to runnable Android instrumentation and
  scripts, as well as production Android/core sources.

Necessary business code remains: KidsAiSettings consent preferences, source-bound
parsers/validators, TripPackGateway/KidsAiTransport seams, AtomicFile/SQLite caches,
shared persisted physical-start limiter and generic public backoff contract. No
YouTube Data API key/code, saved credentials, stories or educational content deleted.

## Verification

**PASS** core293 + Android35tests;0failures/errors/skips. App/testAPK and full lint
PASS(0errors89warnings). **PASS** scripts/test-local.sh, targeted legacy scan and
script syntax/help. Removed host flags --live-refill/--matrix each return argparse
exit2 before ADB or secret access. No matching provider/key adapter/matrix helper
remains in executable Android production/instrumentation or scripts; negative test
patterns and historical documentation deliberately retain provider names.

Build environment/commands unchanged from configured-age receipt: installed QA
venv/JDK17/SDK35/NDK27c, Gradle core+Android tests/debug APK/testAPK/full lint, then
scripts/test-local.sh. Final local logs remain private; hashes/XML-derived totals
and installed artifact hash are in [verification.json](verification.json).

Actual Windows API35 emulator-5580/display2FullHD, same signing and install-r:
- [Coordinator](creative_coordinator.txt): offline cache replay with automatic
  generation OFF and ready fake gateway makes0calls; bounded content retries;
  all injected IO/subscription failures stop once; last-good/cursor/refill,
  pause/revoke/recreation ownership preserved.
- [Story failure](t054_story_failures.txt): injected subscription quota stops at
  author or review without retries; cancelled late output cannot persist a draft.
- [Configured ages](t075_configured_ages.txt) and
  [subscription/storage/UI](t075_subscription.txt) regressions PASS with mocks.
- [Unknown mode](qa_unrecognized_probe.txt): expected qa_status=FAIL/code1 and
  Unknown QA mode. This negative case is a passing rejection guard, not a normal
  successful instrumentation invocation or a hidden recreation PASS.

Installed final APK pulled and hash equals build. Fresh pre-QA private data backup
was compared/restored with target stopped; only ART profileInstalled differs from
original records,0missing. Exact counts in verification.json; legacy credentials,
settings and story DB preserved. No uninstall, clear-data or permission changes.
AVD remains running, target stopped. No new visual/performance/physical soak campaign;
offline soak's transport-off behavior is verified by the coordinator fixture,
not a fabricated physical endurance run. Live auth remains deferred.

Initial cleanup compile failed because Java fake transport threw checked
JSONException outside its declared seam; replaced fake malformed-content failures
with unchecked validation exceptions and reran build/native regressions PASS.
Old static assertion for a subscription-code string was updated to check the
stronger all-IOException terminal rule. Historical failure receipts remain intact.
