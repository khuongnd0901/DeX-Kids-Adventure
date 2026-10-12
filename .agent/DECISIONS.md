## ADR — Parent-controlled indefinite viewing (2026-10-12)

**Decision:** No watching-time cutoff and no automatic closing greeting.
The child Activity lifetime, not a session timer, controls the journey.
On-screen Parent menu/F10/Back keeps explicit Continue/End, and dashboard
provides Dừng hành trình. Purge `session_minutes` legacy preference and
do not restore a saved deadline after Activity recreation. No changes to
AI request RPM, permissions, single-screen DeX or source-backed GPS/POI.
Offline entertainment rotates normally with two former farewell beats
replaced by ongoing discovery. Hardware QA uses fixed-duration observations
(30/60 min) but never expects app auto-exit. **Tests pending.**


## ADR — M10 reuses existing art and animation ownership (2026-10-12)

Decision: M10 is an additive asset/variation layer, **not** a new
`AnimationTimeline`, `AnimationRegistry`, `DynamicSceneComposer`, POI
panorama engine, procedural world or GPS test harness.
Keep T-036..T-042 task IDs, but split execution: T-039.A
manifest/packer first, T-040.A pure anti-repetition selection using existing
art, T-041.A renderer adapter, T-042.A QA baseline; then staged source assets
and T-041.B/T-042.B acceptance. Pack **108 high-priority SVG targets**
before 64 secondary targets. The existing Java seven-state `Biome` is
not identical to six M10 style folders; only map *after* provenance-aware
scene selection. Preserve T-033 lesson animations, T-035 source checks and
T-034 HTTP quotas. Audit: `docs/M10_REUSE_DEPENDENCY_REVIEW.md`.
No M10 code/art implementation is asserted by this ADR.

## Decision — 2026-10-12: remove parent PIN (supersedes local gate ADR-009)

Parent controls are directly accessible on the **single DeX display** with mouse
and F10/Menu/Back. No optional PIN, hashed credential, failed-attempt lockout,
login dialog or screen lock. Clear obsolete PIN preference values on app
upgrade. Keep Android permission checks, session expiry, Audio-only, local AI
emergency actions and signature-protected IPC unchanged. Older ADR-009 below is
retained solely for historical traceability, not current architecture.

## ADR-011 — T-033 local cartoon animation and sourced offline GeoWorld (2026-10-12)

Decision: generated 4–7-year-old lessons yield safe pre-approved `VisualCue`; `LocalVisualPlanner` maps to immutable finite `VisualPlan` with deterministic templates, no extra cloud request. Semantic QUESTION/ANSWER/INTERRUPTED events are beat-bound and transferred atomically Android-to-GL; renderer owns timeline/atlas, and parent F10 pause freezes its delta. POI sourced panoramas outrank offline OSM landcover-derived, strictly illustrative biome art; if no verified polygon index, preserve the original fictional procedural world. Geographic source is local raster derived from real OSM geometry and never directly cloud-sent; 3 GPS fixes/accuracy<=25m and stale fallback are necessary but do not prove road matching. Initial GeoWorld pack is empty, pending lawful verified extracts and physical device/child acceptance. No new backend/CI, Android permission, TTS provider or online art. M5 cloud-authored storyboard remains OFF to avoid degrading T-034 quality/quotas. Do not mark source-written as DONE.


# Architectural Decision Records

## ADR-009 Portable GPX XML security (T-011/T-004)
Android API35 Harmony DOM rejects Xerces disallow-doctype-decl; real regression
failed for a valid bundled GPX. Decode bounded UTF8/UTF16 before parsing; reject
DTD/entity declarations on the exact character stream passed to DOM, and install
a rejecting external entity resolver. Do not ignore unsupported security flags.
Cap bytes at4MiB on all callers and retain20K-point/time/coordinate gates. Literal
declarations inside comments/CDATA are conservatively rejected. Other encodings
are unsupported. Actual Android and JVM safety/load regressions are recorded in
`.agent/evidence/T-011-gpx-android-parser-fix-2026-10-09.md`.


## ADR-001 Two launchers, shared pure game logic
Decision: libGDX :core Java17 shared by Kotlin Android launcher and Java desktop LWJGL3 launcher. Keep GPS/Audio/DeX in Android adapters, not libGDX core.
Reason: GPX testing on desktop, isolated Android dependencies.

## ADR-002 Offline-first and content provenance
Never claim a named POI from coordinates without verified source, accuracy and direction gating. Offline pack and curated facts only. AI may draft pack content but cannot automatically narrate unsupervised text to children.

## ADR-003 Deterministic world visualizer
Chunks are seeded from journey seed and signed chunk index. Visual depiction is stylized, not a geographic map; location/fact correctness is independently verified.

## ADR-004 No implicit Assistant coupling
Independent APK. Future Android IPC must use explicit component and enforce caller identity; signature permission when signing identity supports it.

## ADR-005 Delivery gates
Do not count code-written as build PASS, source-created as hardware validated or proposed thresholds as measurements.


