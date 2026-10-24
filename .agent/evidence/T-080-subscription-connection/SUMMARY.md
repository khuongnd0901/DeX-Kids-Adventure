# T-080 — owner connection diagnosis and actual subscription inference

Owner Main;2026-10-24. Dependencies T-075/T-079, connected Fold3/API35/DeX2,
owner personally completed sign-in. DONE for diagnosis/configuration/safe live test.

## Finding and resolution

Owner reported unsuccessful connection. Without restarting the app or logging
credentials, focused the existing subscription Activity on DeX2 and read only
native Kids UI. It showed Connected, model Chưa chọn. The subscription checkbox
was OFF. Login had succeeded; the feature route was not ready to dispatch.
The initial load-model action correctly displayed Bật quyền dùng subscription trước.

For the explicitly requested connection verification, disclosed the setting change,
then enabled the native subscription checkbox. Did not change any independent
feature/child-text consent. Loaded seven models through the app's account catalog.
Selected the first server-ordered entry GPT-6.1-Sol (slug gpt-6.1-sol); no model
catalog was fabricated or credentials imported. Ran the native safe Vietnamese
parent greeting test once. The app displayed:

    ChatGPT Subscription · Connected
    Model: gpt-6.1-sol
    Inference hoàn tất: Xin kính chào quý phụ huynh!

That native action calls the actual subscription client/public Responses endpoint,
accepts only successful terminal SSE, then parses JSON message before showing
success. This is real inference, not a synthetic fixture or model-list-only claim.

## Evidence and preservation

[verification.json](verification.json): native UI result,7catalog models, selected
model, persisted consent=true/model and successful inference. The existing
kids_ai_settings_v1 file hash was captured after enabling subscription but before
model selection, then compared after inference: exact match. Separate child-text
and AI feature preferences stayed unchanged. No raw audio/GPS/child identifiers
were sent. No token, callback URI, issued client ID or account identity was logged.
No other application or credential store was accessed. No source fix/rebuild was
needed, so previous source build/unit checks remain applicable. Native UI remains
on DeX2 showing the successful result; persisted settings are ready for use.

Real login/catalog/safe greeting inference now VERIFIED. Real refresh/logout,
creative/story content compatibility, endurance and broad T-075 gates remain open.
Only the existing simple native test was run; no child session or automatic content
creation was started. Source/debug behavior was not weakened to obtain success.
