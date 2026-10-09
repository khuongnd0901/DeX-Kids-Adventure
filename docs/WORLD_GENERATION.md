
# World generation

Fixed logical canvas: 1920x1080, FitViewport.
M0 demonstration uses 640-unit chunks, deterministic seeded PRNG and seven stylized biome categories.
Independent distance source drives chunk indices; same index+seed returns the same chunk.
Next: prepare ahead/behind chunks, cross-fade biome changes, manage atlas lifecycle, animation states and actual GeoContext-to-biome policy.
No geometric fidelity to real street layout is promised. Facts and locations must independently match verified data.

## M2 storyboard coherence (2026-10-08)
For the *fictional* demo world, biome seeds are grouped into 4 consecutive 640px chunks (2560px districts). Object decoration remains independently deterministic per chunk. This decreases abrupt biome switching in a single scene, while allowing repeated foreground parallax assets and contiguous camera movement. It does **not** infer actual POI/road geography from synthetic demo scenery.
CI/source tests validate district determinism; real on-device seam inspection and geographically guided transitions remain separate release gates.
