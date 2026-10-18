# T-071 complete painted story characters

Nine original generated transparent sheets: Sâu, Ong and Capybara, each with32
complete action paintings plus14complete emotional paintings and2speech poses.
Built-in image_gen used existing fictional road characters as identity references.
Selected sources and full generation/correction prompts: generation.json.
This records provenance, not a license claim or parent aesthetic approval.

Every figure is painted from head to feet, including its clothes and limbs.
The renderer selects one complete image and applies only rigid whole-image motion.
It loads no head, limb, knee, garment or old rig atlases. Story characters wear
canonical explorer outfits throughout the episode; road costume rotation remains.
Do not make costume variants by swapping body parts or recoloring clothes.

extraction.json pins reviewed WHOLE-figure rectangles to sourceSHA256. It avoids
the generated sheets' uneven row boundaries. The atlas build isolates the complete
connected silhouette from neighbouring-cell fragments, preserves soft alpha,
uniformly resizes and foot-aligns it. It never combines parts of different figures.
Each512x512tile is one whole illustration; fourtiles share a1024²page.
Only one page per visible actor is live; pages are replaced before SpriteBatch.begin.
Character backing <=12MiB; total new-stage bound28344320bytes <32MiB including
objects and two backdrops. This is not a process-RAM or sustained-FPS claim.

Build: python3 tools/build_whole_characters.py. Verify reproducibility, extraction,
144figures, distinct gesture pairs and protected original sources:
python3 tools/test_whole_characters.py. WholeCharacterPose defines all27actions.
Paired drawings cover waving, jumping, dancing, walking/running, clapping and
speech; other actions use dedicated painted poses plus small rigid/prop motion.
This is a finite illustrated pose library, not continuous hand-drawn video.
Parent art approval and sustained physical/audio/field acceptance remain separate.
