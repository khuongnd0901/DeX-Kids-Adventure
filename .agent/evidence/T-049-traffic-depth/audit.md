# T-049 render audit

Owner Main. Base 182841dc309bb23f89f8917b24bbb6b04ac0dfc5. Worktree
/mnt/d/DeX-Kids-T049 isolates unrelated dirty main workspace. No other repo accessed.

AdventureScreen uses a Y-up OrthographicCamera and FitViewport(1920,1080).
SceneryLayout converts absolute fictional distance to 7.5 pixels/metre, chunks640.
WorldPainter road spans y0..268, curb263..278, stripe115..126. Ground scenery
is painted first; drawEnvironment previously mixed traffic with roadside props.
PoiBackdropRenderer paints aspect-preserved panorama at y268..1308 with saved
and restored batch color/alpha. It can therefore cover old tall traffic above268.
The RGB fully opaque path skips hidden sky/ground, but still paints road.

Bus transform is unchanged: x130,y188; legacy950x417 with wheel centre
188+(290-237)/290*417 and radius66 => ground198.2103. Trimmed T-048 PNG
850x599.51 has tire bottom at approximately188. Body bob/pitch remains existing
small suspension motion; lane references use the nominal tire plane so depth
cannot flip with suspension. Passengers use the same existing bus transform.

Old traffic: canvas y110, heights176/158; tire padding ignored, same height
for sedan/minibus/truck, all drawn before bus regardless of wheel plane.
New config: FAR ground250/scale0.74; NEAR ground95/scale1.0. Height per kind
160,160,225,215,115,105. SVG360x185 aspect remains intact. Per-kind source
contact includes tire stroke; classic source anchors supported independently
of T-048 PNG/legacy bus selection. Truck cargo roof enlarged; six detached
shadows moved onto their tire planes and two-wheeler shadow widths reduced.
No new texture or resident atlas page.

Pipeline: sky/ground/decorations -> panorama -> extras -> FAR traffic -> bus
and passengers -> NEAR traffic -> Sâu/Ong Story companions -> GL HUD. Android
native Story/HUD overlays stay above GL. TrafficRenderOrder compares wheel
Y to actual selected bus mode, rather than inferring depth from enumeration
order or biome. No traffic fade/masking, sorting, frame texture load or frame
allocation. POI blending code unchanged.

Movement: existing render effective delta (0 while parent menu open), separate
double clock in traffic model. Same existing20261008 seed, rotating six kinds,
per-kind positive/right-facing speed,22s entries staggered2s between lanes.
One vehicle per lane, every type fully exits before slot replacement (tested
50px safety margin); no visible wrap or dependence on visible chunks. Biome
eligibility latched at offscreen spawn; passing vehicles survive theme changes.
This is decorative traffic on a fictional road; no GPS lane/speed inference.

Reviewed all six active SVG and twelve M10 VEH SVG with source/raster gallery.
M10 remains an art catalog; no runtime integration or extra memory claimed.
