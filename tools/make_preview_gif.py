#!/usr/bin/env python3
"""Create a small storyboard from ACTUAL sequential LibGDX OpenGL frames."""
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SCREENSHOT = ROOT / "build/visual-evidence"
FRAMES = SCREENSHOT / "frames-kids-1920x1080"
OUT = SCREENSHOT / "capybara-bus-animation-preview.gif"

def main():
    files = sorted(FRAMES.glob("*.png"))
    if len(files) < 8:
        raise SystemExit("At least 8 actual rendered frames required")
    frames = []
    for source in files:
        with Image.open(source) as original:
            original.load()
            if original.size != (1920, 1080):
                raise SystemExit("Unexpected screenshot resolution")
            # Save smaller preview; screenshots remain available at full resolution.
            frames.append(original.convert("RGB").resize((960, 540), Image.Resampling.LANCZOS))
    frames[0].save(OUT, format="GIF", save_all=True, append_images=frames[1:],
                   duration=200, loop=0, optimize=True)
    print(f"Created {OUT.name} from {len(files)} actual LibGDX OpenGL frames; SIMULATED GPS")
if __name__ == "__main__":
    main()
