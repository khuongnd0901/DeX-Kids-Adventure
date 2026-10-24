# T-081 — actual ChatGPT subscription feature acceptance

Owner Main;2026-10-24. Source baseline400c383. Actual SM-F926B/API35, active
DeX Desktop display2,1920×1080. Owner requested live testing of all AI features.
Existing native Kids registration, subscription consent and gpt-6.1-sol selection
were used; no account/model switch, key import or billing fallback.

## Final live matrix

All rows below exercised the production gateway/transport, terminal SSE, existing
business validators and isolated storage. Generic test content only; no microphone,
actual child reply, precise GPS, personal identifier or child session was used.

| Feature | Accepted result | Duration | Receipt |
| --- | --- | --- | --- |
| Optional child-text reply | Real response/follow-up; missing consent and private-like synthetic text blocked before network |11.2s|initial/child-report.json|
| POI questions |10factual recall questions, answers selected from local source facts, cache reload; agent inspected all10question/answer pairs |20.8s|final/poi-report.json|
| Curated trip Q&A/English |20eligible local question/word ID pairs, configured focus rotation and cache reload |13.5s|final/trip-report.json|
| Creative lessons + independent batch review |20new lessons with20review provenance rows, all4genres, strict validation and cache reload |71.3s|budget-corrected/creative-report.json|
| Story author + review |4scenes,3questions for configured ages6/5, validated and AI-reviewed draft saved/reloaded in isolated SQLite, parent approval=false |41.0s|budget-corrected/story_author-report.json|
| Separate story review |Real independent review of the live-generated story and isolated repository check |6.2s|budget-corrected/story_review-report.json|
| Regenerate scene + review |One scene rewritten, remaining scenes unchanged, validation/review/storage pass, parent approval=false |33.4s|budget-corrected/story_regenerate-report.json|

[Aggregate verification](verification.json), [device](device.json),
[content inspection](content-inspection.json),
[installed APK exact-match receipt](final/apk-verification.json).

The accepted matrix comprises10logical feature requests. The full campaign used
17logical requests including failed/superseded attempts; physical starts remain
subject to the production persisted10RPM/6100ms gate, including any bounded401
reissue. No ambiguous failed stream was automatically replayed. Review acceptance
was not fabricated or converted into parental approval. Agent inspected this
20-lesson sample and both story artifacts; this is not a general correctness or
parent/editorial certification. Natural rejected-batch refill was not triggered
because the creative batch passed with20items on the first corrected attempt.

## Defects found and corrected

1. **Interactive deadline applied to generation.** Initial creative generation
   failed17.4s; story author20.1s and regeneration21.0s. The transport previously
   limited every response to15sread/20stotal. Long-content requests now have bounded
   45sread/120stotal; cancellation and no-replay semantics remain. Actual corrected
   story generation took35.5s, scene rewrite27.4s and creative generation61.3s,
   followed by separate live reviews. These measured requests could not complete
   within the old20stotal cap.
2. **Batch requests need the same generation budget.** The first tightened POI
   retry explicitly failed at20.1s with RESPONSE_TIMEOUT because its8KiB envelope
   was still classified interactive. Structured batch schemas now also select
   the generation budget. Final POI and curated selection reruns passed. Short
   child replies/native greeting retain15sread/20stotal. Stream/output byte bounds,
   consent, persisted limiter and validators were not weakened.
3. **POI wording quality.** Initial POI structure/cache validation passed, but agent
   inspection found open preferences/performance prompts not answered by their
   selected fixed fact. Prompt now requires literal factual recall, direct
   question/answer agreement and no invented place-name distractors. Final10pairs
   were inspected against the provided facts; no model-authored answer path added.

Initial review of the bundled visual showcase returned approved:false and was
correctly refused. This was not forced into a PASS. After actual story generation
succeeded, separate review/regeneration used that live-generated story. The reusable
harness uses the educational fixture if no live-generated draft exists; fixture
provenance is explicitly recorded. Initial failures and outputs remain under
initial/ and poi-prompt-timeout/; response files for failed requests are not evidence
of completion. Harness now clears prior feature outputs and records STARTED so
future failures cannot be confused with an older successful artifact.

## Verification and preservation

- Core293/293 and Android35/35tests clean, including bounded timeout/no replay and
  prompt active cancellation with the long-content client.
- App/testAPK/desktop builds and lintDebug PASS (0errors,89warnings).
  Latest source build: batch-budget-build.txt; final harness build: final-qa-build.txt.
- Eight relevant local source/privacy/age/display/cache contracts PASS: contracts.txt.
- Explicit live authorization missing: expected instrumentation code1 before
  subscription requests, initial and final guards retained. This is an intentional
  refusal test, not a positive inference run.
- Every accepted feature uses the real production client through a tracing wrapper;
  no synthetic response transport. Only business feature switches/storage are in
  the test namespace. Owner child-cloud/automatic/creative switches stay unchanged.
- First post-live-test checkpoint:48/50backed-up records byte-identical; only real
  limiter history and ART metadata differed (data-after-tests.json). During final
  QA the owner explicitly confirmed enabling global AI and child-text cloud; those
  choices are preserved. Final45/50byte-identical: two deliberate owner setting
  changes share one file, normal source success/remote_pack.checked timestamps
  changed, plus rate/ART metadata. Logical SQLite comparison confirms original
  story content/flags/rows unchanged; other parent/subscription/YouTube preferences
  and encrypted session remain exact. No restoration/credential rollback/logout.
  See data-final-verification.json and owner-settings-confirmation.json. Encrypted
  recovery backup stays protected locally.
- Task cache removed; app stopped after testing. Successful configuration/session
  remains saved. Generic reviewed drafts were never added to the owner's library.

## Limits / remaining broader gates

This verifies primary live content paths for this account/model/configured6/5ages,
with strict business validation and isolated persistence. Real ASR/TTS/child
playback, natural pack exhaustion/rejected-batch refill, navigation coexistence,
long-soak/performance, live refresh/logout and broad content/age/model coverage
remain separate T-075 gates. Previous mock coordinator coverage is not live
acceptance for those scenarios. T-075 remains IN_PROGRESS; T-081 is the bounded
feature test/fix slice, not a claim that every generated lesson is correct.

Official protocol checked with OpenAI Docs:
https://developers.openai.com/siwc/token-sharing-open-source/preview-limitations
and https://developers.openai.com/siwc/token-sharing-open-source/models-and-inference.
Only public Responses/OAuth subscription routing is used; unsupported sampling/
output-token fields remain omitted. User-authorized publication receipts follow.

## Publication result

Verified source18297d0664c5182cb27fefdc8a9f6a128b873c33 pushed, PR67merged into
main d3d4b5816b265e6dff208e8626f8c59d0a566df1; merge tree exactly matches tested
source. Remote feature branch deleted with its exact-tip lease after ancestry
verification; local branch safely deleted. See [publication.json](publication.json).
T-081 bounded feature test/fix/publication acceptance COMPLETE. A documentation-only
receipt commit follows; broader T-075 gates remain open as stated above.
