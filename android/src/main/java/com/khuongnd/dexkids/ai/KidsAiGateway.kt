package com.khuongnd.dexkids.ai

import android.content.Context
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import com.khuongnd.dexkids.story.EntertainmentDirector
import com.khuongnd.dexkids.story.OfflineLearningCatalog
import com.khuongnd.dexkids.story.TripQuestionPack
import com.khuongnd.dexkids.story.CreativeLessonPack
import javax.net.ssl.HttpsURLConnection

data class KidsQuiz(val question: String, val answer: String, val followUp: String)
data class KidsAiReply(val text: String, val nextQuestion: String)

/** POI fact answers stay source-bound; creative fiction/general lessons use independent opt-in, checks and review. Never infer actual vehicle location. */
object KidsAiSafety {
    private val forbidden = Regex(
        "(?i)(vừa đi qua|đang đi qua|đã đến nơi|đang ở tại|rẽ trái|rẽ phải|" +
        "mật khẩu|số điện thoại|địa chỉ nhà|trường con học|tên đầy đủ|bố mẹ tên|https?://)")
    fun safe(text: String, maximum: Int): Boolean =
        text.length in 12..maximum && !forbidden.containsMatchIn(text) &&
        text.none { it.code < 32 || it == '\u007f' } &&
        !text.contains('<') && !text.contains('>')

    /** Refuse personal identifiers before any network request; never echo/log them. */
    fun childCloudInput(spoken: String?): String? {
        val input = spoken?.trim()?.takeIf { it.length in 2..110 } ?: return null
        if (input.any(Char::isDigit) || Regex(
            "(?i)(tên con|tên em|mình tên|con tên|nhà con|địa chỉ|số điện thoại|" +
            "trường con|lớp con|ba con tên|mẹ con tên|mật khẩu|gọi cho|sống ở)").containsMatchIn(input))
            return null
        if (input.any { it.code < 32 || it == '\u007f' }) return null
        return input
    }
}

class KidsAiGateway(context: Context) {
    private val prefs = KidsAiSettings(context)
    private val credentials = KidsAiKeys(context)
    private val appContext = context.applicationContext

    fun ready(): Boolean = KidsAiProvider.entries.any {
        prefs.active(it) && prefs.freeTierAcknowledged(it) &&
            prefs.model(it).isNotEmpty() && credentials.configured(it)
    }

    private fun providers(): List<KidsAiProvider> =
        (listOf(prefs.provider) + KidsAiProvider.entries).distinct().filter {
            prefs.active(it) && prefs.freeTierAcknowledged(it) &&
                prefs.model(it).isNotBlank() && credentials.configured(it)
        }

