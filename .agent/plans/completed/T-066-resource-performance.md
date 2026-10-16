# T-066 — measured RAM and rendering/resource optimization
Owner Main. Dependencies T-047/T-063/T-064/T-065; base bcd832c.
User authorizes implementation and autonomous Fold3 testing. Preserve pending
T-065 source-configuration receipts and the device's enabled example HTTPS URL.

1. Add a bounded same-device resource probe (no cloud, mic, real GPS or forced GC),
   build its test APK against unchanged app and capture cold Full90s/Audio15s/
   restored Full15s on external DeX2. Record process CPU time, PSS/native/Java,
   managed textures and render FPS/P95. Keep same probe for before/after.
2. Investigate atlas space and runtime image budgets; protected originals remain intact;
   defer redundant fallback textures; remove SQLite list N+1 reads; bake exact passenger alpha bounds to avoid
   per-load JNI pixel scans. Confirm
   memory/CPU benefits with actual device measurements, not estimates alone.
3. Core/pack/local contracts and both APK/desktop build; targeted atlas pixel and
   repository semantic regressions. Physical all-action visual, outfit/recreation,
   source-cache/playback, parent controls and resource disposal checks.
4. Compare controlled before/after; retain only changes supported by evidence.
   Restore original preferences, source URL, audience and network. Final installed
   APK matches built checksum; leave app on DeX. Update docs/decisions/trackers.

Acceptance: measured resource reduction without render/behavior regression; all
changed-path tests/builds pass, raw failures retained. No whole-release/field/
60minute/no-leak claims; unrelated historical gates stay open. Evidence
.agent/evidence/T-066-resources/. No other repositories or voice/navigation path edits.

Measured scope refinement: both atlas packing experiments increased actual PSS
and are rejected (raw evidence retained). Keep original atlases. Generate only
Sâu runtime sheets with320x480poses above FullHD display size; retain original
source/fallback. Add scan-resistant list cache traversal without increasing64
persistent entries. Verify physical quality and actual PSS before acceptance.

## Acceptance completed
228core/30-story/full local suite and Android app/test+desktop builds PASS.
Full PSS mean218.26→202.16MiB, steady CPU unchanged, FPS59.89/P95upper19ms.
Warm list333.57→29.78ms,64identities reused/cap64. All11final physical modes
PASS,88action+72costume screenshots reviewed,100Full/Audio cycles PASS.
Original atlas experiments rejected and removed;328source hashes unchanged.
Installed/local APK identical; source enabled,32stories visible on DeX2.
Scope/limits (including higher single Audio PSS sample) explicit in SUMMARY.
No broad release/60minute/no-leak/4K claim. Main0da069f published; PUBLICATION_RECEIPT.md retained.
