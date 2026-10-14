#!/usr/bin/env python3
"""Render every Storybook override beside its classic source (offline art QA).

These are CairoSVG contact sheets, never Android emulator screenshots. The
runtime color treatment is applied at native size before scaling the preview.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import subprocess
from io import BytesIO
from pathlib import Path

import cairosvg
from PIL import Image, ImageDraw

import storybook_art

ROOT = storybook_art.SOURCE_ROOT
BASELINE = "1ab1f42dbb9fe017c9e2e8df2e6260452491c062"


def raster(source: Path, styled: bool) -> Image.Image:
    image = Image.open(BytesIO(cairosvg.svg2png(url=str(source)))).convert("RGBA")
    return storybook_art.stylize(image) if styled else image


def family(relative: Path) -> str:
    if relative.parts[:2] == ("m10", "props"):
        return "m10-" + relative.parts[2]
    return "-".join(relative.parts[:-1])


def build(output: Path) -> int:
    # Force the explicitly requested variant independently of checkout mode.
    storybook_art.MODE = "storybook-v2"
    output.mkdir(parents=True, exist_ok=True)
    groups = {}
    inventory = []
    overrides = sorted((ROOT / "storybook-v2").rglob("*.svg"))
    for override in overrides:
        relative = override.relative_to(ROOT / "storybook-v2")
        classic = ROOT / relative
        if not classic.is_file():
            raise FileNotFoundError(classic)
        before, after = raster(classic, False), raster(override, True)
        if before.size != after.size:
            raise ValueError(f"Changed source dimensions: {relative}")
        groups.setdefault(family(relative), []).append((relative, before, after))
        inventory.append({"source": relative.as_posix(), "size": after.size,
                          "alpha_bbox": after.getchannel("A").getbbox(),
                          "sha256": hashlib.sha256(override.read_bytes()).hexdigest()})
    for category, entries in groups.items():
        for offset in range(0, len(entries), 6):
            batch = entries[offset:offset+6]
            canvas = Image.new("RGB", (1100, 62 + 238*len(batch)), (246, 243, 234))
            draw = ImageDraw.Draw(canvas)
            draw.text((18, 12), f"T-051 {category} | CLASSIC (left) / STORYBOOK V2 (right)", fill=(44, 43, 40))
            draw.text((18, 33), "Offline source raster contact sheet; not an emulator screenshot", fill=(80, 75, 66))
            for row, (relative, before, after) in enumerate(batch):
                top = 62 + row*238
                draw.text((18, top+8), relative.as_posix(), fill=(44, 43, 40))
                for left, bitmap in ((18, before), (560, after)):
                    for y in range(top+30, top+226, 16):
                        for x in range(left, left+520, 16):
                            draw.rectangle((x,y,min(x+15,left+519),min(y+15,top+225)),
                                           fill=(236,237,233) if ((x-left)//16+(y-top-30)//16)%2 else (255,255,251))
                    preview = bitmap.copy()
                    scale = min(510/bitmap.width, 190/bitmap.height)
                    preview = preview.resize((round(bitmap.width*scale), round(bitmap.height*scale)), Image.Resampling.LANCZOS)
                    canvas.paste(preview, (left+(520-preview.width)//2, top+30+(196-preview.height)//2), preview)
            name = f"{category}-{offset//6+1:02d}.png"
            canvas.save(output/name, optimize=True)
            for entry in inventory:
                if entry["source"] in {r.as_posix() for r, _, _ in batch}:
                    entry["sheet"] = name
            print(f"{name}: {len(batch)} pairs")
    (output/"inventory.json").write_text(json.dumps(inventory, indent=2)+"\n")
    print(f"Rendered all {len(overrides)} Storybook override/classic pairs")
    return len(overrides)


def build_fixes(output: Path, baseline_revision: str = BASELINE) -> None:
    """Compare committed V2 baseline to the corrected working source."""
    storybook_art.MODE = "storybook-v2"
    paths = ("m10/props/farm/irrigation_channel.svg", "m10/props/farm/ox_cart.svg",
             "characters/companions/rabbit.svg", "characters/companions/penguin.svg",
             "props/banana_tree.svg", "props/palm.svg", "props/rice_field.svg",
             "props/buffalo.svg", "props/duck.svg")
    canvas = Image.new("RGB", (1100, 60+238*len(paths)), (246,243,234))
    draw = ImageDraw.Draw(canvas)
    draw.text((18,12), "T-051 fixes | BASELINE STORYBOOK V2 / CORRECTED STORYBOOK V2", fill=(44,43,40))
    draw.text((18,32), f"Offline source raster; baseline Git {baseline_revision[:12]}; not an emulator screenshot", fill=(80,75,66))
    for row, relative in enumerate(paths):
        source = ROOT / "storybook-v2" / relative
        git_path = source.relative_to(ROOT.parents[1]).as_posix()
        raw = subprocess.check_output(["git", "show", baseline_revision+":"+git_path], cwd=ROOT.parents[1])
        baseline = storybook_art.stylize(Image.open(BytesIO(cairosvg.svg2png(bytestring=raw))).convert("RGBA"))
        revised = raster(source, True)
        top = 60+238*row
        draw.text((18,top+8),relative,fill=(44,43,40))
        for left, bitmap in ((18,baseline),(560,revised)):
            draw.rectangle((left,top+30,left+520,top+226),fill=(232,237,234))
            scale=min(510/bitmap.width,190/bitmap.height)
            preview=bitmap.resize((round(bitmap.width*scale),round(bitmap.height*scale)),Image.Resampling.LANCZOS)
            canvas.paste(preview,(left+(520-preview.width)//2,top+30+(196-preview.height)//2),preview)
    output.mkdir(parents=True,exist_ok=True)
    canvas.save(output/"t051-source-fixes.png",optimize=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output", type=Path)
    parser.add_argument("--fixes", action="store_true", help="compare corrections to the pinned pre-T051 baseline")
    parser.add_argument("--baseline", default=BASELINE, help="Git revision for --fixes comparison")
    args = parser.parse_args()
    build_fixes(args.output,args.baseline) if args.fixes else build(args.output)
