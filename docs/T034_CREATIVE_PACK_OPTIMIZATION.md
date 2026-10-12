# T-034 — AI creative pack: 1 generate + 1 review; ages 4–7; 10 RPM

Date: 2026-10-11. Authorized source implementation from main bd71a45e.

## Requirements
- 15–20 original safe lessons, parent Sâu/Ong ages 4–7 inclusive, default 5/4.
- One creative pack generation physical request; Java validation; exactly **one** independent review physical request with all 15–20 lessons (on first-try success).
- Generator adds self-check instruction; reviewer remains strict, fail closed for any item. No skipping required review; no partially-approved last-good swap.
- Preserve AtomicFile last-good, request budget, consent, up to six pack attempts including initial, per-request provider 429 Retry-After, privacy; no child audio/GPS/identifiers sent.
- Shared persisted app limiter 10 rolling 60-second starts and >=6100ms spacing across POI, pack, child reply, retries and fallback. Real provider Free Tier quota is not modified; may 429 at a lower rate.
- Old preferences with ages 8, 9 or 10 effective-clamp to 7; cache for former age pair is ineligible and not silently deleted.
- Do not modify other repositories, CI, Android permissions, navigation, TTS, or OpenGL rendering.

## Files
- `KidsAiGateway.kt`: whole-pack review in one call; creative prompt age 4–7 and self-check.
- `KidsAiDispatchLimiter.kt` and `AiRequestLimiter.java`: 10 RPM, 6100ms pacing.
- `ParentSettings.kt`, `ParentActivity.kt`, `CreativeLessonPack.java`, `KidsAiQuizCache.kt`: age validation, picker and stored prefs clamping.
- Unit/Android QA and static contracts are updated for current bounds and review policy.

## Verification required on local environment (GitHub-only session cannot run it)
```bash
bash scripts/test-local.sh
./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug :android:assembleDebugAndroidTest
```
- Real Gemini/Groq call count: 1 author+1 independent full-pack review if pass (not per card); 15/16/20 lessons.
- 4,5,6,7 accepted ages, 8/9/10 refused; BOTH focus 5/4, 4/7, 6/7.
- Review bad content/translation/ambiguous riddles must reject full pack; return last-good; six attempts total; no unsafe auto-relaxation.
- Source-side limiter allows 10 rolling60; separate account 429/Retry-After and process restart persistence.
- No changes to consent, sensitive payloads, TTS/POI, live GPS, Single DeX; complete 30–60m Fold3 runtime.
- Adult fact/age QA for actual children's learning. Earlier QA from T-031/T-032 is not current-revision evidence.

## Risk
One reviewer may miss mistakes when reviewing 20 items; strict Java validation and fail-closed fallback remain, but factual correctness cannot be guaranteed. Provider model quotas and costs must be checked separately.

## Follow-up design — T-046 (2026-10-12)

User-selected proposal: discard invalid lessons, retain individually reviewed lessons, generate/review additional batches and merge into a complete16–20-item pack. See [T-046 filter/refill/merge](T046_AI_PACK_FILTER_REFILL_MERGE.md). This is documentation only: the current whole-pack fail-closed implementation above is unchanged. Per-item review schema and bounded refill/salvage are NOT IMPLEMENTED/NOT VERIFIED. Consent, mandatory whole-batch review, shared limiter and last-good publication gates remain required.

T-046 implementation supersedes whole-pack rejection above: per-ID batch review and immutable refill/merge now require 16–20 complete items. Evidence: `.agent/evidence/T-046-2026-10-12/`. Original T-034 transport/consent limits remain.
