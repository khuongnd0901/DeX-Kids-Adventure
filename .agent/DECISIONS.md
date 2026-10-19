## T-071 — owner chooses personal Premium official-app handoff (2026-10-18)

Owner confirms personal APK/ADB/DeX use only, no store/distribution; Premium
subscription. Chooses official YouTube over embedded 75/25 player, accepts layout
and automatic-turn adjustments to platform capabilities. No Cloud/Data API setup
needed for canonical URL intents. No account/cookie/token access, search or scrape.
YouTube owns Premium, native recommendations/ads and playback; app stores reviewed
local links only. Official package + explicit current external display, no fallback.
Owner subsequently explicitly allows LIVE/GPX handoff with this app's journey,
GPS and POI paused, then resumed on parent confirmation. Preserve manual GPX
pause and leave system GPS/Maps/VietMap unchanged. API HDMI6 did not reach the
physical monitor; actual DeX Desktop2 verified separately. Inherit current valid
Activity display, never select by display name. Pause local audio before launch, durable hold until parent confirms
YouTube stopped on return; do not invent video-end/stop callbacks.
User provides uWXvGSMrwXc for testing only, not permanent child-library approval.
Original embedded M0 NO-GO retained for that deferred design. Details:
`docs/T071_OFFICIAL_YOUTUBE.md`, revised active T-071 YouTube ExecPlan.

## T-071 YouTube — M0 blocks playback implementation (2026-10-18)

Follow approved issue #65 sequencing: NO-GO until child-directed notification,
applicable privacy handling and native recommendation/link behavior are resolved.
Do not infer an Android sideload registration route from website/Play/iOS tools.
Do not conflate madeForKids with owner-only selfDeclaredMadeForKids or promise
that rel=0 removes recommendations. No WebView/network/player implementation
before GO. Parent-approved app selections and YouTube-native surfaces differ;
resolve the existing reviewed-only requirement explicitly. Matrix:
`docs/T071_YOUTUBE_COMPLIANCE_MATRIX.md`; M1–M4 remain NOT IMPLEMENTED.

## T-071 — whole painted figures replace rejected cutout anatomy (2026-10-18)

The parent rejected the assembled story rig twice, including after technical
Fold3 matrix PASS. A passing renderer/performance test is not aesthetic approval.
Story figures must each be one complete painted face/body/clothes/hands/feet
sprite, using canonical explorer appearances from the road identity references.
No runtime head/limb/garment assembly, skeletal deformations or recoloring.
Only rigid whole-figure transforms/frame selection and separate semantic props.
Story outfit rotation is suspended rather than mixing unmatched costume parts;
existing road costume rotation and original source artwork remain untouched.
Human review remains open until explicit acceptance. Bound active character
pages independently of the larger on-disk complete-pose library.

## T-071 — user-approved YouTube media UX (2026-10-18)

Selections **1A, 2B, 3A, 4C, 5A** are binding on Codex:
* Manual parent-supplied videos and playlists only; each video approved before inclusion. No AI external video discovery or unapproved recommendations.
* **70–80% width** YouTube iframe visible beside **20–30% non-overlapping Capybara animated panel** on SAME DeX display. Never cover player with graphics, HUD, mouse shield, buttons or captions. Split layout mandatory; performance adaptation must not silently change it.
* Auto-start at approved scheduling turns subject to current YouTube rules, MFK/privacy checks and OS autoplay; blocked => safe offline fallback, not fake click.
* Parent-configurable story:video scheduling ratio; default successive 5-question boundaries alternate story and video (video only when eligible). Existing T-068 question and story clocks, verified POI priority and unlimited-length session stay.
* YouTube narration audio stays **inside visible player**; no extraction, audio-only YouTube mode, hidden/background player or new licensed-audio architecture in T-071.
Child-directed/privacy/Google notification, embedded player visibility/ads/controls, official metadata eligibility and real Fold3/DeX autoplay/resource tests are blocking acceptance gates. See `.agent/plans/active/T-071-youtube-media-entertainment.md`. This is a design decision, not implementation.

## T-066 — measured runtime resource budgets (2026-10-16)

Retain the original atlas layouts. Both NPOT crop and two-page POT repacking
trials lowered nominal texture bytes but increased actual Fold3 PSS, so reject
both experiments. Do not equate PNG size or RGBA byte estimates with process RAM.

Preserve protected child PNGs unchanged; generate separate Sâu runtime sheets
with320x480poses, larger than the285x427maximum FullHD slot. Resample once with
LANCZOS at build time; retain originals for fallback and require physical visual/
recreation acceptance. Bake exact alpha>=128 passenger crop rectangles to avoid
per-load JNI pixel scans. Keep Pixmap upload/disposal and legacy scan fallback.
Load tiny costume fallback textures only when expressive/complete art is absent.

