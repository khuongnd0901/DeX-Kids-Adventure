# T-064 — verified sample library, 2026-10-16

Owner Main. User requested adding many samples to the existing story file and
committing/pushing source. Canonical stories.example.json: 30 stories, 10 topics
with SAU/ONG/BOTH variants (not 30 independent plots); 27 additions, including
three existing T-063 showcases and 24 newly authored audience variants.
Three original objects/IDs/revisions preserved exactly.132 scenes,364 beats,
90questions.11actions/27prop enum values covered. See content-audit.json.

Production StoryPackCodec/StoryEpisodeValidator accepts all30 stories; semantic
LOOK_UP/PET audit and balanced question-turn assertions pass. All226core tests
remain green. Android app + instrumentation APKs and desktop classes build.
Full command in local ignored verification.log; final pack result pack-final.txt.
Minimal fixture generation succeeds in build/story-samples/stories.example.json,
leaving the authored30story file intact. Story content manually reviewed against
bundled action/prop capabilities; no real AI/cloud or child audio used.

Pending T-063 source and original SVG assets are part of this publication because
the new source pack depends on its bundled enums/renderer. Its existing88capture
Windows visual matrix, expanded Activity recreation and legacy regressions remain
prior verified evidence for unchanged runtime code; this task does not claim a
new simulator pass of every authored story or physical DeX validation.

Publish scoped source/tests/docs and text evidence, excluding APKs/raw historical
QA artifacts. Verify published Git tree against local verified commit. The branch
URL is documented in docs/story-source/README.md; main stays on its previous pack
until integration. New pack requires APK T-063. Publication receipt follows once
remote verification succeeds. No merge requested in this task.
