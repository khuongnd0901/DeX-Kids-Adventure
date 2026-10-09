# M8 functional validation

No-external-display parent UI PASS, no silent phone fallback. Shell direct child launch rejected nonexported Activity. Separate UID/certificate probe sent unauthorized STOP; actual child survived and direct launch rejected: security-probe-20261009T015244Z-7345 (local artifacts). Initial probe harness lacked package visibility and modern dumpsys matching; corrected, prior failed artifacts retained. Matching-signature STOP test pending. Production Assistant signing/START/PAUSE/RESUME/volume/status contract and physical DeX dual-display/audio coexistence BLOCKED/NOT_IMPLEMENTED. Original Assistant repos untouched; IN_PROGRESS.

No milestone DONE. See source-test-map.md for exact steps/expected/code mappings.


## Final local signing probes
Actual untrusted `security-probe-20261009T025743Z-19554` PASS:
different UID/certificate, CONTROL_DENIED, nonexported launch denied, child survives STOP.
Actual trusted `security-probe-20261009T025811Z-19906` PASS:
different UID/same local debug certificate, CONTROL_GRANTED, STOP returns ParentActivity.
No system dangerous permission granted. These project-owned probe APKs request only the custom
signature CONTROL permission and package visibility; they have no location/microphone/network.
Production signing compatibility and other IPC commands remain open. Assistant untouched.
