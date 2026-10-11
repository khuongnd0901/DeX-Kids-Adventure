# T-030 — Per-trip AI question pack: 15–20 Q&A + English words

**Status:** IN_PROGRESS — SOURCE IMPLEMENTED ON `feat/T-030-session-ai-packs`; local Gradle/provider/Fold3 tests NOT VERIFIED.\n**Evidence:** [T-030 source audit](../../evidence/T-030-source-review-2026-10-11.md).
**Parent:** [T-029](T-029-context-aware-ai-trip-companion.md) · [Issue #35](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/35)
**Owner:** Main (orchestration) + Android/AI + Core/Story + QA.
**Target:** Sâu 5 tuổi, Ong 4 tuổi, BOTH 4–5, Samsung Fold3 single-screen DeX.
**No backend**: use the existing direct Gemini / Groq client. No new CI.

## 1. Confirmed product contract

A *new trip* begins when the parent chooses to start an actual KidsActivity
journey (LIVE GPS / GPX replay / DEMO / route learning as explicitly decided
by Parent). Merely opening ParentActivity, changing settings, returning from
background, or rotating/recreating KidsActivity **must not** start a new cycle.
The Android lifecycle can invoke onCreate more than once per trip: define a
stable non-identifying session nonce in saved state and a controller idempotency
key. Clear the nonce on ending the trip; never upload or persist child identity.

On **each new trip**, when the parent has enabled *Automatic AI trip packs*
and a configured/free-tier-acknowledged provider is ready, start **one
nonblocking request immediately** for a fresh general-purpose pack.
Do **not** wait for a GPS POI. The game speaks/plays using the previous
successfully saved pack in parallel; if none exists, use bundled offline
72 questions, 80 English words and 66 activities from first frame.

Each new AI pack contains **15–20 complete items**. Every item:
- a unique local card ID and validated semantic/concept ID;
- Vietnamese question, approved answer (or safe neutral response for an
  open-ended preference question), optional brief follow-up;
- one relevant `wordId` pointing into the authored 80-word English catalog,
  with authoritative meaning/pronunciation determined locally (not by LLM);
- topic, intended focus (SAU/ONG/BOTH), minimum/maximum age and order;
- provenance `GENERAL_CURATED` or `FICTIONAL`, never fabricated GPS fact.

In BOTH mode balance turns approximately Sâu → Ong → shared. Each SAU
question must be age-5 appropriate, ONG and shared questions age-4
appropriate. SOLO never addresses the absent child. Normal POI narration
remains higher priority and pauses the pack cursor, never drops the pack.

## 2. Exact swap / retry / exhaustion semantics

```text
  NEW_TRIP ---------------> LOAD_PREVIOUS_PACK / OFFLINE_FALLBACK
       |                                 |
       +-- (AI enabled & ready) --> FETCH_NEW_PACK (background, 1 in flight)
                                           |
                         +-----------------+------------------+
                         |                                    |
                 VALID 15–20 items                      FAILURE / INVALID
                         |                                    |
                  ATOMIC PERSIST                       KEEP OLD PACK
                         |                                    |
                  SWAP AT NEXT BEAT              BACKOFF -> RETRY <= 5
                         |                                    |
                  DELETE OLD PAYLOAD           FAIL 5 TIMES -> STOP THIS CYCLE
                         |                           |
                  CONSUME WHOLE PACK <--------- KEEP OLD/OFFLINE
                         |
                  PACK EXHAUSTED --> ONE NEW FETCH CYCLE
                         |
                  VALID ? SWAP NEW : REPLAY OLD SAFELY / OFFLINE
```

- **Retry is initial request + up to five additional retries = <=6
  application-level attempts per acquisition cycle**, not six separate
  cycles. Each physical HTTPS attempt (including provider fallback requests)
  must acquire a slot from the global 5-request/minute limiter before dispatch.
  On 429 honor a bounded Retry-After; otherwise use e.g.
  5s, 15s, 35s, 75s, 150s backoff with jitter. The later of rate-limit
  availability and backoff expiry determines the next permitted request;
  never spin or fire a burst of catch-up requests.
- Retrying **never blocks** rendering, offline TTS, Android UI, or input;
  use a single coroutine/worker tied to the living trip session, not a
  background job after trip exit. Do not count a skip as an HTTP attempt
  when provider/network/consent is absent; wait for a bounded eligibility
  recheck rather than an unbounded poll.
- On max five unsuccessful retries, no further automatic calls in that
  acquisition cycle. A new trip or a **new pack-exhaustion transition**
  can open one new cycle, always subject to the **5 RPM rate limiter**,
  provider-specific daily/tokens/spend ceilings if applicable, and
  parent-approved budget settings. Do not refetch every frame or every time
  an already exhausted cursor is checked. Never run two cycles in parallel.
- Successful new pack: verify **entire pack** first; write with
  `AtomicFile`/transaction + version/checksum, then switch active pointer.
  Delete previous payload only after durable success. If process crashes
  mid-write, recover last known valid pack. A card already being spoken may
  complete; next card uses the new pack; reset new-pack cursor once.
- Failed/partial/invalid response: **never overwrite or delete** the
  previous pack. No previous pack -> use built-in offline learning.
- Exhaustion is determined when the last card has been *consumed*
  (question and answer offered), not merely queued on a timer.
  Once exhausted initiate a new acquisition cycle. While fetching, replay
  old content with a reshuffled order and avoid last 3–5 recently played
  cards where possible, interleaving approved offline material. Trigger
  only once per round. Replays do not claim to be new content.
- Cache persists across new trips, not just in-memory. Scope by
  `SAU-5`, `ONG-4`, `BOTH-4-5` (and schema/content fingerprint);
  old packs for other age modes must not bypass age gating. Bound files
  and total bytes. Provide parent option to clear cache.

## 3. Variety: what "not repetitive" means

The model is not allowed to invent factual answers just to create novelty.
Implement a substantial local `CuratedFactCatalog` or extend trusted
offline Q/A concepts and vocabulary. For factual cards the response returns
a **factId / answerId**, which the app resolves to an approved answer.
For English it returns only `wordId` resolved to vetted spelling and meaning.
For open-ended play cards, use preapproved neutral acknowledgements.

Suggested response JSON:
```json
{
  "schemaVersion": 1,
  "items": [
    {
      "conceptId": "sea-wave-wind",
      "focus": "ONG",
      "questionVi": "Gió có thể làm sóng biển chuyển động không?",
      "answerId": "sea-wave-wind-yes",
      "wordId": "wave",
      "topic": "sea",
      "followUpId": "sea-wave-draw"
    }
  ]
}
```
`items` must have 15–20 entries (example shows one shape, not a valid
complete pack). Enforce unique concepts/text inside one pack; check
normalized question hashes and **bounded local recent history**
(e.g. last 50 question hashes and a few previous concept IDs). Ask the
model for topics/concepts not recently used; reject high-overlap batches
and retry within the same five-retry budget if source inventory permits.
Use a mix of >=4 topics per pack and relevant English words instead of
19 wordless rephrasings of one old answer. On insufficient fresh curated
concepts, relax only cross-trip uniqueness, **never factual/age/safety
validation**. Finite knowledge means perfect zero-repeat across unlimited
trips cannot be guaranteed; expand the curated knowledge bank to help
children learn genuinely new facts rather than just novel wording.

AI-generated free-form Vietnamese questions can misstate the answer
even with a valid `answerId`. For fully automatic playback prefer
approved question templates / verified variants or require a reviewed
content validation policy; do not equate syntactically valid JSON with
correct/child-safe dialogue.

## 4. Persistence / cancellation and budget

New interfaces (suggested; names may change after M0 audit):
- `core/story/TripQuestionPack.java`: immutable 15–20 cards + id/age validators.
- `core/story/TripQuestionPackSelector.java`: cursor, round, focus rotation,
  dedup, consume/reshuffle, no repeat last N.
- `android/ai/TripPackApi.kt`: constrained Gemini/Groq response JSON
  to approved fact/word IDs; reuse credential and provider selection logic.
- `android/ai/TripPackStore.kt`: atomic pack storage, recover/age/schema
  integrity, old-pack preservation and recent-concept fingerprints.
- `android/ai/TripPackCoordinator.kt`: `IDLE/FETCHING/BACKOFF/READY/
  REPLAYING/EXHAUSTED/SUSPENDED`, one-in-flight request, session scope.
- Integrate in `KidsActivity` new-trip launch, existing `EntertainmentDirector`
  cadence, `AdventureDashboard` and Parent AI menu. Do not build a new screen.

The old `KidsAiQuizCache` (POI-specific, 14-day TTL) is separate; keep it
functional and do not reuse its `put` as a general-pack swap. Existing
`KidsAiGateway` has `maxCallsPerInstance=20` and per-provider fallback;
count **actual HTTP requests** and coordinate with optional child-reply
and POI quiz prefetch so the pack loop cannot bypass budgets.
**Updated parent-provided rate quota: 5 requests/minute (5 RPM).**
This is a *rate limit*, **not** a claim that the provider offers unlimited
requests per day, tokens per minute, free credits or zero billing.
Do not retain former arbitrary defaults of 8/trip, 20/day, or 20/trip,
40/day as automatic hard cutoffs. The account's actual per-day/token/spend
limits must be read from the configured provider/model by the parent,
and respected independently. A user-configurable daily/spend safeguard
is allowed but must not silently invent a low quota.

**Implementation:** share one persisted, thread-safe async limiter among
pack generation, existing POI prefetch and optional child-text replies.
Conservatively cap *all* actual HTTP dispatches at **5 per rolling 60s**,
including retries and provider fallback; use **one in-flight request**
for new general packs. Easiest safe starting policy is **>=12 seconds
between consecutive HTTP start timestamps**, using a monotonic clock,
with a short timing margin to avoid boundary races. If a provider has
a lower documented RPM, enforce the lower per-provider rate as well.
Do not queue unlimited stale calls; pause or discard nonessential POI
prefetch in favor of one requested trip pack, and keep audio/UI local.

A pack response has 15–20 Q&A items: one successful request can produce
an entire pack, **not** one request per question. Five RPM does not
mean generating every minute when it is unnecessary. Trigger acquisition
only at a **new trip** and on **actual pack exhaustion** (plus the bounded
retry schedule). One initial request and five retries cannot be sent
all at once: with >=12s pacing, six starts span **at least 60 seconds**
before network latency/backoff. 429 Retry-After and exponential backoff
can increase that duration. The retry maximum is a ceiling, not a
guarantee when rate, account quota or parent budget blocks the call.
The existing `KidsAiGateway.maxCallsPerInstance=20` must be audited and
refactored/removed *only when* replaced by a correct shared metering
mechanism: creating another gateway instance must not reset the rate.
Never assume a specific Gemini/Groq model/account really has 5 RPM
until confirmed in Parent/provider dashboard.

Treat 400/401/403, invalid credentials, invalid output schema, no provider
or revoked consent as **nontransient** or deferred conditions, not
infinite retry loops. Transient network/408/429/5xx can retry; successful
HTTP but rejected pack is treated as failed acquisition, still within
the same five-retry limit. When the app goes to background, exits, parent
opens menu, or session changes, suppress callbacks and speech via epoch;
no network call or delayed switch after parent revocation. Do not upload
coordinates, child voice/transcripts, child names, raw location histories
or API keys in prompt or telemetry. The new *Automatic AI trip packs*
preference defaults OFF until parents explicitly enable it; do not
automatically send merely because old `KidsAiSettings.enabled` was ON.

## 5. Test matrix / gates

- Initial start with saved old pack and with empty store: no waiting UI.
- New trip after normal close: exactly one request cycle; Activity recreate,
  pause/resume, return to Parent, GPX rerender: no duplicate cycle.
- Valid 15/18/20 item responses with valid words, ages, topics: atomic swap,
  reset cursor, no data gap, one card at a time.
- Fail at 14 items / 21 items, invalid word, false location, made-up factId,
  repeated concepts, unsupported category, wrong/age-inappropriate focus:
  reject and **keep old**.
- Five retries after first failure: exactly <=6 application attempts;
  enforce shared 5 rolling-60-second HTTP requests max including provider
  failover, retries and parallel POI/chat calls (>=12-second start spacing);
  confirm 429 Retry-After/backoff; 401 stops early; no background activity.
- Crash immediately before/after writing new pack: always recover
  fully validated old or new, never corrupted partial data.
- Exhaust last card: exactly one refill cycle; old card order reshuffled;
  no same immediate last 3–5 cards, offline fallback when impossible.
- BOTH rotates Sâu 5 / Ong 4 / shared 4; SOLO never mentions other child;
  app default audience BOTH unaffected.
- No Wi-Fi, no model/key, Free Tier not acknowledged, provider daily/token
  quota or parent-set spend budget exhausted, HTTP RPM slot unavailable,
  API call timed out, parent disables AI while fetch pending, app background,
  GPS interrupts episode: graceful no-block/no-leak fallback.
- Separate privacy gates: pack prompts include **only** curated topic,
  factId and wordId; never child transcript or precise GPS.
- Android `./scripts/test-local.sh`, `./gradlew --no-daemon
  :core:test :android:testDebugUnitTest :android:assembleDebug`;
  instrumented lifecycle/persistence tests; real Z Fold3 1920×1080 DeX
  audio focus, subtitles, P95 FPS/PSS and provider account usage checks.
  The absence of actual local test logs means **NOT VERIFIED**, never DONE.

### Additional rate-limit regression cases

- Exactly 5 requests within a moving 60s window; sixth is queued until its
  permitted timestamp, not discarded and not fired early. New provider
  instances and Activity recreation cannot bypass the shared limiter.
- Requests from `TripPackCoordinator`, POI cache prefetch and child-text
  response all use the same limiter, including actual fallback-provider HTTP.
- A successful single request containing 20 cards is counted as **one**
  request, not 20; no extra requests while cards remain.
- On 429 with `Retry-After`, observe that delay even when a rate slot opens
  first; 5 retries maximum, exponential backoff and single in-flight fetch.
- Repeated sessions and multiple refills are permitted under 5 RPM and
  confirmed provider daily/token constraints, without a fabricated global
  20-requests/day cutoff. Source-only tests cannot prove external quota.

## 6. Execution slices (Codex implementation order)

- **T-030.M0**: Audit lifecycle/provider budgets, baseline and data policy;
  decide approved semantic source catalog / review method.
- **T-030.M1**: Pack/selection/core models + JUnit (15–20 count,
  audience/focus/age, novelty, exhaustion).
- **T-030.M2**: Durable atomic storage + instrumented corruption tests.
- **T-030.M3**: Constrained schema/provider API + pack validator and
  no-PII prompts, provider-fallback/budget integration.
- **T-030.M4**: Coordinator and bounded retry scheduler + cancellation tests.
- **T-030.M5**: Wire KidsActivity, narrator, learning flow, parent setting
  and HUD to select new/old/offline without UI/GL blocking.
- **T-030.M6**: Local builds, provider tests, 30–60min Fold3 DeX run,
  parent content review and recorded benchmark gates.

Each slice must update `.agent/STATUS.md` and `.agent/TASKS.md`.
No CI or changes to DeX-Assistant. **Source implementation is on feature branch; M6 runtime/device acceptance is still OPEN.**
