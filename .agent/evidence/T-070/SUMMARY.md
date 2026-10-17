# T-070 expanded story world and anatomy correction

Owner Main; dependencies T-063/T-066. User authorizes publication to main and
build/install on Z Fold3. Overall art/field acceptance remains IN_PROGRESS.

## Delivered

60 additional original SVG objects (86 illustrated props plus NONE),14emotions,
27actions,8fictional backdrops,10body costumes per child and a Capybara rig.
Episode schema2 carries immutable actor/emotion/up-to-five support props through
compiler/HUD/renderer; pack wrapper stays1. Schema1 canonical serialization stays
byte-identical for3fixtures+9published episodes. Editor/regeneration preserve v2.
Story finish/cancel restores journey visual; pause freezes animation; Full/Audio
owns renderer disposal. Native narration only drives open/closed HAPPY/CALM heads;
this is not phoneme lip-sync. Original voice/navigation/display policy untouched.
Canonical nine-story source and400-card learning source unchanged.

## Art correction requested on real device

Initial Android183-cue matrix and v2SQLite/Activity lifecycle technically passed,
but user rejected oversized heads, disconnected limbs and arbitrary prop scale.
Those results do not constitute artwork acceptance. Replaced5raster rig sheets
using built-in image_gen; rejected an opaque/missing-row output. Selected outputs,
exact prompts and reviewed part slicing: art/assets-source/story-world/
anatomy-corrections.json. Existing original328source assets remain byte-exact.

Shared body-local hips/shoulders/neck now follow one parent transform. Heads use
32%standing height with preserved aspect. Dress width and shoulder offset are
separate. Upper-arm/elbow overlap hides cut seams; atlas extraction removes dark
cuffs, doubled sleeves, backpack/skirt fragments. Two-bone reach and handheld
objects share grip anchors. Explicit dimensions cover all86props, including legacy
objects. Furniture is floor anchored; seated chairs scale to the acting character.
Drawing supplies a desk/flat notebook/pencil; watering keeps the plant grounded;
inspection separates leaf and magnifier hands. Large scenery has depth placement.
SVG transparent margins are trimmed; furniture has higher raster resolution.
Art conventions: docs/story-source/ART_DIRECTION.md.

## Verification

260core tests,0errors/failures;39Python contracts; Android unit task NO-SOURCE.
Corrected atlas deterministic,257regions,328protected source hashes unchanged.
Three1024²rig pages +one2048x1024object page +two1280x720backdrops bound new
texture backing to28344320bytes (<32MiB); this is not total GPU memory or PSS.
Local final logs: corrected-local-contracts-bash.log, release-review-final-build.log.
Initial desktop356captures and105frame/10FullAudio smoke are retained; corrected
gallery/smoke are rerun and recorded separately. Fixed phases .25/.75 avoid prior
aliased motion comparisons. Prompt4468chars fits existing6000character gateway.
No live AI author/review API was used for content validation.

Device textual receipt will record final corrected APK hash, Fold3 external DeX
matrix, short rolling cadence/PSS samples and v1/v2 lifecycle. Device PNGs and
raw preference backups remain local and are excluded from publication.

## Retained failures

Legacy unsupported-schema test expected v2 rejection; QA constructor mismatch;
relative pack path; stale duplicate-subtitle contract; clipped/adjacent head art;
initial forearm layering/HUD slots/aliased phase samples; disposable gallery disk
full. During correction: one source sheet had opaque background/missing row,
initial support-layout caption test failed and was fixed, one Gradle invocation
misplaced --args, one omitted ANDROID_HOME, and test-local.sh required bash.
Failure logs retained locally. Earlier successful build is never substituted for
final-source verification.

## Open gates

User artwork acceptance, human acoustic/child ASR checks, actual SAF picker,
long-duration thermal/driving/endurance and historical release gates remain open.
Technical rendering/resource probes are bounded evidence, not broad release
acceptance. New raster prompts contain fictional character art only; no real
children's photos, audio or GPS were sent. No external asset license is invented.

Final corrected GL gallery:356captures,54moving actor/action pairs differ inside
actor crops,5renderer rebuilds; integrated105frame/3audience/10FullAudio smoke
passes pause-pixel and journey-restoration assertions. Final11review sheets:
build/story-world-release-sheets-final/. These supersede earlier draft galleries.
Final corrected device install attempt failed: ADB device disconnected and old
wireless port refused connection. Initial physical results are recorded separately;
corrected final physical retest remains NOT VERIFIED pending reconnection.
