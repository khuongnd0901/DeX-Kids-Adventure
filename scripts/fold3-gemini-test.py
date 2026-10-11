#!/usr/bin/env python3
"""Explicit live Gemini QA on physical Android; secrets travel only over stdin."""
import argparse
import json
from pathlib import Path
import subprocess

parser = argparse.ArgumentParser()
parser.add_argument('serial')
parser.add_argument('display', type=int)
parser.add_argument('output', type=Path)
parser.add_argument('--context', action='store_true', help='Test sourced POIs, four day periods and synthetic replies')
parser.add_argument('--creative', action='store_true', help='Test creative generation and review at ages 4–10')
args = parser.parse_args()
if args.display <= 0:
    parser.error('An external DeX display is required')
env = {}
for row in Path('.env').read_text().splitlines():
    row = row.strip()
    if row and not row.startswith('#') and '=' in row:
        name, value = row.split('=', 1)
        env[name.removeprefix('export ').strip()] = value.strip().strip('\"\'')
secret = json.dumps({'key': env['GEMINI_API_KEY'], 'model': env['GEMINI_MODEL']}).encode()
adb = ['adb', '-s', args.serial]
fixture = 'files/qa-gemini-secret.json'
try:
    subprocess.run(adb + ['shell', 'run-as', 'com.khuongnd.dexkids', 'sh', '-c',
        "'umask 077; cat > " + fixture + "'"], input=secret, check=True, capture_output=True)
    result = subprocess.run(adb + ['shell', 'am', 'instrument', '-w', '-r',
        '-e', 'display_id', str(args.display), '-e', 'mode', 'creative_live' if args.creative else 'gemini_context' if args.context else 'gemini_live',
        'com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner'],
        capture_output=True, text=True, timeout=1800 if args.creative else 240 if args.context else 90)
    # Defense in depth: never store credential even if a future runner prints it.
    output = (result.stdout + result.stderr).replace(env['GEMINI_API_KEY'], '[REDACTED]')
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(output)
    print(output)
    raise SystemExit(0 if 'qa_status=PASS' in output else 1)
finally:
    subprocess.run(adb + ['shell', 'run-as', 'com.khuongnd.dexkids', 'rm', '-f', fixture],
                   capture_output=True)
