# M4 performance — actual timed runs, completed with FAIL performance gates

Device:Windows Android Emulator37.1.11 API35 google_apis x86_64,1920×1080 landscape,density240,SwiftShader. Auto GPU previously crashed EGL; no physical DeX performance claim. Exact APK/source hashes:`soak-source-fingerprint.json`.

| Window | Actual app window | FPS | P95 upper bound | Status |
|---|---:|---:|---:|---|
|10min mixed Full/Audio-only cached AI|600s (command605.48s)|44.04|29ms|FAIL performance; functional/cache/audio continuity PASS|
|30min synthetic POI/graphics|1,800.67s (command1,802.22s)|42.09|29ms|PASS functional/stability; FAIL M10 FPS reference|
|60min cached real AI/graphics|3,600s (command3,600.72s)|40.78|30ms|PASS functional/lifetime; FAIL performance|

10min:36 meminfo/CPU samples. Mean Java Heap summary PSS12,765KiB; Native Heap summary PSS26,146KiB; total PSS76,060KiB (range74,378–84,269KiB). Mean live Java heap allocation3,162KiB, native28,442KiB. CPU mean75.82% in guest `top` units (100%=one CPU), not host-emulator CPU. Draw-call samples1–13, median11; measured previous SpriteBatch frame, not every-frame average. Graphics column0 means unavailable accounting on this software emulator, **not zero VRAM**. GC frequency/allocation rate not collected; live allocation snapshots are not allocation-rate measurements.

Actual10min:1 reviewed cached pack,6 observed creative beats,14 total resolutions,3 resolutions in Audio-only,one F10/Continue,zero unavailable voice,zero crash signatures andzero provider dispatches. Performance below M10 target>=55FPS/p95<=25ms; runner's stricter58FPS/34ms gate also fails. No causal comparison against old code on the same backend; no fabricated savings.

Collectors sample memory/CPU/activity and FullHD screenshots;GL RenderRunMetrics supplies FPS/p95/draw-call evidence. Thread sampling started partway through10min; zero sustained trip workers observed in available samples with provider disabled. Blocked-I/O duplication regression is separate fake-provider evidence. No real API stress during any soak.

Raw artifacts:`soak-10min/`, `soak-30min/`, `soak-60min/`; normalized values:`performance-measurements.json`. APK storage audit:23,007,013bytes,103 gallery entries1,226,002compressed bytes, full M10 atlas395,909compressed bytes; storage is not GPU residency. Loaded prop pageRGBA backing≈4MiB is a theoretical texture size, not physical driver measurement. No unbounded-memory-leak conclusion from these samples.

30min:108 samples; CPU mean78.11% of one guest CPU. Java Heap summary PSS mean12,562KiB; Native Heap24,124KiB; total73,296KiB(range70,325–75,702).26/26 introductions,22/22 complete conversations,speech callbacks26, F10pause/resume,appstillactiveafter1,800.67s. Zero crash signatures/provider dispatches in collected log; SpriteBatch draw-call samples10–13,median11. Real driving/LiveFixGate was not exercised by this downstream synthetic fixture.

60min:215 samples; CPU mean79.14% of one guest CPU (56–100%). Java Heap summary PSS mean12,583KiB (12,096–15,200), Native Heap25,927KiB (24,788–32,092), total75,093KiB (73,491–80,891; first75,746,last74,986). Live Java allocation mean3,147KiB/native29,261KiB. Draw-call samples10–14,median11. Actual3,600s/360 display checks:1 real-reviewed cached pack,46 creative beats,86 resolutions (Sau40/Ong28/Both18),voiceUnavailable0; app alive until explicit harness cleanup. Zero collected crash/provider-start signatures. Provider disabled: no API calls during the full100 minutes. Persistent limiter-history hash unchanged between sampled30min and after60min,4 retained old reservations.

Benchmarked production APK SHA256 `34e230f64a3e77975dea50130fe883320a029950d6826cc0e5670d143c53eecf`. A later narrowly scoped content-validator guard was added after these runs; do not claim these are60-minute measurements of that later APK. Renderer/lifecycle performance sources were unchanged. GC frequency, allocation rate, physical GPU memory, host-emulator CPU, thermal/power behavior and an old/new comparison remain NOT VERIFIED.
