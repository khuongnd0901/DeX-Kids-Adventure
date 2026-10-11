#!/usr/bin/env python3
"""T-033 source contract + offline raster smoke. NOT a Gradle/DeX/GPS field test."""
from pathlib import Path
import subprocess
import sys

root=Path(__file__).resolve().parents[1]
read=lambda file:(root/file).read_text(encoding="utf-8")
screen=read("core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java")
game=read("core/src/main/java/com/khuongnd/dexkids/game/KidsGame.java")
sprites=read("core/src/main/java/com/khuongnd/dexkids/game/CartoonSprites.java")
geo=read("core/src/main/java/com/khuongnd/dexkids/geo/GeoWorldIndex.java")
geo_director=read("core/src/main/java/com/khuongnd/dexkids/game/GeoWorldDirector.java")
composer=read("core/src/main/java/com/khuongnd/dexkids/game/DynamicSceneComposer.java")
timeline=read("core/src/main/java/com/khuongnd/dexkids/game/AnimationTimeline.java")
planner=read("core/src/main/java/com/khuongnd/dexkids/story/LocalVisualPlanner.java")
ui=read("android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt")
assert "VisualPlan.Motion.FLY" in timeline or "case FLY" in timeline
assert "case BUTTERFLY, BIRD, KITE" in planner
assert "visualEvent.getAndSet(null)" in screen
assert "animationTimeline.advance(delta)" in screen
assert "setStoryAnimation(animationTimeline.x()" in screen
assert "showEntertainmentPhase(beat.id(), \"QUESTION\")" in ui
assert "showEntertainmentPhase(beat.id(), \"ANSWER\")" in ui
assert "showEntertainmentPhase(\"\", \"INTERRUPTED\")" in ui
assert 'assets/geo/landcover-cells.tsv' in screen
assert "geoWorld.observe(position.get(),now)" in screen
assert "reviewedPoiData || geoWorldAvailable" in screen
assert "poi.active()" in composer and "geo.active()" in composer
assert "sampleId()<=lastSample" in geo_director
assert "fix.sufficientQuality()" in geo_director
assert "nowMs-lastAcceptedMs>20_000" in geo_director
assert "100_000" in geo and "osm:(?:way|relation)" in geo
assert "batch.draw(extras[which],x,y,w,h)" in sprites
assert "storyMotionX" in sprites and "storyMotionY" in sprites
assert "new Texture(" not in sprites[sprites.index("void drawExtras"):sprites.index("private void drawLamp")]
assert "new Texture(" not in sprites[sprites.index("private void drawStoryProp"):sprites.index("private static int costumeIndex")]
assert read("assets/geo/landcover-cells.tsv").startswith("# dexkids-geo-cells-v1")
subprocess.run([sys.executable,str(root/"tools/test_geo_world.py")],check=True)
print("PASS: T-033 static contracts & offline OSM geometry fixtures; Gradle/physical tests NOT RUN")
