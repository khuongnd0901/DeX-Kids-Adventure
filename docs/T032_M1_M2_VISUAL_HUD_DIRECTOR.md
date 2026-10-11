# T-032 — M1 readable DeX HUD + M2 lesson-driven art (source implementation)

Status **IN_PROGRESS / local acceptance in progress (2026-10-11)**. Integrated source is being verified on `feat/T-031-T-032-completion`; see `.agent/evidence/T-031-T-032-completion-2026-10-11/` for current evidence.

## Product decisions
- One external Full HD DeX display remains the only display. No interactive child screen, no new Device Owner, network, GPS or microphone permissions, no backend, no CI.
- AI may invent unlimited educational prose (T-031) but **may not generate arbitrary art instructions**, asset paths, coordinates, unsafe actions or fake GPS-specific visuals.
- Sâu age 5, Ong age 4 defaults; parents can select 4–10 independently. Shared mode respects Sâu/Ong turn focus.
- Existing 66 authored entertainment beats, curated pack, narrated POI, OSM disclaimers, F10/parent pause and offline fallback remain.

## M1: legible native DeX controls
- Native `AdventureDashboard` receives *current* `ParentSettings.sauAge/ongAge`; picker also displays configured ages. Desktop ASCII HUD removes obsolete hard-coded "Sâu 4, Ong 3".
- Full HD question box widened to ~43% viewport (capped 510dp), max six wrapped lines, 15–20sp auto-size. Topic/mode labels enlarge; status bar remains compact. Offline Vietnamese TTS and native Unicode remain.
- Bottom entertainment answer banner widened and up to five multiline rows with 14–18sp auto-size instead of two-line ellipsis. No arbitrary 260-character clipping from the main conversation card. Status/provenance and progress panels reserve the measured answer height, including font-scaled wrapping. Instrumentation checks global rectangle collisions and text clipping; composed screenshots remain a separate acceptance gate.

## M2: controlled visual cues from each lesson
- Each `EntertainmentDirector.Beat` now contains immutable `VisualCue`. Existing 5-/6-argument constructors generate a locally resolved cue, preserving older producers/tests.
- `VisualCue.fromCreative` consumes already validated T-031 `topic`, `prompt`, `englishWord`, `genre`, NOT the opaque `creative-<hash>` ID. It normalizes Vietnamese accents and resolves a **fixed vocabulary** of `Theme`, `Action` and `Prop`.
- `KidsGame` sends immutable `AdventureHud.Prompt` containing the cue and focus from Android to the existing atomic reference. `AdventureScreen` consumes **one** snapshot per render frame, updates pose/focus/prop before rendering passengers. No new per-frame allocations or HTTP.
- `CartoonSprites` selects only preloaded sprite atlas regions: birds, butterflies, rabbits, cats, cloud, flower, tree, boat, car, kite. Matches both outside full-body Sâu/Ong reactions and cabin portrait poses; story/riddle resolutions trigger Capybara SURPRISE, quiz/English wave. Shared BOTH beat displays one common prop between two children.
- Unknown subject maps to safe `GENERAL/WAVE/NONE`. A prompt mentioning three apples does **not** justify drawing three different props as a factual count; exact counts, brand-new object art, complex science diagrams and semantic understanding of every possible AI question require a future explicitly source-reviewed illustration/content model. No AI model is allowed to choose a texture path.

## QA checklist and acceptance
```bash
git switch feat/m1-m2-visual-hud-director
./scripts/test-local.sh
./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug
```
1. Confirm all source/JUnit tests pass and no F10/GPS/TTS/privacy regressions.
2. Real 1920×1080 Fold3 DeX screenshots for SAU, ONG, BOTH with 4,5,7,10 configured ages. No hard-coded old age. Verify prompt (up to ~190 AI chars) and answer (up to 240) fully visible; banner must not hide characters.
3. Real Gemini/Groq T-031 creative packs: butterfly, rabbit, flower, cloud, vehicle, story and unknown-idea cases. Confirm visuals correspond only to recognized words; fallback is neutral; captions remain actual lesson not AI-provided art filenames.
4. Verify three focus transitions (SAU→ONG→BOTH), in-cabin avatars, outside activity props, Capybara speaking/surprise; no duplicated BOTH prop or stale focus after parent pause/POI interruption.
5. Run POI/DEMO/GPX samples, landscape background crossfades, static asset integrity, 10+ theme switches. Measure gl frame time/FPS/P95/texture memory before/after. Preserve 60FPS FullHD device target (historical prior build ~59.8FPS is **not evidence** of this branch).
6. Observe 30–60 min with two passengers and navigation audio; monitor thermal and density/resolution changes.

**Not in M1/M2 scope:** new generated image files, unlimited sprite variations, async POI texture decoding, physically accurate 3D mapping, new road geometry, GPU quality A/B, child attention experiments. Continue those as separate tasks.
