import json,re
from pathlib import Path
p=Path(__file__).parent/'creative-endurance'
rows=[json.loads(x) for x in (p/'resources.jsonl').read_text().splitlines()]
starts=sorted({v for x in rows for v in x.get('dispatch_reservations_ms',[])})
gaps=[b-a for a,b in zip(starts,starts[1:])]
rolling=max((sum(t-60000<v<=t for v in starts) for t in starts),default=0)
metrics={k:v for k,v in re.findall(r'INSTRUMENTATION_RESULT: ([a-zA-Z0-9_]+)=(.*)',(p/'physical-soak.log').read_text())}
duration=int(metrics.get('wall_seconds',3600))
pss=[x['pss_kb'] for x in rows if 60<=x['elapsed_seconds']<=duration and x.get('pss_kb') is not None]
temp=[x['temperature']/10 for x in rows if x.get('temperature') is not None]
summary={'instrumentation':metrics,'resource_samples':len(rows),'warm_pss_kb_min':min(pss) if pss else None,'warm_pss_kb_max':max(pss) if pss else None,'battery_c_min':min(temp) if temp else None,'battery_c_max':max(temp) if temp else None,'max_thermal_status':max((x.get('thermal_status') or 0 for x in rows),default=0),'observed_dispatch_reservations':len(starts),'min_observed_dispatch_spacing_ms':min(gaps) if gaps else None,'max_observed_rolling_60s_reservations':rolling,'reservation_scope':'Conservative persisted reservations sampled each 30s; not HTTP packet capture or billing proof.'}
(p/'metrics-summary.json').write_text(json.dumps(summary,indent=2)+'\n')
print(json.dumps(summary,indent=2))
