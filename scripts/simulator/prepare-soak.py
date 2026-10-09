#!/usr/bin/env python3
"""Prepare a synthetic 60-minute parent-settings fixture on the explicit emulator only."""
import argparse, datetime, os, re, subprocess, time, xml.etree.ElementTree as ET
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__); p.add_argument('--serial',required=True); a=p.parse_args()
b=os.environ.get('ADB_BIN','adb'); pkg='com.khuongnd.dexkids'; root=Path(__file__).resolve().parents[2]
out=root/'build/simulator-artifacts'/('soak-setup-'+datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')); out.mkdir(parents=True)
def run(*args,check=True,input=None):
 return subprocess.run([b,'-s',a.serial,*args],input=input,capture_output=True,check=check)
def text(*args): return run(*args).stdout.decode(errors='replace').replace('\r','')
def host(p): return subprocess.check_output(['wslpath','-w',str(p)],text=True).strip() if b.endswith('.exe') else str(p)
assert text('get-state').strip()=='device'
assert text('shell','getprop','ro.kernel.qemu').strip()=='1', 'Fixture setup allowed on emulator only'
installed=run('install','-r',host(root/'android/build/outputs/apk/debug/android-debug.apk'))
(out/'install.txt').write_bytes(installed.stdout+installed.stderr)
assert re.search(rb'^Success\r?$',installed.stdout,re.M), 'Debug APK install failed; see install.txt'
run('shell','am','force-stop',pkg)
original=run('shell','run-as',pkg,'cat','shared_prefs/parent_settings.xml',check=False)
(out/'original-exists.txt').write_text(str(original.returncode==0)+'\n')
if original.returncode==0: (out/'original-parent-settings.xml').write_bytes(original.stdout)
fixture=b'<?xml version="1.0" encoding="utf-8"?>\n<map><int name="session_minutes" value="60"/><int name="age" value="4"/><boolean name="quiet" value="true"/><boolean name="offline_tts" value="false"/></map>\n'
(out/'synthetic-parent-settings.xml').write_bytes(fixture)
run('shell','run-as',pkg,'mkdir','-p','shared_prefs')
run('shell','run-as',pkg,'tee','shared_prefs/parent_settings.xml',input=fixture)
run('shell','wm','size','1920x1080'); run('shell','wm','density','160')
run('shell','am','start','-W','--activity-clear-top','-n',pkg+'/.ParentActivity'); time.sleep(2)
run('shell','uiautomator','dump','/sdcard/dexkids-soak-ui.xml');run('pull','/sdcard/dexkids-soak-ui.xml',host(out/'parent-ui.xml'))
ui=ET.parse(out/'parent-ui.xml'); nodes=list(ui.iter('node'))
assert any('Session limit: 60 minutes' in n.get('text','') for n in nodes),'Parent fixture not loaded'
button=next(n for n in nodes if n.get('text','').casefold()=='parent preview on this phone (explicit)')
x1,y1,x2,y2=map(int,re.findall(r'\d+',button.get('bounds')))
(out/'launch-request-utc.txt').write_text(datetime.datetime.now(datetime.timezone.utc).isoformat()+'\n')
run('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))
for _ in range(20):
 state=text('shell','dumpsys','activity','activities')
 if re.search(r'(?:topResumedActivity|ResumedActivity).*com.khuongnd.dexkids/\.KidsActivity',state): break
 time.sleep(.2)
else: raise RuntimeError('Child did not resume')
(out/'child-start-utc.txt').write_text(datetime.datetime.now(datetime.timezone.utc).isoformat()+'\n')
(out/'README.txt').write_text('Synthetic emulator fixture: 60-minute existing session limit, quiet ON, TTS OFF.\nOriginal prefs retained here. Restore using restore-soak.py --serial SERIAL --setup THIS_DIRECTORY after collection.\nThis is test configuration, not consent or feature acceptance.\n')
print(out,flush=True)
