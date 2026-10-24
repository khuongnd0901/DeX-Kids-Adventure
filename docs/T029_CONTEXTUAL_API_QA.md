# T-029 — age, POI and time-aware question integration

Owner Main; dependencies T-011/T-027/T-028.
Default ages are Sâu5/Ong4; parent-selected ages4–7 are supported
for AI POI questions/replies. The authored catalog remains ages4–6. Removed the24 offline quiz rows
whose minimum age was3;96 quizzes remain after retaining upstream additions. All80 vocabulary cards remain,
now available from age4. The learning parser rejects any replacement card
below age4. Obsolete age2/3 AI cache entries/cursors are removed locally.
Existing sourced POI dialogues and route narratives remain source-bound.

The production GPS quality/proximity pipeline selects an existing POI locally.
Its sourced facts, selected age and coarse local day period enter the AI
prompt. No precise coordinates, date, timezone or raw audio enter that prompt.
Morning05:00–10:59, noon11:00–13:59, afternoon14:00–17:59, evening18:00–04:59,
using the device local clock. These are conversational categories, not
observations of daylight/weather. Wrong device clock can select the wrong period.

Question answers must reference a provided fact index. The provider must
include the supplied day period in at least one question/follow-up; invalid
responses retain the existing offline fallback. Cache keys separate POI,
age and period. Prefetch captures one period for both request and cache write,
so crossing a period boundary cannot write morning questions into evening.
Replies use the selected child's age and current period with the existing
independent parent text-sharing consent and personal-data filter.

Validation:
- Core boundary checks cover all24 local hours and invalid hours.
- Catalog tests reject ages2/3 and removed toddler quiz packs.
- Existing source/Android/desktop builds and affected DeX tests.
- T-075supersedes retired provider-key scripts. Use native parent **ChatGPT
  Subscription** settings for deliberate sign-in/model/consent and the safe
  Vietnamese inference test. Real sign-in/catalog/safe inference was verified in
  T-080, and the seven primary content paths in T-081. Do not inject credentials
  or infer live success from fixtures.
- Controlled Windows AVD regression uses framework instrumentation modes
  `t075_subscription`, `t075_configured_ages`, `creative_coordinator` and
  `t054_story_failures` on an explicit display. These use synthetic transport and
  never claim real inference, geolocation or ASR. Current receipts:
  [subscription migration](../.agent/evidence/T-075-chatgpt-subscription/SUMMARY.md),
  [configured ages](../.agent/evidence/T-075-chatgpt-subscription/configured-ages/SUMMARY.md).
- `scripts/fold3-soak-resources.py --creative` is offline reviewed-pack replay only.
  The fixture disables automatic generation; no key import/live-refill/matrix
  options remain. Physical runs still require the target device and reviewed
  fixture and do not become PASS because the collector exited successfully.

The original T-029campaign checked downstream GPS-to-POI/API integration with synthetic inputs;
its historical evidence remains unchanged. New controlled transport tests check
the subscription business/validation boundaries.
It does not establish real road position, AndroidGPS acquisition, ASR, human
child engagement or full30/60-minute endurance. Final outcomes are recorded
in `.agent/evidence/T-029-fix-regressions-2026-10-11/summary.md`.

Visual/audio corrections in this same slice: swimming-duck props are excluded
from dry parks, beach/lake illustrations use explicit water anchors, authored
panoramas suppress unrelated procedural lamps/companions. FullHD rabbit/flower
activity objects are approximately2.9/2.6 times their previous widths; counting
flowers80px wide instead of30. Desktop `--story-beat` enables bounded visual
QA of the actual renderer. Main desktop age labels now match5/4.
Parent explicitly chose ambient music ON for both DEMO and LIVE. Existing
manual controls, low0.035f stream level, narration/menu/background pause remain.
`music_smoke` checks actual stream startup and production narration duck/resume;
`enable_ambient` persists only the authorized ambient preference on the device.

Sâu's occasional reminder is a local care activity, once per18 activities
(roughly several minutes depending on pauses and speech), SAU/BOTH only.
It gently asks him to relax his lips, without appearance shaming, sensing,
recording or API input. Uses an existing non-learning slot and persisted
activity cursor; POI and parent/menu/listening priorities remain in effect.

## T-081 live subscription feature check

Owner-authorized live checks use the actual signed-in Kids subscription/model,
production transport/limiter and validators. Business consent/settings/cache/DB
are isolated; only generic sample text and existing public source facts are sent.
Each feature is explicit and bounded; no child session or automatic approval starts.

```sh
adb shell am instrument -w -e mode t081_subscription_live -e display_id 2 \
  -e ai_live_authorized true -e ai_feature poi \
  com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner
```

Feature values: `poi`, `trip`, `creative`, `child`, `story_author`, `story_review`,
`story_regenerate`. Live mode requires the physical Fold3 and active DeX Desktop;
missing authorization fails before any subscription request. Use only when the
owner requests live testing; CI/local unit tests must not consume a saved account.
The child test uses a generic authored phrase and checks consent/private-text
refusal locally, without actual child speech. Generic outputs/reports stay under
app cache `qa-t081-live`; response files are valid only for completed requests.
Clean task cache after collecting receipts; preserve owner settings/library/session
and real rate history. Never restore an old rotating credential after refresh.

Structured batches and story generation/review get bounded45s read/120s total;
short interactions retain15s/20s. A `SUBSCRIPTION_RESPONSE_TIMEOUT` failure is
terminal for that acquisition; there is no ambiguous stream replay or provider
fallback. POI wording must ask literal factual recall directly answerable by its
selected source fact; preference/performance prompts and invented place distractors
are excluded. Semantic inspection remains distinct from structural validation.

[Actual feature matrix and retained failures](../.agent/evidence/T-081-live-subscription/SUMMARY.md).
Natural child playback, live ASR/TTS, long-soak and live refresh/logout remain
separate gates; successful mocked coordinator checks do not establish them.
