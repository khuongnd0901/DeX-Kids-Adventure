
# Architectural Decision Records

## ADR-001 Two launchers, shared pure game logic
Decision: libGDX :core Java17 shared by Kotlin Android launcher and Java desktop LWJGL3 launcher. Keep GPS/Audio/DeX in Android adapters, not libGDX core.
Reason: GPX testing on desktop, isolated Android dependencies.

## ADR-002 Offline-first and content provenance
Never claim a named POI from coordinates without verified source, accuracy and direction gating. Offline pack and curated facts only. AI may draft pack content but cannot automatically narrate unsupervised text to children.

## ADR-003 Deterministic world visualizer
Chunks are seeded from journey seed and signed chunk index. Visual depiction is stylized, not a geographic map; location/fact correctness is independently verified.

## ADR-004 No implicit Assistant coupling
Independent APK. Future Android IPC must use explicit component and enforce caller identity; signature permission when signing identity supports it.

## ADR-005 Delivery gates
Do not count code-written as build PASS, source-created as hardware validated or proposed thresholds as measurements.
