# Parent Controls — single-display Samsung DeX

The parent dashboard and child adventure operate on the **same external DeX display**.
The parent uses the mouse, **Parents · menu** button, F10, Menu or Back to pause a
journey and open Parent Controls. **No PIN, unlock dialog, lock screen, kiosk or
phone-touch interaction** is required. The dashboard is accessible immediately.

## Controls and session safety

- **Continue** closes the parent menu and resumes the journey.
- **End adventure** stops the current session and returns to the parent dashboard.
- The parent menu exposes **Audio-only** and applicable emergency AI/cloud-sharing
  disable actions without additional authentication.
- The session's absolute wall-clock deadline continues while paused and cannot be
  extended by Activity recreation.
- Live GPS/voice are paused while the parent dialog is open. Existing Android OS
  location/microphone permission rules still apply.

## Audio-only

Audio-only is available via **Cài đặt → Giọng kể và microphone**, or from the
in-game Parent Controls menu. It hides moving scenery but keeps the foreground
journey, GPS/GPX, POI selection, captions and session deadline active. It does
**not** enable speech automatically; narration requires the existing offline TTS
configuration and a locally available Vietnamese voice. No online TTS fallback
or background playback service is added.

## Migration from earlier builds (2026-10-12)

The local PIN feature has been removed. When the updated app opens its parent
settings, obsolete `pin_credential`, `pin_failures` and `pin_retry_after`
preferences are deleted. Other child/audience, AI, audio and route settings are
preserved. Historical emulator PIN test evidence is retained solely as history,
not a statement of current functionality.

## Verification

Run `scripts/simulator/runtime-suite.sh` with locally built Android debug and
AndroidTest APKs. The `parent_controls` instrumentation mode checks dashboard
access without PIN, legacy preference cleanup, menu pause/resume, Audio-only,
and existing AI-disable behavior. Actual device/DeX acceptance still requires
hardware testing.
