#!/usr/bin/env python3
"""Compile the small Android allocator adapter for the existing three ABIs."""
import argparse
import os
import platform
import subprocess
from pathlib import Path

p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--out',type=Path,required=True)
a=p.parse_args()
root=Path(__file__).resolve().parents[1]
sdk=os.environ.get('ANDROID_SDK_ROOT') or os.environ.get('ANDROID_HOME')
if not sdk:
    prop=root/'local.properties'
    if prop.exists():
        sdk=next((x.split('=',1)[1].replace('\\:',':').replace('\\\\','\\') for x in prop.read_text().splitlines() if x.startswith('sdk.dir=')),None)
ndk=Path(os.environ['DEXKIDS_NDK_ROOT']) if os.environ.get('DEXKIDS_NDK_ROOT') else Path(sdk or '')/'ndk/27.2.12479018'
host_tag={'Linux':'linux-x86_64','Darwin':'darwin-x86_64','Windows':'windows-x86_64'}.get(platform.system())
if host_tag is None:raise SystemExit('Unsupported NDK host')
bin_dir=ndk/'toolchains/llvm/prebuilt'/host_tag/'bin'
abis={'arm64-v8a':'aarch64-linux-android30-clang',
      'armeabi-v7a':'armv7a-linux-androideabi30-clang',
      'x86_64':'x86_64-linux-android30-clang'}
for abi,compiler in abis.items():
    executable=bin_dir/('clang.exe' if platform.system()=='Windows' else compiler)
    target_flags=['--target='+compiler.removesuffix('-clang')] if platform.system()=='Windows' else []
    if not executable.exists():raise SystemExit('Install NDK 27.2.12479018 or set DEXKIDS_NDK_ROOT; compiler missing for '+abi)
    output=a.out/abi/'libdexkids_memory.so';output.parent.mkdir(parents=True,exist_ok=True)
    staged=output.with_suffix('.so.tmp')
    subprocess.run([str(executable),*target_flags,'-std=c11','-O2','-fPIC','-shared',
        '-fvisibility=hidden','-Wl,--no-undefined','-Wl,--strip-all',
        '-Wl,-z,max-page-size=16384','-Wl,-soname,libdexkids_memory.so',
        str(root/'android/src/main/cpp/native_heap_trim.c'),'-o',str(staged)],check=True)
    staged.replace(output)
    print(abi,output.stat().st_size,'bytes')
