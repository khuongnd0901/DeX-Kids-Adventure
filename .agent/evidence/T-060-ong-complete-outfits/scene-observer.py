import subprocess,json,time,re,datetime,pathlib
ADB=['/mnt/d/Android/Sdk/platform-tools/adb.exe','-P','5038','-s','emulator-5554']
BASE=pathlib.Path('/mnt/d/DeX-Kids-Adventure/.agent/evidence/T-060-ong-complete-outfits')
OUT=BASE/'observed-scenes-final';OUT.mkdir(exist_ok=True)
REMOTE='/sdcard/Android/data/com.khuongnd.dexkids/files/qa-t059-live/events.json'
META=OUT/'observer.json'
previous=json.loads(META.read_text()) if META.exists() else {}
rows=previous.get("observations",[]);seen={tuple(r["key"]) for r in rows};started=time.monotonic();errors=previous.get("errors",[])
def now():return datetime.datetime.now(datetime.timezone.utc).isoformat()
def adb(*args):return subprocess.run(ADB+list(args),capture_output=True,check=True,timeout=20).stdout
def state():
    events=json.loads(adb('shell','cat',REMOTE))
    return next((r for r in reversed(events) if 'state' in r),None)
def win(path):return subprocess.check_output(['wslpath','-w',str(path)],text=True).strip()
def save():META.write_text(json.dumps({'started_utc':startutc,'observations':rows,'errors':errors,'notes':'Events sampled by instrument approximately once per second; before/after state does not prove no transition between samples. Pairs show existing pose+finite sprite gesture; no new limb articulation claim.'},ensure_ascii=False,indent=2))
startutc=previous.get("started_utc",now())
while time.monotonic()-started<1800 and len(seen)<24:
    try:
        before=state()
        match=re.fullmatch(r'(SAU|ONG|BOTH):SPEAKING:Cursor\[scene=(\d+), beat=(\d+), phase=(\d+)\]',before['state'] if before else '')
        if match:
            audience,scene,beat,phase=match.groups();key=(audience,scene,phase)
            # Preserve quota for every audience's four narrated scenes. Question is secondary.
            if phase in ('0','1') and key not in seen:
                stem=f'{len(rows):02d}-{audience.lower()}-scene{scene}-phase{phase}'
                row={'key':key,'before':before,'host_before_utc':now(),'files':[]}
                pending=[]
                for suffix in ('a','b'):
                    remote=f'/sdcard/t059-observer-{suffix}.png';dest=OUT/f'{stem}-{suffix}.png'
                    row['host_capture_'+suffix+'_utc']=now()
                    adb('shell','screencap','-p',remote)
                    pending.append((remote,dest));row['files'].append(dest.name)
                    if suffix=='a':time.sleep(.4)
                # Pull after both captures, so transfer time cannot extend the frame interval.
                row['after_capture']=state();row['host_after_capture_utc']=now()
                row['same_cursor_capture_window']=row['before']['state']==row['after_capture']['state']
                for remote,dest in pending:adb('pull',remote,win(dest))
                row['after']=state();row['host_after_utc']=now()
                row['same_state_before_after']=row['before']['state']==row['after']['state']
                row['same_cursor_before_after']=row['before']['state'].split('Cursor')[-1]==row['after']['state'].split('Cursor')[-1]
                rows.append(row);seen.add(key);save();print(json.dumps({'capture':stem,'same':row['same_state_before_after'],'before':before['state']},ensure_ascii=False),flush=True)
                covered={(a,s) for a,s,p in seen if p=='0'}
                if len(covered)==12:print('All 12 audience/narrated-scene combinations observed',flush=True);break
        log=BASE/'final-live-soak'/'instrumentation.log'
        if log.exists() and re.search(r'INSTRUMENTATION_CODE:|INSTRUMENTATION_FAILED:',log.read_text(errors='replace')):
            print('Instrumentation complete',flush=True);break
    except Exception as exc:
        errors.append({'host_utc':now(),'error':str(exc)});save();print(str(exc),flush=True)
    time.sleep(10)
save();print(json.dumps({'pairs':len(rows),'covered':sorted(seen),'duration_s':time.monotonic()-started}),flush=True)
