# T-071 — whole painted characters installed and opened on DeX

Owner Main; dependencies T-048/T-060/T-066/T-070. Branch
feat/T-071-whole-character-animation. User rejected the assembled limb/head art.
Implementation/build/physical technical acceptance complete; parent aesthetic
approval remains OPEN, so overall task/plan stays IN_PROGRESS. No commit/push.

## Result
Nine versioned transparent source sheets,144complete figures: each actor has
32action drawings +14whole emotional drawings +2whole speech drawings. Every
visible pose is one coherent painted face/body/clothing/arms/hands/feet image.
No head/limb/garment assembly or joint deformation in the story renderer.
Canonical explorer story outfits replace the former10-outfit story rotation;
road costume/voice/navigation/display behavior stays unchanged. Paired drawings
cover seven gesture/speech pairs per actor; other actions use dedicated painted
poses plus small rigid/prop motion. This is a finite pose library, not continuous
frame-by-frame drawn film. Semantic props remain separate, with adjusted positions.

144figures packed deterministically into36four-pose1024²pages. Only one page per
visible actor lives at once and is replaced before SpriteBatch.begin. Texture
upper bound28344320bytes (27.03MiB); original328protected source hashes unchanged.
Whole-figure extraction removes neighbouring-sheet fragments without assembling
parts. Original generated source PNGs remain byte-identical.

Sources: art/assets-source/story-characters-v3/{sau,ong,capybara}-{actions,secondary,expressions}.png.
Full prompts/references/selected outputs: same directory/generation.json.
Built-in image_gen used; no runtime image generation or child-data upload.

## Verification
-264core tests,0failures/errors;40local Python contract scripts PASS through
  bash scripts/test-local.sh. Android unit tests NO-SOURCE.
-Android debug/testAPK and desktop build PASS. Final isolated actual GL gallery:
  296captures,5renderer reconstruction cycles;9review sheets generated. Actual
  IntelHD4000 OpenGL, not synthetic screenshot fabrication. Action sheets, whole
  sources and key full scenes inspected; physical30captures additionally reviewed.
-Actual KidsGame/AdventureScreen105-frame integrated smoke: frozen pause pixels,
  journey restoration,3audiences and10Full/Audio cycles PASS. Its9.807FPS aggregate
  includes repeated deliberate scene disposal/reloads and is NOT a playback FPS
  benchmark; physical short measurements below are separate.
-Actual SM-F926B/API35 external DeX2,1920×1080, ARZOPA physical capture:
  183cues=60objects+42actor/emotion+81actor/action PASS;3audiences/8backdrops,
  10Full/Audio cycles; managed texture counts all13. World24657920texture bytes.
-Journey60.0068FPS/P95 18.1951ms/PSS167613KiB (163.68MiB); world60.0074FPS/
  P95 18.3048ms/PSS202502KiB (197.76MiB).8-second windows,180retained profiler
  frames; screenshot loops excluded. No sustained/endurance/memory-improvement claim.
-v2SQLite, actual Activity recreation/scene restore/persisted pause cursor,
  deleted/edited revision rejection, coalesced resume and stopped callback guards PASS.
-30final device PNGs pulled after matrix completion,76236166bytes; reviewed via
  device/review-0/1/2.jpg. Capture hashes and raw results in verification.json.
-Parent/AI/learning/manager/key preferences preserved; only normal story-source
  success timestamp changed. No credentials copied into evidence.
-Installed base.apk SHA256 matches final local artifact:
  c26543e2d8201d649f5fae2453509273aee30f1098fde8f3a2242022059cac9a;
  64783362bytes. Installation receipt records test APK too. APK includes36whole
  character pages and the new T-072 icon. Final opening through actual parent
  Settings/Library UI, BOTH showcase offline; final-review-opened.png confirms
  new coherent characters, final-activities.txt confirms resumed KidsActivity DeX2.

## Retained failures and limits
Initial build lacked cairosvg; installed pinned dependencies in an isolated
venv (system Python untouched). Initial icon resources were not ordered before
resource merging; explicit generated resource path/preBuild dependency fixed it.
Initial whole-figure extraction check found neighbouring-sheet fragments; whole
silhouette extraction fixed it without part assembly. Initial logs retained.
Early default GL output folder contained historical outfit captures; final gallery
used a new isolated folder and reports296current captures only. Private child
Activity cannot be opened from shell; final playback used actual parent UI.
Human art/acoustic/endurance/field gates remain OPEN/NOT VERIFIED as applicable.
