# M9 actual emulator stability — 2026-10-09

Running APK source **a4e1f34**, recorded installed SHA256
12f84ed18d5d2deffe90f130198ef74bd4ffd651e945a4622dfb2851515a074b.
Windows AVD ZFold3_API35, Android15/API35, WHPX/gfxstream/NVIDIA RTX4060 GLES translator,
1920×1080/160dpi. This is a generic emulator, not Samsung Fold3 or DeX.

| Test | Actual execution / result | Status |
| --- | --- | --- |
| Smoke | Separate123-second resumed-child capture, PID3922 | PASS (observed runtime/render subset) |
| Normal10minutes | First10minutes of the same actual60-minute demo journey; no restart | PASS continuity; not a separate independent run |
| Long scrolling30minutes | First30minutes of same actual60-minute journey; visible sampled chunk transitions | PASS continuity; full seam/GPU allocation review NOT_VERIFIED |
| Child60-minute session | KidsSession started08:53:21.796, limit_reached09:53:21.796; real3600seconds, samePID5991 | PASS session lifetime/expected expiry |
| Strict3600-second sampler | Exit4 when KidsActivity stopped at the unchanged parent limit | FAIL retained; expected timeout, not crash |
| Aggregate frame cadence |169562frames /3571.765renderseconds; average47.473FPS; P95 upper43ms,1ms bins | Recorded prefix measured; complete60-minute histogram NOT_VERIFIED |
| PSS |221samples: first52994KiB, last53128KiB, range52551–53289KiB (51.32–52.04MiB); end+134KiB | PASS observed bounded PSS; individual GPU/texture leaks NOT_VERIFIED |
| Mean PSS trend |First5mean52680KiB, last5mean53193.6KiB (+513.6KiB) | Measured; no large monotonic/unbounded growth observed |
| Crash / ANR |0PID fatal signatures, no restart; final exit-info has only pre-run PACKAGE UPDATED exits; lastanr says no ANR since boot | PASS observed no crash/ANR |
| Stable30FPS acceptance |P95 upper43ms; a mean above30 does not establish stable30FPS | NOT_VERIFIED; gate remains open |
| Physical60-minute Fold3/DeX soak |No physical device, navigation/audio/thermal/battery/signing evidence | BLOCKED |

Raw local evidence: `build/simulator-artifacts/stability-test-20261009T015322Z-7753/`,
including analysis.json,221 meminfo/gfxinfo/activity samples, PID logcat, installed APK/hash,
last-anr-final.txt and exit-info-final.txt. Last resource sample3592seconds; later sampler
assertion observed parent timeout. Original run.txt/exit code retained without a fabricated PASS.
The baseline Game.dispose omission prevents final event=disposed aggregate logging, so the last
28-ish seconds of aggregate coverage must not be inferred from the last119 periodic metric lines.
Metrics describe raw Gdx GL frame cadence, not GPU execution time; UI/Skia gfxinfo is separate.

Actual113PNG sequence spans3500seconds (08:54:48–09:53:08):
`visual-series-20261009T015448Z-7917/frames.txt`, frame-0000..0112.png and contact-sheet.png.
Wrapper exit143 was observed despite the final capture-complete message and all113images;
retain that execution anomaly. It does not erase actual timestamped PNG evidence or establish FPS.
Actual30-second local video: `video-soak-scroll.mp4`, with video-start-utc.txt/video-end-utc.txt.

Host builds/short desktop tests during first~20minutes, periodic screenshot collection and a30-second
screenrecord near40minutes may affect emulator cadence. Soak uses DemoJourneyFeed, not Android GPX
or measured GPS. QuietON/TTSOFF fixture is synthetic; original settings must be restored.
Post-fix APK runtime tests are reported separately; this baseline soak cannot validate later fixes.


## Post-fix smoke (separate source)
Final source bad299a: stability-test-20261009T031619Z-22165 sampled123real seconds and
completed capture126seconds,9PSS samples51753–52201KiB, PID16077 unchanged,0fatal signatures.
Scene started before sampler and ended later by BACK; final actual event=disposed aggregate
frames5975/render_seconds172.582/avgFPS34.621/P95 upper56ms is whole short-scene cadence,
not exactly the126-second sample window. Host instrumentation-APK build occurred during this
short run. No stable30FPS or new60-minute acceptance inferred. Final60-minute source remains a4e1f34.


### Final integrated-cast smoke completed
`stability-test-20261009T034450Z-25275`:123 real wall seconds,9 resource samples,
PID18912 unchanged,0 PID fatal signatures; start/end screenshots manually reviewed
with intact scene. Smoke launch/render/no-restart PASS. PSS57100–61206KiB,
last-first −4106KiB. Last logged cumulative scene prefix2979frames/150.264s
=19.825FPS/P95 upper86ms; prefix begins before sampler, so this is not120-second
window FPS. **30FPS threshold FAIL for this observed prefix**, full sustained
performance and system-wide ANR completeness NOT_VERIFIED. No hardware acceptance.
Do not attribute low FPS to new sprites without a controlled equal-host A/B.
Next task includes controlled old/new-art profiling, complete visual review and
Android GPX selection; source architecture is unchanged by this evidence update.
Original parent fixture/display settings restored; installed app remains integrated
cast APK; QA instrumentation removed. Permission/accessibility state checked separately.

Final QA publication uses a content-tree-verified API commit based on a7f8fb0;
remote lease rejects stale heads, with no force/main push or PR merge. CLI credential
helper is unavailable. If publication fails, resume from the isolated integration
worktree; original worktree retains existing uncommitted line-ending changes.