List stories with two bulk cursors and fresh SQL review/delete/favourite/source
flags. Snapshot at most64immutable cached episode references for one scan, so
lists longer than64do not evict later hits before use. Persistent cache capacity
stays64; no second permanent model cache. Existing leases/voice/navigation and
native allocator policy are unchanged. Physical acceptance recorded in T-066.

## T-047 — process-owned native free-page budget (2026-10-13)

Measured Audio→Full reload leaves ~71MiB logically free in the native allocator
but physically resident. Use documented Bionic mallopt purges, no global decay
policy or render-loop GC. Android-only JNI utility, existing three ABIs; API
version guards; coalesced background worker, foreground pacing30s and32MiB
free threshold. Core/desktop callback defaults no-op. NDK27.2.12479018 required.
Measure current PSS, allocated/free and FPS; reserved virtual bytes are not PSS.
Source API: https://android.googlesource.com/platform/bionic/+/master/libc/include/malloc.h

## Decision — Main-only workflow (2026-10-12)

User explicitly requires pull/push synchronization and all work on `main`.
Use `main` directly for this repository; this supersedes the default feature
branch naming instruction. Preserve local work, integrate remote updates before
publication, and use ordinary pushes without rewriting remote history.

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

### T-048 — immutable PNG visual adapter (2026-10-14)

Preserve original SVG/source atlas and PNG bytes. Generate a separate, bounded
1024 RGBA page with verified SHA/alpha, consistent pose feet and Linear gutters.
Packaged art-mode.txt chooses png by default or legacy on rebuild. Composite bus
already includes driver/wheels, so bypass both legacy overlays; use existing bob/
pitch and change only passenger render geometry. A standing source Capybara shows
existing semantic question/answer/greeting phases through a 180ms paused fade;
controller/story/content/voice/AI/GPS untouched. Baked driver stays static; no
new driver/wheel layer invention. Windows emulator acceptance is separate from
physical Samsung DeX and human/release review. Evidence: T-048 SUMMARY.md.

## T-049 — traffic depth and bounded motion (2026-10-14)
Use nominal tire ground Y as painter depth in the existing Y-up 1920x1080
composition. Dispatch far traffic after panorama/extras, bus/passengers,
near traffic, then Story/HUD. Source wheel contact and per-kind canvas size
live in TrafficLayout; lane ground/scale and per-kind speeds in TrafficDepthModel.
Keep two reused slots and22s deterministic offscreen entries, instead of
attaching moving vehicles to visible chunk membership. Latch biome eligibility
at spawn; never delete a vehicle during a theme transition. No extra atlas,
sort, transparency workaround, GPS traffic inference or M10 VEH runtime integration.

## T-051 render and QA boundary (2026-10-14)

V2 bird art grips a branch, so its renderer uses shared grounded placement;
classic keeps its flight presentation. M10 alpha-cropped regions preserve
visible pixel aspect inside the existing prop envelopes, without new atlas
pages. New CST/MTN override artwork is catalog only until separately reviewed
production integration; Android QA gallery uses host-provisioned external
full catalog and never enlarges the production APK/runtime resident atlas.
Named POI identity defects require verified architecture references before
redraw. Raster review, emulator fixture captures and physical performance
acceptance are separate gates. Evidence T-051/SUMMARY.md.

## T-053 — Pose-aware child garments (2026-10-14)

Fixed whole-character overlays were rejected after actual API35 FullHD visual QA. Generate96 garment layers at build time from original pose-specific fabric/shading and authored palettes/motifs. Preserve protected source PNGs and explicitly exclude eyes/mouth. Story uses full-cell geometry; passenger overlay matches original alpha>=128 crop and base aspect. One2048×2048RGBA page/16MiB;128×192overlayframes; no runtime generation/network/load on rotation. Original clothing/accessories remain recognizable; no face-crossing headband/cape. Windows runtime evidence and optional-atlas fallback are required before delivery; physicalDeX performance remains a separate NOT VERIFIED gate.

## T-054–T-059 — Parent-owned offline story revisions (2026-10-15)

Extend the existing gateway, local audio path and LibGDX engine. A finite schema
compiles to existing poses/props; narration advances on genuine utterance tokens.
Whole-sprite semantic gestures move the original character and matching garment
together; this does not claim articulated limb animation. Story backdrops remain
fiction and never replace GPS observations. POI narration retains priority.

