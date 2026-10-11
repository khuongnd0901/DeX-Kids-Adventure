# T-029 — AI Trip Companion: hội thoại theo ngữ cảnh chuyến đi

**Status: PLANNED / NOT IMPLEMENTED (2026-10-11).**
Owner: Main agent. Tracking issue: https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/33.
Target: Samsung Z Fold3 single-screen 1920×1080 DeX, Kotlin Android, Java core/LibGDX.
**Scope of this PR is planning only. Do not claim tests or integration completed.**

## 1. Product objective and constraints

Capybara becomes a **context-aware, offline-first companion**, not a free-chat assistant:
1. Respond to *accepted approximate* GPS POI cues, current illustrated scenery, approved
   offline questions and vocabulary, with clear distinction between nearby candidate POI
   and fictional illustration. No road-map matching/arrival assertion.
2. Ask simple short questions and follow-ups about sea, road, nature, cities,
   weather, animals, garden and safety; adapt to **Sâu age 5**, **Ong age 4**.
3. Introduce approved English words in context, use installed offline English TTS,
   invite repetition; **never assert speech pronunciation was correct** unless a
   separately reviewed on-device pronunciation evaluation is later implemented.
4. Both mode rotates speaker focus Sâu→Ong→shared. Shared content uses age 4,
   individual turns use ages 5/4. ASR must never infer which child spoke.
5. Continue without Internet, keys, model, microphone or EN voice; preserve the 66
   legacy beats, 72 offline quizzes, 80 English words, GPS cue priority and animations.
6. Keep direct Gemini/Groq Android API: no backend; no DeX-Assistant integration;
   no GitHub Actions CI; no hidden background location collection.

### T-030 confirmed pack-refresh behavior (new requirement, 2026-10-11)

**Priority P0, proposed as the first deliverable under T-029 M3.**
The parent now requires **each new trip** to request a fresh **15–20
general question/answer items with relevant English vocabulary**, while
immediately playing the previous valid pack or bundled offline content.
A successful fully validated pack replaces the old one **atomically**;
a failed request preserves the old pack. Retry up to **five additional
times** per acquisition cycle, with backoff/budget gates. On consuming the
pack, attempt another pack; on failure replay the old pack with novelty
controls and offline fallback. No duplicate requests on Activity recreation
and no invented facts or automatic child-text upload.

