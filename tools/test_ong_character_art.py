#!/usr/bin/env python3
"""Source-only regression for Ong's four public cartoon costumes and DeX routing."""
from __future__ import annotations

import hashlib
import io
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "art/assets-source/characters/ong-costumes.png"
raw = SOURCE.read_bytes()
assert hashlib.sha256(raw).hexdigest() == (
    "6d40e5be12d486f2d218627fc288ae60a8e83221dbc2d6dd0a13ee58bd8b9ad8"
), "Ong sprite bytes changed: review art and checksum before updating"
with Image.open(io.BytesIO(raw)) as sheet:
    rgba = sheet.convert("RGBA")
    assert rgba.size == (128, 192)
    assert rgba.getpixel((0, 0))[3] == 0, "Ong artwork lost transparency"
    for role, index in (("EXPLORER", 0), ("FIREFIGHTER", 1),
                        ("PILOT", 2), ("POLICE", 3)):
        left, top = index % 2 * 64, index // 2 * 96
        tile = rgba.crop((left, top, left + 64, top + 96))
        assert tile.getbbox(), f"Ong {role} missing from spritesheet"

builder = (ROOT / "tools/build_sprites.py").read_text()
sprites = (ROOT / "core/src/main/java/com/khuongnd/dexkids/game/CartoonSprites.java").read_text()
screen = (ROOT / "core/src/main/java/com/khuongnd/dexkids/game/AdventureScreen.java").read_text()
android = (ROOT / "android/src/main/java/com/khuongnd/dexkids/KidsActivity.kt").read_text()
assert "build_ong_art()" in builder
assert 'OUT.parent / "characters" / "ong-costumes.png"' in builder
assert 'Gdx.files.internal("characters/ong-costumes.png")' in sprites
assert "drawOngCompanion" in sprites and "ongCostumesTexture.dispose()" in sprites
assert '!"SAU".equals(audienceMode.get())' in screen
assert '!"ONG".equals(audienceMode.get())' in screen
assert "boolean bothChildren" in screen
assert "setAudienceMode(ParentSettings(this).audienceMode)" in android
assert "showSauCostume(outfit)" in android
assert "adventureDashboard = AdventureDashboard(this)" in android
assert "Gdx.app.getType() != com.badlogic.gdx.Application.ApplicationType.Android" in screen
print("PASS: four Ong costumes, transparent checksum-verified sprite, 3-mode routing and DeX HUD")
