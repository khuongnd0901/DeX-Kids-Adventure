# T-063 — reusable story actions and supporting props

Owner Main; dependencies T-054–T-062 and protected child/Capybara artwork.
User requested richer actions, animation and secondary components, 2026-10-16.
Keep existing JSON schema/enum names working. New finite enums need one APK
update; future authored stories use those bundled capabilities through T-062.

## Implementation slice

Add JUMP/DANCE/BOW/READ/WATER/SHARE/INSPECT with bounded body transforms,
appropriate original pose selection and action-specific animated supporting
objects. Preserve original faces/clothing/PNG sources. Do not claim new limb
frames where existing poses are reused. Add12original editable SVG story props
in a dedicated bounded512atlas; expose four existing companion assets too.
Wire renderer loading/disposal and spatial layers, preserving normal journey,
navigation, on-device voice and existing display rules. No external asset/API calls.

Centralize allowed new action/prop combinations and finite prompt catalog so the
validator, in-app author and external topic prompt agree. Questions retain their
neutral cue; do not claim numeric object counts or arbitrary colors/backgrounds.
Provide extra valid example packs separately; keep the existing public sample
backward compatible and do not update the live URL with new enum values early.

## Acceptance

Core tests: old pack replay; every new action's valid/invalid combinations;
finite bounded motion, pause/reset semantics, shared-object placement, complete
renderer/atlas mapping. Deterministic SVG atlas build, nonempty/transparent art,
no protected source hash changes. Android/desktop compile. Actual Windows API35
FullHD action/prop matrix captures for SAU/ONG/BOTH, pause/recreation and original
story regressions. Visually review screenshots, not only metadata. Physical DeX
and new-art human signoff remain NOT VERIFIED unless actually performed.

Evidence `.agent/evidence/T-063/`. Update TASKS/STATUS; no publish/merge assumed.
Optional user preference requested; start with daily-life/nature assets that also
support exploration stories while awaiting a reply.

## Evidence and state (2026-10-16)

Implementation/simulator slice verified: 226 core tests, Android/desktop builds,
deterministic atlas and protected art checks; 88-frame Windows matrix visually
reviewed, expanded actual Activity recreation plus legacy visual/offline/author
failure/remote-cache regressions passed. See .agent/evidence/T-063/RESULTS.md and
verification.json. Remain active pending physical DeX and human art signoff;
those gates are NOT VERIFIED, not DONE. No publish/merge performed.
