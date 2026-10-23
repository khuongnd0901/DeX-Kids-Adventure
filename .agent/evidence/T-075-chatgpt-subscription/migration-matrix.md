# T-075 inference audit and migration matrix

Baseline Kids `81fa123ed8a89e47b2326692c7d7a868532540f3`.
Assistant reference `a52ddf0ecf3022922401f222f4db2d71282d3a9d`, fetched 2026-10-23.
All statuses below describe implementation; actual test receipts are separate.

| Feature / caller | Before | After / retained contract |
| --- | --- | --- |
| Context-aware Trip Companion / KidsActivity | Offline episode planner + optional AI cache/packs | Offline planner unchanged; same enrichment entry points through subscription gateway |
| POI quiz / parent cache / KidsAiQuizCache | Gateway.generate → Gemini/Groq | Gateway.generate → shared subscription; factIndex resolves local sourced fact, age/dayPeriod validation and 14-day age/source cache unchanged |
| Trip Question Pack / TripPackCoordinator | generateTripPack → selected provider/fallback | Same TripPackGateway injectable seam, ID pairs/rotation/topics validation, saved cursor and atomic last-good replacement; no subscription-error retry/replay |
| Creative Lesson Pack | generation + whole-batch review + reviewed pool | Same schedule/ages/genres, 20 candidates, independent review IDs/verdicts, pool/refill/validation and provenance; shared subscription for both calls |
| Story generation / StoryAuthor | storyRequest + independent review | Same topic privacy filter, JSON codec/allowlists, configured4–7ages (defaults5/4) and educational contract; shared subscription |
| Regenerate scene / StoryAuthor | full episode proposal + review | Same unchanged-scene/question-target/ID checks; shared subscription |
| Story review vs approval | AI review + explicit adult repository approval | Review uses subscription; adult approval remains separate; no automated approval change |
| Child answer / KidsActivity.childReply | Separate explicit child-text consent, privacy filter, sourced facts, epoch guards | Same boundaries; new subscription consent required as well; no audio or coordinate input added |
| Saved stories/question/creative replay | SQLite/AtomicFile/cache/offline catalog | Schemas/files/preferences untouched; no migration/delete/rewrite; no network needed for replay |
| Parent inference test | Provider API-key configuration/probes | Deliberate safe Vietnamese JSON test; catalog-only model picker; subscription opt-in OFF initially |
| YouTube Data API v3 | Separate metadata/key/preview/player | Unchanged; not an AI inference provider |

Transport audit: all production AI HTTPS previously lived in KidsAiGateway.invoke.
StoryAuthor uses storyRequest; no independent model HTTP adapter found. Removed
KidsAiKeys/KidsAiProvider implementation and all provider selection/key UI paths;
retained KidsAiSettings business opt-ins in the same preference namespace. Existing
legacy encrypted credential files and Keystore aliases are never read or deleted
by new runtime code. KidsAiDispatchLimiter has no provider logic and remains the
shared persisted physical-start budget (10 starts/60s, >=6100ms spacing).
Subscription errors do not enter coordinator retry; deterministic schema/content
validation failures retain existing bounded acquisition behavior.

## Reuse / identity / limits

Adapted Assistant auth/{ChatGptAuthManager,ChatGptOidcVerifier,ChatGptSessionStore,
ChatGptHttp}, account catalog and terminal SSE transport. Assistant's
ChatGptSubscriptionProvider/settings were inspected; device tool namespaces,
action router, BYOK fallback, command proofs and history were excluded. Kids uses
KidsSubscriptionAiClient and its own ChatGptSettings instead. MIT POC notice is
retained under third-party/; no token/client registration/credentials copied.

Kids uses `agent_name_hint=DeX Kids Adventure`, a new stable local host UUID,
issued per-install `oaiapp_` registration, and loopback
`http://127.0.0.1:<ephemeral>/auth/callback`. Keystore alias is
`dexkids-chatgpt-subscription-v1`; encrypted AtomicFile is in noBackupFilesDir.
PKCE S256/state/nonce/OIDC signature+issuer+audience+lifetime checks, synchronized
refresh rotation, terminal-session invalidation, local logout and remote revoke
remain. Browser sign-in is parent initiated only. No registered Assistant ID or
private ChatGPT/Codex endpoint is reused.

Response constraints: input/instructions, store=false/stream=true. Account model
catalog supplies visible slug choices. Unsupported temperature/max_output_tokens
are omitted. JSON schema is supplied in instructions and existing strict business
validators remain authoritative; provider schema enforcement is not claimed.
Request byte caps 8192/49152, final text <=16000 code points, bounded2MiB SSE,
20s inference/15s read/5s connect, auth calls<=30s, login<=180s. Instruction-only
generation length may hit local limits: reject and retain offline/last-good content.
Consumer interrupt/consent/model/session changes cancel HTTP and fence late output;
shared refresh remains bounded rather than cancelled by an unrelated consumer.
Only pre-stream401 permits one refreshed reissue; each start counts the rate gate.
No automatic stream replay, paid fallback, silent browser or background login.

Official documentation fetched and read 2026-10-23:
- https://developers.openai.com/siwc/quickstart
- https://developers.openai.com/siwc/token-sharing-open-source/sign-in
- https://developers.openai.com/siwc/token-sharing-open-source/models-and-inference
- https://developers.openai.com/siwc/token-sharing-open-source/preview-limitations

LIVE_AUTH_NOT_VERIFIED until owner separately signs in/consents in Kids. Reference
Assistant live/physical results are not Kids evidence. Multi-account registration
picker is not implemented (single saved account, reconnect must match subject).
Physical Fold3/DeX and Vietnamese audio/vehicle/navigation behavior remain open.

Configured-age follow-up: [current verification](configured-ages/SUMMARY.md)
supersedes fixed5/4prompt assumptions. Defaults remain5/4; shared content uses the
younger configured age. Existing saved content is not automatically rewritten.