    /** Only a selected and parent-enabled, free-tier-acknowledged provider can be used. */
    private fun invoke(system: String, input: String, temperature: Double, maxTokens: Int,
                       responseSchema: JSONObject? = null, requirePackConsent: Boolean = false,
                       requireChildConsent: Boolean = false,
                       requireCreativeConsent: Boolean = false): String {
        check(Looper.myLooper() != Looper.getMainLooper()) { "No network on UI thread" }
        check(prefs.enabled && !Thread.currentThread().isInterrupted)
        var last: Exception = IOException("NO_PROVIDER")
        for (p in providers()) {
            try {
                val model = prefs.model(p)
                val key = credentials.load(p) ?: continue
                val url: String
                val body: JSONObject
                if (p == KidsAiProvider.GEMINI) {
                    require(model.matches(Regex("[a-zA-Z0-9._-]{1,100}")))
                    url = "https://generativelanguage.googleapis.com/v1beta/models/" +
                        model + ":generateContent"
                    body = JSONObject().put("systemInstruction", JSONObject().put("parts",
                        JSONArray().put(JSONObject().put("text", system))))
                        .put("contents", JSONArray().put(JSONObject().put("role","user")
                            .put("parts",JSONArray().put(JSONObject().put("text",input)))))
                        .put("generationConfig",JSONObject().put("temperature",temperature)
                            .put("maxOutputTokens",maxTokens)
                            .put("responseMimeType","application/json").apply {
                                responseSchema?.let { put("responseJsonSchema", it) }
                            })
                } else {
                    require(model.matches(Regex("[a-zA-Z0-9._/-]{1,100}")))
                    url = "https://api.groq.com/openai/v1/chat/completions"
                    body = JSONObject().put("model",model)
                        .put("messages",JSONArray()
                            .put(JSONObject().put("role","system").put("content",system))
                            .put(JSONObject().put("role","user").put("content",input)))
                        .put("temperature",temperature).put("max_tokens",maxTokens)
                        .put("response_format",JSONObject().put("type","json_object"))
                }
                // One persisted, shared 5-RPM limiter across pack, POI, child reply AND fallback.
                KidsAiDispatchLimiter.acquire(appContext)
                // Re-check feature-specific consent AFTER waiting for the shared rate slot.
                if (!prefs.enabled || !prefs.active(p) || !prefs.freeTierAcknowledged(p) ||
                    (requirePackConsent && !prefs.automaticPacks) ||
                    (requireCreativeConsent && !prefs.creativePacks) ||
                    (requireChildConsent && !prefs.cloudChildReply) ||
                    Thread.currentThread().isInterrupted) throw IOException("AI_DISABLED")
                val conn = URL(url).openConnection() as HttpsURLConnection
                try {
                    conn.connectTimeout = 5000
                    conn.readTimeout = 12000
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    conn.setRequestProperty("Accept", "application/json")
                    if (p == KidsAiProvider.GEMINI) conn.setRequestProperty("x-goog-api-key",key)
                    else conn.setRequestProperty("Authorization","Bearer " + key)
                    val bytes = body.toString().toByteArray(Charsets.UTF_8)
                    // Creative review includes all question/answer pairs, unlike compact POI calls.
                    check(bytes.size <= if (requireCreativeConsent) 24_576 else 8192)
                    conn.outputStream.use { it.write(bytes) }
                    val status = conn.responseCode
                    if (status !in 200..299) {
                        if (status == 429) {
                            val seconds = conn.getHeaderField("Retry-After")?.toLongOrNull()
                                ?.coerceIn(1L, 600L) ?: 60L
                            throw KidsAiRetryAfterException(seconds * 1000)
                        }
                        if (status in listOf(408,500,502,503,504)) {
                            last = IOException("AI_RETRYABLE")
                            continue // fallback only to other explicitly enabled provider
                        }
                        throw IOException("AI_PROVIDER_REFUSED")
                    }
                    val raw = conn.inputStream.use { stream ->
                        val buf = ByteArray(32769)
                        var used = 0
                        while (used < buf.size) {
                            val n = stream.read(buf,used,buf.size-used)
                            if (n < 0) break
                            used += n
                        }
                        check(used <= 32768)
                        String(buf,0,used,Charsets.UTF_8)
                    }
                    val data = JSONObject(raw)
                    val text = if (p == KidsAiProvider.GEMINI) {
                        val candidates = data.getJSONArray("candidates")
                        check(candidates.length() == 1)
                        val candidate = candidates.getJSONObject(0)
                        check(candidate.optString("finishReason") == "STOP")
                        candidate.getJSONObject("content").getJSONArray("parts")
                            .getJSONObject(0).getString("text")
                    } else {
                        val choices = data.getJSONArray("choices")
                        check(choices.length() == 1)
                        val choice = choices.getJSONObject(0)
                        check(choice.optString("finish_reason") == "stop")
                        choice.getJSONObject("message").getString("content")
                    }
                    return text.takeIf { it.length <= 16000 }
                        ?: throw IOException("RESPONSE_TOO_LONG")
                } finally { conn.disconnect() }
            } catch (e: IOException) {
                last = e
                if (e is KidsAiRetryAfterException || Thread.currentThread().isInterrupted ||
                    (e.message != "AI_RETRYABLE" && e !is java.net.SocketTimeoutException))
                    break
            } catch (_: RuntimeException) { throw IOException("INVALID_AI_OUTPUT") }
        }
        throw last
    }

