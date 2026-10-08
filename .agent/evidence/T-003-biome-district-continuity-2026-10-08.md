# M2 world continuity checkpoint
Biome selection now hashes a four-chunk district rather than each 640px chunk independently. Per-chunk details still vary deterministically; sliding window remains bounded. Added test for positive/negative floor-divided districts, seed reproducibility and per-chunk variety.
Result: source authored, CI outcome pending at commit time. Visual seam elimination not claimed; on-device inspection remains open.