## ADR-006 Simulator evidence and owned GL resources (T-011, 2026-10-09)
Keep emulator, desktop and physical Fold3 results separate. Android gfxinfo measures UI frames;
LibGDX telemetry reports raw GL render cadence with a 1ms P95 upper bound, not GPU execution time.
Do not convert a screenshot, virtual 60-minute unit test or missing final histogram interval into a
full-run performance PASS. KidsGame owns and disposes its screen once: resolved LibGDX Game.dispose
only hides the screen, confirmed by bytecode and a failing regression.

## ADR-007 Accepted-fix freshness and recreation safety (T-011)
Rejected GPS jumps/timestamps must not refresh accepted signal freshness. A core clock supplier
allows deterministic loss/recovery tests; production defaults to the system clock. Keep an absolute
elapsedRealtime child-session deadline across Activity recreation and enforce it on resume. Retain
only the pure journey feed through non-configuration state, without Activity/GPS adapter references
or disk GPS history. This is configuration continuity, not process-death persistence. No Assistant
integration, production voice changes or network TTS fallback is authorized by these fixes.


## ADR-008 Permission request and refusal verification (T-011)
Declare/request COARSE and FINE together for Android12+ compatibility; inspect results by
permission name and require FINE for live child tracking. Preserve refusal outcome through
parent resume; clear stale outcome on a new explicit action. Coarse-only never starts live GPS.
QA clicks only the permission controller denial node via resource/exact-refusal-text identity,
not dialog coordinates; asserts both grants denied and finally revokes any unexpected new grant
back to its original denied state. A prior QA grant incident is retained, never labeled PASS.

## ADR-009 Optional local parental gate and audio-only (T-008, 2026-10-12)
Same-display controls can use an optional 4–8 digit PIN. Store a random 16-byte
salt and 256-bit PBKDF2-HMAC-SHA256 digest (120,000 iterations), never the PIN;
derive on a worker thread. Gate dashboard entry and child parent menu, keep
absolute session timeout and fail closed on canceled/destroyed dialogs. Three
wrong attempts enforce a persisted 30-second wall-clock delay. This is an app
gate, not OS kiosk/device-owner security. Audio-only skips scene drawing while
foreground journey/caption selection remains active; no background service,
voice-consent override or online voice fallback.

## ADR-009 First offline utterance and Gemini shape (T-021–T-025, 2026-10-10)
Warm the existing asynchronous Vietnamese offline narrator at child journey
initialization when parent-approved voice is enabled, before the first timed
entertainment beat. Lazy construction during speak rejected the first line on
physical Fold3. No network voice fallback. Require zero silent fallback in the
three-audience device regression.
Quiz generation via Gemini uses an explicit JSON response schema as well as
local source-answer, cardinality and child-safety checks. MIME type and prompt
alone produced a top-level array during live device QA; reject malformed output
and preserve supplied answers rather than relaxing safety validation.

## ADR-010 Story companion variants and bounded openings (2026-10-11)
Use transparent four-pose illustrated sheets generated from existing cartoon
references, preserve legacy costume assets as fallback, and load/dispose the
two extra textures once per scene. Narrative IDs choose the pose and BOTH turn
focus. Visual props are authored fiction and must agree with authored counts.
Rotate 18 opening slots per audience using only a local content cursor; preserve
slot and beat cursor on recreation. The 66-beat pool is finite and remains
authored offline entertainment. Native Android diagnostic cards/captions are
compact; internal narration stays complete. Human art approval and public
release provenance remain open. Parent explicitly authorized this device's
Gemini/Free Tier and child-text transmission; production child-text consent stays OFF,
keys stay encrypted, and audio/GPS are excluded from the cloud payload.

### 2026-10-11 — Separate passenger avatars and activity companions (T-023/T-026)
Parent requests half-body Sâu/Ong in bus windows plus original full-body
companions outside the bus for question gestures. Keep separate texture
layers, original story-v2 art, and new passenger-v3 illustrated references.
Capybara is driver in the right-facing front curved glass. Translate bus
left to separate it from original activity positions; preserve bus aspect.

## T-029 — contextual AI questions (2026-10-11)
Use locally selected sourced POI facts, age4/5 and four coarse local day
periods for AI questions. Keep exact GPS/date/timezone/audio out of the
question payload. Cache by POI/age/period and invalidate by source; capture
the same period for asynchronous request/write. Remove age3 quiz rows and
obsolete age2/3 question cache entries; learning packs reject ages below4.
English vocabulary remains available at4+. Actual child text keeps its
independent explicit parent consent. See docs/T029_CONTEXTUAL_API_QA.md.

T-029 visual/audio steering: authored panoramas own their terrain; suppress
unrelated procedural lamps/companions, use explicit beach/lake water anchors,
exclude swimming ducks from dry parks. Activity flowers/rabbits/counting props
use larger fixed FullHD sizes. Parent explicitly selected ambient music ON
for both DEMO and real GPS, retaining manual toggle and speech/menu pause.

## T-031/T-032 — production-content and composed HUD QA (2026-10-11)

