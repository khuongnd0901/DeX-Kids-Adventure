# T-060 visual semantic gate — FAIL / OPEN

Actual external DeX screenshot `hour/external-3540s.png` shows:
“Đây là cây cầu trong tranh. Theo con cầu giúp xe đi qua chỗ nào?”
The visible scene contains bus, child, buildings and road; no bridge is
visible. This fails the claim that the referenced bridge is in the picture.
Source: `core/src/main/java/com/khuongnd/dexkids/story/EntertainmentDirector.java:70`,
`sau-bridge` authored offline beat. No GPS or historical location inference.

Automated physical_soak asserts continuity, focus, display, frame advance,
no automatic expiry and voice availability. It does NOT assert semantic
object visibility. Its PASS therefore does not close visual/content gates.

Production behavior/text was not changed in this verification task, so
the actual60min APK evidence remains tied to source b5b5423.
Follow-up T-033/T-044 content/visual acceptance: either make authored wording
independent of unverified visible objects or gate the wording on a compiler/
renderer guarantee. Review other authored beats; do not claim a general
semantic fix or adult content signoff from this one observation.

Overall T-060 system acceptance IN_PROGRESS; visual/content gate FAIL,
RAM/field/release gates remain OPEN. No overall DONE.
