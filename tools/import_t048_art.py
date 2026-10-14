#!/usr/bin/env python3
"""T-048: verify/import the ORIGINAL PNG bytes. Never silently replace art."""
from __future__ import annotations

import argparse
import hashlib
import io
import json
import os
import tempfile
import zipfile
from pathlib import Path

from PIL import Image

SUBDIR = Path("art/assets-source/ai-capybara-v1")
FILES = ("capybara_idle.png", "capybara_laugh.png", "capybara_ask.png",
         "capybara_wave.png", "bus_side_with_driver.png")


def inspect(zip_path: Path, repo_root: Path):
    dest = repo_root / SUBDIR
    expected = json.loads((dest / "manifest.json").read_text(encoding="utf-8"))
    declarations = {a["file"]: a for a in expected["assets"]}
    if set(declarations) != set(FILES):
        raise ValueError("Repository manifest must declare exactly five T-048 PNGs")
    with zipfile.ZipFile(zip_path) as archive:
        names = archive.namelist()
        allowed = {str(SUBDIR / n) for n in FILES + ("manifest.json", "README.md")}
        if len(names) != len(set(names)) or set(names) != allowed:
            raise ValueError("Unexpected, missing or duplicate ZIP entries")
        pack = json.loads(archive.read(str(SUBDIR / "manifest.json")))
        sources = {a["file"]: a for a in pack["assets"]}
        if set(sources) != set(FILES):
            raise ValueError("ZIP manifest must declare exactly five T-048 PNGs")
        verified = []
        for name in FILES:
            info = archive.getinfo(str(SUBDIR / name))
            if info.file_size > 8_000_000:
                raise ValueError("Oversize PNG: " + name)
            data = archive.read(info)
            digest = hashlib.sha256(data).hexdigest()
            declaration, source = declarations[name], sources[name]
            if digest != declaration["sha256"] or digest != source["sha256"]:
                raise ValueError("SHA256 mismatch: " + name)
            with Image.open(io.BytesIO(data)) as img:
                if img.format != "PNG" or img.mode != "RGBA":
                    raise ValueError("Expected RGBA PNG: " + name)
                img.load()
                size = img.size
                bbox = img.getchannel("A").getbbox()
                extrema = img.getchannel("A").getextrema()
            if (size != (declaration["width"], declaration["height"])
                    or size != (source["width"], source["height"])):
                raise ValueError("Dimension mismatch: " + name)
            if (not bbox or extrema != (0, 255)
                    or list(bbox) != source["alpha_bbox"]):
                raise ValueError("Alpha/bounds mismatch: " + name)
            verified.append((name, data, digest, size, bbox))
    return dest, verified


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("zip", type=Path, help="DeX-Kids-Adventure-art-v1.zip")
    parser.add_argument("--repo-root", type=Path,
                        default=Path(__file__).resolve().parents[1])
    parser.add_argument("--verify-only", action="store_true")
    args = parser.parse_args()
    dest, verified = inspect(args.zip, args.repo_root)
    if not args.verify_only:
        for name, _data, digest, _size, _bbox in verified:
            target = dest / name
            if target.exists() and hashlib.sha256(target.read_bytes()).hexdigest() != digest:
                raise ValueError("Different destination asset exists: " + str(target))
        for name, data, _digest, _size, _bbox in verified:
            target = dest / name
            if target.exists():
                continue
            fd, temp = tempfile.mkstemp(prefix=".t048-", dir=dest)
            try:
                with os.fdopen(fd, "wb") as out:
                    out.write(data)
                os.replace(temp, target)
            finally:
                if os.path.exists(temp):
                    os.unlink(temp)
    for name, _data, digest, size, bbox in verified:
        print(f"{name}: SHA256={digest} RGBA={size[0]}x{size[1]} alpha_bbox={bbox}")
    print("T-048: 5/5 SOURCE VERIFIED" +
          (" (no files written)" if args.verify_only else " and imported"))


if __name__ == "__main__":
    main()
