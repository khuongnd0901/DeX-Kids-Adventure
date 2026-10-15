# Integrated delivery — 2026-10-15

**Windows acceptance PASS** for T-054/T-055/T-056/T-057/T-059 and complete Ong
outfits T-060. T-058 default local artwork/cache/cost gates PASS; optional real
provider/texture integration is deferred, not enabled or claimed implemented.

Final playback/art run d29418d:1800373ms offline Android15/API35 FullHD,
SAU/ONG/BOTH complete,27story loads,890activeTTS samples. Actual Android GPS
provider with synthetic fixes accepted4681.06m through unchanged gates. Sourced
POI queue injection interrupted at241833ms and resumed at331525ms. This tests
orchestration, not real POI arrival/road accuracy. [Raw result](final-live-soak/instrumentation.log),
[summary](final-live-soak/summary.json), [events](live-final-device/events.json),
[performance](live-final-device/performance.json), [APK/source](final-live-soak/environment.json).
Final sampled40.63FPS/P95upper31ms/9textures; PSS66955–107566KiB, final71477KiB,
177samples. No gateway dispatch or AndroidRuntime error lines in the captured
[filtered runtime log](final-live-soak/runtime-dispatch-final.log); the separate
actual coordinator test also asserts zero gateway calls with a ready fake provider.

The later manager-only fix659973c changes queued cancellation, with playback/art
unchanged. [Cancellation](manager-cancellation.log), [full manager regression](manager-regression-final.log),
[zero-dispatch replay](replay-final.log), [build](manager-cancellation-build.log),
[final APK hash](final-apk.json). No30min claim is assigned to this different APK.
[Local suite](local-suite.log) and211core tests PASS; Android JVM tests NO-SOURCE.
[Transport failures](author-failure-tests.log) inject mapped429 and delayed
cancelled responses, never make paid requests. [Restoration](restoration.json)
records returning temporary emulator settings to their original values.

[Story acceptance map](../T-054-T-059-story/ACCEPTANCE.md) records real Gemini
2HTTP200 author/review, realSQLite55stories/100choices/crash rollback/restart,
parent editing/approval/undo and42private-topic zero-dispatch cases. The actual
Gemini output was saved as a reviewed **unapproved draft**. Test-only clones were
approved for QA; this is not a parent's editorial endorsement. The first failed
GPS-disabled30min run and build/test failures are retained.

Development artwork: six built-in imagegen requests used the existing fictional
Ong as reference, never a real child's photo. [Prompt provenance](../../../art/assets-source/characters/ong-complete/generation.json).
24complete frames, no Ong garment overlay, original33protected files unchanged.
All12audience/narrated-scene combinations were captured during the integrated run
in `observed-scenes-final/`; transition metadata marks pairs that cannot prove
same-utterance movement. Static matrix visual assessment follows.

Actual child ASR recognition, external navigator audio arbitration and real image
provider rendering remain NOT VERIFIED. **Physical DeX NOT VERIFIED.** No CI,
backend or other repository changes. Disable story autoplay (defaultOFF) and stop
in F10 to return to the existing entertainment flow.

# T-060 complete Ong outfit visual review

Actual Windows emulator captures from `windows-captures/` were reviewed after the new APK build/install. `windows-outfits.log` reports instrumentation code 0, 72 matrix captures, actual Activity/GL recreation, six unique styles, retained atlas object, and pause/resume. These are scoped emulator checks; final integrated 30-minute acceptance remains separate.

Verified 74 original PNGs are **1920×1080**: 72 SAU/ONG/BOTH × six style indices × four poses, plus parent/recreation images. `windows-capture-dimensions.json` records the exact files. Derived contact sheets use deterministic rectangular crops from the actual BOTH captures; they are QA visualizations, not new character art:

- `windows-ong-companion-contact.png`: all six styles (rose, ocean, sky, forest, starlight, lavender) × WAVE/LOOK_UP/COUNT/PET.
- `windows-ong-passenger-contact.png`: matching six × four upper-body bus crops.

Review result: **no blocking visual defect observed in this matrix**. The six complete outfits are visibly distinct and coherent across poses. Hats, original-style facial features, hands, knees and boots remain visible in the full companions; no adjacent-sheet pose fragments or cut hats were observed. PET is a shorter crouching silhouette rather than an enlarged standing-size body. Dress, body and limbs form one character image; no underlying explorer clothing or separate dress layer appears. Sâu and the original bus/Capybara remain present in the same BOTH captures.

Bus review: each style/pose has a clear face and hat, matching outfit, and an intentional waist crop. The PET outstretched hand reaches the right window sill; it does not cut the face or introduce a second torso. Lower body/legs are intentionally absent from the passenger UV crop, while they remain intact in the standing companion. The LOOK_UP hand points toward the upper side of the window. The full-frame `both-0-pet.png` was also inspected to verify the crop in its bus context rather than solely from contact-sheet thumbnails.

Source/runtime contract: 24 whole-character frames occupy one existing 2048×2048 atlas page alongside the unchanged 48 Sâu regions; 24 passenger regions alias the upper body of those same pixels. Ong renders one sprite in companion and passenger paths. Complete mode avoids loading the old Ong story/passenger textures. Missing/partial optional complete packs use the original Ong artwork without clothing overlays. `packing-tests.log` and `extraction.json` record deterministic packing, protected source checks and independent-column sprite extraction; source art is not redrawn by the packer.

Limits: these static/frozen matrix screenshots establish appearance and pose coverage, not audible TTS synchronization, independently articulated limbs, long-run FPS/RAM or physical-device DeX acceptance. Whole-sprite gestures and final integrated playback are validated separately. **Physical DeX NOT VERIFIED.**
