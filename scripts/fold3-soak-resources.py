#!/usr/bin/env python3
"""Run physical DEMO or reviewed creative playback; retain numeric resources/app logs."""
import argparse, json, re, subprocess, time
import xml.etree.ElementTree as ET
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('serial'); p.add_argument('display',type=int); p.add_argument('output',type=Path)
p.add_argument('--creative',action='store_true',help='Use real reviewed pack in offline creative playback')
p.add_argument('--seconds',type=int,default=1805,help='Creative runtime (30–3600; >=1800 for endurance)')
p.add_argument('--live-refill',action='store_true',help='Authorized .env Gemini new-trip refresh and exhaustion refill')
p.add_argument('--matrix',action='store_true',help='Also verify actual 6/7 and 8/10 creative packs under the shared limiter')
a=p.parse_args()
if a.display <= 0: p.error('External display required')
if not 30 <= a.seconds <= 3600: p.error('Creative seconds must be 30–3600')
if a.live_refill and not a.creative: p.error('--live-refill requires --creative')
if a.matrix and not a.live_refill: p.error('--matrix requires --live-refill')
a.output.mkdir(parents=True,exist_ok=True)
adb=['adb','-s',a.serial]
fixture='files/qa-t031-runtime-secret.json'
if a.live_refill:
    env={}
    for row in Path('.env').read_text().splitlines():
        row=row.strip()
        if row and not row.startswith('#') and '=' in row:
            name,value=row.split('=',1);env[name.removeprefix('export ').strip()]=value.strip().strip('\"\'')
    subprocess.run(adb+['shell','run-as','com.khuongnd.dexkids','sh','-c',"'umask 077; cat > "+fixture+"'"],
        input=json.dumps({'key':env['GEMINI_API_KEY'],'model':env['GEMINI_MODEL']}).encode(),capture_output=True,check=True)
def shell(*cmd):
    return subprocess.run(adb+['shell',*cmd],capture_output=True,text=True,timeout=20).stdout
start=time.monotonic()
# Limit log output to application render tag and QA progress; no broad device dump.
logs=open(a.output/'render.log','w')
logcat=subprocess.Popen(adb+['logcat','-T','1','-v','brief','RenderMetrics:I','Fold3QA:I','*:S'],stdout=logs,stderr=subprocess.DEVNULL)
try:
    with open(a.output/'physical-soak.log','w') as result, open(a.output/'resources.jsonl','w') as resources:
        mode='creative_endurance' if a.creative else 'physical_soak'
        test=subprocess.Popen(adb+['shell','am','instrument','-w','-r','-e','display_id',str(a.display),'-e','mode',mode,
            '-e','creative_seconds',str(a.seconds),'-e','creative_network','true' if a.live_refill else 'false',
            '-e','creative_matrix','true' if a.matrix else 'false',
            'com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner'],stdout=result,stderr=subprocess.STDOUT)
        while test.poll() is None:
            mem=shell('dumpsys','meminfo','com.khuongnd.dexkids')
            battery=shell('dumpsys','battery'); thermal=shell('dumpsys','thermalservice')
            # Samsung dumpsys cpuinfo may contain only stale, pre-launch process data.
            # Sample the current package PID over one second;100% means one CPU core.
            pid=shell('pidof','com.khuongnd.dexkids').strip()
            cpu=shell('top','-b','-n','2','-d','1','-p',pid) if re.fullmatch(r'[0-9]+',pid) else ''
            pss=re.search(r'TOTAL PSS:\s*(\d+)',mem) or re.search(r'^\s*TOTAL\s+(\d+)',mem,re.M)
            row={'elapsed_seconds':round(time.monotonic()-start,2),'pss_kb':int(pss[1]) if pss else None}
            for name in ('level','temperature'):
                match=re.search(r'^\s*'+name+r':\s*(\d+)',battery,re.M)
                row[name]=int(match[1]) if match else None
            match=re.search(r'Thermal Status:\s*(\d+)',thermal)
            row['thermal_status']=int(match[1]) if match else None
            cpu_rows=[line.split() for line in cpu.splitlines()
                if re.match(r'^\s*'+re.escape(pid)+r'\s',line)] if pid else []
            row['cpu_percent']=None
            if cpu_rows and len(cpu_rows[-1])>=12 and cpu_rows[-1][-1]=='com.khuongnd.dexkids':
                try: row['cpu_percent']=float(cpu_rows[-1][8])
                except ValueError: pass
            row['cpu_source']='current_pid_top_1s' if row['cpu_percent'] is not None else 'unavailable'
            row['cpu_scale']='100_percent_per_core'
            if a.live_refill:
                rate=shell('run-as','com.khuongnd.dexkids','cat','shared_prefs/kids_ai_rate_v1.xml')
                root=ET.fromstring(rate)
                starts=root.find("./string[@name='starts']")
                row['dispatch_reservations_ms']=[int(v) for v in (starts.text or '').split(',') if v] if starts is not None else []
            resources.write(json.dumps(row)+'\n');resources.flush()
            if time.monotonic()-start>(a.seconds+90 if a.creative else 3700):
                test.terminate(); raise RuntimeError('Soak timeout; inspect duration preference before cleanup')
            time.sleep(30)
finally:
    logcat.terminate();logcat.wait(timeout=10);logs.close()
    if a.live_refill:
        subprocess.run(adb+['shell','run-as','com.khuongnd.dexkids','rm','-f',fixture],capture_output=True)
result_text = (a.output/'physical-soak.log').read_text()
print(result_text)
raise SystemExit(0 if 'qa_status=PASS' in result_text else 1)
