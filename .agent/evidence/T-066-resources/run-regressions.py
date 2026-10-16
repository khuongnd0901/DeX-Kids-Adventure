import subprocess,json,time
from pathlib import Path
out=Path(__file__).resolve().parent
modes=['t066_library','t062_remote','t062_playback','t056_library','t057_manager','t059_replay_no_network','t063_lifecycle','t063_visual','t053_outfits','scenery_residency','parent_controls']
for mode in modes:
    start=time.monotonic();file=out/(mode+'.txt')
    try:
        with file.open('w') as log:
            result=subprocess.run(['adb','shell','am','instrument','-w','-r','-e','display_id','2','-e','capture_display_id','4620382033380392964','-e','mode',mode,'com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner'],stdout=log,stderr=subprocess.STDOUT,timeout=420)
        text=file.read_text()
        ended='INSTRUMENTATION_CODE: 0' in text or 'INSTRUMENTATION_CODE: -1' in text
        asserted='PASS' in text or mode=='t063_visual' and 'captures=88' in text or mode=='scenery_residency' and 'full_audio_cycles=100' in text
        failed=any(s in text for s in ['qa_status=FAIL','qa_error=','INSTRUMENTATION_FAILED','Process crashed'])
        row={'mode':mode,'status':'PASS' if ended and asserted and not failed and result.returncode==0 else 'FAIL','seconds':round(time.monotonic()-start,2)}
    except Exception as e:row={'mode':mode,'status':'ERROR','error':str(e)}
    with (out/'regressions.jsonl').open('a') as f:f.write(json.dumps(row)+'\n')
    print(json.dumps(row),flush=True)
