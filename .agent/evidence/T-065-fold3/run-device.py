"""Autonomous external-display regression; logs actual assertion results."""
import subprocess,time,json
from pathlib import Path
out=Path(__file__).resolve().parent
adb=['adb','-s','192.168.1.3:36373']
modes=['t063_visual','t063_lifecycle','t062_remote','t062_playback','t054_story_safety','t054_story_failures','t057_cancellation','t057_manager','t059_replay_no_network','recreation','single_display_mouse','parent_controls','t059_story_smoke']
# Initial host supervisor was interrupted by a server restart; allow its device
# instrumentation to finish before rerunning with durable direct-to-file output.
old=Path('/proc/4618/stat')
while old.exists():
 try:
  if old.read_text().split()[2]=='Z':break
 except FileNotFoundError:break
 time.sleep(2)
for mode in modes:
 start=time.monotonic()
 try:
  with (out/(mode+'.txt')).open('w') as logfile:
   p=subprocess.run(adb+['shell','am','instrument','-w','-r','-e','display_id','2','-e','capture_display_id','4620382033380392964','-e','mode',mode,'com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner'],stdout=logfile,stderr=subprocess.STDOUT,text=True,timeout=360)
  log=(out/(mode+'.txt')).read_text()
  bad=any(s in log for s in ['qa_status=FAIL','FAILURES!!!','INSTRUMENTATION_FAILED','Process crashed','qa_error='])
  ended=('INSTRUMENTATION_CODE: 0' in log or 'INSTRUMENTATION_CODE: -1' in log)
  asserted=('PASS' in log or (mode=='t063_visual' and 'captures=88' in log) or (mode=='t059_story_smoke' and 'actual_duration_ms=' in log and 'story_loads=' in log))
  row={'mode':mode,'status':'PASS' if p.returncode==0 and ended and asserted and not bad else 'FAIL','seconds':round(time.monotonic()-start,2)}
 except Exception as e:
  row={'mode':mode,'status':'ERROR','error':str(e)}
 with (out/'device-results.jsonl').open('a') as f:f.write(json.dumps(row)+'\n')
 print(json.dumps(row),flush=True)
