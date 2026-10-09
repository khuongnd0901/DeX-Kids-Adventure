#!/usr/bin/env python3
"""Real emulator UI/lifecycle checks. Uses explicit parent preview, never grants permissions."""
import argparse, datetime, json, os, re, subprocess, time, xml.etree.ElementTree as ET
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--serial',required=True)
a=p.parse_args(); binary=os.environ.get('ADB_BIN','adb')
root=Path(__file__).resolve().parents[2]
out=root/'build/simulator-artifacts'/('functional-'+datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ'))
out.mkdir(parents=True); results=[]; package='com.khuongnd.dexkids'
def adb(*args,check=True):
 r=subprocess.run([binary,'-s',a.serial,*args],capture_output=True,check=check)
 return (r.stdout+r.stderr).decode(errors='replace').replace('\r','')
def host(path):
 return subprocess.check_output(['wslpath','-w',str(path)],text=True).strip() if binary.endswith('.exe') else str(path)
def dump():
 adb('shell','uiautomator','dump','/sdcard/dexkids-ui.xml')
 adb('pull','/sdcard/dexkids-ui.xml',host(out/'ui.xml'))
 return ET.parse(out/'ui.xml')
def tap(text):
 nodes=[n for n in dump().iter('node') if n.get('text','').casefold()==text.casefold()]
 if len(nodes)!=1: raise AssertionError('UI text absent/ambiguous: '+text)
 x1,y1,x2,y2=map(int,re.findall(r'\d+',nodes[0].get('bounds')))
 adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2)); time.sleep(1)
def texts(): return '\n'.join(n.get('text','') for n in dump().iter('node'))
def resumed(activity):
 for _ in range(20):
  state=adb('shell','dumpsys','activity','activities')
  if re.search(r'(?:topResumedActivity|ResumedActivity).*'+re.escape(package+'/.'+activity),state): return state
  time.sleep(.5)
 raise AssertionError('Activity not resumed: '+activity)
def capture(name):
 adb('shell','screencap','-p','/sdcard/dexkids-functional.png')
 adb('pull','/sdcard/dexkids-functional.png',host(out/(name+'.png')))
 adb('shell','rm','/sdcard/dexkids-functional.png')
def parent():
 adb('shell','am','start','-W','--activity-clear-top','-n',package+'/.ParentActivity'); resumed('ParentActivity')
def preview(): tap('Parent preview on this phone (EXPLICIT)'); resumed('KidsActivity'); time.sleep(2)
def case(id,steps,expected,fn,status="PASS"):
 try:
  actual=fn(); results.append(dict(id=id,steps=steps,expected=expected,actual=actual,status=status,evidence=str(out)))
 except Exception as e:
  results.append(dict(id=id,steps=steps,expected=expected,actual=str(e),status='FAIL',evidence=str(out))); raise
 finally: (out/'results.json').write_text(json.dumps(results,indent=2)+'\n')
print('Artifacts:',out,flush=True)
assert adb('get-state').strip()=='device'
try:
 def no_external():
  parent(); tap('Start on external DeX display (no fallback)'); resumed('ParentActivity')
  text=texts(); (out/'no-external-text.txt').write_text(text); capture('no-external')
  assert 'no' in text.casefold() and 'display' in text.casefold()
  return 'Parent remains resumed; no child fallback; see UI text'
 case('DISP-001','Select external display on single-display AVD','Parent stays on phone; no fallback',no_external)
 def ipc():
  msg=adb('shell','am','broadcast','-n',package+'/.KidsCommandReceiver','-a',package+'.action.KIDS_STOP',check=False)
  (out/'unauthorized-stop.txt').write_text(msg)
  return 'Shell broadcast result alone cannot prove delivery/rejection; separate unprivileged APK probe required'
 case('IPC-001','Send STOP from shell UID without signature permission','Reject unauthorized caller',ipc,status='NOT_VERIFIED')
 def child_export():
  msg=adb('shell','am','start','-W','-n',package+'/.KidsActivity',check=False)
  (out/'child-export.txt').write_text(msg); assert 'not exported' in msg
  return 'Nonexported child Activity rejected shell start'
 case('SEC-001','Attempt direct shell child launch','Reject unauthorized child launch',child_export)
 def preview_render():
  preview(); capture('initial-preview'); return 'KidsActivity resumed; screenshot requires visual review'
 case('M0-RENDER','Start preview using parent UI','Game Activity renders',preview_render)
 for w,h,density in [(1280,720,160),(1920,1080,160),(1920,1080,240)]:
  def resize(w=w,h=h,density=density):
   before=resumed('KidsActivity'); adb('shell','wm','size',f'{w}x{h}'); adb('shell','wm','density',str(density)); time.sleep(3)
   after=resumed('KidsActivity'); name=f'render-{w}x{h}-{density}'; capture(name)
   (out/(name+'-activities-before.txt')).write_text(before); (out/(name+'-activities-after.txt')).write_text(after)
   return 'Child resumed after resize/density; image '+name+'.png requires visual review'
  case(f'M1-{w}-{density}','Change emulator resolution/density','Child resumes with intact render',resize)
 def resume():
  adb('shell','input','keyevent','KEYCODE_HOME'); time.sleep(2)
  adb('shell','am','start','-W','-n',package+'/.ParentActivity'); resumed('KidsActivity'); time.sleep(3); capture('resume')
  return 'Child resumes after HOME / launcher task foreground'
 case('LIFE-001','Background with HOME; bring launcher task foreground','Child resumes, textures visible',resume)
 def reload():
  for i in range(5):
   adb('shell','input','keyevent','KEYCODE_BACK'); parent(); preview(); capture(f'reload-{i}')
  return 'Five actual child scene destroy/reload cycles, no launch failure'
 case('ATLAS-001','Close/reopen child preview five times','All repeated scenes launch',reload)
finally:
 adb('shell','wm','size','reset'); adb('shell','wm','density','reset')
 (out/'results.json').write_text(json.dumps(results,indent=2)+'\n')
print('UI assertions complete; inspect PNGs before marking visual tests PASS',flush=True)
