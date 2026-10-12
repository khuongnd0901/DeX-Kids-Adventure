#!/usr/bin/env python3
"""Static regression guard for parent-controlled unlimited DeX journeys.

This is not Android instrumented acceptance. Duration limits in a test
harness remain legitimate; duration limits on a child's session do not.
"""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
android = root / "android/src/main/java/com/khuongnd/dexkids"
child = (android / "KidsActivity.kt").read_text(encoding="utf-8")
parent = (android / "ParentActivity.kt").read_text(encoding="utf-8")
prefs = (android / "ParentSettings.kt").read_text(encoding="utf-8")
story = (root / "core/src/main/java/com/khuongnd/dexkids/story/EntertainmentDirector.java").read_text(encoding="utf-8")
runner = (root / "android/src/androidTest/java/com/khuongnd/dexkids/qa/LifecycleValidationRunner.java").read_text(encoding="utf-8")
route = (root / "android/src/androidTest/java/com/khuongnd/dexkids/qa/RouteThirtyValidation.java").read_text(encoding="utf-8")

for token in ("sessionDeadlineElapsed", "sessionStop", "SESSION_DEADLINE",
              "event=limit_reached", "postDelayed(sessionStop"):
    assert token not in child, f"Child session auto-end reintroduced: {token}"
assert "duration=unlimited" in child
assert "KidsSessionControl.register(this)" in child
assert 'isSessionActive = {' in child and '!parentMenuOpen && !isFinishing && !isDestroyed' in child
assert 'outState.putLong("entertainment.next.elapsed"' in child
assert 'KidsSessionControl.stop()' in parent
assert 'setPositiveButton("End adventure") { _, _ -> finish() }' in child
assert 'event.keyCode == KeyEvent.KEYCODE_F10' in child
assert "settings.sessionMinutes" not in parent and "Thời gian mỗi chuyến" not in parent
assert "15 phút\",\"30 phút\",\"60 phút" not in parent
assert "var sessionMinutes" not in prefs
assert '.remove("session_minutes")' in prefs, "Old duration preference must be purged"
for token in ('both-goodbye', 'common-goodbye', 'Tạm biệt'):
    assert token not in story, f"Ending greeting beat remains: {token}"
assert "no_auto_expiry" in runner and "session_expiry" not in runner
assert "continues_after_30min" in route and "natural_30min_expiry" not in route
assert not (root / "core/src/main/java/com/khuongnd/dexkids/session/SessionTimeLimit.java").exists()
m10_catalog=(root / "docs/ART_EXPANSION_PACK.md").read_text(encoding="utf-8")
m10_inventory=(root / "art/M10_ASSET_INVENTORY.csv").read_text(encoding="utf-8")
assert "GOODBYE_FADE" not in m10_catalog
assert "sticker_goodbye" not in m10_inventory
assert "SCENE_TRANSITION_SOFT" in m10_catalog
print("PASS: unlimited parent-controlled journeys, no auto deadline or farewell, manual exit preserved; Android QA still required")
