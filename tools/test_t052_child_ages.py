#!/usr/bin/env python3
"""Local-only source contract for the corrected child age defaults and one-time migration."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
settings = (root / "android/src/main/java/com/khuongnd/dexkids/ParentSettings.kt").read_text(encoding="utf8")
director = (root / "core/src/main/java/com/khuongnd/dexkids/story/EntertainmentDirector.java").read_text(encoding="utf8")
ai = (root / "android/src/main/java/com/khuongnd/dexkids/ai/KidsAiGateway.kt").read_text(encoding="utf8")
screen = (root / "android/src/main/java/com/khuongnd/dexkids/ParentActivity.kt").read_text(encoding="utf8")
assert 'prefs.getInt("sau_age",5)' in settings, "Sâu must default to age 5"
assert 'prefs.getInt("ong_age",4)' in settings, "Ong must default to age 4"
assert 'sauSaved == 4 && ongSaved == 5' in settings, "Repair only the reversed saved pair"
assert 'editor.putInt("sau_age", 5).putInt("ong_age", 4)' in settings
assert 'prefs.getBoolean("child_age_5_4_migrated", false)' in settings
assert "Audience.SAU -> sauAge" in settings
assert "Audience.ONG -> ongAge" in settings
assert "Audience.BOTH -> minOf(sauAge,ongAge)" in settings
assert 'this == SAU ? 5 : 4' in director
assert 'case SAU -> "Cho Sâu · 5 tuổi"' in director
assert 'case ONG -> "Cho Ong · 4 tuổi"' in director
assert 'SAU (age 5), ONG (age 4), BOTH (age 4)' in ai
assert 'settings.sauAge' in screen and 'settings.ongAge' in screen
print("T-052 age contract PASS: Sâu 5, Ong 4, BOTH 4, one-time swapped preference repair")
