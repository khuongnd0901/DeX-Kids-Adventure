#!/usr/bin/env python3
"""Local Windows-emulator story soak; synthetic sourced-center fixes, never AI HTTP.

Build/install APKs first. Explicitly grant test GPS permissions and disable device
network before starting; restore original emulator settings after verification.
"""
import argparse
import hashlib
import json
import math
from pathlib import Path
import subprocess
import threading
import time

p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--adb', required=True)
p.add_argument('--port', default='5038')
p.add_argument('--serial', default='emulator-5554')
p.add_argument('--out', type=Path, required=True)
a = p.parse_args()
if not a.serial.startswith('emulator-'):
    p.error('Only an explicitly selected Android emulator is allowed')
adb = [a.adb, '-P', a.port, '-s', a.serial]
def query(*args):
    return subprocess.run(adb + list(args), check=True, capture_output=True, text=True, timeout=15).stdout.strip()
if query('shell', 'getprop', 'ro.hardware') not in ('ranchu', 'goldfish'):
    p.error('Selected target is not an Android emulator')
if 'Active default network: none' not in query('shell', 'dumpsys', 'connectivity'):
    p.error('Disable emulator network explicitly before offline replay soak')
root = Path(__file__).resolve().parent.parent
rows = (root / 'assets/poi/sample-hcm.tsv').read_text().splitlines()
center = next(line.split('\t') for line in rows if line.startswith('osm:way:500888743\t'))
lat_center, lon_center = float(center[2]), float(center[3])
a.out.mkdir(parents=True, exist_ok=True)
metadata = {'serial': a.serial, 'sdk': query('shell', 'getprop', 'ro.build.version.sdk'),
            'size': query('shell', 'wm', 'size'), 'synthetic_center_source': center[0],
            'offline': True, 'api_dispatches_by_driver': 0}
metadata['apk_sha256'] = {str(path.relative_to(root)): hashlib.sha256(path.read_bytes()).hexdigest()
    for path in (root / 'android/build/outputs/apk').rglob('*.apk')}
installed_path = query('shell', 'pm', 'path', 'com.khuongnd.dexkids').splitlines()[0].removeprefix('package:')
installed_hash = query('shell', 'sha256sum', installed_path).split()[0]
local_hash = hashlib.sha256((root / 'android/build/outputs/apk/debug/android-debug.apk').read_bytes()).hexdigest()
if installed_hash != local_hash:
    p.error('Installed APK differs from local candidate; install it before running the soak')
metadata['installed_apk_sha256'] = installed_hash
metadata['source_commit'] = subprocess.run(['git', 'rev-parse', 'HEAD'], cwd=root,
    capture_output=True, text=True, check=True).stdout.strip()
(a.out / 'environment.json').write_text(json.dumps(metadata, indent=2))
stop = threading.Event()
started = time.monotonic()
errors = []
def gps():
    try:
        with (a.out / 'gps-fixes.jsonl').open('w') as log:
            index = 0
            while not stop.is_set():
                elapsed = time.monotonic() - started
                angle = elapsed * 2 * math.pi / 120
                lat = lat_center + .0004 * math.sin(angle)
                lon = lon_center + .0005 * math.cos(angle)
                result = query('emu', 'geo', 'fix', f'{lon:.7f}', f'{lat:.7f}')
                if 'OK' not in result:
                    raise RuntimeError('Emulator rejected synthetic fix')
                index += 1
                log.write(json.dumps({'elapsed_s': elapsed, 'lat': lat, 'lon': lon,
                    'synthetic': True, 'source_center': center[0]}) + '\n')
                log.flush()
                (a.out / 'progress.json').write_text(json.dumps({'elapsed_s': elapsed, 'fixes': index}))
                stop.wait(2)
    except Exception as error:
        errors.append(type(error).__name__ + ': ' + str(error))
thread = threading.Thread(target=gps)
thread.start()
try:
    with (a.out / 'instrumentation.log').open('w') as log:
        subprocess.run(adb + ['shell', 'am', 'instrument', '-w', '-e', 'mode', 't059_story_live_soak',
            '-e', 'display_id', '0',
            'com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner'],
            stdout=log, stderr=subprocess.STDOUT, check=True, timeout=1900)
finally:
    stop.set()
    thread.join()
text = (a.out / 'instrumentation.log').read_text()
passed = ('actual_duration_ms=' in text and 'qa_status=FAIL' not in text and
          'shortMsg=Process crashed' not in text and not errors)
(a.out / 'driver-result.json').write_text(json.dumps({'completed': passed,
    'elapsed_s': time.monotonic() - started, 'gps_errors': errors}, indent=2))
if not passed:
    raise SystemExit('FAIL: inspect instrumentation.log; no acceptance inferred from adb exit code')
print('Completed local soak; inspect metrics, screenshots and limitations before acceptance.')
