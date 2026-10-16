# T-065 — latest merge and autonomous physical Fold3 verification

Owner Main; dependencies T-062/T-063/T-064. Candidate source76595b6 incorporates
T-063 richer actions/props and T-06430-story library over maina0d447b. All42
local/remote refs audited are ancestors of candidate. All13physical modes PASS; merge/publication receipt recorded separately after
source integration.

## Local verification

- Core226tests,0failures/errors/skips; production codec accepts30stories.
- Android debug+instrumentation APK and desktop classes BUILD SUCCESSFUL.
- Full scripts/test-local.sh PASS. Fixed two stale Python source-contract files
  expecting pre-T-063 active-child poses without the new `!guideActs()` guard.
  Production code is unchanged by these fixes; no new APK needed afterward.
- Installed both APKs successfully on actual SM-F926B/API35. Installed application
  hash equals local APK: accab9e2af3329bb961d6be11ed9815f93d5307308db5dec8fdcb415ea884056.
- Commands/environment: REPRODUCE.md; numeric receipt verification.json.

## Physical scope

Explicit DeX logical display2 / ARZOPA SurfaceFlinger4620382033380392964,
1920x1080. Internal displays remain unused. Raw per-mode logs and numeric results
in device-results.jsonl. Fixture finally blocks restore preferences/scoped data.
No real AI provider, GPS injection/field navigation or child ASR requested here.

Actual t063_visual PASS:44action/prop cases,88A/B FullHD captures for SAU/ONG/BOTH
and Capybara. Checked44-case contact sheet and full-size Sâu INSPECT/WATER and
Capybara READ: associated props visible, guide attachment at guide, children
neutral during guide action. Existing pose art plus finite body transforms are
reused; screenshots alone do not prove realistic articulated motion. Local image
files/manifest retained; new-art human approval remains outside automated QA.

t063_lifecycle, t062_remote, t062_playback, t054_story_safety,
t054_story_failures, t057_cancellation, t057_manager,
t059_replay_no_network, recreation, single_display_mouse and parent_controls
PASS. Remote tests use mocked HTTPS with real SQLite/Activities; do not relabel
this as new real GitHub HTTPS/offline-radio verification. Manager assertions
cover source-dialog footer/HTTP rejection, library search/filter, approval,
edit/cancel/cue repair, delete/undo and replay. Parent/mouse checks cover actual
DeX UI, immediate parent access, pause/resume, audio-only and AI-disable actions.

Final actual180.581s story smoke PASS:3story loads,16captures,30TTS-active
samples,1,504.09m synthetic DEMO distance advance and POI downstream pause/resume.
This short run did not complete all audience episodes (`completed_audiences=[]`);
all-audience remote load assertions belong to t062_playback, not this smoke.
No real GPS/ASR or real provider call is claimed.

## Preserved failures and limits

Initial environment failures (JAVA_HOME and Python art module path) and both stale
pose assertions retained in local logs. Server restart interrupted the initial
visual host supervisor; that unrecorded terminal result is not counted as PASS.
Repeated full visual mode with direct file logging PASS136.02s.

This is a merge/build/device regression slice. It does not close old bridge
semantic/native-memory findings, broad field/navigation/content/signing/license
acceptance, human audio/art approval, or60-minute endurance on this APK.

Final smoke resource sample: {"elapsed_ms": 171071, "frames": 10259, "render_seconds": 171.03156464442145, "average_fps": 59.98307985621658, "p95_upper_ms": 20, "draw_calls": 11, "pss_kib": 167297, "managed_textures": 10}.
