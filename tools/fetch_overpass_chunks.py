#!/usr/bin/env python3
"""Operator-only rate-limited retrieval of already generated OSM Overpass QL chunks.

Does NOT run in Android or CI, respects 429 Retry-After, and will not overwrite
existing chunks. Use a local regional PBF or privately hosted Overpass for bulk.
"""
import argparse
import json
import time
from pathlib import Path
from urllib.parse import urlencode
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

HOSTS={"https://overpass-api.de/api/interpreter","https://overpass.kumi.systems/api/interpreter"}

def save_queries(qdir, outdir, endpoint, max_requests, delay):
    if endpoint not in HOSTS:raise ValueError("Overpass host not allowlisted")
    queries=sorted(qdir.glob("chunk-*.ql"))
    if not queries:raise ValueError("No query chunks found")
    outdir.mkdir(parents=True,exist_ok=True)
    completed=0
    for file in queries:
        output=outdir/(file.stem+".json")
        if output.exists():
            try:
                existing=json.loads(output.read_text(encoding="utf-8"))
                if isinstance(existing.get("elements"),list):continue
            except (ValueError,AttributeError):
                pass
            raise ValueError("Invalid existing extract, do not overwrite automatically: "+str(output))
        if completed>=max_requests:break
        q=file.read_bytes()
        if len(q)>16000:raise ValueError("Overpass query too large: "+str(file))
        if completed:time.sleep(delay)
        req=Request(endpoint,data=urlencode({"data":q.decode("utf-8")}).encode("utf-8"),headers={
            "User-Agent":"DeX-Kids-Adventure/0.1 (local manual POI corridor research)",
            "Content-Type":"application/x-www-form-urlencoded; charset=utf-8",
            "Accept":"application/json"},method="POST")
        # Public service may be busy. Respect Retry-After, fail closed rather than hammering.
        for attempt in range(3):
            try:
                with urlopen(req,timeout=110) as response:
                    data=response.read(30*1024*1024+1)
                if len(data)>30*1024*1024:raise ValueError("Overpass extract exceeds 30 MiB")
                body=json.loads(data)
                if not isinstance(body,dict) or not isinstance(body.get("elements"),list):
                    raise ValueError("Response is not valid Overpass JSON")
                output.write_bytes(data)
                completed+=1
                print(f"{file.name}: {len(body['elements'])} elements")
                break
            except HTTPError as exc:
                if exc.code not in (429,502,503,504) or attempt==2:raise
                retry=exc.headers.get("Retry-After")
                time.sleep(min(120,max(15,int(retry) if retry and retry.isdigit() else 30*(attempt+1))))
    return completed,len(queries)

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument("--query-dir",required=True,type=Path)
    p.add_argument("--extract-dir",required=True,type=Path)
    p.add_argument("--endpoint",choices=sorted(HOSTS),default="https://overpass-api.de/api/interpreter")
    p.add_argument("--max-requests",type=int,default=5)
    p.add_argument("--delay",type=float,default=12.0)
    args=p.parse_args()
    if not 1<=args.max_requests<=10:p.error("max-requests must be 1..10 per invocation")
    if args.delay<10:p.error("delay must be >=10 seconds between calls")
    try:
        for path in (args.query_dir,args.extract_dir):
            if "build" not in path.resolve().relative_to(Path.cwd().resolve()).parts:
                p.error("Directories must be under build/")
        count,total=save_queries(args.query_dir,args.extract_dir,args.endpoint,args.max_requests,args.delay)
        print(f"Downloaded {count}/{total} query chunks this run. Re-run later; existing valid JSON is skipped.")
    except (ValueError,HTTPError,URLError,TimeoutError) as exc:
        p.error(str(exc))
if __name__=="__main__":main()
