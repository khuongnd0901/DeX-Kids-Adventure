# T-003 M2 — Sprite Art, Atlas, and Visual Acceptance

## Scope
Original editable 2D art is checked into `art/assets-source/` (4 Capybara frames, yellow bus, wheel and tree).
The pinned CairoSVG + Pillow toolchain generates individual PNG sprites and single-page `assets/generated/kids.{png,atlas}`; both Android and Desktop read a TextureAtlas via LibGDX SpriteBatch.

## Implemented checkpoints
- Source asset commit d6aec1f43c27be4a7657d0dce855482aa88ad26c
- Android, Desktop classes, unit test and atlas packaging CI run 37801172670: SUCCESS.
- OpenGL screenshot smoke code commit 9b9f3cb5cf08bd310d7c7be0d49d0660a48eccce.
- Desktop smoke run 37801588527: FAILED; atlas path resolution differs between Android and desktop.
- Atlas path fix commit ce72cf0e85d750dc64c806bef65db556ac020347. Rerun CI in progress.

## Acceptance gates
- [x] Source SVGs exist in GitHub, deterministic atlas generation and debug APK CI PASS.
- [ ] Desktop OpenGL renders at 1280x720 and 1920x1080 with actual captured screenshots.
- [ ] Visual review of transparency, layering, correct wheel/capybara placement and chunk seam.
- [ ] Talk animation actually follows verified Vietnamese narration, no fake speech.
- [ ] Final production art direction approved and license audit complete.
- [ ] 30-minute and 60-minute Fold3/DeX performance and crash-free acceptance.

Status: IN_PROGRESS; do not mark DONE based only on compilation.
Next: confirm rerun CI, review real desktop screenshot artifacts, then device validation.
