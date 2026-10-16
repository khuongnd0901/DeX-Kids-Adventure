# T-062 — Remote interactive story source

Owner Main. Dependencies T-054–T-061. User authorized implementation 2026-10-16.

Implement one configurable public HTTPS URL containing strict UTF-8 JSON
`{"schemaVersion":1,"stories":[StoryEpisode...]}`. Parent-authored remote stories
are immediately playable after deterministic validation, without AI or approval.
Preserve local library and speech/rendering behavior. Sync in background on
foreground, five-minute throttle, manual refresh, conditional requests and offline
last-good cache. Validate whole pack before atomic activation; defer activation
while any story controller holds a playback lease. Removed stories are ineligible
for future selection. Remote copies are read-only, with local draft-copy action.

Implementation: core pack codec/checker/samples; SQLite v1→v2 additive migration
with independent remote provenance/content tables; process coordinator and Android
foreground lifecycle integration; parent source controls and status.

Acceptance: core malformed/duplicate/bounds cases; real SQLite migration and
add/update/remove/isolation/restart/lease cases; HTTP conditional/error/timeout
checks; Android debug and test APK build; existing story regression checks.
Physical DeX runtime gates require equipment; report NOT VERIFIED if unavailable.
Record exact commands/results and retained failures under `.agent/evidence/T-062/`.
Do not commit/push unless requested. Preserve pre-existing local artifacts and
STATUS/TASKS synchronization notes.

Progress: implementation complete; 220 core tests, sample checker, Android APKs
and desktop compile PASS. Real API35 FullHD emulator migration/sync/playback/UI/
legacy lifecycle/replay/cancellation assertions PASS. Evidence:
`.agent/evidence/T-062/SUMMARY.md`. Initial missing display argument retained.
Physical DeX and live user-hosted URL NOT VERIFIED (no hardware/URL supplied).
Plan remains active and task IN_PROGRESS under equipment verification rule.


Follow-up 2026-10-16: user requested public test content in the app repository.
Published only the sample JSON on main d24e0f9; actual anonymous HTTPS fetch and
core Java client/codec PASS with3audiences and conditional304. Local implementation
preserved after content-only fast-forward. Evidence `T-062/PUBLISHED_SAMPLE.md`.
This supersedes the absence of a live sample URL above; physical DeX/Android live
host acceptance remains NOT VERIFIED.


Windows live-host execution authorized 2026-10-16: use existing app APK and new
framework-only live QA modes; actual visible source-dialog configuration, Android
HTTPS download and conditional GET, SAU/ONG/BOTH narration-to-question, external
force-stop + disconnected network/cache/cursor recovery, restore test preferences
and network. Keep requested source URL configured and simulator library visible.
Record live evidence under `.agent/evidence/T-062/windows-live/`.


Windows live-host verification executed: actual Android public GitHub source UI,
conditional304, SAU/ONG/BOTH narration→question and externally force-stopped
no-network cache/cursor recovery PASS. Product APK unchanged. Final network and
parent/AI/autoplay preferences restored; public URL intentionally retained and
simulator library visible. Initial SystemUI/QA-harness failures retained. Evidence
`.agent/evidence/T-062/windows-live/RESULTS.md`. This supersedes Android live-host
NOT VERIFIED notes above. Physical DeX remains NOT VERIFIED; keep plan active.


Authoring prompt follow-up 2026-10-16: user requested topic-driven prompts that
choose current actions/scenes. Published self-contained parent copy/paste prompt
on main25b679c, with all finite cues, spatial/quantity constraints, schema/example
and safe old-pack append workflow. Authored embedded example passes actual pack
validator; current enum coverage and anonymous Raw bytes match. Evidence
`.agent/evidence/T-062/AUTHORING_PROMPT.md`. Documentation-only; no AI calls,
product code/APK changes, or new physical acceptance claims.


Publication explicitly authorized 2026-10-16: commit and publish verified source,
open/merge PR into main. Final build/pack/core220 checks PASS (Gradle up-to-date
verification for unchanged tasks). Record main/PR/tree receipt after merge; keep
physical DeX gate open. Missing CLI credential helper handled by existing GitHub
connector, without changing user's credential configuration.
