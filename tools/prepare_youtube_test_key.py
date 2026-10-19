#!/usr/bin/env python3
"""Explicit debug-only provisioning. Never print secret values or raw exceptions."""
import argparse
import json
import re
from pathlib import Path


def read_key(path: Path) -> str:
    candidates = []
    for raw in path.read_text(encoding="utf-8-sig").splitlines():
        row = raw.strip()
        if row.startswith("export "):
            row = row[7:].lstrip()
        if "=" not in row:
            continue
        name, value = row.split("=", 1)
        if name.strip() != "YOUTUBE_KEY":
            continue
        value = value.strip()
        if value.startswith(('"', "'")):
            quote = value[0]
            end = value.find(quote, 1)
            if end < 0 or (value[end + 1:].strip() and not value[end + 1:].strip().startswith("#")):
                raise ValueError("Invalid YOUTUBE_KEY syntax")
            value = value[1:end]
        else:
            value = value.split("#", 1)[0].strip()
        candidates.append(value)
    if len(candidates) != 1 or not re.fullmatch(r"[A-Za-z0-9_-]{20,200}", candidates[0]):
        raise ValueError("Missing, duplicate or invalid YOUTUBE_KEY")
    return candidates[0]


def prepare(env_path: Path, output: Path, inject: bool) -> None:
    # Always delete first: disabled or failed regeneration must never keep a stale key.
    target = output / "youtube-test-key.json"
    target.unlink(missing_ok=True)
    if not inject:
        return
    key = read_key(env_path)
    output.mkdir(parents=True, exist_ok=True)
    target.touch(mode=0o600)
    target.write_text(json.dumps({"key": key}, separators=(",", ":")), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--env", type=Path, default=Path(".env"))
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--inject", action="store_true")
    args = parser.parse_args()
    try:
        prepare(args.env, args.output, args.inject)
    except Exception:
        print("YouTube test-key preparation failed; no secret values logged.")
        return 1
    print("YouTube debug key prepared (private test APK)." if args.inject else "YouTube debug key not packaged.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
