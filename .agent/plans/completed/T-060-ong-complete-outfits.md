# T-060 — Ong complete character outfits

Status: WINDOWS ACCEPTANCE COMPLETE · 2026-10-15 · Owner Main / Game rendering
Dependency: T-053 original poses and outfit rotation; integrates with T-055.

Parent requested replacing unattractive Ong dress overlays with newly generated
complete character sprites. This is an explicit change to the artwork method;
preserve Ong's original identity, protected original sources, Sâu, bus and Capybara.

Generate six coherent outfits with the built-in imagegen tool using the original
fictional Ong sheet as reference. Each transparent sheet contains WAVE, LOOK_UP,
COUNT and PET in the existing 2×2 layout. Store source sheets and prompt provenance
in the repository. No game runtime image API, photos, child data or credentials.

Pack 24 complete Ong frames and upper-body passenger UV aliases into the existing
2048² outfit page, retaining all Sâu pixels. Render Ong once from a complete frame;
never draw an old dress layer over it. All-or-none capability gate falls back to
the original Ong without overlays. Preserve pose rotation and finite gestures.

Acceptance: inspect all six sheets for identity/style/transparent edges; deterministic
packing, source-hash preservation, UV bounds and no-overlay tests; existing local
suite and APK build; Windows API35 FullHD screenshots of all styles/poses including
passengers, pause/recreation, and FPS/RAM. A prior 30min story soak on APK01ff8f69
is retained as pre-art evidence; rerun acceptance on the integrated APK before merge.
Physical DeX NOT VERIFIED. No DONE before actual device evidence.

## Recorded outcome

See `.agent/evidence/T-054-T-059-story/ACCEPTANCE.md` and
`.agent/evidence/T-060-ong-complete-outfits/README.md` for scoped actual results.
Integrated artwork1800373ms soak:40.63FPS/P95upper31ms/9textures; accepted
synthetic GPS4681.06m, all audiences complete, POI pause/resume. Manager-only
queued-cancel fix verified separately after this exact playback/art run.
T-058 optional real provider/renderer remains deferred and disabled. Real child
ASR accuracy/external navigation NOT VERIFIED. Physical DeX NOT VERIFIED.
