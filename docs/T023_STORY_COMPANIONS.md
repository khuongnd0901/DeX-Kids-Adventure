# T-003 / T-023 — Story companions and opening variety

Owner Main, 2026-10-11. Parent reported fixed yellow-bus/wave openings, blurred
Sâu artwork, absent Ong and oversized captions. Current source integrates
origin/main 389d058's Ong and 66 authored offline beats with local verified
offline-TTS warmup and Gemini response-schema fixes.

New journeys rotate among 18 opening slots per audience. Only the next content
slot is stored locally in parent settings; no answers or child behavior are
recorded. Recreation saves the slot and sequence position without advancing
the next-session slot. BOTH still rotates Sâu → Ong → together; solo content
never addresses the other child. The finite authored pool eventually repeats;
this change does not make general entertainment AI-generated.

Built-in imagegen edited the existing illustrated Sâu and Ong references into
`art/assets-source/characters/sau-story-v2.png` and `ong-story-v2.png`.
Each transparent RGBA sheet is 1024×1536, four cells of 512×768, versus the
original Sâu 112×168 and Ong 96×144 cells. Original costume sources remain.
Gradle validates/copies story PNGs into generated `assets/characters/`.
Two extra textures are loaded once per scene and disposed with that scene.
These are illustrated avatars, not identity recognition or real GPS evidence.
Asset/resemblance approval and release license review remain human gates.

Prompt set used with the built-in tool:

- Sâu: identity-preserving edit of original Sâu costume sheet; crisp high-resolution
  2D children's game cartoon, preserve illustrated face/hair/age/skin tone; consistent
  blue explorer jacket/shorts/backpack; exact equal 2×2 cells, full body and feet,
  generous transparent margins. Poses in order: welcome wave, look/point upward,
  three-finger counting, playful crouch/point toward imaginary rabbit. Transparent
  background, no text, no floor or animals, no cross-cell overlap, 1024×1536.
- Ong: identity-preserving edit of original Ong sheet retrieved from origin/main;
  same crisp game-cartoon style, preserve toddler illustrated face/proportions;
  consistent tan explorer hat, blue outfit/yellow backpack/brown shoes; exact 2×2
  full-body cells. Poses: wave, look/point upward, one-finger counting, crouch/reach
  toward imaginary rabbit. Transparent background, no text/background/floor or
  animals, no cross-cell overlap, 1024×1536.

Narrative topic drives wave/look-up/count/pet pose and small toy props; BOTH
emphasizes the current speaker's child while keeping both visible. Native
diagnostic cards are 11–12sp, lower-right POI panel 11sp/280dp, subtitle banner
16sp/520dp/two lines. Desktop HUD is also compact. Long visible text may be
ellipsized; narration and internal full subtitle remain unchanged.

Verification and device measurements: see T-011 all-task re-verification
evidence. No release-ready or child-engagement claim from artwork generation.

Parent follow-up: an unintroduced teddy bear in common-sleep confused the
child. Replace that beat with an explicit pretend goodnight/quiet-voice game
with the already-known Capybara. Mouse/rain/farewell beats now introduce their
own imagined setup or refer to Capybara; do not presume a previously heard
story. Remove unobserved claims that Ong waved or both children found a bridge.
Content edits require a fresh APK; preceding device results retain their build
identity and are not mislabeled as verification of the corrected narration.

Physical BOTH capture showed an inactive child's 70% alpha looked ghost-like.
Use opaque 82% brightness instead; the current turn remains emphasized while
both avatars stay solid and readable.

Final source review corrected the toy prop to select rabbit/fox/panda/cat from
the current beat ID instead of always drawing a rabbit, and shows a flower
during flower beats. It reuses existing atlas regions with no new textures.

Latest upstream3787d19 relocates companions into VehicleLayout bus cabin.
Integration retains HQ regions, topic poses and solid turn emphasis, while
using the common bus bob and 1.4px breathing. Props move with cabin staging.
The earlier right-of-bus screenshot evidence describes the previous build.

## Passenger busts and separate story companions — 2026-10-11
Parent requests half-body bus avatars plus full-body companions in their
original activity positions. Both render layers are retained. New source
assets: `art/assets-source/characters/sau-passenger-v3.png`,
`art/assets-source/characters/ong-passenger-v3.png`. Original story-v2 sheets
remain the question/action companions. Bus translated left to x130 (950x417)
to avoid overlap; Capybara driver sits in the rightmost curved front glass.
SAU/ONG/BOTH filters both layers. Passenger sheets load once, trim alpha cell
padding once with Pixmap, preserve aspect and align waist at window sill;
all textures/Pixmap disposed. No per-frame decode/upload or extra child data.

Built-in imagegen, identity-preserve edits of existing illustrated story-v2
references, no original photos. Prompt set: 1024x1536 RGBA 2x2 equal cells,
half-body seated busts only, no legs/shoes/background/text, preserve faces/
hair/blue outfits; Sâu planet T-shirt, Ong yellow hat/binoculars. Reading
order wave, look-up/point, finger counting, gentle reaching/petting gesture.
Requested consistent waist baseline and transparent gutters; renderer aligns
actual opaque content because generated padding varied between poses.
Human likeness/art approval and license review remain open.
