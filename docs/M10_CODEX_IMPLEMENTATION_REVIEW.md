> **PR #51 merged into `main` on 2026-10-12 at `6c60bb58bcc87cc34c12072eda90cbff296fbb19`.** This is now the **main-integrated QA runbook**. Prior branch-only/unmerged labels below are historical. Local Codex should use `git switch main && git pull --ff-only` and verify the tested SHA is at least this merge commit. Run all listed build/gallery/Android checks on main and keep M10 NOT DONE until evidence is recorded. Do not treat the merge itself as a test PASS.

# M10 — Codex implementation review and continuation

Date: 2026-10-12
Branch: `feat/M10-implementation-review-20261012`
Baseline: `main@64d186062766dd224fd26938826f0e9d5a35ad98`
Status: **SOURCE IMPLEMENTED / UNTESTED / UNMERGED.**

## What is actually implemented

| Ordered task | Files/behavior | State |
|---|---|---|
| T-039.A | `tools/build_m10_atlas.py`, `tools/test_m10_art.py`, hook in `tools/build_sprites.py` and `scripts/test-local.sh` | SOURCE IMPLEMENTED, local execution pending |
| T-040.A | `M10SceneVariety.java` + `M10SceneVarietyTest.java`: seeded variation, last-eight cooldown, replay rewind, synthetic 30-minute histogram | SOURCE IMPLEMENTED, JUnit pending |
| T-041.A | `CartoonSprites.drawExtras` uses selector with existing 18 extra props; existing renderer/AnimationTimeline/POI preserved | SOURCE IMPLEMENTED, visual QA pending |
| T-042.A | `tools/render_m10_gallery.py` contact sheet from actual generated sprite PNGs | SOURCE IMPLEMENTED, screenshots pending |
| T-038.P0 | 48 new original 160×160 biome SVG illustrations, 8 per art biome group | SOURCE_CREATED_UNREVIEWED |
| T-036.P0 | 8 new Capybara expressive SVG poses, retaining original face/vector anchors | SOURCE_PARTIAL — 16 Sâu/Ong P0 missing |
| T-037.P0 | 12 newly authored 280×160 SVG vehicle silhouettes | SOURCE_CREATED_UNREVIEWED |
| T-039.B | 16 FX source SVGs + 8 icon UI SVGs | SOURCE_PARTIAL — runtime 26 profiles/8 triggers not yet wired |
| T-041.B | New environment atlas → runtime mapping and <=12MiB resident target | PARTIAL (props atlas only; other art/event mapping not implemented) |
| T-042.B | Android/real Samsung Fold3 acceptance | NOT RUN |
| P1 | 64 lower priority source concepts | PLANNED |

**Inventory counts:** 172 IDs total, 108 P0 / 64 P1. This branch
contains **92 source SVGs**: 48 props + 8 Capybara + 12 vehicles +
16 FX + 8 UI. **80 missing** (16 child-avatar P0 +64 P1).
The 92 are **SOURCE_CREATED_UNREVIEWED**, not PACKED/RENDERED/APPROVED.
Current game runtime now renders **new M10 environment prop sprites** from
a dedicated 1–3-page resident `props.atlas` alongside the old extra sprites,
with allowlisted generated `props-map.tsv`. Other new CAP/vehicle/FX/UI
sprites are packed but **not yet wired** to visual events or gameplay.
The restricted M10 prop atlas uses about 4MiB per RGBA page; its actual
physical GPU allocation must be measured before acceptance.

## Local Codex review instructions

Start from a clean worktree and fetch the branch. Respect other ongoing
changes. Read `AGENTS.md`, `.agent/STATUS.md`, `.agent/TASKS.md`,
`.agent/plans/active/T-036-T-042-art-variety-and-animation-pack.md`
and `docs/M10_REUSE_DEPENDENCY_REVIEW.md` before modifying code.

```bash
git fetch origin
git switch --track origin/feat/M10-implementation-review-20261012
git rev-parse HEAD
git status --short
python3 -m pip install -r tools/requirements-art.txt
python3 tools/build_sprites.py
python3 tools/test_sprite_atlas.py
python3 tools/test_m10_art.py
python3 tools/render_m10_gallery.py
bash scripts/test-local.sh
./gradlew --no-daemon :core:test :android:testDebugUnitTest \
  :android:assembleDebug :android:assembleDebugAndroidTest
find assets/generated/m10 -maxdepth 1 -type f
find assets/generated/m10-gallery -maxdepth 1 -type f
```

Use the current checkout SHA for ALL evidence. If the test runner is
missing dependencies or packaging fails, record exact failure and fix
on this branch, not on main; rerun affected gates.

## Code review questions — all must be answered

- [ ] Does the 172-row CSV schema reject duplicate IDs/paths,
  mismatched filenames and malicious SVG (DOCTYPE/script/external URLs)?
- [ ] Do all 92 SVGs rasterize in CairoSVG/Pillow with nonempty
  transparency and correct dimensions? Any clipping/empty/visual errors?
- [ ] Is generated multi-page `m10.atlas` actually consumable by
  LibGDX `TextureAtlas`? Verify with a real desktop/Android loader, not
  only Python string inspection. Preserve `kids.atlas` and `extras.atlas`.
