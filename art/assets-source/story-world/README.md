> T-071 supersedes the character rig described below after the parent rejected
> its appearance on DeX. Current story rendering uses complete painted characters
> from `../story-characters-v3/`; the former rigs are historical source only.
> The60props and8backdrops remain active.

# T-070 original story world assets

60 original editable SVG props extend the existing SVG library. Authoring source:
`tools/create_story_world_sources.py` and `objects.json`; no external SVG assets.
PNG expression/rig sheets and eight backdrops were created for this task with
built-in image_gen using existing illustrated characters as visual references.
No real-child photographs, GPS, recordings or child responses were supplied.
Exact prompts, selected outputs and one rejected expression iteration are in
`generation.json`. Generation is provenance, not a license or human art signoff.

Original project art stays immutable. New heads use 14 emotional expressions and
2 speech variants. Each child body has six current plus four classic costumes.
`head-bounds.json` records complete silhouette crops, protected by source hashes;
rig extraction bounds are reviewed constants in `tools/build_story_world.py`.
Generated atlas parts are build artifacts, never redrawn over protected originals.

Build `python3 tools/build_story_world.py`; verify `python3 tools/test_story_world.py`.
Three1024²RGBA rig pages + one2048x1024RGBA object page + two maximum1280x720RGBA backdrops =28344320bytes,
below32MiB additional texture backing. Background JPEGs load only current/previous.
No claim that backing bytes equal actual device PSS or total GPU memory.

The renderer uses painted costume-bearing cutout limbs, not arbitrary full-body
rotation as a substitute for articulated actions. It is a finite 2D rig, not
physics, phoneme lip-sync or a 3D skeletal character. HUMAN/DEX acceptance remains
separate from deterministic asset validation and desktop GL screenshot review.

The first physical assembly was rejected for anatomy/scale. Corrected rig sheets,
reviewed part extraction, shared anatomical pivots and semantic object sizes are
described in docs/story-source/ART_DIRECTION.md and anatomy-corrections.json.
