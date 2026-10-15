# ExecPlan — T-054..T-059 Interactive AI Story Adventure
Status: IN_PROGRESS / implementation and verification · 2026-10-15
Repository: `khuongnd0901/DeX-Kids-Adventure` · base `main`
Design: [docs/AI_INTERACTIVE_STORY_ADVENTURE.md](../../../docs/AI_INTERACTIVE_STORY_ADVENTURE.md)

## Objective
Create a fully replayable offline educational story mode featuring Sâu (5), Ong (4), or BOTH, with AI-authored multi-scene narrative, age-matched question/answer turns, consistent animated avatars, parent-owned library CRUD and optional offline-cached image generation. Integrate with existing game rather than replacing it. Prioritize story function in the current sprite system over expensive AI image generation.

## Boundaries and invariants
- Do not remove/replace existing `CreativeLessonPack`, `EntertainmentDirector`, `KidsAiGateway`, audio focus, approved POI and narration flows. Preserve GPS/POI correctness, parent control F10, free-tier limits, optional child-cloud consent and local TTS/ASR.
- No new backend, CI, cloud image provider dependency or compulsory paid API. Never send child photos/audio/transcripts/GPS; no API keys in source/logs.
- Original Sâu/Ong art and T-048 Capybara/bus files protected by hash. Preserve current outfit rotation, character proportions and render layer occlusion.
- P0 should run without Internet from saved approved episodes; when nothing saved, fall back to current offline entertainment. Do not assume any new pose exists before verifying atlas and registry.
- All resource/network/JSON DB work off render thread; GL creation/disposal on render thread; audio/POI override story narration.

## Work breakdown (all initially TODO)
### T-054 (P0) — structured story authoring and quality gate
1. Audit real current `core/story` contracts, `KidsAiGateway` provider response schemas, review gates and dispatch limiter. Preserve existing behaviour.
2. Define versioned `StoryEpisode/Scene/Beat/Question/Actor`, strict parser + deterministic validator; avoid implausible 3D/freeform asset commands. Create fixtures for audience SAU/ONG/BOTH and teacher goal.
3. Implement background generation through existing provider selection with adult-controlled opt-in, bounded retries, one independent review per candidate according to token/rate budget. Persist a DRAFT only; never autoplay unreviewed or invalid content. Document uncertain factual review.
4. Test schema migration, safety/age/voice turn, no GPS/private child data/URLs, invalid JSON/oversized prompts, provider 429/timeout/retry and current creative pack unaffected.
Acceptance: draft story passes deterministic + independent review, parent approval still required; existing offline stays operational. Evidence: local core/Android unit tests and saved validated fixture.

### T-055 (P0) — scene compiler, story playback, Q/A and animation
1. Audit `AnimationTimeline`, `AnimationRegistry`, existing Sâu/Ong pose keys and costume overlays. Map beat actions onto allowlisted local keys with safe fallbacks.
2. Implement `StorySceneCompiler` and `StoryPlaybackDirector` with typed events, main-thread-safe renderer bridge and callback-based TTS clock. Do not synchronise with guessed word durations.
3. Integrate with `KidsActivity` so utterance/question/ASR/feedback never overlap; silent/no mic fallback. BOTH rotates audience fairly, focus learning age min(Sâu,Ong). Keep Capybara guided dialogue.
4. POI interrupts safely, resume at correct beat, no duplicate answer/playback effects, app recreation/F10 works, GPS never falsely inferred from fiction.
Acceptance: 4–8 scene fixture with 2–4 questions, correct poses and narration, offline, pause/POI/ASR timeout tests, visual screenshots on emulator.

### T-056 (P0) — persistent parent-owned story library
1. Introduce local versioned Room store (or justify existing SQLite) with episodes, revisions, scenes, cache manifest and playback cursor. Do not conflate this with replaceable `CreativePackStore`.
2. Transactional generate-draft -> review -> approve/publish. Atomic revision updates and schema migrations, survive force-stop and rollback after crash.
3. Implement list/filter/favourite/manual play, repeat-avoiding seeded random, expiry/invalid legacy compatibility, edit text with semantic cue invalidation, undo/version restore, safe delete and orphaned media cleanup.
4. Enforce hard disk limits, media integrity and offline availability. Never store child transcripts by default.
Acceptance: restart/reinstall distinction documented, data migration tested, 50+ stories selection without immediate repeats, replay causes zero generation HTTP calls.

