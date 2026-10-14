# T-051 — executed Windows Android visual QA, 2026-10-14

Owner Main, with distinct Art/Render/QA scopes; dependencies T-048/T-049/T-050.
**IN_PROGRESS / verified slice delivered; full art acceptance OPEN.**
Branch `feat/T-051-art-visual-qa`; fetched base `1ab1f42dbb9fe017c9e2e8df2e6260452491c062`.
`git fetch origin`, tracked-work stash, checkout main, ff-only pull, feature
branch creation executed. Stash `pre-T051-tracked-work-20261014` preserves
unrelated tracked work; unrelated untracked historical evidence retained.
No main merge, CI, other repository, credential or permissions change.

## M0 environment and protected boundary

Existing Windows AVD `DeX_Stability_API35_20261005`, serial emulator-5554,
Android15/API35 x86_64, physical1920×1080, density160, EGL `emulation`.
Windows SDK `/mnt/d/Android/Sdk`, Windows adb.exe used through WSL; no Linux
ADB server created. Existing emulator reused; no AVD deletion/recreation.
Actual APK install, launch and PNG capture/pull succeeded.
[Commands/results](test-results/environment.json).

33 inherited protected source hashes, including Sâu/Ong sources and the five
original T-048 PNGs, match their allowlist exactly:
[hashes](test-results/protected-hashes.json). No story/GPS/AI/voice/dialogue,
traffic motion/gameplay, Device Owner, Accessibility or Android permissions
source edits. Test fixtures temporarily disable speech/cloud/mic preferences
and restore original snapshots; no real GPS or child audio is uploaded.

## M1 tests and implementation

[Executed local/build results](test-results/SUMMARY.md), with exact commands,
exit status and compact retained logs. Required scenery/animal/atlas/M10/local
checks PASS after fixes. **189 core tests/66 suites, zero failures/errors/skips**;
Android debug + AndroidTest APK builds PASS. Android JVM unit task NO-SOURCE.

Real initial failures retained: irrigation right-edge crop; subsequent ox-cart
crop; Android compile rejected Files.writeString; stale testAPK tried to load
catalog atlas excluded from production. Fixed source bounds, portable UTF-8
write and host-provisioned test-only catalog respectively. Assertions unchanged.

Nine original SVG corrections: irrigation/ox-cart/rabbit/banana/palm/rice edge
clearance, penguin head identity, buffalo curved horns, swimming duck shorter
neck/waterline/submerged feet. V2 perched bird now uses grounded382px placement
and110px width instead of old floating branch; classic preserves old flight.
M10 visible alpha-cropped regions retain source pixel aspect in their existing
max170×158 envelopes, centered on the original baseline. Shared ScenePropLayout
and a regression test cover aspect/envelope constraints. No new runtime atlas,
texture/network load, traffic order or motion model.

## M2/M4 raster audit and completion

**All83 baseline Storybook SVGs inspected as actual rasters**, grouped classic/V2
Before/After; nine source fixes additionally compare baseline V2 to final V2.
16 distinct original editable Coast/Mountain overrides preserve160×160 regions,
bringing the total to99. All99 pairs reviewed in21 sheets; source dimensions,
alpha bounds and SHA in [inventory](comparisons/inventory.json).
[Visual audit and remaining inventory](art-review.md).

Additional54 raster assets reviewed:16FX,8UI,8Cap gestures,22 generated POI PNGs,
with8 sheets in atlas-review. This is source/atlas review, not54 Android cases.
Named palace/museum PNGs are identical: identity acceptance remains OPEN.
Remaining clipped confetti/UI outlines, UI English sticker reading"MF", gesture
style mismatch and schematic named POIs are explicitly recorded, not PASS.

16 new Coast/Mountain sprites are **catalog art**, not production biome support.
The production APK excludes full M10 catalog pages; runtime props remain32
regions/one1024page (4MiB RGBA backing), T-048 one1024page. Catalog QA temporarily
loads4 existing pages from a host-provisioned external test folder and disposes
them before ending; the catalog is absent from measured normal DEMO.
[APK inspection](test-results/apk-artifacts.json).

## M3/M5 actual Android captures and visual review

All files under screenshots are actual Android1920×1080 captures. Framework
UiAutomation captures native display0; ADB pulls the originals. adb-initial.png
was independently captured with `adb shell screencap -p` and pulled. No desktop
or source contact sheet masquerades as emulator screenshot. See screenshots.json
for pixel sizes/SHA/source provenance. Frozen scene cases use production
SpriteBatch/atlases, offline BOTH-child DEMO and controlled scene choices.
Fixtures/capture PASS is separate from the visual observations below.

