# T-063 verification — 2026-10-16

Owner Main; branch feat/T-063-story-actions-props, base a0d447b.
Implemented seven new finite actions (11 total), 12 original editable SVG props,
four existing companion illustrations (27 Prop values including NONE), and
three small effects in one generated 512x512 RGBA atlas (1 MiB backing).
Existing character source art is unchanged. Actors remain SAU/ONG/CAPYBARA.
Actor metadata now reaches the renderer so the guide can perform story actions;
attached objects are drawn once at the acting character. Existing poses are
reused with bounded transforms; no new articulated limb frames are claimed.

## Verified

- 226 core tests, zero failures/errors/skips. Includes semantic action/prop
  rejection, codec/compiler actor roundtrip, Vietnamese inference ambiguity,
  old and expanded fixtures, bounded finite/repeated motion and object geometry.
- Android debug APK, instrumentation APK and desktop classes built successfully.
  `build-final.log` (local ignored log), artifact hashes in verification.json.
- Expanded three-audience pack passes production StoryPackCodec/validator:
  expanded-pack.txt. Original public sample has no source changes.
- Deterministic atlas regeneration; all 15 SVG regions nonempty; transparent
  bounds; no modified tracked source art: asset-verification.json.
  Initial missing-Pillow environment failure retained in local build log;
  pinned tools/requirements-art.txt installed in /tmp/dexkids-art-t063 venv.
- Actual Windows Android API35 FullHD emulator 127.0.0.1:5583:
  t063_visual: 44 fixed action/prop cases, 88 A/B captures, all return success.
  Seven actions for SAU, ONG, BOTH and CAPYBARA; 16 new prop values individually
  exercised in BOTH. Final files have target-action-prop-a/b names. Initial
  56-frame run is also retained; do not count it as the final matrix.
  final-matrix.jpg visually inspected: books/cans/lenses/gifts below faces,
  guide attachments present, all added props and animals visible, inactive child
  neutral. Full-size Sâu READ/WATER and BOTH INSPECT/guide WATER also inspected.
  Frame-difference boxes are supporting evidence only: ordinary idle motion is
  also present, so image differences alone do not prove each action's semantics.
- t063_lifecycle: production controller with expanded fixture, actual Activity
  recreation retains episode ID/scene and persisted pause cursor; deleted/edited
  revisions rejected, repeated resume coalesced and stale stop callbacks rejected.
- t059_story_visual: 12 legacy LOOK_UP/FLOWER + PET/RABBIT captures, all audiences.
- t059_replay_no_network: zero gateway calls, cached legacy resume and local-first
  startup, fake provider deliberately used; no real AI/API claim.
- t054_story_failures: author/review failures rejected without persisted draft;
  approved offline story retained. t062_remote: SQLite/cache/source regression.
- Existing child voice privacy guard passes: privacy.txt. No child recordings,
  transcripts or location sent by this work. QA preferences/fixtures restored.

## Limits and delivery

Simulator checks do not establish physical DeX behavior, real speech quality,
new art approval by a human, or frame-rate measurements. These remain NOT VERIFIED.
PNG story art is the tested rendering mode. Remote T-062 HTTPS/304/offline real
GitHub evidence remains prior-task evidence, not a newly repeated live test.
New finite enum values require this APK once; later stories can use HTTPS JSON
without rebuilding. Old APKs must retain their compatible source pack until
updated. New example is separate: docs/story-source/stories.expanded.example.json.
Prompt and capability docs updated. No GitHub publish/merge in this slice.
