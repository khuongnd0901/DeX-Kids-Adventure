# Interactive stories: implementation and verification

T-054–T-059 extend the existing LibGDX journey and Android local narration. Source
implementation is on `feat/T-054-ai-interactive-storytelling`; acceptance is tracked
in `.agent/evidence/T-054-T-059-story/`. Physical DeX NOT VERIFIED.

Parents open **Thư viện truyện** in Settings or F10. Choose a general topic, learning
goal and Sâu/Ong/BOTH. The existing provider and shared dispatch limiter perform
bounded background authoring followed by independent review. Only a validated draft
is saved. Parents inspect every beat, finite action/prop and question before approving
the exact revision. AI review is fallible. The local sample is authored content,
not proof of AI review. Nothing is sent to a new backend.

The new immutable schema v1 has 4–8 scenes, 2–4 questions, Sâu5/Ong4 and explicit
fiction labels. BOTH includes separate Sâu/Ong and shared questions. Strict JSON
rejects unsupported versions, duplicate/unknown fields, unsupported commands, unsafe
phrases, private-data patterns and excessive depth/size. Free-form parent topic text
is warned and filtered, not a guarantee of personal-information redaction.

`StoryPlaybackDirector` advances narration from genuine TTS completion tokens.
Stale callbacks are ignored. Questions open on-device listening only after speech;
answers remain ephemeral and never enter the cloud reply path. Without a supported
local voice, captions use a separately identified read-along dwell. A missing TTS
callback pauses the story after45s rather than opening a microphone on a guessed
completion. Parent can pause/resume/stop in F10. Offline silence receives one gentle
fallback. POIs preempt the story, preserving its cursor; resume waits for both the
priority interval and active narration/listening to finish. The GPS feed continues
through ordinary story playback. Existing navigation/device control permissions are
unchanged.

`StorySceneCompiler` maps only existing local actions and props. Sâu uses the
original poses and outfit layers; Ong uses six complete generated
four-pose outfits without clothing overlays (T-060), with matching finite movement
transforms. Original source identities were used as generation references; protected
bus, child and Capybara source art are unchanged. Question/feedback visuals use neutral
local cues rather than retaining a contradictory previous prop. Text edits rebuild
narration cues and invalidate approval. A parent can request one-scene regeneration;
all other scenes are immutable, the replacement is reviewed and a new draft needs
parent approval again.

SQLite persistence is independent of disposable lesson caches. See
[storage/version/undo behavior](STORY_LIBRARY_STORAGE.md). Replay loads validated
approved content and cursor on a serialized worker; it has no gateway dependency.
File/DB/network work stays off UI/render threads. Fictional story backdrops are
visual themes and never claims about the actual GPS position.

Optional illustration authoring has an unsupported adapter by default. Sprite-only
playback always works. Private staged PNGs require explicit cost/feature opt-in and
parent review before a checksum-verified bounded cache entry can be read. Replay
never calls the adapter, including cache misses. No image provider or pricing is
assumed, no image API has been enabled, and image texture rendering integration is
not claimed. This optional path remains gated until a real provider and renderer
integration are separately verified.

Rollback: disable story autoplay (default OFF), stop the current story in F10, and
continue the existing entertainment flow. Uninstall removes the private library.
No CI workflow was introduced or enabled.