    /** Generates ten or more questions about *only* an already sourced POI fact pack. */
    fun generate(poiId: String, sourceFact: String, base: KidsQuiz, age: Int): List<KidsQuiz> {
        require(poiId.matches(Regex("osm:(node|way|relation):[1-9][0-9]{0,15}")))
        require(sourceFact.length in 20..240 && age in 2..6)
        val facts = (listOf(base.answer) +
            sourceFact.split(Regex("(?<=[.!?])\\s+")).map { it.trim() })
            .filter { it.length in 12..150 }.distinct().take(5)
        require(facts.isNotEmpty())
        val prompt = JSONObject().put("age",age).put("facts",JSONArray(facts))
            .put("sampleQuestion",base.question)
            .put("require", "Generate 10-12 distinct friendly Vietnamese questions. " +
                "For every question output factIndex selecting EXACTLY one provided answer fact. " +
                "Never generate an answer sentence. Output only JSON object with questions list.")
        val system = "You prepare short, kind, safe educational quizzes for ages 2-6 in Vietnamese. " +
            "Use ONLY user-provided sourced facts, never add external history or claim current road position. " +
            "Output {\"questions\":[{\"question\":string,\"factIndex\":number,\"followUp\":string}]} " +
            "with 10-12 entries. Short friendly questions, encouraging neutral follow-up, no danger, " +
            "no personal data, no directions, no locations beyond the provided text."
        val rowSchema = JSONObject().put("type", "object")
            .put("properties", JSONObject()
                .put("question", JSONObject().put("type", "string"))
                .put("factIndex", JSONObject().put("type", "integer")
                    .put("enum", JSONArray(facts.indices.toList())))
                .put("followUp", JSONObject().put("type", "string")))
            .put("required", JSONArray(listOf("question", "factIndex", "followUp")))
            .put("additionalProperties", false)
        val schema = JSONObject().put("type", "object")
            .put("properties", JSONObject().put("questions", JSONObject().put("type", "array")
                .put("minItems", 10).put("maxItems", 12).put("items", rowSchema)))
            .put("required", JSONArray(listOf("questions")))
            .put("additionalProperties", false)
        val raw = invoke(system,prompt.toString(),0.6,1600,schema)
        val arr = JSONObject(raw).getJSONArray("questions")
        require(arr.length() in 10..20)
        val result = ArrayList<KidsQuiz>()
        val seen = HashSet<String>()
        for (i in 0 until arr.length()) {
            val row = arr.getJSONObject(i)
            val q = row.getString("question").trim()
            val follow = row.getString("followUp").trim()
            val idx = row.getInt("factIndex")
            require(idx in facts.indices && q.endsWith("?") &&
                KidsAiSafety.safe(q,150) && KidsAiSafety.safe(follow,150))
            require(seen.add(q.lowercase()))
            result.add(KidsQuiz(q,facts[idx],follow))
        }
        return result
    }

    /**
     * T-030: AI only selects from local reviewed question and word IDs.
     * No model-authored question/answer/translation is played; no GPS or child speech is sent.
     */
    fun generateTripPack(catalog: OfflineLearningCatalog, audience: EntertainmentDirector.Audience,
                         recentIds: List<String>): TripQuestionPack {
        check(prefs.enabled && prefs.automaticPacks && ready())
        val quizzes = catalog.cards().filter { it.kind() == OfflineLearningCatalog.Kind.QUIZ }
        val words = catalog.cards().filter { it.kind() == OfflineLearningCatalog.Kind.WORD }
        require(quizzes.size >= 20 && words.size >= 20)
        val options = JSONObject()
            .put("audience", audience.name)
            .put("quizOptions", JSONArray(quizzes.map {
                JSONArray().put(it.id()).put(it.topic()).put(it.minAge())
            }))
            .put("wordOptions", JSONArray(words.map {
                JSONArray().put(it.id()).put(it.topic()).put(it.minAge())
            }))
            .put("avoidQuizIds", JSONArray(recentIds.takeLast(50)))
            .put("rules", "Select 18 UNIQUE quiz IDs and same-topic word IDs from supplied lists. " +
                "For BOTH focus must rotate SAU (age 5), ONG (age 4), BOTH (age 4) each item; " +
                "for SOLO focus is audience. Respect minimum ages, >=4 different topics. " +
                "Prefer IDs not in avoidQuizIds, but use reviewed older IDs if unavoidable. " +
                "No original questions, answers, child names, GPS or location claims.")
        val row = JSONObject().put("type", "object")
            .put("properties", JSONObject()
                .put("quizId",JSONObject().put("type","string"))
                .put("wordId",JSONObject().put("type","string"))
                .put("focus",JSONObject().put("type","string")
                    .put("enum",JSONArray(listOf("SAU","ONG","BOTH")))))
            .put("required",JSONArray(listOf("quizId","wordId","focus")))
            .put("additionalProperties",false)
        val schema = JSONObject().put("type","object")
            .put("properties",JSONObject().put("items",JSONObject().put("type","array")
                .put("minItems",15).put("maxItems",20).put("items",row)))
            .put("required",JSONArray(listOf("items"))).put("additionalProperties",false)
        val raw = invoke("Select IDs only, never invent IDs. Return JSON {items:[{quizId,wordId,focus}]} " +
            "with 15-20 unique vetted ID pairs; aim for 18.",options.toString(),0.7,2800,
            schema,requirePackConsent=true)
        val response=JSONObject(raw)
        require(response.length()==1)
        val arr=response.getJSONArray("items")
        require(arr.length() in 15..20)
        val selected=(0 until arr.length()).map { index ->
            val item=arr.getJSONObject(index)
            require(item.length()==3)
            TripQuestionPack.Item(item.getString("quizId"),item.getString("wordId"),
                EntertainmentDirector.Audience.valueOf(item.getString("focus")))
        }
        return TripQuestionPack.validate(selected,catalog,audience)
    }