### T-057 (P0) — Parent Story Manager UI
1. Add `Thư viện truyện` under existing parent settings/F10 rather than clutter home; mouse/airmouse-friendly on ONE DeX screen.
2. Flow: choose audience, goal, topic, approximate length -> generate -> progress/error -> draft preview -> edit each narration/question -> revalidate -> approve -> save -> play/replay.
3. Add story list search/filter, delete confirmation, favourite, stop automatic story injection, preview without child speech upload, optional regenerate one affected scene.
4. Editing meaningful verbs/props must recompute safe cue/fallback; never show a stale image with contradictory narration. Preserve legacy parent UI.
Acceptance: screenshot/e2e cases at 1920×1080 using mouse/F10/back, cancellation, offline reads, text editing and safe deletion.

### T-058 (P1 optional) — Hybrid illustrations
1. P0 default uses existing foreground/background art and pose/costume overlays only; keep child faces consistent and artwork hashes protected.
2. Define a pluggable adapter for adult-opt-in image authoring *only before story publication*, never on replay and never using children's source photos. Prefer character-free backgrounds with original Sâu/Ong sprites over AI-redrawn children.
3. Cache bounded vetted images with manifest hash, thumbnails, separate decode worker, proper LibGDX texture residency/disposal; deterministic artwork fallback on provider failure/no API/quota/parent rejection.
4. Document provider capabilities/actual pricing before implementation; no promise of free-tier image API. Explicit parent review for every newly generated image.
Acceptance: offline replay and image-miss fallback, no render-thread network or decode, no leaked files or mismatched child likeness, bounded memory measured.

### T-059 (P0 final verification) — QA and delivery
1. Run existing local Python contracts, Gradle JVM/Android tests, assembleDebug and asset builders, preserve current unchanged module tests.
2. On existing Windows Android15/API35 simulator 1920×1080, capture SAU/ONG/BOTH storytelling, costumes, timing/Q&A, parent CRUD, screenshot diffs, offline replay, GPS/POI interruption, force-stop/resume and 30/60m soak. Record FPS/P95 frame, texture count, PSS, GL errors, TTS/ASR interaction, HTTP starts.
3. Preserve baseline T-053 Windows 21.70FPS sampled and T-044 older 40.78FPS/60min **as different test environments/configurations**: compare under matching hardware/configuration only. Do not label target >=30FPS PASS unless observed.
4. Physical Fold3 DeX remains NOT VERIFIED until run on actual device. Keep CI disabled. Publish log paths, failing cases and rollback story flag.
Acceptance: explicit PASS/FAIL per matrix, evidence paths and outstanding device gates. No invented tests.

## Order, branch strategy and proof
Execute T-054 -> T-055 -> T-056 -> T-057 -> T-058 (optional) -> T-059 with unit tests after each milestone; cross-integrate small vertical slices early. Use short-lived feature branches, refresh main before merge. Commit code/tests/docs and evidence, maintain `.agent/TASKS.md` and `.agent/STATUS.md` with measured truth. Do not mark a task DONE merely from source changes. Never run CI or merge unfinished artwork to main. For each milestone report changed files, SHA, test command/output, screenshots/logs, data migrations and remaining risks.

## Startup for Codex
1. `git status --short; git fetch origin; git switch main; git pull --ff-only`.
2. Read `.agent/STATUS.md`, `.agent/TASKS.md`, this ExecPlan and `docs/AI_INTERACTIVE_STORY_ADVENTURE.md`; audit actual code before any change.
3. Start a dedicated implementation branch, checkpoint source and tests incrementally; follow this plan without expanding provider services or modifying other repos.
4. Implement T-054 first; continue subsequent tasks only after evidence-backed acceptance and preserve original gameplay fallback.
