# T-078 — usable YouTube key configuration (2026-10-20)

Owner Main; dependencies T-071/T-076/T-077 and connected Fold3. DONE for requested
personal-key/use setup. User declines contact collection and explicitly chooses
“Cho phép kết nối YouTube” after disclosure. No invented contact or consent.

## Change and actual persistent configuration

Personal/family contact is optional in settings readiness, configure persistence,
native form and library notice. Encrypted key/country/actual privacy consent still
required. Key remains Android Keystore encrypted; existing ciphertext hash exact.
Persisted native setup: blank contact, countryVN, embedded selected, consent=true,
ready after reload. Explicit persistent QA mode requires separate authorization
flag; negative invocation without it rejects before setup writes.

Native parent library checkbox enabled approved-video use. Existing notice was
already accepted in the baseline, so no new notice dialog was required. At that
moment HDMI screenshot showed automatic interleaving OFF. A later read found
it ON and device navigated to ParentActivity without additional agent navigation;
preserve current device selection rather than resetting it. Final feature=true,
automatic=true, no pending hold. Do not claim the agent enabled automatic mode.
All9rows were already approved/enabled in this task baseline (unlike earlier
T-077 evidence). Exact library equality proves all9rows/18questions/flags retained;
no video approval was fabricated. User can now use saved setup.

## Verification

Android app/testAPK and final corrected testAPK builds PASS; core unchanged
UP-TO-DATE with290results zero failures/errors/skips. Installed APK matches SHA
in verification.json. Existing production Android credential/metadata diagnostic
PASS, key/isolated delete/reload and offline mocked port tests code0. Actual native
configuration save/reload code0. Real production preview using actual persistent
blank-contact setup: PLAYING -> F10 CANCELLED2744ms; second PLAYING -> background
CANCELLED1805ms, host disposed, catalog/settings exact; code0. Natural end already
covered by prior full preview fixture, not repeated/claimed for this smoke.
Updated isolated storage regression confirms missing consent blocked and consent
with blank contact ready, encrypted roundtrip/delete/reload; real Activity
recreation retains external hold and refuses IPC bypass, explicit release code0.

Physical DeX2 setup/library screenshots and actual preview frame reviewed.
Final current screenshot shows ParentActivity, preserving observed owner/device
interaction. Only intended consent_v1 and library enabled/automatic keys changed;
normal opening counter and story-source success timestamp changed; AI settings
exact. Key ciphertext exact and raw secret absent from source/evidence scan.

Two fixture failures retained: CheckBox toggled but performClick returned false
without a click listener, corrected assertion checks checked state; initial preview
fixture required pending flags, while existing baseline already had approvals,
corrected existing-owner smoke preserves those flags. No production workaround,
review reset or false successful initial run. No Cloud provisioning, Google
notification/certification, account login, DO/Accessibility/navigation change,
other-repository access, commit or push. Broader T-071 field/endurance/privacy
and post-video automated-question gates remain open/unchanged.