| Case | Actual evidence | Observation / limitation |
|---|---|---|
| TC01 | [bus + Capybara](screenshots/final/TC01-bus-capybara.png) | PNG bus aspect/guide intact, no duplicate wheels/driver |
| TC02 | [passengers](screenshots/final/TC02-both-passengers.png) | Both original children visible in cabin and story plane; same BOTH composition as TC01 |
| TC03 | [buffalo](screenshots/final/TC03-buffalo.png) | Curved horns, barrel body/four hoof silhouettes, behind bus, small scene scale; tiny detail limited at native180px |
| TC04 | [bird](screenshots/final/TC04-bird.png) | Sparrow beak/feathers/claws and supporting branch now on ground, smaller than buffalo; no floating airborne branch |
| TC05 | [duck](screenshots/final/TC05-duck.png) | Flat bill/short neck/wing, lower-body waterline and ripples, feet no longer stand on water |
| TC06 | [urban](screenshots/final/TC06-urban.png) | Buildings/lamps remain behind bus/cabin, no matte rectangle |
| TC07 | [village](screenshots/final/TC07-village.png) | House/well bases remain behind bus, original HUD clear |
| TC08 | [forest](screenshots/final/TC08-forest.png) | Existing PARK/forest prop selection, tree/rock silhouettes readable; not a new world biome |
| TC09 | [farm](screenshots/final/TC09-farm.png) | Rice bundle fits source frame and grounded prop envelope |
| TC10 | [coast backdrop](screenshots/final/TC10-coast-backdrop.png), [new catalog](screenshots/final/TC10-m10-coast-catalog.png) | Existing named beach backdrop; new8CST shown separately in labelled Android catalog, no production CST integration claim |
| TC11 | [mountain backdrop](screenshots/final/TC11-mountain-backdrop.png), [new catalog](screenshots/final/TC11-m10-mountain-catalog.png) | Existing named mountain backdrop; new8MTN catalog separate, not a new production biome |
| TC12 | [two lanes](screenshots/traffic/two-lanes.png), [far](screenshots/traffic/sedan-far.png), [near truck](screenshots/traffic/truck-near.png) | Additional8 traffic cases reviewed; far silhouettes behind bus, near car/truck cover lower tire/body while cabin/children/HUD remain clear |
| TC13 | [POI](screenshots/final/TC13-poi-backdrop.png) | Panorama layering retained; named landmark identity NOT ACCEPTED (palace/museum duplicated source) |
| TC14 | [story + bird](screenshots/final/TC14-animals-hud.png) | Existing authored yellow-bus question, native Vietnamese text readable, bird and companions do not crop HUD |
| TC15 | [classic/png](screenshots/TC15-classic-png.png) | Actual separately built/installed classic APK, legacy source scenery restored, original PNG bus/children retained |
| TC16 | [legacy](screenshots/TC16-storybook-legacy.png), [PNG](screenshots/final/TC02-both-passengers.png) | Actual independently packaged art-mode legacy/PNG, both render paths captured |

Final Art fixture16 captures, classic16 and legacy16 captures successful;
only selected rollback originals retained. T-049 traffic8case fixture re-executed
on restored final V2/PNG APK. Original13 frozen scenes have expected scenery
layering, no observed crop/white matte at reviewed frames; this is not continuous
flicker or complete animation acceptance. Source rasters cover99regions, Android
journey captures are representative, not proof of99individual scene appearances.

## M6 measured continuous DEMO

[Run result](test-results/android-soak.txt), [20samples](test-results/soak-samples.json),
[performance JSON](test-results/performance.json), [raw metric lines](test-results/soak-render-metrics.txt).
Normal offline DEMO ran **301.005 continuous seconds**, then separate1s pause/
1s resume feed checks PASS. Frames advanced in every15s sample. Start/end actual
screenshots retained in screenshots/soak. The rendered journey did not require
physical GPS. No voice/AI/audio/physical-road acceptance claim.

At300.158s cumulative renderer lifetime: **33.073 average FPS, P95 upper bound44ms,
9,927 frames**; last previous-frame draw call sample13. Lifetime ends306.308s:
33.009FPS/44ms, including startup and separate pause/resume. P95 is1ms histogram
upper bound, not an exact percentile. The20process samples end285.63s into
continuous DEMO; sampled draw calls10–14, managed textures **9 throughout**,
Android total PSS65,440–82,609KiB (first82,609, last67,860).

No sustained managed-texture growth observed in this window; this is not VRAM
or unbounded leak proof. CPU utilization/GPU time not measured. No controlled
before/after performance A/B or continuous flicker video; frame cadence does
not establish smooth60FPS. Performance/animation acceptance remains OPEN.
Emulator results never establish Samsung Z Fold3/DeX performance.

## Remaining acceptance and delivery

FX/UI/special gestures/22namedPOI bespoke work remains, with exact names/defects
in art-review.md.64 M10 planned sources remain absent. New CST/MTN scene runtime,
per-region Android appearance, parent visual approval, continuous z-order/flicker
review, sustained performance tuning and physical DeX remain open. This task
is **not DONE**. Protected art and scenario/content logic remain unchanged.

Packaged rollback build hashes/config and restored final V2 atlas equality:
[rollback-artifacts.json](test-results/rollback-artifacts.json). Source switches
restored exactly to storybook-v2/png; final emulator APK restored likewise.

Source/evidence commit and Draft PR are published separately in DELIVERY.md.
PR #59 merged into main by explicit user authorization; see MERGE.md.
ExecPlan stays active and T-051 remains IN_PROGRESS. Local APK/cache/raw build outputs
under build remain ignored, unrelated historical evidence not staged.
