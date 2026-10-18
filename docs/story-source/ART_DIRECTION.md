# Story stage proportions and interaction rules

The stage uses character height as its unit, rather than assigning the same icon
box to every object. `StoryStageGeometry` owns dimensions; `StoryWorldLayout`
owns ground anchors and depth lanes. These apply only to schema v2 story scenes.

## Character model

Standing heights: Capybara 540, Sâu 500, Ong 465 stage units. All stand on the
same floor at y=245. T-071 supersedes the rejected anatomical cutout rig with
144 complete painted figures (48 per actor), canonical explorer outfits only.
Each frame is a whole head/body/clothing/hand/foot image. Runtime uses whole
sprite transforms and pose changes; it never assembles limbs or swaps heads.
Props use explicit per-pose anchors and retain independent scene geometry.
Source sheets/prompts/extraction bounds: `art/assets-source/story-characters-v3/`.
Road costume rotation remains unchanged.

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
atlas is2048x1024; at most three active whole-character pages are1024x1024. The conservative live backing
bound is28344320bytes, below32MiB, not total process/GPU memory.
Transparent SVG margins are removed before atlas packing, so size refers to the
visible silhouette. Furniture uses floor anchors; seats use the acting character's
height. Background lanes use a shared perspective scale, and large scenery is
placed behind actors. Sky objects occupy a separate lane away from the caption.

## Review gate

Inspect neutral, walk/run, seated, bow, clap, reading, carrying, drawing, watering,
inspection, eating, drinking and pickup/putdown at multiple phases. Check coherent whole-body silhouettes, feet on ground, chair/hip contact,
hand/object contact and HUD clearance for all three audiences.
Technical matrix completion is not human artwork approval. These are finite painted 2D poses; side views, physics and phoneme lip-sync are not implemented.

Historical rejected rig corrections are retained under
`art/assets-source/story-world/anatomy-corrections.json`; they are not active
character art. Technical verification does not constitute parent art approval.