    /**
     * Open-ended content: model authors NEW child-sized lessons, riddles, stories and English
     * vocabulary without selecting from a finite offline ID set. Strict validation happens
     * before the caller can atomically replace the last good pack.
     *
     * A model cannot guarantee every general-knowledge answer is correct: avoid claims tied
     * to specific places/events or high-stakes topics; always keep offline fallback.
     */
    fun generateCreativePack(audience: EntertainmentDirector.Audience, sauAge: Int, ongAge: Int,
                             recentFingerprints: List<String>,
                             recentQuestions: List<String>): CreativeLessonPack {
        check(prefs.enabled && prefs.automaticPacks && prefs.creativePacks && ready())
        require(sauAge in 4..10 && ongAge in 4..10)
        val input = JSONObject()
            .put("audience",audience.name).put("sauAge",sauAge).put("ongAge",ongAge)
            .put("avoidQuestionExamples",JSONArray(recentQuestions.takeLast(10)))
            .put("requirements","Invent 15-20 completely NEW lessons (aim 16), no finite catalog. " +
                "Use at least 4 different safe topics and at least 3 genres QUIZ/STORY/RIDDLE/ENGLISH. " +
                "At least 2 explicitly imaginary STORY items; set fictional=true for each STORY. " +
                "For BOTH rotate focus SAU, ONG, BOTH each consecutive card, " +
                "with ages sauAge, ongAge, min(sauAge,ongAge). SOLO focus stays audience. " +
                "No repeated question wording or recent examples. " +
                "Each item introduces exactly one simple English word related to the question/story and its accurate Vietnamese meaning. " +
                "Every answer must directly and unambiguously answer the question. " +
                "Avoid uncertain trivia, calculations you cannot verify, names/dates/events, " +
                "child identity, GPS, real-time location, road instructions, fear, violence, " +
                "commercial messages, dangerous actions or personal questions. " +
                "Use everyday nature, animals, science, creativity, art, social skills and language. " +
                "Invent imaginary characters where appropriate and label them as fictional. " +
                "For ages 4-5: one concrete idea and 1-2 simple short sentences, no abstract puzzles. " +
                "For ages 6-7: simple comparisons and causes. Ages 8-10: richer why/how reasoning. " +
                "All Vietnamese, friendly and concise for TTS. Output JSON object only.")
        val textField=JSONObject().put("type","string")
        val row=JSONObject().put("type","object")
            .put("properties",JSONObject()
                .put("prompt",textField).put("answer",textField)
                .put("englishWord",textField).put("meaningVi",textField)
                .put("topic",textField)
                .put("genre",JSONObject().put("type","string")
                    .put("enum",JSONArray(listOf("QUIZ","STORY","RIDDLE","ENGLISH"))))
                .put("focus",JSONObject().put("type","string")
                    .put("enum",JSONArray(listOf("SAU","ONG","BOTH"))))
                .put("targetAge",JSONObject().put("type","integer"))
                .put("fictional",JSONObject().put("type","boolean")))
            .put("required",JSONArray(listOf("prompt","answer","englishWord","meaningVi",
                "topic","genre","focus","targetAge","fictional")))
            .put("additionalProperties",false)
        val schema=JSONObject().put("type","object")
            .put("properties",JSONObject().put("items",JSONObject().put("type","array")
                .put("minItems",15).put("maxItems",20).put("items",row)))
            .put("required",JSONArray(listOf("items"))).put("additionalProperties",false)
        val system="You are an expert Vietnamese preschool/primary teacher and creative children's writer. " +
            "Write original, coherent, emotionally safe, strictly age-adapted lessons for ages 4-10. " +
            "Avoid unverifiable facts; be scientifically accurate and internally consistent. " +
            "No child data, no real-time location knowledge, no claims the vehicle is near anything. " +
            "For a STORY use an imaginary setup as the prompt and a fictional ending as answer. " +
            "For other genres ask an actual question ending in ?. " +
            "Keep questions short and answers clear; vocabulary translated correctly. JSON only."
        val response=JSONObject(invoke(system,input.toString(),0.85,7500,schema,
            requirePackConsent=true,requireCreativeConsent=true))
        require(response.length()==1)
        val arr=response.getJSONArray("items")
        require(arr.length() in 15..20)
        val entries=(0 until arr.length()).map { i ->
            val v=arr.getJSONObject(i)
            require(v.length()==9)
            CreativeLessonPack.Lesson(v.getString("prompt").trim(),
                v.getString("answer").trim(),v.getString("englishWord").trim().lowercase(),
                v.getString("meaningVi").trim(),v.getString("topic").trim().lowercase(),
                CreativeLessonPack.Genre.valueOf(v.getString("genre")),
                EntertainmentDirector.Audience.valueOf(v.getString("focus")),
                v.getInt("targetAge"),v.getBoolean("fictional"))
        }
        val validated=CreativeLessonPack.validate(entries,audience,sauAge,ongAge,
            recentFingerprints)
        // Second pass: reject packs with apparent contradictions, wrong answers,
        // misleading English translations or questions too complex for target ages.
        // This improves quality but does NOT mathematically guarantee truth.
        val reviewInput=JSONObject().put("lessons",JSONArray(validated.lessons().map {
            JSONArray().put(it.targetAge()).put(it.genre().name)
                .put(it.prompt()).put(it.answer())
                .put(it.englishWord()).put(it.meaningVi())
                .put(it.fictional())
        }))
        val reviewSchema=JSONObject().put("type","object")
            .put("properties",JSONObject()
                .put("approved",JSONObject().put("type","boolean")))
            .put("required",JSONArray(listOf("approved")))
            .put("additionalProperties",false)
        val review=JSONObject(invoke(
            "You are a strict independent Vietnamese education quality checker for ages 4-10. " +
            "Reject the entire pack if ANY answer is factually inaccurate, logically inconsistent " +
            "with its question, misleading, translated incorrectly, age-inappropriate, " +
            "frightening or unsafe. Fiction is acceptable ONLY if explicitly fictional. " +
            "Do not invent facts. Return JSON {approved:boolean} only.",
            reviewInput.toString(),0.1,180,reviewSchema,
            requirePackConsent=true,requireCreativeConsent=true))
        if (review.length()!=1 || !review.getBoolean("approved"))
            throw IOException("AI_CONTENT_REVIEW_REJECTED")
        return validated
    }

