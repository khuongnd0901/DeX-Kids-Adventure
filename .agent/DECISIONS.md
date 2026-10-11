
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
