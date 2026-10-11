#!/usr/bin/env python3
"""Physical DeX QA, synthetic downstream POIs. No GPS injection/cloud/child recording."""
import argparse,json,re,subprocess,time
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('serial');p.add_argument('display',type=int);p.add_argument('output',type=Path)
p.add_argument('--smoke',action='store_true')
a=p.parse_args()
if a.display<=0:p.error('Explicit external DeX display required')
a.output.mkdir(parents=True,exist_ok=True)
adb=['adb','-s',a.serial]
def call(args,**kw):return subprocess.run(adb+args,capture_output=True,timeout=20,**kw)
def shell(*args):
 r=call(['shell',*args],text=True)
 if r.returncode:raise RuntimeError('ADB command failed: '+str(args))
 return r.stdout
if 'SM-F926' not in shell('getprop','ro.product.model'):raise RuntimeError('Z Fold3 required')
for name in ['parent_settings','kids_ai_settings_v1']:
 r=call(['exec-out','run-as','com.khuongnd.dexkids','cat','shared_prefs/'+name+'.xml'])
 if r.returncode:raise RuntimeError('Cannot back up preferences')
 (a.output/(name+'.before.xml')).write_bytes(r.stdout)
mode='route_30_smoke' if a.smoke else 'route_30min'
with open(a.output/'render.log','w') as log,open(a.output/'instrumentation.log','w') as output,open(a.output/'resources.jsonl','w') as resources:
 logs=subprocess.Popen(adb+['logcat','-T','1','-v','threadtime','RenderMetrics:I','Route30QA:I','OfflinePOI:I','PoiScene:I','OfflineTTS:I','KidsSession:I','*:S'],stdout=log,stderr=subprocess.DEVNULL)
 test=None;start=time.monotonic();next_sample=0
 try:
  test=subprocess.Popen(adb+['shell','am','instrument','-w','-r','-e','display_id',str(a.display),'-e','mode',mode,'com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner'],stdout=output,stderr=subprocess.STDOUT)
  while test.poll() is None:
   elapsed=time.monotonic()-start
   if elapsed>(180 if a.smoke else 1900):
    test.terminate();raise RuntimeError('QA timeout; cleanup/restore needs inspection')
   if elapsed>=next_sample:
    mem=shell('dumpsys','meminfo','com.khuongnd.dexkids');battery=shell('dumpsys','battery');thermal=shell('dumpsys','thermalservice')
    match=re.search(r'TOTAL PSS:\s*(\d+)',mem) or re.search(r'^\s*TOTAL\s+(\d+)',mem,re.M)
    row={'elapsed_seconds':round(elapsed,2),'pss_kb':int(match[1]) if match else None}
    for key in ['level','temperature']:
     match=re.search(r'^\s*'+key+r':\s*(\d+)',battery,re.M);row[key]=int(match[1]) if match else None
    match=re.search(r'Thermal Status:\s*(\d+)',thermal);row['thermal_status']=int(match[1]) if match else None
    resources.write(json.dumps(row)+'\n');resources.flush();print(json.dumps(row),flush=True)
    next_sample=elapsed+30
   time.sleep(1)
 finally:
  if test is not None and test.poll() is None:test.terminate()
  logs.terminate();logs.wait(timeout=10)
r=call(['pull','/sdcard/Android/data/com.khuongnd.dexkids/files/qa-route30/points.json',str(a.output/'points.json')],text=True)
if r.returncode:print('Point evidence pull failed',flush=True)
for name in ['parent_settings','kids_ai_settings_v1']:
 r=call(['exec-out','run-as','com.khuongnd.dexkids','cat','shared_prefs/'+name+'.xml'])
 if r.returncode:raise RuntimeError('Cannot verify preference restore')
 (a.output/(name+'.after.xml')).write_bytes(r.stdout)
# XML ordering can change after restoring; compare key/value elements.
import xml.etree.ElementTree as ET
def prefs(path):return sorted(ET.tostring(e,encoding='unicode') for e in ET.parse(path).getroot())
for name in ['parent_settings','kids_ai_settings_v1']:
 if prefs(a.output/(name+'.before.xml'))!=prefs(a.output/(name+'.after.xml')):raise RuntimeError('Preferences differ after test: '+name)
text=(a.output/'instrumentation.log').read_text();print(text,flush=True)
raise SystemExit(0 if 'qa_status=PASS' in text else 1)
