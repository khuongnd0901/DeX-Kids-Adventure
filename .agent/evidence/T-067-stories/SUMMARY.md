# T-067 — authored public stories and playback contract

Owner Main; 2026-10-16; base465aef7; dependencies T-062/T-063/T-066.
Latest user steering: commit/push only; user synchronizes when opening the app.
No device sync, local story removal or APK install performed in this task.

## Verified scope

Replace all30objects in docs/story-source/stories.example.json with9new IDs:
Rùa và Thỏ, Rùa con tìm nhà, Sói và bảy dê con × SAU/ONG/BOTH. No old source ID
remains. Newly authored Vietnamese prose; gentle wolf ending, seven kids safe;
Sâu/Ong have spoken roles. Keep the separate expanded animation showcase fixture.
Only READ/BOOK and WAVE/NONE cues; no invented turtle/wolf/goat image support.

Each episode has6continuous narration scenes/36beats, an explicit ending before
the first question, then3questions (scenes6/7/8). The final2scenes only introduce
recall turns. BOTH targets SAU→ONG→BOTH. All27questions set maxWaitSeconds=6.
Silent playback waits6seconds after the question callback; existing recognized
answers may finish the wait earlier. Voice/microphone/navigation paths unchanged.

Main narration1471–1538Vietnamese whitespace units, approximate syllables. At
220–320units/min the per-episode estimate is4.597–6.991minutes. This excludes
questions/feedback/answer pauses and is NOT a measured TTS duration; punctuation,
engine/rate and read-along mode can differ. No physical/human listening claim.

## Executed checks

- Production StoryPackCodec/StoryEpisodeValidator accepts9stories,179220UTF-8bytes,
  SHA25649ef1b5fe13d4f3100b941615566de3a30246af7924bd7b628deb51fbd6c73fb.
- content-audit.json records all IDs, removed30IDs, byte sizes, estimates and turns.
- 230JUnit tests,0failures/errors. New2tests exercise all9published stories:
  actual callback-driven StoryPlaybackDirector reaches the explicit ending after
  36narration callbacks before any question; fair targets; no speech at5.99s of
  silent listening and timeout at6s; all episodes finish. Deterministic clock
  simulation, not real-time device playback. Gradle declares JSON as test input
  so a content-only edit invalidates test results.
- Android debug APK and desktop classes BUILD SUCCESSFUL; production source/APK
  behavior unchanged. Final core rerun and pack check BUILD SUCCESSFUL.
- git diff --check clean before publication. Authoring prompt/source guide updated
  to long uninterrupted stories,6s deadlines, current main URL and9-story library.

Initial pack check correctly rejected the folded phrase “từ từ” via the existing
safety filter. Rephrased to “dần dần”; filter unchanged. Initial/final build logs
retained locally; no failed result relabelled. Publication receipt follows after
push and anonymous HTTPS byte verification. Historical device/release gates stay
unchanged; physical replay and exact spoken duration NOT VERIFIED in this slice.
