# AI Interactive Story Adventure — Sâu & Ong
Status: IMPLEMENTATION IN PROGRESS · 2026-10-15
Implementation notes: [source behavior and limitations](STORY_ADVENTURE_IMPLEMENTATION.md). Runtime acceptance remains evidence-gated.
Implementation tasks: T-054 through T-059; canonical execution plan: [T-054–T-059 ExecPlan](../.agent/plans/active/T-054-T-059-ai-interactive-storytelling.md).

## 1. Product goal
Turn educational storytelling into an interactive, replayable, illustrated journey feature. Sâu is **5**, Ong **4**; support SAU, ONG, BOTH (BOTH shared learning content uses age 4). Make them story characters, not merely spectators; Capybara serves as guide. Story episodes interleave narration, 2–4 age-appropriate questions, answers, feedback, animated poses, and a concrete non-preachy lesson. Typical episode: 4–8 scenes / about 4–8 minutes; timing is flexible and driven by actual speech completion.

Parent can create by topic/learning goal/audience, preview/review, edit dialogue, regenerate only affected scene, approve, archive/delete, favourite, and choose auto-play/random. Saved approved stories can replay offline with no AI calls. Provide offline fallback whenever AI, mic, network, or optional image provider is unavailable.

## 2. Existing architecture / reuse
- Core: `EntertainmentDirector`, `CreativeLessonPack`, `VisualCue`, `VisualPlan`, `LocalVisualPlanner`, `AnimationTimeline`, `AnimationRegistry`, `CharacterAnimationController`, `OfflineEpisodePlanner`.
- Android: `KidsAiGateway`, `KidsAiDispatchLimiter`, `CreativePackStore`, `TripPackCoordinator`, `OfflineVietnameseNarrator`, `OnDeviceChildSpeech`, `KidsActivity`, `ParentActivity` and `ParentSettings`.
- Graphics: current LibGDX single SpriteBatch / storybook-v2 scene/backdrop and Sâu/Ong/passenger poses, approved 12 random outfit overlays; do not overwrite the original character art or the T-048 Capybara/bus originals.
- Existing creative STORY cards are *short lessons*, not persisted interactive multi-scene stories. Do not mutate that format in-place; introduce versioned independent story persistence. Maintain shared AI limiter and child-cloud consents.

## 3. Domain contract (core Java, deterministic)
`StoryEpisode(id, schemaVersion, revision, title, audience, sauAge, ongAge, durationHint, tags, educationalGoal, synopsis, authoredSource, moderationStatus, scenes, createdAt, updatedAt)`.
`StoryScene(id, order, backdropKey, narrationBeats, actors, props, question?, learningMoment?, visualVersion)`.
`StoryBeat(actorId, utterance, emotionKey, actionKey, propKeys, transition, timingPolicy)`.
`StoryQuestion(target: SAU|ONG|BOTH, promptVi, acceptedConcepts, hints, feedbackVi, silenceFallbackVi, maxWait)`.
`StoryActor(actorId: SAU|ONG|CAPYBARA|ALLOWLISTED_COMPANION, poseKey, costumeKey, movementKey)`.
All AI fields must pass explicit length, profanity/safety/age, chronology/coherence, real-vs-fiction, asset-key allowlist and cross-field consistency checks. Never allow AI file paths, GLSL/shaders, arbitrary sprite IDs, coordinates, executable commands, external media URLs or hallucinated GPS/POI claims. Schema must support migrations.

