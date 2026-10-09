# M2 animation/world/art

Core model checks actually passed in56-test suite: defaultIDLE, blink30/60FPS timing,
wave/rolling/sleep/resume, explicit model narration flag/open-close frames, surprise priority,
zero/slow delta, stationary wheel/bob, same-distance wheel angles and bounded pitch.
Synthetic GPX replay exercised >100chunks, all7 enum biomes, four-chunk districts,
bounded windows and deterministic reset. These are pure model tests, not GPU/hardware soak.
Exact test names are mapped in source-test-map.md and current core/src/test.

18 original SVG regions generated and actual PNG/TextureAtlas alpha validator PASS.
Real Android render images show yellow bus/Capybara/scenery at1280×720/1920×1080;
real scrolling screenshot series currently being captured under build/simulator-artifacts/
visual-series-20261009T015448Z-7917. Review of all biome seams/blink/sleep frames pending;
no narration-audio synchronization exists in Android runtime. Talking model PASS does not
prove spoken audio or permit artificial talking in a child session.

Actual bug: KidsGame inherited Game.dispose, which hides but does not dispose Screen.
JUnit failed disposal count0; ownership fix + full regression PASS; desktop actual dispose
log/clean software-GL shutdown PASS. Android actual recreation/post-fix runtime pending.

M2 remains IN_PROGRESS: production art/license approval, actual narration sync, full seam
review and physical Fold3/DeX stability not accepted.


## Actual Android follow-up
Post-fix Activity recreation PASS with visible before/after PNGs reviewed (m1-rendering.md).
HUD background now contains metrics in real Android1080p captures. Actual113PNG series spanning
3500seconds plus30-second screenrecord confirm real scenery scrolling and visible bus/Capybara;
contact sheet inspected across residential/city/tree/river/bridge scenes. Full seven-biome visual
classification, complete seam inspection, individual GPU allocation count and actual talking
synchronized to audio remain NOT_VERIFIED/NOT_IMPLEMENTED. No fake narration signal injected
into a real child session. Actual60-minute baseline PSS stays51.32–52.04MiB; source fixes were
installed afterward, so baseline metrics do not inherit post-fix acceptance.

Final source bad299a follow-up completed: actual permission/offline/recreation PASS `privacy-20261009T032118Z/`; final post-fix smoke capture126seconds and GL disposal log PASS. See final-test-summary.md for current gates and restoration.