Use Android SQLiteOpenHelper for the new private, transactional story library,
consistent with the existing Android persistence stack and without adding Room
code generation. There was no previous story database to migrate. Schema1 stores
immutable revisions, approval of an exact revision and resumable cursors. Edits
invalidate approval; replay and resume revalidate against the current approved
revision on a serialized worker. Unsupported future versions fail closed.

Automatic replay resolves approved local content before starting any creative
lesson worker. While a story owns playback, the existing automatic lesson
coordinator is paused; returning to lessons initially uses cache only. Replay has
no authoring dependency and never requires new text/image generation.

Hybrid illustrations default to protected local sprites. The optional authoring
adapter is unsupported until a provider and renderer are separately verified.
Explicit cost consent, parent approval, bounded PNG/hash storage and cache-only
reads are implemented, but no real image API or image-texture integration is
claimed. Do not infer image API entitlement from text-provider configuration.

Windows AVD GPS must be verified through actual Android fixes and accepted trip
distance, not merely an OK response from the emulator console. Preserve failed
runs and original AVD settings. Physical DeX remains NOT VERIFIED.


## T-062 — parent-authored remote story packs — 2026-10-16

A configured public HTTPS URL is an editorial source owned by the parent. Valid
remote episodes are immediately playable, with explicit remote provenance rather
than falsely recording AI review. No per-episode approval or AI calls occur.
Core schema remains StoryEpisode v1, wrapped in pack schema v1. Additive database
v2 stores bounded remote snapshots separately from local revisions/approvals.
Source URL + publisher ID defines identity; normalized content defines updates.
A process-wide playback lease defers snapshot activation until controllers stop
or finish. Loading acquires a lease before querying, and all lease/SQLite work is
on workers. Foreground download uses a separate executor and five-minute throttle.
At most three source snapshots are retained; only idle activation/cleanup evicts.
Remote text is read-only; a local copy starts as an unreviewed draft. See
`docs/story-source/README.md` for authoring and operational behavior.

## T-063 — finite story capabilities v2 (2026-10-16)

Keep episode/pack schemaVersion=1 and existing enum names. Extend bundled finite
actions/props through one APK update; future authored HTTPS JSON needs no build.
Use StoryActionCatalog as the action/prop and AI-prompt contract. New attached
actions require their target prop; supporting tools are renderer-owned. Preserve
original character artwork and use bounded transforms/props without claiming new
limb frames. Carry actor metadata through the atomic HUD bridge for guide
actions. Keep the legacy public pack unchanged and provide a separate expanded
pack so older APKs retain a compatible feed.

## T-064 — authored sample library is the canonical feed (2026-10-16)

The user requested many samples in the existing file. Expand stories.example.json
to30stories while preserving its original3objects. New content requires the T-063
APK; publish on the feature branch with an explicit raw URL before integration.
Keep minimal fixture regeneration under build/story-samples instead of overwriting
the authored library. No runtime, external asset or schema changes in this slice.

## T-067 — uninterrupted fairy tales replace the sample feed (2026-10-16)

The user's explicit replacement request supersedes T-064's preserve-originals
rule for stories.example.json. The canonical main feed now contains three topics
(Rùa và Thỏ, Rùa con tìm nhà, Sói và bảy dê con), each SAU/ONG/BOTH. Keep the
separate expanded showcase as a renderer fixture. Narration ends before the
first question; three post-story questions have six-second silent deadlines.
Use six narration scenes plus two short recall scenes within schema v1; no new
runtime/voice APIs or art enums. Target 4–7 minutes by a documented Vietnamese
syllable-count estimate, not an invented device audio measurement. Update the
authoring prompt accordingly. Latest user steering limits delivery to verified
source commit/push; user handles app sync, so do not change device/local stories.


## T-068 — question timing and independent parent learning source (2026-10-17)

Latest parent instruction fixes response wait3–5s and transition2–5s with short
attention cues inside the gap. Measure response from prompt completion and gap
from answer completion; callback/watchdog progression must be exactly once.
Use shuffled eligible cards with shared session history, keep configured ages and
Sâu/Ong/shared focus. Narration interludes follow five completed questions, prefer
permitted matching-audience library content, then original bundled fallback;
never auto-approve a library draft. Existing legacy autoplay must not turn this
into a continuous-story loop. Host clamps story listening3–5s without rewriting
approved/source content or changing the recognition provider.

