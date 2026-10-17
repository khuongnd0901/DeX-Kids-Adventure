#!/usr/bin/env python3
"""M1/M2 source contract. Does not replace Gradle, screenshot, FPS or DeX device QA."""
from pathlib import Path
r=Path(__file__).resolve().parents[1]
def get(p): return (r/p).read_text(encoding="utf-8")

dashboard=get("android/src/main/java/com/khuongnd/dexkids/AdventureDashboard.kt")
parent=get("android/src/main/java/com/khuongnd/dexkids/ParentActivity.kt")
kids=get("android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt")
settings=get("android/src/main/java/com/khuongnd/dexkids/ParentSettings.kt")
hud=get("core/src/main/java/com/khuongnd/dexkids/game/AdventureHud.java")
game=get("core/src/main/java/com/khuongnd/dexkids/game/KidsGame.java")
screen=get("core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java")
sprites=get("core/src/main/java/com/khuongnd/dexkids/game/CartoonSprites.java")
visual=get("core/src/main/java/com/khuongnd/dexkids/story/VisualCue.java")
beats=get("core/src/main/java/com/khuongnd/dexkids/story/EntertainmentDirector.java")
coordinator=get("android/src/main/java/com/khuongnd/dexkids/ai/TripPackCoordinator.kt")

# M1: no hardcoded displayed ages, text wraps instead of ellipsizing away AI questions.
assert "SÂU · $sauAge TUỔI" in dashboard and "ONG · $ongAge TUỔI" in dashboard
assert "fun install(mode: String, demo: Boolean, sauAge: Int = 5, ongAge: Int = 4)" in dashboard
assert "setMode(mode,sauAge,ongAge)" in dashboard
assert "setAutoSizeTextTypeUniformWithConfiguration" in dashboard
assert "ellipsize = null" in dashboard and "maxLines = lines" in dashboard
assert "promptLabel.text = beat.introduction()" in dashboard
assert "promptLabel.text = text // Let" in dashboard
assert '"SAU".equals(audience) ? "CHO SAU"' in hud and '"ONG".equals(audience) ? "CHO ONG"' in hud
assert '"SAU" -> "SÂU (5 tuổi)' not in dashboard
assert "${settings.sauAge}" in parent and "${settings.ongAge}" in parent
assert "var sauAge" in settings and "var ongAge" in settings
assert "it.install(profile.audienceMode, feed !is LiveJourneyFeed," in kids
assert "profile.sauAge,profile.ongAge" in kids
# T-068 consolidated captions into the wrapping dashboard card. The old native
# five-line banner was removed before T-070; assert the actual single-card path.
banner=kids[kids.index("private fun showEntertainmentBanner"):kids.index("private fun scheduleNextQuestion")]
assert "adventureDashboard?.showCaption(line)" in banner
assert "TextView(" not in banner
assert "fun showCaption(" in dashboard

# M2: explicit creative metadata survives generated UUID IDs and selects ONLY known art.
assert "enum Prop { NONE, BIRD, BUTTERFLY, CAT, RABBIT" in visual
assert "fromCreative(CreativeLessonPack.Lesson lesson)" in visual
assert "fromAuthored(String id, String prompt, String englishWord)" in visual
assert "return new VisualCue(theme,action,prop)" in visual
assert "class VisualCueTest" in get("core/src/test/java/com/khuongnd/dexkids/story/VisualCueTest.java")
assert "VisualCue visual" in beats and "VisualCue.fromAuthored" in beats
assert "VisualCue.fromCreative(item)" in coordinator
assert "showEntertainmentPrompt(beat, focus)" in kids
assert "Prompt.fromBeat(beat,focus)" in game
assert "visual.visual()" in screen and "cartoonSprites.setVisualCue" in screen
assert "cartoonSprites.drawBusPassengers" in screen
assert screen.index("cartoonSprites.setVisualCue") < screen.index("cartoonSprites.drawBusPassengers")
assert "setVisualCue(VisualCue cue)" in sprites
assert "case BUTTERFLY -> extras[15]" in sprites
assert "case BIRD -> extras[14]" in sprites
assert "case KITE -> extras[17]" in sprites
assert "case RABBIT -> friendSprites[0]" in sprites
assert "case CAT -> friendSprites[3]" in sprites
assert "storyPose.ordinal()" in sprites
assert "new Texture(" not in sprites[sprites.index("private void drawStoryProp"):sprites.index("private static int costumeIndex")]
assert 'Gdx.app.getType() != com.badlogic.gdx.Application.ApplicationType.Android' in screen
assert "showEntertainmentReaction" in game and "setNarrationActive" in sprites
print("PASS: M1 dynamic 4-7 age/legible native DeX UI + M2 safe AI visual cues, pose and offline atlas mapping")
