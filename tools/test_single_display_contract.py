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
assert "installParentControls()" in game and "dispatchKeyEvent" in game and "setOnLongClickListener" in game
assert "setParentMenuOpen" in core and "JourneyRenderPause.effectiveDelta" in (root/"core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java").read_text()
assert 'android:resizeableActivity="true"' in manifest
print("PASS: Single-display DeX source contract (static only, not hardware acceptance)")