    /** Optional cloud input requires independent parent consent; no raw audio or GPS is sent. */
    fun childReply(question: String, sourceFact: String, spoken: String): KidsAiReply {
        check(prefs.enabled && prefs.cloudChildReply)
        val safe = KidsAiSafety.childCloudInput(spoken) ?: throw IOException("CHILD_TEXT_BLOCKED")
        require(KidsAiSafety.safe(question,150) && sourceFact.length in 20..240)
        val input = JSONObject().put("question",question)
            .put("verifiedFact",sourceFact).put("childShortReply",safe)
        val system = "Respond as a friendly Vietnamese educational capybara for ages 2-6. " +
            "Never claim an unknown fact or physical location; only verifiedFact is reference. " +
            "Do not ask name, address, school or other private info. " +
            "If child's question needs outside knowledge, admit uncertainty. " +
            "Only output JSON {\"reply\":string,\"followUp\":string}. " +
            "Each field one short sentence; do not repeat child personal text."
        val output = JSONObject(invoke(system,input.toString(),0.35,220,
            requireChildConsent=true))
        val answer = output.getString("reply").trim()
        val follow = output.getString("followUp").trim()
        require(KidsAiSafety.safe(answer,200) && KidsAiSafety.safe(follow,150))
        return KidsAiReply(answer,follow)
    }
}
