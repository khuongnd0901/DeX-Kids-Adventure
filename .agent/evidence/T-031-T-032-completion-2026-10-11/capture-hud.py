import pathlib, subprocess, time, os
out=pathlib.Path(__file__).parent
seen=set()
with (out/'visual-hud.log').open('wb') as log:
    p=subprocess.Popen(['adb','-s',os.environ['DEX_QA_SERIAL'],'shell','am','instrument','-w','-r','-e','display_id','2','-e','mode','visual_hud','com.khuongnd.dexkids.test/com.khuongnd.dexkids.qa.LifecycleValidationRunner'],stdout=log,stderr=subprocess.STDOUT)
    while p.poll() is None:
        r=subprocess.run(['adb','-s',os.environ['DEX_QA_SERIAL'],'shell','run-as','com.khuongnd.dexkids','cat','cache/qa-t032-frame'],capture_output=True,text=True)
        name=r.stdout.strip()
        if r.returncode==0 and name and name not in seen and all(c in 'SAUONGBTHCE0123456789-' for c in name):
            shot=subprocess.run(['adb','-s',os.environ['DEX_QA_SERIAL'],'exec-out','screencap','-p','-d','4620382033380392964'],capture_output=True)
            after=subprocess.run(['adb','-s',os.environ['DEX_QA_SERIAL'],'shell','run-as','com.khuongnd.dexkids','cat','cache/qa-t032-frame'],capture_output=True,text=True)
            if after.returncode==0 and after.stdout.strip()==name and shot.returncode==0 and shot.stdout.startswith(b'\x89PNG'):
                (out/(name+'.png')).write_bytes(shot.stdout);seen.add(name)
        time.sleep(.1)
print('Captured',len(seen),'frames:',sorted(seen))
print((out/'visual-hud.log').read_text())
