# M7 functional validation

Actual parent preview/audio-OFF-by-default explanation and external-route no-fallback exercised. Android session limit still enforced in60-minute fixture; expiry result pending. Recreation budget originally reset on new Activity; source fix saves absolute elapsedRealtime deadline, enforces on resume and retains pure feed only in non-configuration memory. Actual instrumentation pending; permission denial/revoke checks pending after soak. PIN, pause/resume UI and audio-only absent; IN_PROGRESS.

No milestone DONE. See source-test-map.md for exact steps/expected/code mappings.


## Executed expiry and recreation
Original unchanged60-minute parent limit expired at09:53:21.796, exactly3600seconds after
08:53:21.796 session start. Strict resumed-child sampler exit4 retained; expected expiry PASS.
Actual baseline recreation proves missing saved deadline/feed reset; post-fix framework
instrumentation PASS preserves deadline8765104 and feed identity/distance. No PIN/audio-only
or parent isolation claim follows from these narrow tests. Full permission revoke/live route
and settings persistence UI matrix remain open.


## Final permission/offline verification
Actual baseline denial-node test FAIL: `permission-baseline-final.txt` reports dialog clicked,
fine/coarse denied, explanation missing. Source bad299a preserves permission outcome and uses
the Android COARSE/FINE pair with result lookup by name. Corrected framework test PASS in
`privacy-20261009T032118Z/`: actual parent Button.performClick, denial node ACTION_CLICK, both grants denied,
visible refusal PNG, and offline demo/recreation with no active default network. PNGs reviewed.
Previous `privacy-20261009T031515Z` also completed all3 privacy cases. Actual API35 did show a
fine-only baseline dialog; early ignored-request diagnosis was incomplete. Pair request follows
[official Android contract](https://developer.android.com/develop/sensors-and-location/location/permissions/runtime).

QA incident: early coordinate-based dialog attempt was followed by unexpected location grants;
FINE/COARSE immediately revoked. A later reinstall preflight found FINE granted again; exact
input/install-state cause remains unproven. No live child GPS started (no external display), no
network TTS/GPS upload invoked. Failed runs retained. Final harness has no permission grant path;
original denied grants restored, USER_SET/USER_FIXED flags cleared and original parent prefs restored.
Selected-accuracy internal flag may remain; no claim of bit-identical permission bookkeeping.
Parent PIN/audio-only/full isolation, live revoke/background and complete settings/consent UI
matrix remain NOT_IMPLEMENTED/NOT_RUN. This privacy subset does not close release/security gates.