Example fragment:
```json
{
  "schemaVersion": 1,
  "audience": "BOTH",
  "educationalGoal": "Giúp đỡ bạn bè",
  "scenes": [{
    "id": "forest-02",
    "backdropKey": "FOREST",
    "narrationBeats": [{"actorId":"CAPYBARA","utterance":"Sâu và Ong gặp bạn thỏ đang buồn.","actionKey":"TALK"}],
    "actors": [{"actorId":"SAU","poseKey":"POINT","costumeKey":"SUPERHERO"},
               {"actorId":"ONG","poseKey":"CURIOUS","costumeKey":"PRINCESS"}],
    "props": ["RABBIT"],
    "question": {"target":"ONG","promptVi":"Mình có thể giúp thỏ thế nào?",
      "acceptedConcepts":["Giúp tìm thức ăn","Hỏi xem bạn cần gì"],
      "silenceFallbackVi":"Mình thử hỏi bạn thỏ có cần giúp gì nhé."}
  }]
}
```
This is a *proposal*; adapt the exact DTO names/keys after auditing active asset registry. `POINT` may need a supported pose or explicit fallback; never assume it already renders.

## 4. AI authoring + quality
Parent request contains only general topic, goal, speaker enums, ages and optionally a non-identifying setting. Generate entire JSON episode on a background worker, deterministic validate, then independent AI review for age suitability, narrative continuity, learning value, absence of fear/shame/danger, factual soundness where applicable, and role/action/scene consistency; parent approval before publishing. AI review is fallible: parent can reject/edit. Store DRAFT separately; only fully approved, validated revisions enter autoplay. Hard-bound requests/retries, input/output size, count of scenes and actors, retry-after, shared 10 dispatches / rolling 60s and >=6.1s spacing *as currently implemented*, provider-specific quotas and explicit AI consent. Never send GPS tracks, home address, children's audio, raw transcripts, photos, full names or child history. Keep provider keys in existing protected storage. No backend and no new service credentials required for offline replay.

## 5. Story playback orchestration
Use one deterministic state machine: IDLE -> OPENING -> NARRATING -> QUESTION -> WAIT_TTS_COMPLETION -> LISTENING -> FEEDBACK -> NEXT_SCENE -> COMPLETE, with PAUSED / INTERRUPTED / FAILED. Actual TTS completion gates ASR: never listen while TTS or navigator audio is speaking. Age-matched questions rotated Sâu/Ong/BOTH for BOTH, no repeated pressure on nonresponse: after bounded silence proceed with fallback. Offline ASR and text fallback; cloud child reply only under separate explicit consent. Child answers are ephemeral by default: do not upload/log/persist personal speech. Store only playback episodeId/approvedRevision/scene cursor and last-played metadata.

Existing source-backed POIs / GPS / navigation audio remain higher priority. Interrupt at safe beat boundary (or immediate pause for safety-critical audio), preserve scene+question state, resume without duplicating prompt/side effects. Fiction must be labelled as pretend; never assert the real vehicle is at an invented place. Parent F10 / Menu / Back and lifecycle pause/resume work on the single, non-touch Samsung DeX 1920x1080 display.

## 6. Visual story direction: hybrid
P0: story beats map to current allowlisted sprites, original Sâu superhero / Ong princess costume overlays, local backdrop props, existing timeline and animation registry. Extend small pose/action vocabulary only where assets and tests verify support. Keep child likeness consistently based on *illustrated in-repo assets*, no original face photos. Narration and animation run from the same canonical beat clock, with event-based updates rather than per-frame parsing.

P1: *optional* image generation for missing exceptional backdrops / static prop illustrations **at story creation time only**. Image provider may be billed; check capabilities and cost before enabling, default OFF. No invented claim that Gemini/Groq chat keys provide a free image tier. Use character-agnostic backgrounds and overlay the unchanged Sâu/Ong sprites for identity consistency. If image provider supports referenced cartoons, adult approval and image rights review still gate use. Limit image resolution/count per episode, asynchronous decoding/scaling/cache/GL-thread upload, texture memory budget; no network or decode in render loop. No arbitrary SVG/image import by AI. Offline fallback to approved resident atlas if image generation fails.

