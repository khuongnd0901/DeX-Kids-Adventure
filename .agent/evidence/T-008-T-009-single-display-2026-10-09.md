# T-008/M7 + T-009/M8 single-display design change
Date: 2026-10-09

## Physical requirement
Samsung Z Fold3 builtin touchscreen/display is damaged. The external DeX monitor is the only usable screen. Previous phone-controller/DeX-child-screen requirement superseded by parent and child sequential activities on ONE active DeX display.

## Changes committed
- `ParentActivity` starts `KidsActivity` on the same Activity display using ordinary `startActivity`; no `DisplayRouter` invocation. The old helper file exists but is no longer part of this user path.
- `KidsActivity`: native long-press parent menu or F10/Menu key on same Activity/window display; Continue or End returns to parent Activity in the same task. Screen overlay uses `addContentView`, not `ViewOverlay`.
- `KidsGame` atomic cross-thread pause flag; `JourneyRenderPause` stops virtual/animation delta when menu opens. Foreground GPS listener paused during menu; elapsed-time session deadline is NOT suspended.
- Parent app + KidsActivity `resizeableActivity` enabled; mouse and keyboard can operate controls without touching phone.
- AndroidInstrumentation mode `single_display` tests the actual Activity display IDs and F10 menu/finish flow, but must be RUN on an emulator or real device to claim runtime PASS.
- [Spec/test matrix](https://github.com/khuongnd0901/DeX-Kids-Adventure/blob/feat/T-001-bootstrap-libgdx/docs/SINGLE_DISPLAY_DEX.md)

## Verified automated checks
GitHub Actions [#37883012846](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37883012846) SUCCESS: static single-display contract, core unit tests, Android debug APK, Android instrumentation APK compilation, and desktop OpenGL visual smoke. The workflow does NOT execute Android instrumentation.

## Acceptance NOT verified
Actual Samsung DeX monitor launch/focus/mouse longpress/F10, external permission dialog, window resizing, DeX↔Maps coexistence, child-safe PIN, 60-minute thermal/performance soak. Emulator `single_display` instrumentation is NOT_RUN here; physical Fold3 `SD-012` BLOCKED pending connected device.

Status M7/M8: **IN_PROGRESS**, PR DRAFT, no release/merge.
