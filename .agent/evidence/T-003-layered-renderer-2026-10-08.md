# T-003 M2 layered scene implementation checkpoint

Replaced one-layer render with render order sky -> distant original SVG hills/clouds -> chunk ground/road -> near biome-specific vector sprites -> animated bus/Capybara -> HUD labels.
Changes include deterministic absolute-distance parallax, stable chunk offsets, props matching visual biomes, independent physical-distance wheels, capped vehicle bob/pitch, and actual actor animation states. No named POI claims.
Python atlas test validates required sprite regions and alpha transparency; Java tests check chunk continuity and animation transitions.
`CartoonSprites.setNarrationActive` is available for a future reviewed narrator. It is never automatically set just to show talking.

CI result pending at commit time. Hardware GPU measurements, screenshot visual review and final art approval NOT VERIFIED. Task IN_PROGRESS.