- [ ] Does Android Gradle `generateKidsArt` include generated pages and
  manifest in the APK? Does repeated build produce identical hashes?
- [ ] Does `M10SceneVariety` avoid repetition when >=9 choices,
  cache same chunk on every render frame, vary across trips, replay
  deterministically and reset correctly after GPX seek/rewind?
- [ ] Does its trip seed cause undesirable screenshot nondeterminism
  or activity recreation scene flicker? Fix via injected test fixture
  if needed, not by suppressing test output.
- [ ] Is `CartoonSprites` free from extra bitmap IO/texture allocation
  per rendered frame? Does full/Audio-only mode and F10 keep working?
- [ ] Does this extend—not replace—`AnimationTimeline`,
  `AnimationRegistry`, `ProceduralWorldGenerator`, `SceneryCast`,
  `VisualCue`, `PoiSceneDirector` or `DynamicSceneComposer`?
- [ ] Are 48 biome props visually distinguishable from original 18?
  Do source descriptions avoid fake nearby animals/traffic/weather?
- [ ] Do 8 Capybara faces preserve original eye/nose/muzzle anchors?
  Do limbs overlap bus/passenger avatars? Inspect pixel-level galleries.
- [ ] Do vehicle shapes align with real roads vs ships/rail/cable
  and avoid false live traffic assertions?
- [ ] Are 16 FX gentle and non-strobing? Are 8 UI icons legible at
  1920×1080 without embedded child text?
- [ ] All new SVGs are currently **unreviewed visual first passes**:
  approve individually or revise; don't certify from XML validity.
- [ ] Is parent dashboard PIN-free, same DeX display, with no new
  GPS/cloud request or AI 10-RPM/reviewer changes?
- [ ] Is any original child photograph, key or raw GPS/audio leaked?
  Never commit family photographs.

## Remaining implementation after this source pass

1. Rebuild and visually review the 92 actual SVG sources. Fix
   silhouette, face, scale and atlas metadata issues first.
2. Complete `T-036.P0`: 8 Sâu + 8 Ong expressions using only approved
   existing cartoon avatars (`sau-story-v2.png` and `ong-story-v2.png`)
   and preserve likeness; **do not invent replacement faces**.
   Human visual approval needed.
3. Complete `T-039.B` by mapping the 26 motion profiles and
   8 semantic one-shots into existing `AnimationTimeline` and
   `AnimationRegistry`. No second animation engine or cloud calls.
4. Complete `T-041.B` with allowlisted manifest-driven atlas region
   access, bounded actual texture residency (<=12 MiB incremental is
   a target to verify on Fold3), and real in-scene use for new SVGs.
   The current packer outputs multiple pages but **the runtime has
   no new M10 atlas consumer** yet.
5. Complete `T-042.B` by extending the existing T-026/T-033
   QA harness (not a second GPS test runner). Demo/GPX tests of
   SAU/ONG/BOTH, one-screen DeX FullHD, F10/no-PIN, Audio-only,
   POI>GeoWorld>fiction, runtime alpha/lifetime, offline TTS,
   30-minute FPS/P95/PSS/thermal and diversity histogram. Target
   >=55 FPS average / p95<=25ms and <=10% drop vs same-build
   controlled baseline. Historic metrics are not M10 PASS.
6. Defer P1 (64 art ideas) until P0 source quality, runtime,
   GPU budget and real parent review are approved.

## Evidence template for Codex

```text
Feature commit SHA:
Build tool versions / Android device / external display id:
Executed commands (exact):
92-source pack: PASS/FAIL/BLOCKED, screenshots:
Source counts / packed counts / rendered counts / approved counts:
Atlas pages & resident GPU/PSS:
JUnit/Gradle/emulator PASS/FAIL/BLOCKED:
Diversity: unique per 10/30m, recent-8 duplication rate:
SAU/ONG/BOTH full HD visual review:
No regression: no PIN, F10, GPS, POI and TTS:
Fix commits and remaining blockers:
Final recommendation: MERGE or BLOCK, with reasons:
```

Save actual outputs to `.agent/evidence/M10-codex-review/`
without keys, photo originals, user GPS or raw child speech.
Update `.agent/STATUS.md` and `.agent/TASKS.md` after every
checkpoint; never mark a task done just because its source exists.

## Copy/paste launch prompt

> Read `AGENTS.md`, `.agent/STATUS.md`, `.agent/TASKS.md`,
> `.agent/plans/active/T-036-T-042-art-variety-and-animation-pack.md`,
> `docs/M10_REUSE_DEPENDENCY_REVIEW.md` and this M10 Codex review guide.
> Checkout `feat/M10-implementation-review-20261012`; audit against
> main and run all local SVG/atlas, Python, JUnit and Gradle checks.
> Render the source art gallery and inspect at 1920×1080.
> Fix reproducible bugs on the feature branch with focused tests.
> Avoid building duplicate visual engines; preserve verified OSM POIs,
> offline voice, 10-RPM creative API rules, no PIN, single DeX display,
> F10 and original Sâu/Ong likeness.
> Report PASS/FAIL/BLOCKED and exact files/lines; keep 16 Sâu/Ong
> sources, T-039.B runtime motion, T-041.B atlas integration and
> T-042.B device QA explicitly OPEN until actually implemented and
> measured. Write all evidence into Markdown before recommending merge.
