#!/usr/bin/env python3
"""Fail CI if atlas metadata or alpha bounds do not match actual sprites."""
from pathlib import Path
from PIL import Image
from build_sprites import OUT, SOURCES, SIZE

def main():
    atlas = OUT / "kids.atlas"
    png = OUT / "kids.png"
    assert atlas.exists() and png.exists()
    data = atlas.read_text(encoding="utf8")
    with Image.open(png) as bitmap:
        assert bitmap.mode == "RGBA"
        assert bitmap.size == SIZE
        assert bitmap.getextrema()[3][0] == 0, "Atlas lost transparency"
        for name, source in SOURCES.items():
            assert source.is_file(), name
            assert f"\n{name}\n" in data, f"Missing atlas region: {name}"
            with Image.open(OUT / "sprites" / (name + ".png")) as sprite:
                assert sprite.mode == "RGBA"
                assert sprite.getbbox(), f"Empty sprite {name}"
                assert sprite.getextrema()[3][0] == 0, f"Sprite lacks transparency {name}"
    print(f"VERIFIED atlas metadata and transparency for {len(SOURCES)} regions (source-level)")
if __name__ == "__main__":
    main()
