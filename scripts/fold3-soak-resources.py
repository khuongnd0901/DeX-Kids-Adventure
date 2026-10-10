#!/usr/bin/env python3
"""Run physical one-hour DEMO, retain only numeric resources and app render logs."""
import argparse, json, re, subprocess, time
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('serial'); p.add_argument('display',type=int); p.add_argument('output',type=Path)
a=p.parse_args()
if a.display <= 0: p.error('External display required')
a.output.mkdir(parents=True,exist_ok=True)
adb=['adb','-s',a.serial]
def shell(*cmd):
    return subprocess.run(adb+['shell',*cmd],capture_output=True,text=True,timeout=20).stdout
start=time.monotonic()
# Limit log output to application render tag and QA progress; no broad device dump.
logs=open(a.output/'render.log','w')
logcat=subprocess.Popen(adb+['logcat','-T','1','-v','brief','RenderMetrics:I','Fold3QA:I','*:S'],stdout=logs,stderr=subprocess.DEVNULL)
try:
    with open(a.output/'physical-soak.log','w') as result, open(a.output/'resources.jsonl','w') as resources:
        test=subprocess.Popen(adb+['shell','am','instrument','-w','-r','-e','display_id',str(a.display),'-e','mode','physical_soak','com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner'],stdout=result,stderr=subprocess.STDOUT)
        while test.poll() is None:
            mem=shell('dumpsys','meminfo','com.khuongnd.dexkids')
            battery=shell('dumpsys','battery'); thermal=shell('dumpsys','thermalservice')
            cpu=shell('dumpsys','cpuinfo')
            pss=re.search(r'TOTAL PSS:\s*(\d+)',mem) or re.search(r'^\s*TOTAL\s+(\d+)',mem,re.M)
            row={'elapsed_seconds':round(time.monotonic()-start,2),'pss_kb':int(pss[1]) if pss else None}
            for name in ('level','temperature'):
                match=re.search(r'^\s*'+name+r':\s*(\d+)',battery,re.M)
                row[name]=int(match[1]) if match else None
            match=re.search(r'Thermal Status:\s*(\d+)',thermal)
            row['thermal_status']=int(match[1]) if match else None
            match=re.search(r'([\d.]+)%\s+\d+/com\.khuongnd\.dexkids:',cpu)
            row['cpu_percent']=float(match[1]) if match else None
            resources.write(json.dumps(row)+'\n');resources.flush()
            if time.monotonic()-start>3700:
                test.terminate(); raise RuntimeError('Soak timeout; inspect duration preference before cleanup')
            time.sleep(30)
finally:
    logcat.terminate();logcat.wait(timeout=10);logs.close()
result_text = (a.output/'physical-soak.log').read_text()
print(result_text)
raise SystemExit(0 if 'qa_status=PASS' in result_text else 1)
