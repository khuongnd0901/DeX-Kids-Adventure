# T-060 — latest source / physical Fold3 verification

2026-10-15 · Owner Main · acceptance IN_PROGRESS, not DONE.
Automated verification execution completed. Visual/content gate FAIL;
Native retention/accounting, field/navigation and release acceptance OPEN.

## Source and environment

Clean main fast-forwarded adf282c → b5b5423ce49c41a74ceb112fc46b8945f371c4e7.
Checkpoint commits/pushes published after each verification step on main
(initial evidence branch was fast-forwarded into main per durable decision).
Application source/assets unchanged by T-060; debug APK hashes in environment.json.
Physical SM-F926B/q2q, Android15/API35/arm64, active external DeX2/1920×1080,
SurfaceFlinger4620382033380392964/ARZOPA. No phone fallback. No CI/cloud calls,
child recording/transcripts or real GPS injection. No other repository changed.

## Results

| Gate | Actual result | Evidence |
|---|---|---|
| Local contracts/raster + JVM | PASS;194core tests,0failure/error/skip;Android JVM NO-SOURCE | 04-local-final.txt /04-core-counts.json |
| Debug + AndroidTest build/install | PASS;generated art before complete suite | 03-build.txt /05-capture-build.txt /06–10 install/build logs |
| Physical GPX/lifecycle/display/mouse/F10/parent controls | PASS scoped assertions | mode-results.json /device-*.txt |
| Mic/GPS denial | PASS after explicit denied fixture;original grant booleans restored | permissions-*.json |
| Cache/review/coordinator/limiter | PASS;simulated provider faults/fake limiter clock,not live provider QA | device-creative*.txt /device-ai_limiter.txt |
| Outfit/age | PASS fixture assertions;72pose+parent+recreation=74captures;actual force-stop/new-process persistence PASS | 08-* /t053_age_*.txt |
| Scene fixtures | 16physical captures;agent review separate from adult approval | 11-art-* |
| Audio SAU/ONG/BOTH | PASS3/3/9resolutions;BOTH4Sau/4Ong/1shared;voiceUnavailable0;duck/F10/disposal/volume preservation | 12-audience-audio-result.json |
| Native Vietnamese TTS | PASS queued/started/finished callback and local tone;human listening NOT VERIFIED | device-audio_playback.txt |
| 100Full/Audio cycles | PASS1managed Audio texture;native allocated firstFull21632080B→lastFull15740480B;not VRAM leak proof | device-scenery_residency.txt |
| Reviewed cache600s | PASS60checks,59.86885FPS,P95upper20ms,14resolutions/8creative beats,voiceUnavailable0;F10 and3Audio-only resolutions | cache600/ |
| Full offline SAU3600s | PASS120checks,86resolutions,voiceUnavailable0,no auto expiry;instrumentation3603.817s | hour/physical-soak.txt |
| One-hour performance | PASS declared>=58FPS/P95upper<=34ms;observed59.945FPS/P95upper20.0ms over3601.158render seconds | hour/summary.json /render.txt |
| Visual semantic alignment | FAIL:caption says bridge is pictured;no bridge visible | VISUAL_FINDING.md /hour/external-3540s.png |
| Original settings/dashboard restoration | PASS semantic XML comparison after parent launch;resumed ParentActivity on display2 | final-restoration.json |

The30min receipt is a prefix of the same uninterrupted60min run, not an
independent second endurance test. The600s cache test includes Full→Audio→Full.
The hour is SAU/Full/offline; all-audience audio was a separate555.325s test.
T-051 log retains legacy Windows wording;actual host/device selection and
external physical captures identify this run, not a substituted Windows test.

## Resources and unresolved findings

Hour:112resource samples;PSS231684–242023KiB
(~226–236MiB),median237791KiB. App current-PID1s CPU
mean29.99%,median30.0%,
range17.0–126.0%;100%means one core,
not whole-device capacity. Initial CPU peak is included. Thermal status0
throughout sampled rows;battery temperature30.6–30.7°C;level93→89.
These battery observations are not an isolated app-only consumption experiment.

Native PSS71158→71658KiB in two same-process mid/late dumps while allocation
16729→17197KiB. Free~11MiB stays below existing32MiB purge threshold.
No manual purge/GC/render toggle during the hour. Cause/reclaimability/leak
not established;T-047 retention/accounting remains OPEN. Earlier restoredFull
after Audio PSS172126–188061KiB is a different sequence, not causal A/B.

T-033/T-044 visual/content gate FAIL:EntertainmentDirector.java:70
`sau-bridge` claims a visible bridge absent from the late capture.
Automated continuity tests do not certify semantic picture/text accuracy.
Production wording/renderer was not modified;no unverified fix or global
visual PASS is claimed. Adult art/content review remains OPEN.

Real-road GPS/POI,Maps/Vietmap navigation audio coexistence,actual child ASR,
human listening/appeal,power-loss,release signing/license remain NOT VERIFIED.
T-010 release remains BLOCKED;T-054–T-059 remain design-only/unimplemented.

## Corrections and provenance

Initial invocations failed for missing default CairoSVG/JDK environment and
generated outfit atlas. Existing host dependencies plus asset generation
resolved prerequisites;failures retained. Parent-controls fixture fixed an
obsolete fixed AI menu index and restores all AI preferences. Physical
screenshot helper now requires explicit numeric external capture ID and
validates FullHD images. CPU sampler uses current-PID top second sample
over1s because dumpsys snapshots can be stale;old cache600CPU data unmodified.
Initial mic precondition and missing external catalog failures retained
as *initial-fail.txt. No failed history erased by later PASS receipts.

Raw90physical art/outfit PNGs are indexed in tracked SHA256 manifests;
full raw sets remain ignored under build/qa/T-060. Contact sheets,selected
originals and three real-hour captures are tracked. See RUNBOOK.md for commands.
Final Git push/fetch equality and clean-tree check are reported in session.
