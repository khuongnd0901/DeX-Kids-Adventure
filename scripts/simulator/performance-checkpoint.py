#!/usr/bin/env python3
"""Run existing scoped Android endurance QA and collect process-only metrics.

Requires an explicit external display. Never uploads data or changes permissions.
The instrumentation backs up/restores its temporary preferences/cache.
"""
import argparse
import hashlib
import json
import re
import subprocess
import time
from pathlib import Path

p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--adb',default='adb')
p.add_argument('--serial',required=True)
p.add_argument('--display',type=int,required=True)
p.add_argument('--seconds',type=int,default=600)
p.add_argument('--out',type=Path,required=True)
a=p.parse_args()
if a.display<=0 or not 30<=a.seconds<=3600:p.error('Explicit external display and 30–3600 seconds required')
if a.out.exists():p.error('Output already exists; preserve previous evidence')
adb=[a.adb,'-s',a.serial]
def read(*args):
    r=subprocess.run(adb+list(args),capture_output=True,text=True,timeout=25)
    if r.returncode:raise RuntimeError('ADB command failed: '+args[0])
    return r.stdout
info=read('shell','dumpsys','display')
match=re.search(r'DisplayInfo\{"Desktop", displayId '+str(a.display)+r',[^\n]+',info)
if not match or 'real 1920 x 1080' not in match.group():
    raise SystemExit('Selected display is not the verified 1920x1080 Samsung Desktop')
package=read('shell','pm','path','com.khuongnd.dexkids').strip().removeprefix('package:')
if not package.startswith('/data/app/') or '\n' in package:raise SystemExit('Unexpected package path')
apk_sha=read('shell','sha256sum',package).split()[0]
a.out.mkdir(parents=True)
(a.out/'environment.json').write_text(json.dumps({'apk_sha256':apk_sha,
    'display_id':a.display,'display':'Samsung Desktop 1920x1080','seconds':a.seconds,
    'model':read('shell','getprop','ro.product.model').strip(),
    'firmware':read('shell','getprop','ro.build.fingerprint').strip()},indent=2)+'\n')
with (a.out/'instrumentation.txt').open('w') as log:
    process=subprocess.Popen(adb+['shell','am','instrument','-w','-e','mode','creative_endurance',
        '-e','display_id',str(a.display),'-e','creative_seconds',str(a.seconds),
        'com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner'],stdout=log,stderr=subprocess.STDOUT)
    start=time.monotonic();samples=[]
    while process.poll() is None:
        row={'elapsed_s':round(time.monotonic()-start,3)}
        try:
            memory=read('shell','dumpsys','meminfo','com.khuongnd.dexkids')
            row['native_heap_details']=[x.strip() for x in memory.splitlines() if re.match(r'\s*Native Heap\s+\d',x)]
            row['memory']=[x.strip() for x in memory.splitlines() if re.search(
                r'Java Heap:|Native Heap:|Graphics:|TOTAL PSS:|TOTAL RSS:|GL mtrack:|EGL mtrack:',x)]
            cpu=read('shell','top','-b','-n','1','-o','PID,%CPU,RES,ARGS')
            row['cpu']=[x.strip() for x in cpu.splitlines() if x.rstrip().endswith('com.khuongnd.dexkids')]
            battery=read('shell','dumpsys','battery')
            row['battery_temperature_tenths_c']=next((int(x.split(':')[1]) for x in battery.splitlines() if x.strip().startswith('temperature:')),None)
        except (RuntimeError,subprocess.TimeoutExpired) as e:
            row['sampling_error']=type(e).__name__
        samples.append(row)
        (a.out/'measurements.json').write_text(json.dumps(samples,indent=2)+'\n')
        if time.monotonic()-start>a.seconds+180:
            print('Instrumentation did not settle; collection stopped, remote QA may still be active.',flush=True)
            raise SystemExit(2)
        # 20-second sampling, responsive between calls; no long blocking tool wait.
        for _ in range(20):
            if process.poll() is not None:break
            time.sleep(1)
    process_ids={x.split()[0] for sample in samples for x in sample.get('cpu',[])}
    for pid in process_ids:
        try:
            metrics=read('logcat','-d','--pid',pid,'-s','RenderMetrics:I','*:S')
            (a.out/('render-metrics-'+pid+'.txt')).write_text(metrics)
        except (RuntimeError,subprocess.TimeoutExpired):pass
    result=(a.out/'instrumentation.txt').read_text()
    print(result[-2400:],flush=True)
    raise SystemExit(0 if 'qa_status=PASS' in result else 1)
