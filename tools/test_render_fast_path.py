#!/usr/bin/env python3
"""Local-only renderer fast-path guard; not an FPS measurement or emulator test."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
src = root / "core/src/main/java/com/khuongnd/dexkids"
android = root / "android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt"
screen = (src / "game/AdventureScreen.java").read_text()
painter = (src / "game/WorldPainter.java").read_text()
sprites = (src / "game/CartoonSprites.java").read_text()
window = (src / "world/WorldWindow.java").read_text()
device = android.read_text()
assert "ShapeRenderer" not in screen and "ShapeRenderer" not in painter
assert screen.count("batch.begin();") == 2, "One audio-only and one animated batch expected"
assert "painter.paintSky(batch, mood)" in screen
assert "painter.paintGround(batch, journey.distanceMeters())" in screen
assert "painter.paintHud(batch)" in screen
assert "nextMoodCheckAt = moodNow + 60_000L;" in screen
assert "frameProfiler.record(rawDelta);" in screen
assert "runMetrics.record(rawDelta);" in screen
assert "new WorldWindow(generator, 0, 3)" in painter
assert "new WorldWindow(new ProceduralWorldGenerator(20261008L), 0, 3)" in sprites
assert "for (long idx = first; idx <= first + 3; idx++)" in painter
assert "for (long idx = first; idx <= first + 3; idx++)" in sprites
assert "new Texture(pix)" in painter and "pix.fillCircle(" in painter
assert "primitiveAtlas.dispose()" in painter and "pix.dispose()" in painter
assert "prepared && preparedFor == currentChunk" in window
assert "WorldChunk[] slots" in window and "HashMap" not in window
assert "depth = 0" in device and "stencil = 0" in device and "numSamples = 0" in device
assert "setParentMenuOpen" in screen or "parentMenuOpen.getAsBoolean()" in screen
assert "OfflinePoiEngine" in screen and "pollNarrationCue" in screen
workflows = root / ".github/workflows"
assert not workflows.exists() or not list(workflows.glob("*.yml")), "CI should remain removed"
print("PASS: local static render fast-path: one animated SpriteBatch, four visible chunks, ring cache, static shapes, no CI")
