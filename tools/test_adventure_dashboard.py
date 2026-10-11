#!/usr/bin/env python3
"""Local contract: 3-audience DeX story HUD and four original Ong costumes."""
import hashlib
from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1]
art = root / "art/assets-source/characters/ong-costumes.png"
payload = art.read_bytes()
assert hashlib.sha256(payload).hexdigest() == (
    "deef87a482965d5f5c8fe4c692e0891416869c43eb9c4170d47a24987d987bfd"
)
with Image.open(art) as im:
    assert im.size == (192, 288), im.size
    image = im.convert("RGBA")
    assert image.getpixel((0, 0))[3] == 0
    for index in range(4):
        x, y = index % 2 * 96, index // 2 * 144
        assert image.crop((x, y, x + 96, y + 144)).getbbox(), index

builder = (root / "tools/build_sprites.py").read_text()
sprites = (root / "core/src/main/java/com/khuongnd/dexkids/game/CartoonSprites.java").read_text()
screen = (root / "core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java").read_text()
game = (root / "core/src/main/java/com/khuongnd/dexkids/game/KidsGame.java").read_text()
android = (root / "android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt").read_text()
hud = (root / "android/src/main/java/com/khuongnd/dexkids/AdventureDashboard.kt").read_text()
entertainment = (root / "core/src/main/java/com/khuongnd/dexkids/story/EntertainmentDirector.java").read_text()

assert "build_ong_art()" in builder
assert 'ONG_SHEET_SIZE = (192, 288)' in builder
assert 'Gdx.files.internal("characters/ong-costumes.png")' in sprites
assert "ongCostumesTexture.dispose()" in sprites
assert "drawOngCompanion(batch, clock" in screen
assert "drawSauCompanion(batch, clock" in screen
assert '!"ONG".equals(audienceMode.get())' in screen
assert '!"SAU".equals(audienceMode.get())' in screen
assert "adventureDashboard?.showBeat(beat, focus)" in android
assert "adventureDashboard?.showPoiIntro(intro, live)" in android
assert "adventureDashboard?.setVisualsEnabled(!settings.audioOnly)" in android
assert "showAudiencePicker()" in (root / "android/src/main/java/com/khuongnd/dexkids/ParentActivity.kt").read_text()
assert all(value in hud for value in ('"SAU"', '"ONG"', '"BOTH"'))
assert "© OpenStreetMap" in hud and "chưa khớp tuyến" in hud
assert entertainment.count("new Beat(") == 66, entertainment.count("new Beat(")
assert "showSauCostume" in game
print("PASS: Ong art SHA/dimensions/4 costumes + 3-audience dashboard + 66 offline beats")
