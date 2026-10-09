#!/usr/bin/env python3
"""Summarize recorded metrics honestly; never turn capture completion into automatic PASS."""
import argparse, json, math, re, statistics
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--run',required=True,type=Path);a=p.parse_args()
r=a.run; rows=[];pss=[]
pattern=re.compile(r'frames=(\d+) render_seconds=([\d.]+) avg_fps=([\d.]+) p95_upper_ms=(\S+)')
log=(r/'app-logcat.txt').read_text(errors='replace') if (r/'app-logcat.txt').exists() else ''
for line in log.splitlines():
 m=pattern.search(line)
 if m:
  tail=float(m.group(4)); rows.append(dict(frames=int(m.group(1)),render_seconds=float(m.group(2)),average_fps=float(m.group(3)),p95_upper_ms=tail if math.isfinite(tail) else 'histogram_overflow_at_10000ms',disposed='event=disposed' in line))
for f in sorted(r.glob('meminfo-*.txt'),key=lambda f:int(f.stem.split('-')[-1])):
 m=re.search(r'TOTAL PSS:\s*(\d+)',f.read_text(errors='replace'))
 if m: pss.append(dict(sample=int(f.stem.split('-')[-1]),pss_kib=int(m.group(1))))
meta=(r/'run.txt').read_text() if (r/'run.txt').exists() else ''
samples=(r/'samples.txt').read_text().splitlines() if (r/'samples.txt').exists() else []
values=[s['pss_kib'] for s in pss]
summary={'artifact_directory':str(r),'run_metadata':meta,'sample_count':len(samples),'last_resource_sample':samples[-1] if samples else None,'last_cumulative_render_metrics':rows[-1] if rows else None,'metric_log_count':len(rows),'metrics_scope':'raw Gdx GL frame cadence; 1ms P95 upper bound; final interval missing unless disposed=true','observed_pid_fatal_signatures':len(re.findall(r'FATAL EXCEPTION|Fatal signal',log)),'system_anr_completeness':'NOT_VERIFIED from PID-only logs','pss_kib':{'count':len(values),'first':values[0],'last':values[-1],'min':min(values),'max':max(values),'first_five_mean':statistics.mean(values[:5]),'last_five_mean':statistics.mean(values[-5:])} if values else None,'acceptance':'REQUIRES_ANALYSIS; emulator never closes physical Fold3 gate'}
(r/'analysis.json').write_text(json.dumps(summary,indent=2)+'\n');print(json.dumps(summary,indent=2))
