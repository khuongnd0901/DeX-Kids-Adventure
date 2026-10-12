# T-046 verification — 2026-10-12

Owner Main; base 611314115a091b240fb618022841c91a85741524.
Implemented: 20-item generation, local per-item salvage, one complete per-ID review,
immutable worker-cycle staging, bounded six attempts, final 16–20 gate, atomic v2
cache with batch/question and actual provider/model provenance; legacy valid v1 load.
Transport quota, pacing, Retry-After, consent and offline fallback are unchanged.

Verification on current source:
- Local Python contracts PASS; 171 JVM tests, zero failures/errors/skips.
- Android unit target NO-SOURCE. Debug APK + instrumentation APK BUILD SUCCESSFUL.
- Physical Samsung SM-F926B: creative_review, creative_cache (all audiences,
  legacy/v2 provenance roundtrip), creative_coordinator and ai_limiter PASS.
- TC01/02/04/05/11: ReviewedLessonPoolTest; TC03: Android JSON parser.
- TC06/07/08/09/10: real Android coordinator/AtomicFile with simulated provider
  timeout/429/5xx/refusal/malformed and blocked-I/O lifecycle, six-attempt bounds.
- TC12: actual configured Gemini, BOTH ages 5/4, 27.260 seconds. First attempt
  CREATIVE_REFILL_REQUIRED; second completed 20 reviewed lessons. Exactly two
  generation and two review starts, four HTTP 200 responses observed in tagged log.
  Revoked consent blocked further generation. No GPS/audio/child text submitted.

Not verified: human factual/age acceptance, physical power-loss, long playback of
this actual generated pack. Real generation/merge and isolated cache/playback
orchestration were tested separately; do not claim one end-to-end real-pack soak.
No generated prose, credentials or personal data are included in this evidence.

Initial local failures were missing JAVA_HOME/Python art path, one overlapping
atlas-generation race, and a corrected Kotlin schema parenthesis. Final checks
ran sequentially using installed JDK17, SDK and cached art Python dependencies.
