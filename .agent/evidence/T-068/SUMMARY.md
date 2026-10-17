> Superseded by the physical follow-up below: device connected, final APK installed;
> English-sample ordering and duplicate subtitles corrected. See device/SUMMARY.md.

# T-068 — pacing, shuffled questions, story interludes and external learning

Owner Main. Dependencies T-030/T-053/T-062/T-067. 2026-10-17.
Branch feat/T-068-question-flow-external-learning. Source/build acceptance verified;
overall IN_PROGRESS because physical DeX/car/audio/SAF/network acceptance is NOT VERIFIED.

## Final behavior

Latest user steering supersedes the initially proposed 15s/3s and intermediate
1s gap: question response window is randomly 3/4/5 seconds AFTER prompt completion;
next activity starts after a randomly chosen 2/3/4/5 second gap AFTER answer
completion. Six varied attention cues (including the three requested phrases)
play inside that gap. Callback progression is exactly once; epoch cancellation
prevents stale playback; caption and stuck-engine paths are bounded. English voice
runs in the answer phase. Transition timer wakes the existing foreground tick at
the selected deadline; missing transition completion is capped at five seconds.
Actual acoustic timings are NOT VERIFIED.

Regular offline turns now alternate quiz/word cards with shuffled eligible
selection, topic preference without cycling one topic prematurely, configured
per-child ages and shared used-card history. Activity saved state retains history.
At exhaustion avoid immediate same-kind repeats when alternatives exist. Optional
companion intro preserves the selected shuffled card. Existing reviewed AI pack
playback/rotation retained; imported catalog cannot be replaced by AI caches and
disables trip-pack workers for that session source.

Five completed questions trigger a library story of the selected audience;
empty approved library uses three original bundled stories in rotation. Bundled
stories pass the production validator, narrate before three recall questions,
resume through existing controller/cursors and never auto-approve a draft.
Parent checkbox can disable interleaving. Legacy library autoplay may start the
session; with interleaving enabled subsequent stories follow the five-question
cadence rather than playing continuously. Story response waits are host-clamped
3–5 seconds without modifying parent-source revisions. Empty/unmatched ASR does
not bypass the listening window. Existing narration/recognition engines untouched.

New parent source Activity accepts a SAF UTF-8 TSV import or explicit public HTTPS
URL. Existing secure downloader is parameterized for TSV Accept/131072-byte limit;
story JSON defaults unchanged. Foreground fetch throttled five minutes, manual
refresh supported. Whole-file validation precedes AtomicFile replacement; errors
retain last-good. Generation checks discard obsolete downloads. Current session
holds an immutable catalog; next journey uses updates. Source choice independent
of story library. No child/GPS/audio payload, AI calls or publication performed.

## Executed verification

Environment JAVA_HOME=/home/kunrg/.local/opt/jdk17,
ANDROID_HOME=/home/kunrg/Android/Sdk.

- `./gradlew :core:test :core:checkStoryPack :android:assembleDebug :android:assembleDebugAndroidTest :desktop:classes --console=plain`
  final run BUILD SUCCESSFUL in21s; 240 tests,0failures/errors/skips across77suites;
  public9-story pack accepted. Log build-final-pacing.log.
- Eight relevant source contracts PASS: trip_learning, creative_lessons,
  t024_t025_audio_engagement, child_voice_privacy, dual_child_entertainment,
  single_display_contract, t030_ai_packs, t052_child_ages. contracts-final.log.
- New tests exercise actual QuestionTurn callback clock, 3–5s response bounds,
  attention cue shuffle/2–5s gaps, seeded differing question orders, exhaustion/
  age/context/history restore, companion preservation, malformed/empty/duplicate/
  oversized TSV, full production playback of all9 bundled audience/story variants,
  story host wait clamp, and secure downloader TSV byte limit/Accept/cleanup.
- `git diff --check` passed. File/sample/APK SHA256 + XML totals in verification.json.
- `adb devices -l`: no devices. No installation, real SAF picker, Android storage
  execution, live HTTPS endpoint, DeX rendering/listening, car, latency or endurance
  claim. New isolated Android `t068_learning_source` mode COMPILED ONLY; exercises
  AtomicFile retention, import/mode switch/fallback/disk reload when equipment is
  available. Run with the existing LifecycleValidationRunner and explicit display.

## Retained failures and limitations

Initial build:2/230 tests failed because a synthetic fixture reused the same WORD
prompt24times; the new duplicate-prompt gate correctly rejected it. Fixture now
has distinct prompts; existing pack-validation tests preserved. Initial static
contract expected the old companion selector; companion decoration now preserves
the randomized/age-safe question and the contract passes. Original logs kept.
Intermediate successful builds reflect prior timing values; final-pacing is the
final source validation. No fake device PASS or measured duration asserted.

TSV schema deliberately retains ages4–6; it does not relabel these cards for age7.
Content/factual quality still requires parental reading; schema validation is not
an AI/editorial accuracy certificate. Sample16cards is illustrative and exhausts
sooner than a larger parent pack. New files: docs/learning-source/README.md,
learning.example.tsv, AUTHORING_PROMPT.md. No other repository accessed/modified,
no navigation/device-owner/Accessibility/provider changes, no commit/push/install.
