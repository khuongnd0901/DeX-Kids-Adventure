#!/usr/bin/env python3
"""Emulator-only refused-location/offline tests using real framework UI actions, never permission grants."""
import argparse, datetime, json, os, re, subprocess, time
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--serial',required=True);a=p.parse_args()
binary=os.environ.get('ADB_BIN','adb');package='com.khuongnd.dexkids';root=Path(__file__).resolve().parents[2]
out=root/'build/simulator-artifacts'/('privacy-'+datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ'));out.mkdir(parents=True);results=[]
def adb(*args):
 return subprocess.check_output([binary,'-s',a.serial,*args],stderr=subprocess.STDOUT).decode(errors='replace').replace('\r','')
def host(path):
 return subprocess.check_output(['wslpath','-w',str(path)],text=True).strip() if binary.endswith('.exe') else str(path)
def record(id,status,actual):
 results.append(dict(id=id,status=status,actual=actual,evidence=str(out)));(out/'results.json').write_text(json.dumps(results,indent=2)+'\n')
def instrument(mode):
 adb('shell','am','force-stop',package)  # Only this app; closes stale QA permission requests.
 text=adb('shell','am','instrument','-w','-e','mode',mode,package+'.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner')
 (out/(mode+'-instrumentation.txt')).write_text(text)
 assert re.search(r'^INSTRUMENTATION_RESULT: qa_status=PASS$',text,re.M), 'Real instrumentation FAIL: '+mode
 return text
assert adb('shell','getprop','ro.kernel.qemu').strip()=='1','BLOCKED: emulator required'
print('Artifacts:',out,flush=True)
plane=adb('shell','cmd','connectivity','airplane-mode').strip();wifi=adb('shell','cmd','wifi','status').startswith('Wifi is enabled')
before=adb('shell','dumpsys','package',package);(out/'package-before.txt').write_text(before)
try:
 test_apk=root/'android/build/outputs/apk/androidTest/debug/android-debug-androidTest.apk'
 assert test_apk.is_file(),'BLOCKED: build :android:assembleDebugAndroidTest first'
 installed=adb('install','-r',host(test_apk));(out/'test-install.txt').write_text(installed)
 assert re.search(r'^Success$',installed,re.M),'Test APK install failed'
 assert 'android.permission.INTERNET' not in before and 'android.permission.RECORD_AUDIO' not in before
 record('PRIV-MANIFEST','PASS','Installed package requests neither INTERNET nor RECORD_AUDIO')
 if 'android.permission.ACCESS_FINE_LOCATION: granted=false' in before and 'android.permission.ACCESS_COARSE_LOCATION: granted=true' not in before:
  actual=instrument('permission');after=adb('shell','dumpsys','package',package);(out/'package-after-refusal.txt').write_text(after)
  assert 'android.permission.ACCESS_FINE_LOCATION: granted=false' in after,'Unexpected location grant'
  adb('pull','/sdcard/Android/data/'+package+'/files/qa-permission',host(out/'permission-screenshots'))
  assert (out/'permission-screenshots/permission-refused.png').stat().st_size>0
  record('PRIV-DENIED','PASS','Actual parent Button.performClick; only permission deny node ACTION_CLICK (resource/text identity); fine/coarse denied; callback explanation survives resume. '+actual)
 else:
  record('PRIV-DENIED','BLOCKED','Existing grant preserved; no revoke/regrant performed')
 adb('shell','cmd','connectivity','airplane-mode','enable');adb('shell','cmd','wifi','set-wifi-enabled','disabled');time.sleep(2)
 assert adb('shell','cmd','connectivity','airplane-mode').strip()=='enabled'
 assert adb('shell','cmd','wifi','status').startswith('Wifi is disabled')
 deadline=time.monotonic()+15
 while True:
  connectivity=adb('shell','dumpsys','connectivity')
  if 'Active default network: none' in connectivity or time.monotonic()>=deadline: break
  time.sleep(1)
 (out/'offline-connectivity.txt').write_text(connectivity)
 assert 'Active default network: none' in connectivity,'Airplane/Wi-Fi-off did not remove active default network'
 instrument('recreation')
 adb('pull','/sdcard/Android/data/'+package+'/files/qa-recreation',host(out/'offline-screenshots'))
 assert (out/'offline-screenshots/before.png').stat().st_size>0 and (out/'offline-screenshots/after.png').stat().st_size>0
 record('PRIV-OFFLINE','PASS','Airplane/Wi-Fi-off; no active default network; explicit parent demo preview/recreation rendered actual PNGs. No live GPS/audio validation.')
except Exception as e:
 record('PRIV-RUN','FAIL',str(e));raise
finally:
 current=adb('shell','dumpsys','package',package)
 for permission in ('ACCESS_FINE_LOCATION','ACCESS_COARSE_LOCATION'):
  granted=f'android.permission.{permission}: granted=true'
  if granted not in before and granted in current:
   adb('shell','pm','revoke',package,'android.permission.'+permission)
   record('PRIV-UNEXPECTED-GRANT','FAIL',permission+' unexpectedly granted; revoked to original denied state')
 adb('shell','cmd','connectivity','airplane-mode','enable' if plane=='enabled' else 'disable')
 adb('shell','cmd','wifi','set-wifi-enabled','enabled' if wifi else 'disabled')
 (out/'restored-radio-state.txt').write_text(adb('shell','cmd','connectivity','airplane-mode')+adb('shell','cmd','wifi','status'))
 (out/'package-final.txt').write_text(adb('shell','dumpsys','package',package))
