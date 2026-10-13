# T-047 measured M1 + M2 comparison

Same Fold3/firmware, actual Samsung Desktop display2 1920×1080, same 600s mixed-mode harness and cached reviewed pack; providers disabled.

| Metric | Before | M1 + M2 |
|---|---:|---:|
| Average FPS | 59.9158 | 59.8876 |
| P95 upper | 19ms | 19ms |
| APK bytes | 21,770,740 | 21,229,145 |
| Audio-only Java Heap KiB PSS | 15547.2 | 16100 |
| Audio-only Native Heap KiB PSS | 18208.8 | 18241.6 |
| Audio-only Graphics KiB PSS | 96588.8 | 45732 |
| Audio-only TOTAL PSS KiB PSS | 158967.4 | 108381 |
| Audio-only CPU % of one CPU | 20.56 | 19.96 |
| Full Java Heap KiB PSS | 16264.7 | 16215.5 |
| Full Native Heap KiB PSS | 18749.6 | 76783.5 |
| Full Graphics KiB PSS | 96934.8 | 96940.9 |
| Full TOTAL PSS KiB PSS | 160697.9 | 217996.9 |
| Full CPU % of one CPU | 24.92 | 23.76 |

Five Audio-only and seventeen Full samples per APK, excluding transitions/startup.
Graphics PSS Audio-only drops ~49.7MiB; Full graphics unchanged. FPS is vsync limited; no FPS improvement claim.
Full Native Heap PSS rises ~56.7MiB and total PSS ~56MiB: RAM acceptance OPEN pending allocation/free and long-run checks, not a proven leak.
M3 separate actual 100-cycle native allocation: 21,635,696 bytes first Full, 16,114,240 last Full, 16,240,800 final Audio. Managed textures return to one on every Audio step. This rules out growth in these measured counters, not all driver retention.
Before/after thermal conditions not controlled beyond recorded snapshots; CPU snapshots are noisy; no per-frame draw-call average or GPU timing claim.
Both 600s runs: 60 display checks, eight creative beats, fourteen resolutions, three Audio-only resolutions, F10/Continue, voiceUnavailable zero.
All performance measurements above apply to M1+M2 APK; M3 fresh benchmark pending.
