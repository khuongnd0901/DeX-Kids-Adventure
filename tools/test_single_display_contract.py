#!/usr/bin/env python3
"""Source-level guardrail: no separate phone parent controller is reintroduced."""
from pathlib import Path

root=Path(__file__).resolve().parents[1]
ui=(root/"android/src/main/java/com/khuongnd/dexkids/ParentActivity.kt").read_text()
game=(root/"android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt").read_text()
manifest=(root/"android/src/main/AndroidManifest.xml").read_text()
core=(root/"core/src/main/java/com/khuongnd/dexkids/game/KidsGame.java").read_text()
assert "DisplayRouter(" not in ui, "Old dual-display launcher used"
assert "startGameOnCurrentDisplay" in ui and "startActivity(Intent(this, KidsActivity::class.java)" in ui
assert "launchDisplayId" not in ui, "Must not force child onto a second display"
assert "installParentControls()" in game and "dispatchKeyEvent" in game and "setOnClickListener { showParentMenu() }" in game
assert "setParentMenuOpen" in core and "JourneyRenderPause.effectiveDelta" in (root/"core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java").read_text()
assert 'android:resizeableActivity="true"' in manifest
print("PASS: Single-display DeX source contract (static only, not hardware acceptance)")

# GPX replay must keep the same-display selection and a bounded import.
picker=ui
assert "Intent.ACTION_OPEN_DOCUMENT" in picker
assert "KidsActivity.EXTRA_GPX_REPLAY" in picker
assert "startActivityForResult" in picker
assert "this.data = uri" in picker
kids=(root/"android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt").read_text()
assert "DeferredGpxJourneyFeed" in kids
assert "BoundedGpxInputStream" in kids
assert "parseReplayAsync" in kids and "initializeJourney(feed, config)" in kids
assert "holder.replay()?.reset()" in kids
assert 'Gdx.app?.postRunnable' in kids
assert "EXTRA_GPX_REPLAY" in kids
print("PASS: bounded, same-display GPX replay source contract (static only)")

# M7/M8 guards: no touchscreen hold, lock screen or unprotected IPC.
settings=(root/"android/src/main/java/com/khuongnd/dexkids/ParentSettings.kt").read_text()
receiver=(root/"android/src/main/java/com/khuongnd/dexkids/KidsCommandReceiver.kt").read_text()
scene=(root/"core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java").read_text()
assert "var audioOnly" in settings and "setAudioOnly" in game
assert "audioOnly.getAsBoolean()" in scene and "journey.update(delta)" in scene
assert "KEYCODE_F10" in game and "Parents · menu" in game
assert "setOnLongClickListener" not in game
assert all(x in receiver for x in ("ACTION_STOP","ACTION_PAUSE","ACTION_RESUME"))
assert 'android:protectionLevel="signature"' in manifest
assert 'android:permission="com.khuongnd.dexkids.permission.CONTROL"' in manifest
assert "KeyguardManager" not in game and "lockNow" not in game
print("PASS: mouse controls/audio-only/protected IPC (static, NOT device acceptance)")
