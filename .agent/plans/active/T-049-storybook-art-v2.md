# T-049 — cohesive non-child art redesign

Status: IN_PROGRESS / PARTIAL. Owner: Main. Branch: feat/T-049-storybook-art-v2.
Reference: T-048 original 5 PNGs and PR #55, merged to main.
Protected: ALL Sâu and Ong art. No story/voice/GPS/animation logic changes.

## Milestones
- M0 inventory & protected boundary: DONE (source inventory examined).
- M1 original V2 source shapes: PARTIAL (5 companions + 12 M10 vehicles).
- M2 deterministic non-child source finishing & atlas integration: IMPLEMENTED,
  code review pending; runtime/unittest checks NOT RUN.
- M3 full bespoke art per existing original sprite: NOT DONE.
- M4 build, local tests, Android emulator before-after QA: NOT VERIFIED.
- M5 physical Samsung DeX, art acceptance, licensing: NOT VERIFIED.

## Acceptance
New art should retain the original slot/pose/silhouette intention, landmark
identity, atlas region names and existing gameplay. No modification to
Sâu/Ong source, image bytes, sprite copy logic or their face geometry. T-048
5 original PNG bytes and driver compositing remain unchanged. Include visual
before/after sheets, hashes and reproducible local build logs before claiming
the full redesign is done. Do not merge unfinished art based only on tests.

See docs/STORYBOOK_ART_V2.md.