Reuse strict existing UTF-8 TSV learning schema4–6 for independent parent
file/HTTPS source. Validate entire bounded pack before AtomicFile replacement;
retain last-good through errors/offline, discard stale download generations.
Updates apply at new journey boundary to preserve immutable active snapshots.
Imported content takes precedence over AI caches; no trip-pack generation for an
external-source session. HTTPS uses only a public GET, no child/GPS/audio data.
No change to story JSON schema, navigation, Device Owner, Accessibility or DeX
screen selection. Runtime/hardware gates remain NOT VERIFIED without equipment.


### T-068 physical feedback — final English order and one caption (2026-10-17)

Parent observed the English sample in the wrong phase. Final learning order is
Vietnamese invitation → native offline English sample →3–5s imitation window →
Vietnamese answer/meaning. Start that window only after native English completion;
never queue the English model in the answer phase. Normal question turns continue
unchanged. Keep existing narrator/provider and recognition implementation.

Use AdventureDashboard.promptLabel as the single spoken-content caption in full
and audio-only modes. Remove the bottom entertainment TextView and subtitle mirror
in the GPS/status panel; that panel retains provenance/status/attribution. Replace
the English-word progress mirror with a generic listening instruction. Sequential
question/answer layout fixtures target the same canonical card.

Physical test exposed5.634s story listening from the1s polling tick. Schedule the
actual host-bounded deadline directly; generation/listen-ticket/state guards must
reject old deadlines and late recognition from a previous question. Existing tick
remains a fallback. Restore all temporary QA parent/AI/source options; preserve
configured ages/library and explicit external DeX display. No physical-to-phone
fallback, navigation, Accessibility, Device Owner or provider change.


## T-069 — 400 age5–7 cards and main source on DeX (2026-10-17)

