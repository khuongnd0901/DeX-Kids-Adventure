# T-070 — story world expansion

Owner Main. Dependencies T-063/T-066. Branch feat/T-070-story-world-expansion.
State IN_PROGRESS. User authorized implementation in this session.

## Accepted scope
60 new objects across ten daily-life/learning/nature groups, 10 additional emotions,
16 additional actions, eight fictional backdrops. New expressive limb poses for
Capybara/Sâu/Ong, supporting every current costume; up to six objects per beat.
Versioned source art, prompts/provenance, shared capability catalog, backward
compatible story loading, updated authoring documentation and separate showcase.
No canonical source publication, device installation or other repository changes.

## Implementation
Episode schema v2 supports optional supportingProps (five fixed slots); v1
canonical serialization remains unchanged. Carry actor/emotion/objects atomically
through compiler/HUD to renderer. Build reusable body-part rigs and expression
heads from identity-preserving new art, finite joint keyframes, local prop anchors
and eight story-only backdrops. Keep all original art byte-exact. Reproducible
atlas build, bounded live textures (new budget <=32 MiB), paused-clock animation,
resource disposal. Preserve existing navigation, voice, privacy and display rules.

## Acceptance gates
Core v1/v2 round trips and strict validation, legacy pack/digest preservation,
finite actor motion, action/prop semantics, supporting slot layout and reset.
Transparent nonempty art/complete catalog/atlas determinism/original SHA256 checks.
Android debug and test APK + desktop build. Visual galleries for all objects,
actors/emotions/actions/costumes and all backgrounds; actual render smoke if
runtime available. Actual DeX FPS/PSS/lifecycle and user art acceptance remain
NOT VERIFIED unless performed. Do not mark overall DONE with missing equipment.

## Execution log
- Grounding: main8708af0, clean tracked tree; pre-existing untracked evidence kept.
- ADB available but no attached device at planning checkpoint.

## Verified implementation checkpoint

Delivered all catalog/schema/source-art/rig/background/authoring/sample slices.
257core tests and39Python contracts PASS; Android app/test APK and desktop builds
PASS. Actual356GL captures,54non-aliased moving actor/action comparisons,5renderer
rebuilds and integrated105-frame/3audience/10FullAudio smoke with identical paused
pixels verified. 328protected sources unchanged; original3fixtures+9feed codec
digests unchanged. Full details, retained failures and artifacts in evidence
T-070/SUMMARY.md and verification.json. Keep plan active: physical DeX performance
and Android v2 lifecycle/audio/user art acceptance NOT VERIFIED. No publication
or installation performed.

## User-authorized publication and physical follow-up

User now authorizes commit/push to main, build/install on Z Fold3 and verification.
Device SM-F926B/API35 is connected; active external DeX display2 FullHD verified.
Extend scope to test-only Android v2 matrix/lifecycle/resource probes, preserve
parent/AI/source preferences and remove only QA-created story rows. Verify final
APK hash, same-display launch, then publish verified source/art/docs plus bounded
text evidence. Raw device captures and preference backups remain local.

## User art rejection and correction gate

User rejected the first physical stage: oversized heads, disconnected/mismatched
limbs and props with arbitrary scale. Publication is deferred until corrected.
Replace visible socket/doubled-sleeve rig art; introduce shared anatomical stage
geometry, body-local hierarchical joints, grip anchors shared by hands/props,
semantic object dimensions, supported chair seating and depth-consistent ground
placement. Review neutral and interaction poses in actual GL and on Fold3. Earlier
183-cue physical PASS describes technical rendering only, NOT art acceptance.

## Corrected-source verification and reconnect checkpoint

Five rig sheets replaced using built-in image_gen; discarded opaque/incomplete
Ong output.260core,39Python contracts, Android app/test APK,356GL captures,
54moving actor/action comparisons,5renderer rebuilds and105frame integrated smoke
with pause pixels,3audiences,10FullAudio cycles verified on final corrected source.
New backing bound28344320bytes. Single-child foreground avoids hero lane; blanket
projects onto floor. Final11contact sheets inspected; human art approval remains
open. Initial physical Fold3 DeX2 matrix183/v2SQLite+Activity lifecycle passed
technically but artwork was rejected. Before corrected APK installation ADB
connection disappeared; reconnect old port refused. User asked asynchronously for
current wireless endpoint. Final physical install/retest NOT VERIFIED. Publish
verified corrected source as authorized, keep plan active for hardware/human gates.