**Implementation source of truth:**
[`T-030 Session AI Question Packs`](T-030-session-ai-question-packs.md),
[issue #35](https://github.com/khuongnd0901/DeX-Kids-Adventure/issues/35).
T-029 M0/M1/M2 are dependencies; M3 requires T-030.M1–M5, while M6
covers its real-device acceptance. All production work remains **PLANNED**.

## 2. Baseline audit (read before implementing)

- `KidsActivity.queueLiveConversation` only retrieves cached AI POI quizzes or
  prefetches packs; it does not generate a coherent conversation episode.
- `KidsAiGateway.generate` generates 10–12 questions tied to supplied factIndex;
  `childReply` is only used after separately consenting to short child-text cloud.
- `KidsAiQuizCache` has age + source-digest keyed cache, TTL 14 days.
- `EntertainmentDirector.nextJourney` alternates deterministic offline beats and
  152 learning cards; context currently derives from `PoiBackdropCatalog` for 150s.
- `KidsAiSettings.enabled` defaults true as preference but provider key/model/free-ack
  are required. `cloudChildReply` is **false by default**.
- `OfflineVietnameseNarrator` and `OnDeviceChildSpeech` are separate local speech
  outputs/inputs; preserve audio-focus and epoch-cancel guards.
- Known weaknesses to audit: generic content incorrectly using last sourcedFact;
  stale POI/topic after GPS loss; triggering cloud replies from unsupported provenance;
  multiple futures speaking after a new POI; too many quota-consuming prefetches.

## 3. Runtime design

```text
GPS accepted POI candidate -----┐
Rendered scene snapshot ---------┤
OfflineLearningCatalog / beats --┼--> TripContextEngine (pure Java, immutable)
Parent audience + 4/5 age ------┘             |
                                  ConversationOrchestrator (Android state machine)
                                     |
                    +----------------+----------------+
                    |                                 |
                 offline                         AI extension
         episode / quiz / English       parent enabled + provider ready?
         deterministic and instant          cache valid?
                    |                  bounded async prefetch/call
                    +----------------+----------------+
                                     |
                        schema + fact/word ID validation
                                     |
                            TTS -> optional local ASR
                                     |
                             age-aware short response
                                     |
                       subtitle/animation via existing game
```

Two separate context types:
- `SOURCED_POI_ESTIMATE`: approved input format with OSM ID, **allowed fact
  IDs/text**, provenance level, coarse theme and expiry (not raw lat/lon).
  Label remains `GPS gần địa danh · ước tính`; absence of validated facts
  prohibits factual AI landmark answers.
- `FICTIONAL_SCENE` / `GENERAL_OFFLINE`: visual and offline-topic IDs only,
  never passed off as an actual nearby place. Screen artwork is a game world.

`TripContext` has a version, freshness/expiry, audience, **focus** age,
topic, source type, curated text IDs, vocabulary IDs, episode sequence and
privacy permissions. Deliberately exclude coordinates, heading, speed trace,
full ASR transcripts, names, private family/school data and GPS history.

State machine:
`IDLE -> SELECT_CONTEXT -> SPEAK_QUESTION -> [LOCAL_LISTEN] -> RESPONSE
-> OPTIONAL_FOLLOWUP -> COOLDOWN`.
New POI cue, parent F10, session stop, consent revocation or audio interruption
invalidates `talkEpoch` and drops pending work. Maximum 2 follow-up exchanges.
Never overlap local TTS and microphone or bypass parent microphone permissions.

## 4. Milestones / child-task execution order

### M0 — Audit and baseline capture (P0)
**Files:** `KidsActivity.kt`, `KidsAiGateway.kt`, `KidsAiStore.kt`,
`KidsAiQuizCache.kt`, `EntertainmentDirector.java`, source tests.
- Document actual event priorities, 5s connect/12s read, 20 per-gateway cap,
  consent boundaries and cache behaviour; no source changes in this subtask.
- Record baseline local `:core:test`, `:android:testDebugUnitTest`, APK build,
  and Fold3 snapshot if a device is connected. Do not fabricate results.
**Gate:** baseline/reproducible failures and guardrails recorded.

### M1 — TripContextEngine, local provenance-safe resolver (P0)
**Owner: Core/Geo.**
Suggested new `core/.../story/TripContext.java`,
`TripContextResolver.java`, `TripContextTest.java`, small renderer-to-UI
scene snapshot bridge.
- Immutable bounded context, source classification, validation/expiry,
  child focus and age. Rendered scenery is fictional unless backed by POI.
- Reject stale POI/old ASR/unsupported facts; reset on journey restart.
**Gate:** no coordinate/child-input leakage, test source/expiry/source-type matrix.

### M2 — Offline-first episode conductor (P0)
**Owner: Core/Story + Android.**
Suggested `OfflineEpisodePlanner`, `ConversationOrchestrator`; integrate
`EntertainmentDirector`, `OfflineLearningCatalog`, and existing HUD/TTS.
- Build 3–5 beat mini-episodes from approved cards (intro, question, hint,
  English word, recap). Do **not** let LLM generate factual answers.
- Sâu 5 gets counting/comparison/why questions, Ong 4 gets recognition,
  short two-choice questions; together rotates turns with age 4 shared.
- No POI = generic or current explicitly fictional scene game.
**Gate:** no AI/key/network required, no naming other child in SOLO, no repeats
in a short run, no blocked GL/UI thread, POI still interrupts episode.

### M3 — AI-assisted bounded lesson drafts (P0)
**Owner: Android/AI + Core content validation.**
Suggested `KidsAiLessonGateway.kt`, `KidsAiLessonCache.kt`,
`AiLessonValidator.java`, added Parent setting `aiTripEnrichment`
**OFF by default** until explicitly enabled.
- Only issue request with `SOURCED_POI_ESTIMATE` and curated fact IDs, or
  `FICTIONAL_SCENE` with approved topic/word IDs. No raw GPS or child text.
- Constrain AI JSON: episode theme, question phrasing, `factIndex` or
  curated `wordId`, neutral encouragement and optional next step. Resolve
  actual factual answer/English translation locally, never accept model claims.
- Validate output limits, repetition, age, prohibited location assertions,
  bad words and source IDs. An index match alone is **not** proof that a
  model-generated question is correct: add adult preview/review for prebuilt
  lesson packs before general use.
- Prefetch asynchronously only when allowed and useful; reuse existing
  Gemini/Groq credentials, eligible provider toggles, free-tier acknowledgment.
- Cache key = source hash + lesson schema version + topic + focus age +
  approved content fingerprint. TTL <=14d, bounded item count, manual clear.
**Gate:** malformed/unsafe/hallucinated outputs fail closed to M2; no
model-generated commands, no unsolicited network on startup.

### M4 — Safe two-way conversation (P1)
**Owner: Android/Voice.**
Extend `askAndListen`/response flow via a single orchestrator:
- Local Vietnamese ASR and local `ChildAnswerInterpreter` stay available.
- Short cloud-child-text call ONLY with existing distinct explicit consent,
  configured provider, validated sourced/context-specific reference and
  live consent recheck; no open-ended autonomous model chat.
- Default without consent = complete offline reply + optional follow-up.
  In BOTH, **do not infer Sâu/Ong identity from the voice**; rotate scripted focus.
- Keep only bounded *non-personal* topic/word progress in memory until end;
  never persist child transcripts or conversation logs. PII filtering is
  imperfect; reject uncertain input and always offer local fallback.
- Cancel calls/readback with epoch on parent F10, GPS priority, lost audio
  focus, permission removal, app exit; guarantee no late answer after stop.
**Gate:** transcript never sent under disabled/revoked consent or unsupported
provenance, no duplicate speech, follow-up count capped, interruption safe.

### M5 — English Trip Companion (P1)
**Owner: Content/Voice/UI.**
- Reuse **80 authored English words** and local translations (sea/wave,
  bus/wheel, tree/flower etc.); attach lesson to rendered/curated theme.
- Short drills `listen -> repeat -> choose picture -> recap`; variant by 4/5.
- Play English ONLY via installed non-network EN TTS; otherwise show
  bilingual text and continue. No pronunciation score based on silence/ASR
  guesses, no network translation or new cloud STT.
- Visual captions in existing DeX single screen, no touch requirement.
**Gate:** offline EN voice missing test, correct vocab ID/meaning and focused
turns, clear subtitle on FullHD without covering navigation overlays.

### M6 — Parent controls, quotas, integration & Fold3 QA (P0)
**Owner: Main/QA.**
- In existing Parent AI menu add `AI Trip Companion` toggle (OFF initially),
  clear cached lesson packs, request usage counter and cloud-child-text consent
  warning; no separate fullscreen menu.
- **T-030 overrides the former 8/trip, 20/day suggestion.** A new 15–20
  item pack can require initial + five retries and additional refills;
  therefore define ONE shared physical HTTP request budget for general
  packs, POI prefetch and cloud-child replies. Suggested starting policy:
  <=20 requests per trip and <=40/day with parent-adjustable LOWER limits,
  never exceeding account/provider quotas or verified price constraints.
  At most one pack fetch in-flight. Daily/session budget and explicit
  provider consent always override retries; count provider failover
  requests too. No Free Tier or zero-billing guarantee.
- Prefer cache before request, no repeated retries during an active dialogue,
  nonblocking cancellation/fallback; instrument local latency and failures
  **without** speech text, GPS, child IDs, or API secrets.
- Local tests: offline/no-key/airplane mode, both provider errors 429/500,
  5s+12s timeouts, stale location, invalid schema, parent revoke mid-request,
  EN voice absent, mic denied, interruption, 4/5/BOTH focus, 60-min soak.
- Fold3: 1920×1080 one-screen, F10/parent pause, actual Vietnamese child
  voice in a safe stationary test, navigation audio mixing, frame-time/P95 FPS,
  PSS and API request tally. Never claim PASS without local evidence.
**Gate:** working APK/tests, fact and privacy review, adult approval of AI
content, no FPS regression versus an actually measured device baseline.
No release until privacy and provider terms reviewed.

## 5. Contract examples (illustrative, not executable code)

Allowed AI-input facts (context only):
```json
{"kind":"SOURCED_POI_ESTIMATE","topic":"sea","factIds":["sea-wave-wind"],
 "audience":"BOTH","focus":"SAU","age":5,"wordIds":["wave","boat"],
 "locationLabel":"nearby_candidate_only"}
```
Non-POI content input:
```json
{"kind":"FICTIONAL_SCENE","topic":"garden","factIds":[],
 "audience":"ONG","focus":"ONG","age":4,"wordIds":["flower","tree"]}
```
Model response must refer to an existing `factIndex` **or** `wordId`.
No claimed actual place/position, novel factual answer, pronunciation grade,
free-form model action or direct game-state mutation.

## 6. Test commands and definition of done

```bash
./scripts/test-local.sh
./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug
# Then run instrumented + actual Samsung Fold3/DeX smoke,
# record build SHA, device, P95 FPS/latency, request counts and evidence.
```

Check each milestone separately, merge independently after its local gates.
CI remains disabled. Main updates `.agent/TASKS.md`, `.agent/STATUS.md`
and `.agent/evidence/T-029-*/` after each verifiable milestone.
**This plan does not authorize automatic production implementation.**