AI review approval is insufficient for acceptance: real Gemini passed romanized
Vietnamese and abstract age-4 explanations. Creative validation now rejects
mostly unaccented packs, selected abstract concepts at 4–5 and unlabelled fiction;
privacy/location filters also compare unaccented forms. At least two actual STORY
items, with spoken fiction labels, are required. These are conservative guardrails,
not factual or language proofs; adult age/translation QA stays mandatory.
Provider prompts/reviewer specify the same limits and reject ambiguous riddles.

HUD status/provenance and progress reserve the measured answer height rather
than a fixed margin. Android instrumentation checks global rectangle collisions
as well as native line clipping. A narrow TripPackGateway and injectable retry
delay allow real coordinator/cache tests with simulated faults and zero HTTP;
production defaults still use KidsAiGateway and normal sleep/5-RPM scheduling.

Creative provider output now contains prose/genre/topic/vocabulary/fiction only.
The application supplies a 20-slot age/focus schedule and binds the returned
15–20 prefix to parent-owned metadata. The same focusAt/ageForFocus functions
validate the pack; the independent review receives each authoritative target age.
This prevents model-authored routing metadata from determining who hears a card;
it does not bypass prose suitability or quality review. The earlier real 6/7
cycle exhausted six attempts with the composite age/focus/duplicate validator
error; precise historical cause was not distinguishable. Diagnostics now separate
those failures by item index without logging generated unsafe text.

Creative answer + English word + Vietnamese meaning must fit 240 characters
before persistence. Lesson.resolution() returns the complete validated line;
the coordinator no longer silently drops vocabulary to fit the HUD. A new
core regression checks this display/playback contract.

Real 8/10 generation exhausted six attempts because the independent reviewer
rejected every pack; no quality gate was weakened. Reviewer input now uses
named fields (including authoritative targetAge) and returns bounded enum issue
codes plus item indices. A rejected pack remains unsaved. Up to five previously
validated AI-authored rows and their issue codes are supplied to the next
attempt in that worker's gateway; no child input/GPS/audio, unvalidated output
or persistent rejection cache is included. Approved output must have zero
issues; rejected output must identify a concrete issue.

### T-031 observed content failures and final prompt corrections (2026-10-11)

Real Gemini review can accept inconsistent object clues or imprecise vocabulary.
Preserve failed samples; never treat reviewer approval as factual proof or parent
acceptance. Generation/review now explicitly require precise cat/kitten and
whale/fish vocabulary, literal object-part clues, distinctive riddles and sound
causal explanations. Final provider content is regenerated after these prompt
changes. The 30-minute runtime measures the same render/coordinator source before
these final prompt-only changes; record both APK hashes and this limit explicitly.

### T-031 short Vietnamese safety terms and actionable validation (2026-10-11)

Use whole-word boundaries and preserve accents for short violence terms to avoid
confusing bơm with bom or the benign phrase sung sướng with súng. Known benign
phrase normalization does not allow actual accented weapons or unaccented
weapon/privacy/location phrases. Core regression tests cover both acceptance
and rejection. Lesson validation emits constant gate codes; the gateway adds
only item index/code to bounded in-memory retry feedback, never rejected prose
or personal data. Reviewer feedback still includes only Java-validated AI rows.

### T-032 fixed-scene capture isolation (2026-10-11)

Test-only visual fixtures remove the native DEMO status timer and queued opening
/resolution callbacks before forcing a scene. Hold each marker five seconds and
accept Wi-Fi screenshots only if the marker is unchanged after capture. Actual
30-minute natural playback remains a separate test; fixed screenshots are not
proof of child attention, driving or navigation-audio behavior.

T-031 reviewer rejection now includes bounded English reasons alongside
indices/codes; only validated AI-authored lesson rows enter ephemeral retry
feedback. Review output budget is1600 tokens. Latest device acceptance pending.

Reviewer reasons may quote Vietnamese spelling with diacritics, within160
characters and without control characters/URLs. Requiring ASCII contradicted
the spelling-correction instruction; a rejected reason now has a safe error code.

T-031: after repeated measured false approvals, review at most4cards per
request and require every batch approved before returning a pack.16cards
need1authoring+4reviewcalls;20need1+5. Existing5RPM/12100ms gate, six attempts,
consent/revocation and last-good persistence stay authoritative. Reject review
indices outside the batch. Known incorrect word/meaning pairs get conservative
Java rejection; new words and unrelated meanings remain allowed, not a catalog.
Test-only audio result survives ADB loss; selected BOTH runs9beats. An external
Android volume-down event invalidated volume preservation, not voice playback.

T-031: Gemini 3.5 Flash-Lite creative requests use supported medium thinking
instead of the documented minimal default. Preserve the configured model,
shared limiter/consent and 7500-token authoring cap. Reviewer cap 4096 includes
thinking and JSON. After multiple measured 12s timeouts, explicitly consented
background creative reads allow 30s; other reads stay 12s and connect stays 5s.
Source: [Google thinking documentation](https://ai.google.dev/gemini-api/docs/generate-content/thinking).
Negative review still rejects. Normalize control whitespace and bound retry
reasons to 160 characters, with fixed fallback for unhelpfully short reasons.
Valid issue codes survive overly verbose feedback.
The parent confirmed personally changing volume during the nine-beat BOTH run;
retain its failed volume assertion and independent 10→10 smoke result.
