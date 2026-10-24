# T-079 — latest subscription integration, Fold3 audit and main publication

Owner Main; 2026-10-24. Integration source: existing PR66 branch abae09d, based
on main81fa123, plus preserved owner YouTube T-071/T-076/T-077/T-078 work.

## Source and routing

Initial origin fetch refspec only fetched main. Explicitly fetched all remote
heads and corrected the local refspec. Existing subscription branch includes the
configured4–7age follow-up and retired provider cleanup. Integrated that branch;
no duplicate adapter retained. Both tracking records and QA dispatch additions
were preserved during conflict resolution. Recovery stash retained.

All existing text inference uses KidsSubscriptionAiClient through KidsAiGateway:
POI quiz, curated/creative trip packs, independent creative review, story author/
regeneration/review and optional child-text response. No executable Gemini/Groq
path, API-key UI or automatic paid-provider fallback remains. Subscription consent
and child-text consent stay independent; no login, consent or token import was
performed. Voice/navigation/device-owner/Accessibility integrations were not changed
in this integration. Retained upstream API31 guard keeps on-device ASR safe on30.
YouTube is separate; owner-local content/configuration and their earlier acceptance
summaries are preserved, with raw historical artifacts remaining local.

Official SIWC sign-in/models/limitations checked2026-10-24. Public Responses uses
OAuth, store:false/stream:true, bounded terminal SSE and catalog models. Existing
OAuth PKCE/state/nonce/OIDC/Keystore/refresh/cancellation tests cover the contract.
References: https://developers.openai.com/siwc/token-sharing-open-source/sign-in,
https://developers.openai.com/siwc/token-sharing-open-source/models-and-inference,
https://developers.openai.com/siwc/token-sharing-open-source/preview-limitations.

## Verification

- [Integrated build](integrated-build-tests.txt): core293 + Android35tests,
  app/testAPK/desktop classes and lintDebug BUILD SUCCESSFUL;0errors89warnings.
- [Full local suite](integrated-contracts-with-art.txt): all repository local
  contracts and core/Android unit checks PASS. First attempt lacked art Python
  imports; existing pinned art dependency directory fixed PYTHONPATH, no source
  workaround or dependency upgrades. Earlier default JAVA_HOME issue is local setup.
- [Corrected QA build](corrected-qa-build.txt): final testAPK and lint PASS after
  isolating the story-failure fixture's fixed5/4ages from owner preferences.
- Actual connected SM-F926B/API35, active Desktop display2,1920x1080. Installed app
  APK hash exactly matches built APK: [receipt](apk-verification.json).
- [Subscription](fold3-t075_subscription.txt): actual gateway parsers with synthetic
  transport, real Android Keystore encrypted roundtrip, native subscription settings
  on explicitly requested DeX2; disconnected test refusal, simulated expiry/quota.
- [Configured ages](fold3-t075_configured_ages.txt):7/6,4/7,6/4,5/4prompt/validation/
  cache consistency and late configuration change rejection, synthetic transport.
- [Coordinator](fold3-creative_coordinator.txt): bounded content-validation retry,
  terminal transport/quota/no-replay, offline cache/cursor/recreation, cancellation.
- [Limiter](fold3-ai_limiter.txt):601fake-clock dispatches, real persisted10RPM/
  6100ms spacing/rollback; zero provider calls.
- [Story failures corrected](fold3-t054_story_failures-corrected.txt): quota at
  author/review makes no retry, cancellation rejects late output, approved offline
  library retained. Initial real-device fixture failure is retained in
  fold3-t054_story_failures.txt; it read owner ages against a fixed5/4episode.
  Only the fixture now uses isolated preferences. No production guard was weakened.

All five final instrumentation modes returned code0. An initial subscription run
used an unrecognized display argument; it is retained separately and not cited for
DeX routing. Explicit display_id2 rerun is the accepted receipt. Early draft build/
4protocol-test logs predate discovery of the existing branch and are superseded;
they are not acceptance evidence for the integrated implementation.

Data comparison/publication receipts follow. LIVE_AUTH_NOT_VERIFIED: real Kids
OAuth registration, account entitlement/model catalog/inference/refresh/logout
remain owner-led. Physical synthetic tests do not establish live service success,
real voice/navigation coexistence, endurance or full natural story acceptance.
Overall T-075 remains IN_PROGRESS. Owner explicitly authorizes verified source
merge despite those open gates; no claim of overall DONE.

## Data preservation and publication scope

After physical fixtures,47/48original app data files are byte-identical to the
protected baseline. Only ART files/profileInstalled differs after installation.
Startup/test runs changed framework IDs, story-source preferences and the story
DB; those three originals were restored byte-for-byte after stopping the app.
Seven isolated QA preference files were removed. The new subscription encrypted
record was created by the disconnected-state fixture; no owner login/tokens were
introduced. [Final data verification](device-final-data-verification.json).
App remains stopped to preserve restored data. Normal source refresh may run on
the owner's next launch. Legacy AI key ciphertext/YouTube settings and parent
preferences are unchanged. Private backup stays in protected local /tmp storage.

Includes original owner source/docs and sanitized prior summaries. Raw historical
screenshots, dumps and private debug provisioning assets are not published.
Publication is explicitly authorized by owner; PR66/main merge receipts and
expected-tip branch cleanup receipt are recorded separately. Live/overall T-075
gates remain open after source publication.