## 7. Parent Story Library
Android Room/SQLite repository (or justified existing local storage adaptation): `story_episode`, `story_revision`, `story_scene`, `story_asset`, `story_playback` with migrations and transactional writes. Cache media under app-private files with manifest/checksum, atomic publish, size/count limits and orphan cleanup; image/shared-asset reference handling on deletion. Expose create, generate draft, preview, approve, edit text (scene-level invalidation+revalidation), regenerate scene visuals, version history/undo, delete (confirmation), favourite, disable autoplay, filter audience/goal, and replay offline. Editing text that changes action/prop/meaning invalidates affected cue/assets until rebuilt or safe fallback; never silently retain mismatched visuals.

Random rotation: avoid last N stories, prioritise unheard, deterministic seed for tests; permit manual replay any story. Age/content filters use authoritative parent ages; no mixing wrong-audience stories. Keep total generated-story store bounded and prevent the OS storage from being filled with images.

## 8. UX / audio
No new parent home-screen top-level button required: add **Thư viện truyện** under Parents/Cài đặt, optionally shortcut in F10. Parent uses mouse/air mouse; child screen is read-only, not touch-dependent. Parent reviews complete story, education goal, approximate duration, number of questions, runtime imagery availability and online/API cost before approval. Support continue, replay, skip story, pause/resume, volume focus. Navigation/GPS and POI announcement should not be masked. Preserve offline TTS, soundscape, audio focus and accessibility.

## 9. Nonfunctional, security and tests
- Preserve Kotlin/Java, LibGDX, existing single display, Device Owner/DeX-Assistant IPC, GPS/POI, provider limits, old content, no CI workflows. No blocking operations on render/UI threads.
- Bound scene strings, JSON size, episode count, texture residency, archive size; prevent renderer memory growth over 30–60 minute sessions. Benchmark against current checked-in emulator baseline and document any regression; target stable >=30 FPS if achievable but **do not claim it without measurements**. Physical Fold3 DeX gate remains separate.
- JVM/unit tests for schema, review rejection, cue allowlist, age focus (Sâu 5/Ong 4), BOTH turn-taking, state resumes, POI interruptions, deterministic shuffle, revisions, storage rollback, asset reference cleanup, malicious/invalid JSON and API limit. Android emulator 15/API35 Windows 1920x1080 integration with realistic ASR timeout and network-off story replay.
- No new CI; local `bash scripts/test-local.sh`, `./gradlew :core:test :android:testDebugUnitTest :android:assembleDebug`, art/atlas checks, actual simulator screenshot and performance logs required for status PASS.
- No silent paid API calls; image generation is P1 optional, disabled by default. Content generated by AI cannot be assumed factually accurate or age appropriate merely due to AI review.

## 10. Milestones
| Task | Deliverable | Priority | Acceptance |
| --- | --- | --- | --- |
| T-054 | Versioned story JSON, author/gateway, safety+review | P0 | valid reviewed draft, harmful/age-wrong JSON rejected, fallback and no secret/child data leakage |
| T-055 | StoryPlaybackDirector, scene compiler, captions/TTS/ASR/pose/POI | P0 | time-synced movement, both children correctly targeted, interruption and replay stable |
| T-056 | Story library + Room/migrations, offline cache, random/no-repeat, revision | P0 | survives force-stop; edit undo/delete clean; no AI on replay |
| T-057 | Parent Story Manager: generate, inspect, edit, approve, delete, replay | P0 | DeX mouse/F10 usable, safe editing lifecycle and preview |
| T-058 | Hybrid illustration and optional image adapter/caching | P1 | missing art fallback; budgets enforced; no child identity drift or per-frame network |
| T-059 | Windows API35 FullHD 30/60min QA and separately real Fold3 gate | P0 | logs/screenshots/quality & performance claims evidence-backed; regressions documented |

## 11. Definition of Done
All six tasks have executable evidence and acceptance gates independently reported; no fabricated simulator/physical-device results, no new CI. Publish on the appropriate branch only after local review. Physical Fold3 requirement cannot be marked PASS from Windows emulator results. Keep prior T-048–T-053 task histories unchanged.
