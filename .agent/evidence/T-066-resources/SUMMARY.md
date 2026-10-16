# T-066 — resource optimization on physical Fold3

Owner Main; dependencies T-047/T-063–T-065, base bcd832c.
Implementation/build/device acceptance DONE for this bounded optimization slice.
Main publication receipt recorded separately.

Actual SM-F926B/API35, external DeX2 FullHD. Same instrumented90sFull/15sAudio/
15sFull sequence, no forced GC/purge. Full mean uses samples30–90s; CPU100%=one
core, measured with process CPU-time deltas. Before app hash verified unchanged.

| Metric | Before | Final candidate |
| --- | ---: | ---: |
| Full process PSS mean | 218.26 MiB | 202.16 MiB |
| Full native PSS mean | 71.39 MiB | 63.40 MiB |
| Scene RGBA upper bytes | 57.56 MiB | 49.75 MiB |
| Last Full whole-run FPS | 59.79 | 59.89 |
| P95 histogram upper bound |20ms|19ms|
| Steady CPU, one-core scale | 32.67% | 32.77% |
| Restored Full PSS, one sample | 230.06 MiB | 218.01 MiB |
| Audio-only PSS, one sample | 102.79 MiB | 110.14 MiB |
| Warm69-entry list,20-read mean | 333.57ms | 29.78ms |

Full PSS reduced16.10MiB/7.38% in this matched short probe. Scene texture upper
bytes reduced7.81MiB/13.57%; this is distinct from physical driver allocation.
CPU is effectively unchanged; no battery/CPU-reduction claim. Audio sample PSS
was higher despite no resident scene textures in either run; Java/native used
values were similar (after native PSS slightly lower). No Audio PSS improvement
or causal attribution is claimed from that one sample. Full improvement and
functional disposal are the scope; this is not an all-mode/endurance/no-leak claim.

## Changes retained

- Keep all original atlases. Generate separate Sâu sheets with320x480poses,
  above maximum285x427FullHD slot, using build-time LANCZOS; original protected
  sources/fallbacks unchanged. Physical quality review required below; 4K unverified.
- Bake exact alpha>=128 passenger bounds, eliminating per-load JNI pixel scans;
  retain existing Pixmap upload/disposal and legacy scan fallback.
- Avoid allocating tiny legacy costume textures when expressive/complete art exists.
- Read library lists in two bulk SELECTs (source inspection), keep SQL metadata
  fresh. Freeze up to64cached immutable references during a scan to prevent
  cache thrashing when list length exceeds64; persistent limit stays64.
  Actual isolated70-story fixture:69visible,64episode identities reused, all
  favourite/source/deleted/undo/order assertions PASS. Warm latency333.57→29.78ms
  (~11.2×). Platform bypassed Context cursor factory; initial counter0 is NOT
  zero queries. Direct query-count instrumentation remains NOT VERIFIED.

## Local and physical verification

228core tests,0failures; production codec30stories; full local contract suite and
Android app/test APK+desktop build SUCCESS. Runtime resampling is deterministic,
all original328source file hashes unchanged; eight original and four runtime
crop bounds verified against real alpha scans. Geometry parser bounds/duplicates/
missing-pose/dimension rejection covered by core tests. Commands: REPRODUCE.md.

Final physical results in regressions.jsonl and per-mode logs. Expanded visual
88captures PASS. Full probe screenshot inspected: BOTH faces/outfits/crops intact
at actual FullHD. 44-case visual contact sheet and72-pose costume contact sheet inspected; full-size
Sâu WATER and BOTH probe images inspected. Faces/outfit registration/passenger
geometry intact on FullHD; no general human art approval claim.
All11final physical modes PASS, including actual Activity/GL recreation, remote
cache/playback, isolated library, manager, replay and parent controls.100Full/
Audio cycles PASS46.9s: managed textures return to1after disposal; native allocated
first Full22,212,864bytes, last Full16,683,280bytes, final Audio16,663,056bytes.
These bounded ownership checks are not physical VRAM/no-leak proof.
Final installed app SHA256976c73b50eaa21767108fefd8f5b05c9f93d2d1ec9463adecd65aa5cee747d92
matches local artifact. Final UI navigation initially used stale coordinates;
reopened and resolved buttons from accessibility nodes. Library left visible on
DeX2 with32stories and exact enabled example HTTPS source verified. No device
user participation was required. Numeric receipts: verification.json and
final-library.json; raw screenshots/logs remain local.

## Rejected experiments and retained limits

Four NPOT atlas crops lowered theoretical texture bytes but increased Full PSS
mean to236.46MiB. Retrying original Pixmap upload retained the regression. Two
POT pages for the main atlas also increased PSS to228.91MiB; reject both. Original
atlas layout retained. Experimental source/pages/logs remain local, not shipped.
Initial probe API compile mismatch, early GL screen race and JUnit import error
were fixed, with initial logs retained; no failing result relabelled as PASS.

Broad field/60-minute release/human audio/4K/signing/license and existing bridge/
native-accounting findings remain open. Voice/navigation/device-owner paths and
original audience/source preferences remain unchanged outside restored fixtures.
