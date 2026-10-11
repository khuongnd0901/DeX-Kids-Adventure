# T-011 latest GitHub build checkpoint — 2026-10-11

Owner Main; source origin/main 3787d19 integrated with local HQ Sâu/Ong,
compact HUD, varied openings and story-context fixes. Local contracts and
106 core tests PASS; Android debug/AndroidTest and desktop classes build PASS.
Final cabin SVG removes anonymous baked-in passengers that overlapped avatars.
Final target build SUCCESS in 29s. APK hashes recorded in build-summary.json.

Earlier latest-source APK installed successfully and actual desktop GPX render
showed POI park and new cabin layout. The subsequent final clean-cabin install
failed: `adb: device offline`; reconnect to 192.168.1.3:34095 was refused.
Wireless debugging endpoint requested from parent. Final device install/run,
SAF, current-scene/audio regression and authorized permanent Gemini setup are
NOT VERIFIED/PENDING CONNECTION. Existing permission grants unchanged.
Previous-build results remain in T-011-all-task-2026-10-11; they do not prove
this final APK. Interrupted 30min run and unrun 60min remain open gates.

Upstream changed scripts/test-local.sh to non-executable, causing Permission
denied; restored executable bit and reran successfully. No cloud child data
sent for this checkpoint. Secrets/preferences are excluded from evidence.

ADB reconnected at 192.168.1.3:34663; final debug and test APK install Success.
Physical regression running. Clean-cabin desktop render PASS (149 frames,
28.376 FPS, P95 upper79ms); performance gate >=30 FPS not met on this short
desktop sample. Screenshot is actual OpenGL output.

## Final parent-requested two-layer build
Half-body passenger-v3 avatars in bus; original full-body story-v2 companions
at original separate activity positions; bus translated left, Capybara driver
in rightmost front curved glass. Final build/local checks106tests PASS.
Desktop screenshot reviewed;149frames27.808FPS/P95upper84ms, desktop>=30FPS
gate remains unmet. Physical SAU/ONG/BOTH captures reviewed: cabin busts,
separate question poses/props, driver placement and compact native panels.

Before final layout change, latest-source15-mode physical regression13PASS,
2BLOCKED_PREEXISTING_GRANT. Final two-layer target audience picker PASS;
3audience304421ms audio PASS,9completed beats,0voiceUnavailable,6sample
SoundPool/TTSduck/F10/Continue/disposal/volume contract PASS. First final
recreation started before APK install finished, returned Process crashed;
no AndroidRuntime stack observed and no root cause inferred. Retained FAIL.
Sequential recreation retry after install/audio PASS.

SAF gpx_picker FAIL21378ms: exact owned synthetic file not selectable on
external DocumentsUI. No broad storage grant/phone fallback; owned fixture
deleted. Gate remains NOT VERIFIED, not dismissed as successful.

Physical POI smoke PASS (~132s):1synthetic source-backed point, intro/quiz/
answer/chat/appSpeechStarted observed,10sF10pause/freeze/resume verified.
7842frames131.170render_seconds59.785FPS/P95upper20ms. Warm sampledPSS
151032–197128kB, thermal0,battery31.0–31.1C. Brief sample, not30/60min
endurance or real-road/GPS/ASR proof. POI theme request logged; capture after
scene expiry cannot prove actual Android landmark texture appearance. Actual
desktop GPX landmark render verified;22-scene transitions/human approval open.

Live Gemini PASS2795ms,11validatedquizzes; authored sourced fact+age only,
no child reply/audio/GPS. Parent/AI pre-QA prefs restored byte-identically;
then explicit parent-authorized permanent Gemini/cloud-text setup PASS63ms,
gemini_ready=true,parent_authorized_cloud_text=true, encrypted BYOK. Setup
makes no network request. Test APK removed, Parent launched on DeX2.
Mic/GPS grants preserved. No private XML/keys/transcripts stored here.
