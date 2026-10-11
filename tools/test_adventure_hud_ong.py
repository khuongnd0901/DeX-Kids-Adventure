#!/usr/bin/env python3
"""Offline Ong sprite and DeX journey HUD source contracts. No device FPS claim."""
from pathlib import Path
from hashlib import sha256
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
def source(path):
    return (ROOT / path).read_text(encoding="utf-8")

png = ROOT / "art/assets-source/characters/ong-costumes.png"
data = png.read_bytes()
assert sha256(data).hexdigest() == "deef87a482965d5f5c8fe4c692e0891416869c43eb9c4170d47a24987d987bfd"
with Image.open(png) as image:
    assert image.size == (192, 288)
    rgba = image.convert("RGBA")
    assert rgba.getpixel((0, 0))[3] == 0
    for i in range(4):
        x, y = (i % 2)*96, (i // 2)*144
        assert rgba.crop((x, y, x+96, y+144)).getbbox(), f"Missing outfit {i}"

build = source("tools/build_sprites.py")
scene = source("core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java")
sprites = source("core/src/main/java/com/khuongnd/dexkids/game/CartoonSprites.java")
hud = source("core/src/main/java/com/khuongnd/dexkids/game/AdventureHud.java")
game = source("core/src/main/java/com/khuongnd/dexkids/game/KidsGame.java")
android = source("android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt")
assert "build_ong_art()" in build and 'ONG_SHEET_SIZE = (192, 288)' in build
assert 'Gdx.files.internal("characters/ong-costumes.png")' in sprites
assert "ongCostumesTexture.dispose()" in sprites
assert "drawOngCompanion" in sprites and "drawOngCompanion(batch" in scene
assert "drawSauCompanion(batch" in scene
assert '!"SAU".equals(audienceMode.get())' in scene
assert '!"ONG".equals(audienceMode.get())' in scene
assert '"BOTH".equals(audienceMode.get())' in scene
assert "showEntertainmentPrompt(beat.id(), beat.introduction())" in android
assert "AdventureHud.Prompt.fromBeat" in game
assert "adventureHud.draw(batch" in scene
assert "painter.paintHud(batch)" in scene
assert "KHAM PHA" in hud and "DO VUI" in hud and "NHAT KY CHUYEN DI" in hud
assert scene.count("batch.begin();") == 2, "Animated and audio-only batches only"
assert "ShapeRenderer" not in scene and "ShapeRenderer" not in hud
print("PASS: original Ong artwork checksum, 4 visible outfits, 3 audiences, HUD and render fast path")
