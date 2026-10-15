# T-060 complete Ong outfit visual review

Actual Windows emulator captures from `windows-captures/` were reviewed after the new APK build/install. `windows-outfits.log` reports instrumentation code 0, 72 matrix captures, actual Activity/GL recreation, six unique styles, retained atlas object, and pause/resume. These are scoped emulator checks; final integrated 30-minute acceptance remains separate.

Verified 74 original PNGs are **1920×1080**: 72 SAU/ONG/BOTH × six style indices × four poses, plus parent/recreation images. `windows-capture-dimensions.json` records the exact files. Derived contact sheets use deterministic rectangular crops from the actual BOTH captures; they are QA visualizations, not new character art:

- `windows-ong-companion-contact.png`: all six styles (rose, ocean, sky, forest, starlight, lavender) × WAVE/LOOK_UP/COUNT/PET.
- `windows-ong-passenger-contact.png`: matching six × four upper-body bus crops.

Review result: **no blocking visual defect observed in this matrix**. The six complete outfits are visibly distinct and coherent across poses. Hats, original-style facial features, hands, knees and boots remain visible in the full companions; no adjacent-sheet pose fragments or cut hats were observed. PET is a shorter crouching silhouette rather than an enlarged standing-size body. Dress, body and limbs form one character image; no underlying explorer clothing or separate dress layer appears. Sâu and the original bus/Capybara remain present in the same BOTH captures.

Bus review: each style/pose has a clear face and hat, matching outfit, and an intentional waist crop. The PET outstretched hand reaches the right window sill; it does not cut the face or introduce a second torso. Lower body/legs are intentionally absent from the passenger UV crop, while they remain intact in the standing companion. The LOOK_UP hand points toward the upper side of the window. The full-frame `both-0-pet.png` was also inspected to verify the crop in its bus context rather than solely from contact-sheet thumbnails.

Source/runtime contract: 24 whole-character frames occupy one existing 2048×2048 atlas page alongside the unchanged 48 Sâu regions; 24 passenger regions alias the upper body of those same pixels. Ong renders one sprite in companion and passenger paths. Complete mode avoids loading the old Ong story/passenger textures. Missing/partial optional complete packs use the original Ong artwork without clothing overlays. `packing-tests.log` and `extraction.json` record deterministic packing, protected source checks and independent-column sprite extraction; source art is not redrawn by the packer.

Limits: these static/frozen matrix screenshots establish appearance and pose coverage, not audible TTS synchronization, independently articulated limbs, long-run FPS/RAM or physical-device DeX acceptance. Whole-sprite gestures and final integrated playback are validated separately. **Physical DeX NOT VERIFIED.**
