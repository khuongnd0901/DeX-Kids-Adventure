#!/usr/bin/env python3
"""T-022/T-023 local source contract. Run with scripts/test-local.sh; no CI."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
parent=(r/"android/src/main/java/com/khuongnd/dexkids/ParentActivity.kt").read_text()
prefs=(r/"android/src/main/java/com/khuongnd/dexkids/ParentSettings.kt").read_text()
kids=(r/"android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt").read_text()
game=(r/"core/src/main/java/com/khuongnd/dexkids/game/KidsGame.java").read_text()
scene=(r/"core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java").read_text()
director=(r/"core/src/main/java/com/khuongnd/dexkids/story/EntertainmentDirector.java").read_text()
for name in ("SAU", "ONG", "BOTH"):
    assert f'"{name}"' in prefs and f'Audience.{name}' in director
assert 'getString("audience_mode", "BOTH")' in prefs
assert 'showAudiencePicker()' in parent and 'Chọn người xem' in parent
assert 'Cả Sâu và Ong cùng xem' in parent
assert 'ParentSettings(this).activeAge' in kids
assert 'EntertainmentDirector.FIRST_BEAT_DELAY_MS' in kids
assert 'EntertainmentDirector.BETWEEN_BEATS_MS' in kids
assert 'EntertainmentDirector.POI_PRIORITY_DELAY_MS' in kids
assert 'entertainment.position' in kids and 'entertainment.next.elapsed' in kids
assert 'playEntertainment(beat)' in kids and 'narrateIfApproved(beat.introduction()' in kids
assert 'showEntertainmentReaction' in game and 'entertainmentReaction.getAndSet(null)' in scene
assert 'clearTalkQueue()' in kids and 'narrator?.stop()' in kids
assert 'queueLiveConversation(it,cue.textVi())' in kids
assert 'getBoolean("child_text_cloud_explicit", false)' in (r/"android/src/main/java/com/khuongnd/dexkids/ai/KidsAiStore.kt").read_text()
assert not list((r/".github/workflows").glob("*.yml"))
print("PASS: T-022 three viewers, T-023 offline beats, POI priority and DeX contracts")
