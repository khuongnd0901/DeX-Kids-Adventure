# T-077 — recall questions configured on Fold3 (2026-10-20)

Owner Main; dependencies T-071/T-076 and existing9video library. DONE for
owner-requested authored-question configuration. Parent video review and broader
T-071 gates retained; no automatic post-embed questioning implemented or claimed.

Authored18original Vietnamese prompts, two per story: recall and everyday
application. Sources and all wording in docs/T077_YOUTUBE_RECALL_QUESTIONS.md and
questions.json. Used published preschool/school story texts/themes plus previously
observed Rùa/thỏ playback; no transcript scraping or claim of full audiovisual
review of all videos. Open wording avoids demanding adaptation-specific counts,
exact quotes or a single graded answer. Responses are not uploaded or graded.

Source starter defaults include editable suggestions. Native button Bổ sung câu
hỏi gợi nhớ cho truyện mẫu fills only blank recall for known IDs. Preserves custom
questions/unknown IDs and every other field. New imports include defaults;
ordinary repeated sample import still never overwrites existing records. Parent
editor hint describes questions as suggestions to review. Approval/enable flags,
privacy/automatic settings and existing voice/navigation path unchanged.

## Verification

Android app/testAPK build SUCCESS; core unchanged UP-TO-DATE with290clean results.
Corrected testAPK also SUCCESS. Actual SM-F926B/API35/DeX2 native fill persisted
18questions on9stories; library JSON proves exact equality of all other fields.
First fixture then failed finding uppercase native ĐÓNG using mixed-case text;
retained initial-notice-locator-failure.txt. Corrected fixture uses actual native
uppercase ĐÓNG/HỦY; rerun code0 confirms18stored questions,0additional changes,
repeat-idempotence, actual editor exact text and custom/unknown preservation.
No production workaround or reset/reapproval used to force a green test.

Seven preference files/keys unchanged; story-source changes only success timestamp
(per-key hash proof). No other parent/AI/learning/manager/YouTube privacy/handoff
change. Installed APK matches build hash in verification.json. Raw local key absent
from evidence/source scan; private debug APK remains ignored local artifact.
Physical HDMI screenshot final-editor.png reviewed: both Nhổ củ cải questions
visible, parent-review/enable boxes unchecked. Left that editor open for owner.
Earlier fixture editor screenshot is keyboard-obscured, retained separately.

No content safety certification, permanent parent approval/enablement, embedded
post-video TTS/conversation implementation, commit or push claimed. Existing
recall field can be edited through each story's Sửa / duyệt lại action; legacy
external handoff can show approved recall in its existing parent return dialog.
