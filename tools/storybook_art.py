#!/usr/bin/env python3
"""Optional, deterministic storybook finish for non-child 2D artwork.

This is a visual treatment, not an AI illustration generator. Original SVGs
and T-048 PNGs remain available unchanged for reproducibility and rollback.
"""
from __future__ import annotations

from pathlib import Path
from PIL import Image, ImageChops, ImageEnhance, ImageFilter, ImageOps

ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOT = ROOT / "art" / "assets-source"
MODE_FILE = ROOT / "assets" / "art-style.txt"
MODE = MODE_FILE.read_text(encoding="utf-8").strip() if MODE_FILE.exists() else "classic"

def enabled() -> bool:
    if MODE not in ("classic", "storybook-v2"):
        raise ValueError("Invalid art-style.txt; use classic or storybook-v2")
    return MODE == "storybook-v2"

def select_source(source: Path) -> Path:
    """Use hand-authored new SVGs when available; retain old paths as fallback."""
    if not enabled():
        return source
    try:
        rel = source.relative_to(SOURCE_ROOT)
    except ValueError:
        return source
    if "sau" in rel.parts or "ong" in rel.parts or (
        rel.name.lower().startswith(("sau", "ong", "capybara", "cap_"))
    ):
        return source
    override = SOURCE_ROOT / "storybook-v2" / rel
    return override if override.is_file() else source

def stylize(image: Image.Image, family: str = "sprite") -> Image.Image:
    """Preserve source frame size/alpha; add controlled warmth and inner keylines.

    family='protected' means Sâu/Ong, untouched. T-048 PNG atlases never call
    this helper. No random noise, external I/O, or per-frame GPU work.
    """
    if family == "protected" or not enabled():
        return image
    rgba = image.convert("RGBA")
    alpha = rgba.getchannel("A")
    w, h = rgba.size
    rgb = rgba.convert("RGB")
    rgb = ImageEnhance.Color(rgb).enhance(1.13)
    rgb = ImageEnhance.Contrast(rgb).enhance(1.035)
    top = ImageOps.invert(Image.linear_gradient("L").resize((w, h)))
    warm = ImageChops.multiply(alpha, top).point(
        (lambda p: int(p * (0.085 if family == "backdrop" else 0.115))))
    rgb = Image.composite(Image.new("RGB", (w, h), (255, 234, 194)), rgb, warm)
    lower = ImageChops.multiply(alpha, ImageOps.invert(top)).point(
        (lambda p: int(p * (0.025 if family == "backdrop" else 0.055))))
    rgb = Image.composite(Image.new("RGB", (w, h), (108, 130, 170)), rgb, lower)
    if family not in ("backdrop", "effect", "ui"):
        inside = ImageChops.subtract(alpha, alpha.filter(ImageFilter.MinFilter(3)))
        ink = inside.point(lambda p: int(p * 0.24))
        rgb = Image.composite(Image.new("RGB", (w, h), (94, 64, 57)), rgb, ink)
    rgb.putalpha(alpha)
    return rgb
