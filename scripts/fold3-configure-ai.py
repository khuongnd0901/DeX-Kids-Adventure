#!/usr/bin/env python3
"""Apply explicitly parent-authorized Gemini setup to a local debug Fold3.

Requires an explicit consent flag and confirmed Free Tier; never sends a request.
Secrets enter app-private storage over stdin, then Android Keystore encrypts them.
"""
import argparse
import json
from pathlib import Path
import subprocess

p = argparse.ArgumentParser()
p.add_argument('serial')
p.add_argument('display', type=int)
p.add_argument('output', type=Path)
p.add_argument('--parent-authorized-child-text', action='store_true', required=True)
p.add_argument('--confirmed-free-tier', action='store_true', required=True)
a = p.parse_args()
if a.display <= 0:
    p.error('Explicit external DeX display required')
env = {}
for row in Path('.env').read_text().splitlines():
    if row.strip() and not row.lstrip().startswith('#') and '=' in row:
        name, value = row.split('=', 1)
        env[name.removeprefix('export ').strip()] = value.strip().strip('\"\'')
key = env['GEMINI_API_KEY']
payload = json.dumps({'key': key, 'model': env['GEMINI_MODEL'],
    'parent_authorized_child_text': True, 'free_tier_acknowledged': True}).encode()
adb = ['adb', '-s', a.serial]
fixture = 'files/qa-parent-ai-secret.json'
try:
    subprocess.run(adb + ['shell', 'run-as', 'com.khuongnd.dexkids', 'sh', '-c',
        "'umask 077; cat > " + fixture + "'"], input=payload, capture_output=True, check=True)
    r = subprocess.run(adb + ['shell', 'am', 'instrument', '-w', '-r',
        '-e', 'display_id', str(a.display), '-e', 'mode', 'configure_parent_ai',
        'com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner'],
        capture_output=True, text=True, timeout=60)
    output = (r.stdout + r.stderr).replace(key, '[REDACTED]')
    a.output.parent.mkdir(parents=True, exist_ok=True)
    a.output.write_text(output)
    print(output)
    raise SystemExit(0 if 'qa_status=PASS' in output and 'gemini_ready=true' in output else 1)
finally:
    subprocess.run(adb + ['shell', 'run-as', 'com.khuongnd.dexkids', 'rm', '-f', fixture],
        capture_output=True)
