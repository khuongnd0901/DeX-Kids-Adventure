# T-077 — story recall questions (2026-10-20)

Owner Main; dependencies T-071/T-076, existing nine-video Fold3 library.
DONE for authored-question configuration. Owner explicitly asks assistant to add corresponding questions.
Author two original Vietnamese open prompts per story: recall plus application,
using sourced story themes and cautious wording across adaptations. No video
transcript scraping, fabricated exact audiovisual review, answer grading or child
response transmission. Questions remain editable drafts while videos pending.

Add source defaults and explicit native action that fills only blank recall
fields for known sample IDs. Preserve every other row field, unknown/custom
questions and library/consent/automatic settings. Importing new samples includes
these defaults; ordinary reimport still never replaces existing rows. Do not
expand embedded playback/voice behavior in this content configuration slice.

Acceptance: Android app/testAPK builds, native Fold3 fill/reload/idempotence,
preserve custom question fixture and unrelated rows/fields/preferences;18prompts
on9stored videos, visible editor evidence, installed APK hash. Record sources and
limits. Evidence T-077-youtube-recall; broader T-071 gates retained.

2026-10-20 acceptance: app/testAPK and corrected testAPK build PASS; core unchanged
290clean results. Actual native first fill saved18prompts/9stories, then fixture
failed uppercase notice locator (retained). Corrected rerun code0,0additional
writes confirms exact idempotence/reload/editor and custom/unknown preservation.
All other video fields and7prefs exact; source sync only success timestamp.
Installed hash verified, physical fullHD editor screenshot reviewed; both draft
questions visible and review/enable unchecked. No auto-read/approval/enablement
or publication claim. Evidence T-077-youtube-recall/SUMMARY.md,verification.json.
