# T-081 — actual subscription feature acceptance

Owner Main. Status IN_PROGRESS. Dependencies T-075/T-079/T-080, owner-authorized
live feature testing, connected Fold3/DeX and existing Kids account/model.

Acceptance: actually exercise POI quizzes, curated trip question/English pack,
creative generation + independent review/refill, child-text reply using generic
synthetic text, story author + review, independent story review and scene regeneration
+ review. Run production validators and isolated cache/repository roundtrips. Never
approve/publish content for children. Preserve owner settings, subscription session,
model and libraries; do not export tokens or real child/route/audio data. Keep
physical limiter history and any successfully refreshed credentials current.

Use explicit authorized instrumentation mode on active Fold3 Desktop; delegate
transport directly to production KidsSubscriptionAiClient with isolated business
preferences/files/DB, original current ages. Record successful calls, duration,
validator errors and generic generated text only; bounded candidate attempts, no
ambiguous transport replay. Fix measured defects without weakening consent,
content/safety/schema/terminal-response validation; appropriate unit/build checks
and real device reruns required for fixes. Model/settings unchanged by test.

Pre-test protected app-data backup. Per-feature status retained for failed as well
as corrected runs. Final summary distinguishes live accepted features, refused
content, missing field/voice/endurance and mocks. Update STATUS/TASKS and publish
verified source/evidence under existing owner authorization after acceptance.

## Measured findings and corrections

Initial live matrix: child sample, POI structure/cache and curated IDs passed;
creative transport failed17.4s, author failed20.1s and regeneration21.0s. Review
of the bundled visual showcase was explicitly declined (approved:false), correctly
not admitted. Inspection found POI open preferences/performance prompts that did
not match the spoken source-fact answer; tightened factual recall/direct answer
instructions, no new model-authored answer path.

Background authoring uses bounded45s read/120s total; short interactions retain
15s/20s. Actual corrected generation: story35.5s + review5.3s; separate review6.0s;
regeneration27.4s + review5.7s;20creative lessons61.3s + independent review9.4s,
all production validators and isolated storage passed. No field guard removed.
POI after prompt tightening hit explicit20s timeout because the8KiB envelope was
still classified interactive. Include structured multi-item schema requests in
the generation tier; actual corrected POI + curated selection rerun pending.
Typed RESPONSE_TIMEOUT is terminal and does not trigger transport replay. Unit
stalled-body timeout/no-replay and long-budget cancellation checks retained.
