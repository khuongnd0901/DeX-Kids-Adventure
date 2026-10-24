# T-079 — latest subscription integration and main publication

Owner Main. Status IN_PROGRESS. Dependencies T-075 subscription branch abae09d,
owner-local T-071/T-076/T-077/T-078 YouTube work and latest origin/main81fa123.
User explicitly authorizes commit/push/main merge and deleting fully merged branches.

Repo fetch refspec initially limited to main; fetched all remote heads explicitly
and found existing Draft PR66 subscription implementation plus configured-age and
cleanup follow-ups. Integrate that implementation; discard newly drafted duplicate
implementation (backup /tmp/dexkids-T079-superseded), not owner changes. Early
T079 build/protocol results apply only to superseded draft and are not acceptance.
Owner YouTube work preserved in stash and restored; resolve documentation and QA
dispatch conflicts by preserving both branches' features.

Acceptance: subscription-only shared gateway is the default for all text AI,
no executable legacy-provider path/billing fallback, independent subscription and
child-cloud consent preserved; official protocol audit; local contracts/core/Android
unit/build/lint; physical Fold3 isolated settings/Keystore/feature regressions with
no live inference or credential transfer. Live owner login/inference NOT VERIFIED.
Publish verified source/docs to main, verify remote SHA; delete only branches whose
tip is an ancestor of published main, rechecking tip against audited SHA on deletion.
Leave raw historical device evidence and recovery stash intact.
