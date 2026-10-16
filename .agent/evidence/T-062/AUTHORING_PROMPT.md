# T-062 — topic-based remote story authoring prompt — 2026-10-16

Owner Main; dependencies StoryEpisode schema/validator, StorySceneCompiler,
VisualCue, StoryPropLayout, public sample pack. DONE for this documentation slice.
Full remote-source task retains physical DeX gates.

User requested a prompt to create stories for a chosen topic and automatically
assign available actions/scenes appropriately. Added a copyable Vietnamese prompt
next to the public sample: `docs/story-source/AUTHORING_PROMPT.md`.

Inputs: topic; optional educational goal; SAU/ONG/BOTH (default BOTH); optional
current complete JSON pack so new content is appended without removing old stories.
Output: exact pack schema, one new four-scene story and existing entries preserved.
Prompt documents all3actors,4emotions,10themes,4actions and11prop options. Guidance
maps WAVE/LOOK_UP/PET/COUNT to supported placement/meaning, requires COUNT+NONE,
and forbids promises of arbitrary backgrounds, object quantities, colors, custom
motions or new artwork. Question cues are neutral, so questions rely on narration
rather than assuming the previous object remains visible. BOTH turns SAU/ONG/BOTH;
solo actor/turn rules; age, privacy and fiction rules; strict field/size limits.

The self-contained example is an authored variant of the known-valid BOTH sample,
not a claimed AI-generated/AI-reviewed result. Validator command:

```text
JAVA_HOME=/home/khuongnd/.local/opt/jdk17 ANDROID_HOME=/home/khuongnd/Android/Sdk ./gradlew :core:checkStoryPack -PstoryPack=/tmp/t062-prompt-example.json --console=plain
```

BUILD SUCCESSFUL in3s, Valid pack:1stories; exact output in
`authoring-prompt-check.txt`. Embedded JSON was extracted and compared to the
validated pack. All Theme/Action/Prop enum values were checked against the actual
Java definitions and found in the prompt. `git diff --check` clean.
No new tests duplicating documentation, no app code or APK changes, no AI requests.
No empirical promise that an arbitrary model will always produce valid output;
parents still review content and run the checker before publishing.

Published only the prompt Markdown to main via GitHub contents API:
commit `25b679ccabca8910ae20ed5dc065fec91872ab61`. Anonymous Raw download returned
success and matches local bytes exactly:18192bytes, SHA256
`1a783def5a33b995f369ee079fec1e1575b34825bc75b6c1987aaad8c8be462a`.

Public prompt:
https://github.com/khuongnd0901/DeX-Kids-Adventure/blob/main/docs/story-source/AUTHORING_PROMPT.md

Local feature branch fast-forwarded to this documentation-only commit after
checking the remote diff contains only that file. Other pending implementation,
QA/docs and existing artifacts were preserved. Local source guide links the
prompt; previously published sample JSON and configured simulator source unchanged.
