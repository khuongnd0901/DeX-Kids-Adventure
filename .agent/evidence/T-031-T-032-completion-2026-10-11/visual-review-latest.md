# Latest physical visual QA — Main, 2026-10-11

Installed final source APK on SM-F926B/external FullHD DeX2.
26 actual screenshots: 12 age/audience fixtures and 14 visual cue scenes.
Instrumentation PASS,179355ms,11 theme transitions. Measured native rectangles
do not overlap and long prompt/answer last characters remain visible.
Test-only scheduler isolation prevents natural DEMO overwriting fixtures.

Main inspected latest BOTH-10-10, CUE-12 cloud/SAU and CUE-13 flower/ONG:
full question/three-line answer readable, clear answer/provenance separation,
correct focused poses and one cue prop. Prior isolated run covered all original
11 subjects; latest adds three theme transitions. Parent artwork acceptance
and attention in a moving car remain separate open gates.

Eight tracked preloaded GL textures keep identical handles, sizes and formats
before/after cue transitions. RGBA8888 backing dimensions total 46,659,584 bytes
(44.50 MiB). This measures tracked sprite texture backing only, not driver VRAM,
all scene textures, compression, mipmaps or rendering overhead.