Ship one original UTF-8 TSV with200QUIZ/200WORD, eight topics×25of each kind.
Per-card minimum age5/6/7, maximum7; preserve legacy4–6 cards and configured device
ages. Bounded decoder now accepts500cards/age4–7 under the same131072byte limit.
Terminal TAB on QUIZ rows is a required empty eighth field, so scope Git's
blank-at-eol exception to docs/learning-source/*.tsv; code whitespace checks stay.

External parent file/HTTPS source does not instantiate AI trip-pack updater at
startup. User explicitly requests persistent enabled/automatic/creative AI OFF
on DeX; preserve provider keys, child-sharing consent, story source and ages.
Use public main Raw TSV URL after user-authorized merge of all T-068/T-069 work;
verify actual Android HTTPS bytes/hash400cards and new-source playback before
accepting deployment. Only public file GETs remain for content sync; no AI API
content refresh on app open. No other repository or navigation/provider changes.

## T-070 — finite world capabilities v3 and episode schema v2 (2026-10-17)

Keep pack envelope schema1; accept episode1 and2. V1 canonical serialization and
validation vocabulary stay fixed to preserve reviewed JSON/fingerprints/cursors.
Only v2 accepts new enums and optional five-slot supportingProps. One primary
object plus up to five supports; COUNT stays verbal with no visible objects.
Scene compiler and atomic HUD snapshot carry actor/emotion/support list/world
mode together. Neutral recall keeps background but clears objects and gestures.

New story-only cutout renderer owns four1024²RGBA pages and at most two1280x720
backgrounds (24150016bytes conservative backing bound, not measured PSS).
Prepare/upload/dispose outside SpriteBatch.begin; dispose world pages on return
to normal journey or Full/Audio teardown. Restore last journey cue explicitly on
finish/cancellation. Paused timeline drives limb joints and backdrop transition.
Original PNG/SVGs stay byte-exact; new painted limbs carry complete costumes,
including Ong's clothing. Ten outfits per child: six current plus four classic.
Face extraction uses source-hash-guarded silhouette bounds and alpha component
isolation to avoid generated-sheet neighbour fragments; body extraction remains
reviewed bounds. Sources/prompts/provenance are retained; AI origin is not license
or human art acceptance. No source publication, device install or new network
path. Authoring and saved text edits preserve v2 scene metadata; no SQLite schema
migration. New source stories need the T-070 APK, existing feed remains unchanged.

### T-070 physical art correction: shared anatomy and object units

The initial device matrix established technical rendering only. User rejected
head/body proportions, disconnected limbs and arbitrary object boxes. Treat that
as a failed art gate. World rigs use shared body-local hips/shoulders/neck, real
arm lengths and two-bone hand reach; props consume the same grip positions.
Explicit visible-silhouette dimension profiles replace equal-size icons; trim
transparent SVG margins in the atlas builder. Floor/depth lanes and chair size
use the same stage units. Corrected raster sheets remove exposed socket art;
reviewed extraction removes duplicated sleeves/backpack/skirt fragments. Original
journey art, voice path and navigation stay unchanged. Human art acceptance is
separate from compile/tests, GL gallery and physical cadence/resource probes.

The correction supersedes the initial texture dimensions: one2048x1024object page
and three1024²rig pages, at most two1280x720backgrounds:28344320bytes (<32MiB).
Foreground slots move away from the single-child hero lane; blankets project
onto the floor. High-resolution large SVG objects are packed after alpha trim.

## T-071 — optional official YouTube managed pause (2026-10-19)

Owner requests autonomous video start/stop/return while limiting YouTube discovery.
Use optional, default-OFF NotificationListener access with explicit in-app parent
disclosure/system grant. Read no notification payloads; target only official
YouTube MediaController sessions, never global media keys or other apps. An
entered-duration deadline is not real end detection. Confirm nonempty exact
session tokens PAUSED/STOPPED before automatic local audio/GPS/GPX release.
Missing permission prevents managed launches; unknown session/timeout keeps the
durable parent hold. Best-effort foreground only the existing KidsActivity task
on its saved valid external display, never phone/finished journey. No original
Accessibility/Device Owner/voice/navigation change. Official branding and related
content remain visible; a fully concealed/curated experience is not achievable
through official-app handoff. Physical DeX automatic pause/return requires actual
verification, not intent or unit-test assertions.

## T-071 — embedded YouTube setup-first (2026-10-19)

Owner explicitly selects Data API v3 + official IFrame and accepts logo/UI. Owner
has no project/key or Google child-directed designation and asks for setup UI
first. Implement dormant source now; require encrypted local key, actual owner
notification reference/contact and explicit separate privacy/terms consent before
network. These fields are owner attestations, not Google/legal verification.
Keep live M0/physical gates open. Select embedded by default but default playback
OFF/unready; missing setup uses offline, never implicit external YouTube fallback.
Legacy official mode remains explicit, with its pending audio hold preserved.
Fresh bounded metadata must confirm reviewed MFK/embedding/availability/region/
duration before one visible 75/25 player + native Capybara sidebar is created.
Keep branding/ads/native links; exact-origin port + random nonce, no JS interface
or API key in HTML. First terminal callback disposes/restores once; background
teardown retains embedded journal until foreground. Audio-only skips. External
native links dispose embed and retain external hold, not automatic local resume.
No app/account provisioning, child-data upload, Accessibility/DO/navigation change
or device claims. Current design docs/T071_EMBEDDED_YOUTUBE.md.

T-071 latest owner clarification: Cloud project already exists; key retrieval is
not yet complete. Setup-first source scope stays the same. Do not provision a
duplicate project or treat the project alone as child-directed notification.

## T-071 — env key and Windows simulator test (2026-10-19)

Owner explicitly authorizes ignored .env YOUTUBE_KEY for injection and asks to
use Windows simulator. Use opt-in generated debug-only key asset; regular/release
builds do not package it, and runtime requires debuggable flag. The private test
APK contains the key and is not a publication artifact. Import into Keystore
without inventing notification/consent or enablement. Delete retains nonsecret
seed marker to prevent reimport. Probe only owner-authorized video metadata as
adult developer; Windows test separates real key/API/Keystore/UI from a network-
blocked mocked player. No Google registration or actual child playback claimed.
Real WebView revealed Android JSONObject.getLong rounds numeric strings >2^53;
match generation as a decimal string instead. Keep failed initial evidence and
verify corrected real simulator run. No DO/Accessibility/navigation changes.

## 2026-10-19 — T-071 personal APK readiness revision
Owner explicitly declines child-directed designation, confirms family-only APK
and manually reviewed videos, no store publication. Remove local required
notification reference/Google-completion checkbox; retain explicit privacy
consent, contact, region, encrypted key, manual catalog and fresh MFK/embedding
checks. Preserve any historical reference but do not invent one. Local readiness
is not compliance certification; YouTube III.J notification remains unresolved,
with no claim that personal use creates a policy exemption. Test real IFrame on
Windows emulator under explicit adult authorization; no Google form submission.

## 2026-10-19 — T-071 parent preview before approval
Immediate parent review is a separate nonexported Activity, not a scheduled turn.
It accepts pending/disabled stored rows through an explicit metadata review entry
point that shares technical eligibility checks, without synthesizing approved
flags. Scheduled validation keeps strict approval/enable requirements. Parent
privacy/key setup and same external display remain mandatory; do not overlap a
child journey or unresolved external playback. Preview does not consume child
budgets or change library/automatic/review state. End/cancel/background closes;
explicit external native links retain the existing durable manual-return hold.
