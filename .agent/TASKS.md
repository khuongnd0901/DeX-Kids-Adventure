# DeX Kids Adventure — task tracker
Updated 2026-10-09
Allowed states: BACKLOG, PLANNED, READY, IN_PROGRESS, BLOCKED, DONE. No DONE without actual acceptance.

| ID | Milestone | Issue | Status | Evidence / unresolved gate |
| --- | --- | --- | --- | --- |
| T-001 | M0 | #2 | IN_PROGRESS | Build [37795183419](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37795183419); Fold3 smoke/private repo open |
| T-002 | M1 | #3 | IN_PROGRESS | OpenGL 720p/1080p [37806245673](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37806245673) PASS; real sustained Fold3 FPS and lifecycle open |
| T-003 | M2 | #4 | IN_PROGRESS | Capybara facial-landmark test, 5 extra characters + 6 vehicles, 29-sprite atlas and OpenGL visual review [CI 37879894270](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37879894270) SUCCESS | Penguin and companion visibility [CI 37880239651](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37880239651) PASS; final art signoff, simulator/Z Fold3/DeX, lipsync, long-run visual seams |
| T-004 | M3 | #5 | IN_PROGRESS | GPX unit tests [37796769631](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37796769631); real loss/recovery open |
| T-005 | M4 | #6 | IN_PROGRESS | Live GPS adapter [37798067824](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37798067824); verified OSM database open |
| T-006 | M5 | #11 | IN_PROGRESS | Narration scaffold [37797120923](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797120923); approved facts/audio on Z Fold3 open |
| T-007 | M6 | #7 | IN_PROGRESS | Day/night and session events [37797221624](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797221624); verified geo context open |
| T-008 | M7 | #8 | IN_PROGRESS | Parent UI [37797413766](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797413766); PIN/audio-only/DeX independent UX open |
| T-009 | M8 | #9 | IN_PROGRESS | Screen routing/stop IPC [37797528265](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797528265); physical DeX and Assistant signing/open |
| T-010 | M9 | #10 | BLOCKED | Virtual soak [37797706168](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37797706168); real Fold3 60-minute run/signing/licensing open |
| T-011 | M0–M9 local simulator QA | #12 / draft PR #1 | IN_PROGRESS | Main agent; T-001–T-010 dependencies. Baseline56corePASS; integrated artwork58corePASS/29atlasPASS/buildSUCCESS; real3600s emulator child lifetime/expiry, rendering/recreation/IPC/denial/offline/post-fix smoke verified. Mandatory60cases40PASS/1BLOCKED/1NOT_RUN/8NOT_IMPLEMENTED/10NOT_VERIFIED; retained historical failures/incidents explicit. Physical Fold3/DeX/data/content/release gates open. Evidence `.agent/evidence/simulator/final-test-summary.md`; active T-011 plan. Integrated123s smokePASS; observed19.825FPS prefix below30FPS (FAIL), controlled profiling and Android GPX next. Published QA cdb9a17/receipt1b2958b; source integration worktree documented in STATUS. |

Latest source work: M2 animation test, editable art, layered renderer and GIF capture. Production voice pipeline and Device Owner from DeX-Assistant remain unchanged.


T-011 follow-up: local T-003 merge conflict resolved in8bc27c6; exact tree matches fc308b9; original unstaged changes retained. Evidence `.agent/evidence/T-011-conflict-resolution-2026-10-09.md`.

T-011 commit-all: all remaining tracked changes audited as CRLF/LF only; normalized LF with Git text attributes. Source behavior unchanged; artifact ignore rules retained.
