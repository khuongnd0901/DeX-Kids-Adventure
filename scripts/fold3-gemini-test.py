#!/usr/bin/env python3
"""Explicit live Gemini QA on physical Android; secrets travel only over stdin."""
import argparse
import asyncio
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

async def run_validation():
    command = adb + ['shell', 'am', 'instrument', '-w', '-r',
        '-e', 'display_id', str(args.display), '-e', 'mode',
        'creative_live' if args.creative else 'gemini_context' if args.context else 'gemini_live',
        'com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner']
    process = await asyncio.create_subprocess_exec(*command,
        stdout=asyncio.subprocess.PIPE, stderr=asyncio.subprocess.STDOUT)
    deadline = asyncio.get_running_loop().time() + (2700 if args.creative else 240 if args.context else 90)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    output = []
    try:
        with args.output.open('w') as log:
            while True:
                remaining = deadline - asyncio.get_running_loop().time()
                if remaining <= 0:
                    raise TimeoutError('Host QA timeout; allow device cleanup before another test')
                line = await asyncio.wait_for(process.stdout.readline(), remaining)
                if not line:
                    break
                # Redact before both durable evidence and live progress output.
                safe = line.decode(errors='replace').replace(env['GEMINI_API_KEY'], '[REDACTED]')
                output.append(safe)
                log.write(safe)
                log.flush()
                print(safe, end='', flush=True)
        await asyncio.wait_for(process.wait(), max(1, deadline - asyncio.get_running_loop().time()))
        return ''.join(output)
    finally:
        if process.returncode is None:
            process.terminate()
            await process.wait()

try:
    subprocess.run(adb + ['shell', 'run-as', 'com.khuongnd.dexkids', 'sh', '-c',
        "'umask 077; cat > " + fixture + "'"], input=secret, check=True, capture_output=True)
    output = asyncio.run(run_validation())
    raise SystemExit(0 if 'qa_status=PASS' in output else 1)
finally:
    subprocess.run(adb + ['shell', 'run-as', 'com.khuongnd.dexkids', 'rm', '-f', fixture],
                   capture_output=True)
