
# World generation

Fixed logical canvas: 1920x1080, FitViewport.
M0 demonstration uses 640-unit chunks, deterministic seeded PRNG and seven stylized biome categories.
Independent distance source drives chunk indices; same index+seed returns the same chunk.
Next: prepare ahead/behind chunks, cross-fade biome changes, manage atlas lifecycle, animation states and actual GeoContext-to-biome policy.
No geometric fidelity to real street layout is promised. Facts and locations must independently match verified data.
