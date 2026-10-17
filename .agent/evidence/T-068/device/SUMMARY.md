# T-068 — physical Fold3 installation and corrections, 2026-10-17

Owner Main. User authorized install/retest on connected DeX, then corrected the
English sample order and requested one subtitle location. Final debug APK installed
on SM-F926B/API35; exact installed/local SHA256 match:
`b10040d9dbf918b27ca09886c1e490dfa648f6f1e59971b8197c389877d71a07`.
Receipt: install-receipt.json. External Desktop display2,1920×1080; ARZOPA physical
capture4620382033380392964. No phone fallback. Parent-selected ages6/5 preserved.

## Corrections made during physical testing

- English turns now speak the Vietnamese invitation, native offline English model,
  wait3–5s for imitation, then give the Vietnamese meaning/answer. No native English
  model after the answer. Model word is shown in the canonical card during imitation.
- Actual story wait initially5.634s (FAIL). Replaced reliance on1s polling with a
  guarded Handler deadline. Generation/listening ticket/state checks reject stale
  timers and late recognition from a previous question; original speech/ASR provider
  unchanged. Original failing run and JSON retained.
- One caption card in AdventureDashboard. Removed bottom entertainment TextView,
  GPS-panel subtitle mirror and duplicate English word in progress label. Caption
  remains readable in Audio-only. Legacy screenshot/long-text fixtures now check
  question and answer sequentially in the same card, not simultaneously in two.
- Restored JVM overloads for controller.load so existing reflected2-argument QA
  entry remains valid after adding interlude support.

## Executed gates

- Final `:core:test :android:assembleDebug :android:assembleDebugAndroidTest
  :desktop:classes`: SUCCESS,240core tests0failures/errors/skips. Final build log
  build-final.log. Nine source contracts PASS in contracts-final.log, including
  dashboard, voice/privacy, single display, AI packs and age contracts.
- `audio_playback`: actual Google offline Vietnamese TTS/native onDone and local
  media output PASS;6offline VI voices. Device media volume15 restored15. Does not
  certify human audibility/quality. audio-playback.txt.
- `t068_learning_source`: real isolated Android import, malformed/oversized
  rejection, AtomicFile byte-identical retention, source switching and disk reload
  PASS. Actual source Activity buttons also exercised by UI fixture.
- `t068_learning_https`: actual Android GET from public app-repo learning TSV PASS,
 30815bytes/SHA25657824184f154e2d9d185e7a2cb7a54584e123dec3d0331b7c3dd7ece9ffdc6be;
  production parser176cards, activated cache/fresh-context reload. Parent real source
  URL not changed. source-https.txt. No API generation or child/GPS/audio payload.
- `t059_story_lifecycle`: actual recreation/ID/cursor, changed/deleted revision
  rejection, coalesced resume and stopped stale load callback regression PASS.
- Native pacing: five consecutive unique cards, balanced BOTH focus, five distinct
  spoken attention cues, two English models using native `en` voice BEFORE the
  response window and `vi` answers AFTER it, permitted matching-audience library
  narration after exactly five completed questions. Real forwarded production
  completion callbacks; no synthetic completion clock.
  Corrected run101.856s: response3.052–5.057s, gaps3.118–4.116s.
  Caption-candidate run113.441s: response4.081–5.076s, gaps4.196–5.200s.
  Observations include35ms polling/Android main-thread scheduling; test tolerance
  response250ms/gap500ms. Exact event logs preserved in pacing-corrected.txt,
  pacing-final.txt and final-artifacts/pacing.json. This verifies callback/order
  timing, not acoustic waveform duration or a full long library story.
- Exact final APK `t068_single_caption`: PASS. Actual view tree has exactly one
  case-insensitive occurrence through question/English sample/answer/transition/
  story fixtures; no GPS mirror; real WORD beat also rules out uppercase progress
  duplication. Full/Audio-only screenshots inspected. single-caption-final.txt,
  final-artifacts/single-english-caption.png and single-audio-caption.png.
- Exact final APK `t068_story_wait`: PASS,3native recall questions through story
  completion; measured5.086/5.093/5.082s (observation overhead included). Deliberate
  fixture starts at recall scene2, preceding narration is not claimed exercised by
  this test. story-wait-final.txt. Previous corrected run5.056/5.056/5.059s retained.
- Installed artifact hash compared to actual package base.apk and matches. QA
  preferences restored by each fixture; source Activity defaults and original
  parent ages/settings retained. Library content preserved; legitimate playback
  advances selected library cursors/last-played timestamps.

Native pacing runs precede the last generic English progress-label text and late
recognition-ticket guard. No pacing/voice behavior changed in those last edits;
canonical-caption and story deadline tests were rerun on the exact final installed
APK. No broader all-audience/endurance or full-episode claim.

## Final state and limits

Opened exported ParentActivity on display2 and clicked its real DEMO button, using
mouse-equivalent ADB input. Direct shell launch of non-exported KidsActivity was
correctly denied by Android; no export/security change or bypass. Normal parent
launch/click succeeded. Final app remains in explicitly labelled DEMO on external
DeX for the user to hear/try, with original parent settings.

Not certified: human acoustic quality, actual child ASR, moving-car/GPS/navigation
coexistence, full multi-minute library episode, endurance, or actual SAF picker
file-selection/grant path. Android file import/storage and actual source controls
were verified, but they do not assert a real document-picker selection. These
limits retain overall field/release gates; install/retest/correction scope completed.
No other-repository access, commit, push or publication.

Initial QA compile used unavailable Files.writeString; corrected to Android-safe
Files.write, then built successfully. Original compile failure and the original
5.634s story timeout failure retained. Initial successful timing run had wrong
English ordering (user observed); it is superseded by native-language/order checks.
