#!/usr/bin/env python3
"""Local-only proof that Sâu's four published cartoon assets decode exactly."""
import base64
import hashlib
import io
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
src = ROOT / "art/assets-source/characters"
parts = [src / f"sau-costumes.b64.part{i:02}" for i in range(1, 6)]
encoded = "".join(part.read_text(encoding="ascii").strip() for part in parts)
binary = base64.b64decode(encoded, validate=True)
assert hashlib.sha256(binary).hexdigest() == (
    "3cd623f54e37cf7c3d6e9e6a371f5dd280ba2f0d9df5c9575917125ac8427f6d"
), "Corrupted child art — abort before packaging"
assert len(binary) == 15_766
with Image.open(io.BytesIO(binary)) as image:
    assert image.size == (224, 336)
    assert image.getpixel((0, 0)) == 0 or image.convert("RGBA").getpixel((0, 0))[3] == 0
    rgba = image.convert("RGBA")
    for y in (0, 168):
        for x in (0, 112):
            assert rgba.crop((x, y, x + 112, y + 168)).getbbox(), (x, y)
sprite = (ROOT / "core/src/main/java/com/khuongnd/dexkids/game/CartoonSprites.java").read_text()
screen = (ROOT / "core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java").read_text()
game = (ROOT / "core/src/main/java/com/khuongnd/dexkids/game/KidsGame.java").read_text()
android = (ROOT / "android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt").read_text()
builder = (ROOT / "tools/build_sprites.py").read_text()
assert "build_sau_art()" in builder and 'OUT.parent / "characters" / "sau-costumes.png"' in builder
assert 'Gdx.files.internal("characters/sau-costumes.png")' in sprite
assert 'drawSauCompanion' in sprite and 'sauCostumesTexture.dispose()' in sprite
assert "drawSauCompanion(batch, clock" in screen
assert '"ONG".equals(audienceMode.get())' in screen
assert "game.setAudienceMode(ParentSettings(this).audienceMode)" in android
assert "runningGame?.showSauCostume(outfit)" in android
assert "showSauCostume(String role)" in game
assert "showEntertainmentReaction" in game, "Keep existing Capybara animations"
assert not list((ROOT / ".github/workflows").glob("*.yml"))
print("PASS: 4 Sâu PNG quadrants, byte-exact verified, Android sprite wiring, audience/privacy gates; local source contract")
