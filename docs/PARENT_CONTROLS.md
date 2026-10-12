# Parent Controls — single-display Samsung DeX

The parent dashboard and child adventure share the **same external DeX
display**. Use the mouse, **Parents · menu**, F10/Menu or Back to open
Parent Controls on that display. No PIN, locked screen, kiosk or phone-touch
interaction is necessary.

## Continuous viewing and manual exit

The app has **no viewing-duration setting or automatic session deadline**.
DEMO/LIVE/GPX continues until the parent chooses **End adventure** or
**Dừng hành trình**, or Android/the user closes the Activity. **Continue**
dismisses the menu and resumes the same trip at any time, regardless of
hours elapsed. The parent menu still pauses GPS, narration and visuals while
it is open. Ending a journey does not play a scripted farewell; it exits
directly to the parent dashboard. A finished GPX track can remain stationary;
this does not end the Activity.

Legacy `session_minutes` preferences from older versions are removed at
first access to ParentSettings. A saved Activity recreation state no longer
stores or restores an expiry. App backgrounding still obeys Android
lifecycle suspension; this does not imply indefinite background service.

## Audio-only and privacy

Audio-only hides moving scenery but does not stop GPS/GPX, POI detection,
captions or narration. Live GPS/microphone require Android permission.
Offline voices and AI still respect the user's existing consent and
provider quota controls. No new permissions or cloud calls.

## QA and support

The old 30/60-minute fixed viewing-duration acceptance cases are
redefined as 30/60-minute *observations*; the app must still be alive at
the end. Manual stop, F10 pause/resume, Activity recreation, LIVE recovery,
DEMO playback, GPX import and audio-only remain required test cases.
Run `python3 tools/test_unlimited_session.py`,
`bash scripts/test-local.sh` and the current Android instrumented suite
locally. Actual Fold3 DeX execution has not yet been performed for this
change.
