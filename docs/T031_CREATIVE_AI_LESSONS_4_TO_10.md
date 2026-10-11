# T-031 · AI tự sáng tạo bài học vô hạn theo tuổi 4–10 (source implementation)

**Status: IN_PROGRESS / DRAFT.** Code authored on `feat/creative-ai-lessons-ages-4-10`; no new Gradle, provider, simulator or Samsung Fold3 DeX tests were executed in this GitHub connector environment. Do not describe this implementation as release-verified.

## What "unlimited" means
There is **no finite catalog of allowed questions, story ideas, riddles, topics or English words** for this creative mode. A verified provider may author another 15–20-card pack on a new trip and when the current pack is exhausted. It does **not** mean unlimited API traffic, billing, token consumption or infinite content in a single response. All physical requests, including the second-stage quality review and retries, use the existing persisted **5 starts/rolling 60 seconds and >=12.1s spacing**. Provider model/API daily and billable quotas still apply.

T-030's curated 120 quiz/80 word pack and all offline entertainment remain unchanged and available as fallback. The T-016 POI-specific fact-index generation remains separate: AI cannot invent fact claims from GPS or assert the car has arrived somewhere.

## Parent setup
1. On the same Fold3 DeX monitor: Cài đặt → Độ tuổi bài học → select Sâu and Ong independently (each 4–10; defaults Sâu 5, Ong 4). BOTH distributes turns Sâu, Ong, BOTH; the shared turn uses the younger age.
2. AI Kids → configure Gemini/Groq key, specific model and verified Free Tier/quota, then enable **AI tự sáng tạo nội dung mới · 4–10 tuổi**. This is an **independent explicit opt-in, OFF by default**; it also enables automatic question packs.
3. Start a NEW trip. The app plays last-good content while generating a new pack asynchronously. If there is no saved AI pack, use curated/offline activities immediately. If AI or its reviewer fails, keep the prior pack and retry at most five additional times; refill on exhaustion, not every render frame.
4. Parent can revoke automatic generation/AI on the F10 menu during the trip, or disable creativity in settings and delete saved packages. Cloud child transcript sharing is separately consented and remains OFF unless independently enabled.

## Creative lesson contract
- Model authors **15–20** original items across >=4 topic labels and >=3 of QUIZ, STORY, RIDDLE, ENGLISH. At least two imaginative stories are explicitly fictional. Each item has a question/short setup, a direct answer/story ending, a simple English word, its Vietnamese meaning, focus and exact target age.
- Ages 4–5: brief concrete one-idea sentences, no abstract or advanced concepts. Ages 6–7: basic comparisons and reasons. Ages 8–10: richer why/how questions, still short enough to hear in the car. Per-child age checked in the Java core validator before playback or persistence.
- Built-in deterministic checks reject malformed schema, non-age-matched focus, duplicates including a bounded recently heard hash history, too little topic/genre diversity, invalid text and selected unsafe/location/private-data phrases.
- **Second AI call** explicitly reviews question-answer consistency, factual soundness, English translations and age/safety; if it rejects, keep last-good. The second model pass **does not guarantee truth**. Known hard limits: facts outside the app's vetted catalog are not independently source-backed; translated words can be wrong; semantic near-duplicates may escape normalized-hash dedup; natural-language content filters cannot catch all unsuitable output. Adult supervision and content review are required.
- Never send GPS, location traces, captured child audio, raw child transcript, names or personal data to the creative-pack API. Age, non-identifying speaker enum and previous AI-authored questions are allowed to shape the lesson; API credentials stay in AndroidKeyStore.
- Independent AtomicFile cache per audience and age pair, SHA-256 integrity check, cursor and last-100 question hashes. Pack saved only after successful full validation and AI review; interruptions preserve previous pack.

## Local verification required
```bash
./scripts/test-local.sh
./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug
```

On actual SM-F926B Fold3 DeX, verify: AI enabled+OFF creative uses old curated logic; ages 4/5/6/8/10 and BOTH rotation; initial 15–20 unique lessons with direct answers; four genres and vocabulary; absence of child PII/GPS in outbound payload; reviewer rejection; invalid/offline/timeout/429 and up to five retries; cache survival across app restart and power loss; refill after exhaustion; parent F10 revocation; POI priority; microphone and no-crosstalk; >30 min stable FPS/audio with adult reviewing content. Ensure model output token limits and 5-RPM/daily quota for the **actual configured provider**.

**Acceptance gates remain open:** generation/quality review through real provider, actual on-device compile and unit test, child-age appropriateness verified by adult, 30–60 minute Fold3 DeX drive and privacy/billing observation. Never auto-merge on source checks alone.
