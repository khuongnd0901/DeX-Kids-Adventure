#!/usr/bin/env python3
"""Restore settings captured by prepare-soak.py; resets test display overrides."""
import argparse,os,subprocess
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--serial',required=True);p.add_argument('--setup',required=True,type=Path);a=p.parse_args()
b=os.environ.get('ADB_BIN','adb');pkg='com.khuongnd.dexkids'
def run(*args,input=None): subprocess.run([b,'-s',a.serial,*args],input=input,check=True,capture_output=True)
run('shell','am','force-stop',pkg)
if (a.setup/'original-exists.txt').read_text().strip()=='True':
 run('shell','run-as',pkg,'tee','shared_prefs/parent_settings.xml',input=(a.setup/'original-parent-settings.xml').read_bytes())
else: run('shell','run-as',pkg,'rm','-f','shared_prefs/parent_settings.xml')
run('shell','wm','size','reset');run('shell','wm','density','reset')
print('Original parent settings restored; test display overrides reset.')
