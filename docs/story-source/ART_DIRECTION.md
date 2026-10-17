# Story stage proportions and interaction rules

The stage uses character height as its unit, rather than assigning the same icon
box to every object. `StoryStageGeometry` owns dimensions; `StoryWorldLayout`
owns ground anchors and depth lanes. These apply only to schema v2 story scenes.

## Character model

Standing heights: Capybara 540, Sâu 500, Ong 465 stage units. All stand on the
same floor at y=245. Heads occupy 32% of standing height, including hair/hat;
head aspect ratio is preserved. The torso pivots at the hips. Shoulder, elbow,
wrist and neck positions follow the same body transform. Dress silhouettes are
wider than shirt silhouettes; their shoulder positions are independent of the
skirt width. Upper arms overlap behind the torso; forearms overlap at the elbow.
The source sheets must not show ball joints, dark sockets or duplicated sleeves.

Two-bone arm reach uses the real upper-arm/forearm lengths. Handheld objects use
those same computed wrist/grip anchors. Reading and carrying use both hands;
eating/drinking bring the right hand toward the face. Drawing supplies a desk,
notebook and pencil. Watering leaves the plant on the ground and keeps the can in
the hand. Inspection separates the leaf hand from the magnifier hand.

## Object scale

Examples relative to standing height H (bounding envelopes; aspect preserved):

| Object | Width | Height |
|---|---:|---:|
| Cup | 0.105H | 0.115H |
| Book | 0.29H | 0.23H |
| Teddy bear | 0.24H | 0.33H |
| Chair | 0.54H | 0.62H |
| Table | 0.95H | 0.56H |
| Snail | 0.12H | 0.08H |
| Elephant | 1.50H | 1.45H |

Every nonempty prop has an explicit dimension profile, including legacy props.
Furniture is rasterized at higher resolution than small handheld items. The object
atlas is2048x1024; three rig atlases remain1024x1024. The conservative live backing
bound is28344320bytes, below32MiB, not total process/GPU memory.
Transparent SVG margins are removed before atlas packing, so size refers to the
visible silhouette. Furniture uses floor anchors; seats use the acting character's
height. Background lanes use a shared perspective scale, and large scenery is
placed behind actors. Sky objects occupy a separate lane away from the caption.

## Review gate

Inspect neutral, walk/run, seated, bow, clap, reading, carrying, drawing, watering,
inspection, eating, drinking and pickup/putdown at multiple phases. Check shoulder
continuity, elbow overlap, feet on ground, chair/hip contact, hand/object contact,
head/torso proportions and HUD clearance for all three audiences and costumes.
Technical matrix completion is not human artwork approval. These are finite 2D
cutouts; side views, physics and phoneme lip-sync are not implemented.

New raster rig corrections use built-in image_gen. Selected outputs, rejected
opaque/missing-row output and extraction choices are recorded in
`art/assets-source/story-world/anatomy-corrections.json`. Source generation is
provenance, not a claim of external artwork licensing or user signoff.
