package com.khuongnd.dexkids.ai

import android.content.Context
import com.khuongnd.dexkids.story.DayPeriod
import android.os.Looper
import com.khuongnd.dexkids.ParentSettings
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

class KidsAiGateway(context: Context) : TripPackGateway {
    private val prefs = KidsAiSettings(context)
    private val credentials = KidsAiKeys(context)
    private val appContext = context.applicationContext
    // Only validated AI-authored text, held in this worker's gateway; never child input or disk cache.
    private var creativeCorrections = JSONArray()
    private var creativeValidationCorrection: JSONObject? = null

    override fun ready(): Boolean = KidsAiProvider.entries.any {
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
                                // This supported Lite model otherwise defaults to minimal reasoning.
                                // Creative generation/review stays background-only and within its token cap.
                                if(requireCreativeConsent && model=="gemini-3.5-flash-lite")
                                    put("thinkingConfig",JSONObject().put("thinkingLevel","medium"))
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
                    // Creative authoring/review with medium thinking exceeded the measured 12s budget.
                    conn.readTimeout = if (requireCreativeConsent) 30000 else 12000
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
    @JvmOverloads
    fun generate(poiId: String, sourceFact: String, base: KidsQuiz, age: Int,
                 dayPeriod: DayPeriod = DayPeriod.current()): List<KidsQuiz> {
        require(poiId.matches(Regex("osm:(node|way|relation):[1-9][0-9]{0,15}")))
        require(sourceFact.length in 20..240 && age in 4..10)
        val facts = (listOf(base.answer) +
            sourceFact.split(Regex("(?<=[.!?])\\s+")).map { it.trim() })
            .filter { it.length in 12..150 }.distinct().take(5)
        require(facts.isNotEmpty())
        val prompt = JSONObject().put("age",age).put("facts",JSONArray(facts))
            .put("dayPeriod",dayPeriod.label())
            .put("sampleQuestion",base.question)
            .put("require", "Generate 10-12 distinct friendly Vietnamese questions. " +
                "For every question output factIndex selecting EXACTLY one provided answer fact. " +
                "Never generate an answer sentence. Output only JSON object with questions list.")
        val system = "You prepare short, kind, safe age-adjusted educational quizzes for ages 4-10 in Vietnamese. " +
            "Use ONLY user-provided sourced facts, never add external history or claim current road position. " +
            "Output {\"questions\":[{\"question\":string,\"factIndex\":number,\"followUp\":string}]} " +
            "with 10-12 entries. Short friendly questions, encouraging neutral follow-up, no danger, " +
            "no personal data, no directions, no locations beyond the provided text. " +
            "Adapt questions and follow-ups to dayPeriod and age. Invite imagination about this period, " +
            "never assert observed light, weather, an arrival or actual surroundings. " +
            "At evening use calm prompts; at morning invite gentle observation. Include the exact provided dayPeriod in at least one question or followUp. " +
            "The facts come from a locally selected sourced POI catalogue; do not claim arrival or road matching."
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
        require(arr.length() in 10..20) { "QUIZ_COUNT_INVALID" }
        val result = ArrayList<KidsQuiz>()
        val seen = HashSet<String>()
        for (i in 0 until arr.length()) {
            val row = arr.getJSONObject(i)
            val q = row.getString("question").trim()
            val follow = row.getString("followUp").trim()
            val idx = row.getInt("factIndex")
            require(idx in facts.indices && q.endsWith("?") &&
                KidsAiSafety.safe(q,150) && KidsAiSafety.safe(follow,150)) { "QUIZ_ROW_INVALID: index=$i;questionLength=${q.length};followLength=${follow.length};factIndex=$idx;questionMark=${q.endsWith("?")}" }
            require(seen.add(q.lowercase())) { "QUIZ_QUESTION_DUPLICATED" }
            result.add(KidsQuiz(q,facts[idx],follow))
        }
        require(result.any { it.question.contains(dayPeriod.label(), ignoreCase = true) ||
            it.followUp.contains(dayPeriod.label(), ignoreCase = true) }) { "QUIZ_DAY_PERIOD_MISSING" }
        return result
    }

    /**
     * T-030: AI only selects from local reviewed question and word IDs.
     * No model-authored question/answer/translation is played; no GPS or child speech is sent.
     */
    override fun generateTripPack(catalog: OfflineLearningCatalog, audience: EntertainmentDirector.Audience,
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
    override fun generateCreativePack(audience: EntertainmentDirector.Audience, sauAge: Int, ongAge: Int,
                             recentFingerprints: List<String>,
                             recentQuestions: List<String>): CreativeLessonPack {
        check(prefs.enabled && prefs.automaticPacks && prefs.creativePacks && ready())
        require(sauAge in 4..10 && ongAge in 4..10)
        val schedule=JSONArray((0 until 20).map { index ->
            val focus=CreativeLessonPack.focusAt(audience,index)
            JSONObject().put("index",index).put("focus",focus.name)
                .put("targetAge",CreativeLessonPack.ageForFocus(focus,sauAge,ongAge))
        })
        val input = JSONObject()
            .put("audience",audience.name).put("sauAge",sauAge).put("ongAge",ongAge)
            .put("lessonSchedule",schedule)
            .put("previousReviewCorrections",creativeCorrections)
            .put("previousValidationFailure",creativeValidationCorrection ?: JSONObject.NULL)
            .put("avoidQuestionExamples",JSONArray(recentQuestions.takeLast(10)))
            .put("requirements","Invent 15-20 completely NEW lessons (aim 16), no finite catalog. " +
                "Use at least 4 different safe topics and at least 3 genres QUIZ/STORY/RIDDLE/ENGLISH. " +
                "At least 2 explicitly imaginary STORY items; set fictional=true for each STORY. " +
                "Every STORY prompt must explicitly contain 'tưởng tượng', 'cổ tích' or 'hư cấu' so children hear the fiction label. " +
                "For BOTH rotate focus SAU, ONG, BOTH each consecutive card, " +
                "with ages sauAge, ongAge, min(sauAge,ongAge). SOLO focus stays audience. " +
                "Follow the first 15-20 entries of lessonSchedule in order; write each lesson for that entry's targetAge. " +
                "The app owns age/focus; do not output targetAge, focus or index fields. " +
                "No repeated question wording or recent examples. " +
                "Each item introduces exactly one simple English word related to the question/story and its accurate Vietnamese meaning. " +
                "Choose a precise word for the main subject, not a misleading broader animal category. " +
                "Translate cat as con mèo, kitten as mèo con, whale as cá voi; never label a whale fish. " +
                "Check literal vocabulary: acorn means hạt sồi, chestnut means hạt dẻ; " +
                "marigold means hoa cúc vạn thọ, glasses means kính mắt, magnifying glass means kính lúp. " +
                "Keep englishWord and meaningVi equally specific: fish means con cá, goldfish means cá vàng; " +
                "goat means con dê, mountain goat means dê núi. " +
                "For ENGLISH questions ask exactly the noun in meaningVi, not a narrower species or object subtype. " +
                "Every answer must directly and unambiguously answer the question. " +
                "For ENGLISH lessons, answer in a complete Vietnamese sentence containing the English word, " +
                "for example Từ tiếng Anh cho con ong là bee. Never return only a bare English word. " +
                "Avoid uncertain trivia, calculations you cannot verify, names/dates/events, " +
                "child identity, GPS, real-time location, road instructions, fear, violence, " +
                "commercial messages, dangerous actions or personal questions. " +
                "Use everyday nature, animals, science, creativity, art, social skills and language. " +
                "Invent imaginary characters where appropriate and label them as fictional. " +
                "For ages 4-5: one concrete idea and 1-2 simple short sentences, no abstract puzzles. " +
                "Aim for 10-16 Vietnamese words in each age4-5 prompt and 8-18 in its answer, " +
                "leaving room below the hard word limits; count each space-separated syllable as a word. " +
                "For ages 4-5 avoid WHY explanations of chlorophyll/photosynthesis/condensation/atoms or light speed. " +
                "Use visible colors/shapes, familiar animals, everyday kindness and simple explicitly fictional stories instead. " +
                "Write proper Vietnamese WITH all diacritics in prompt, answer and meaningVi; never romanize Vietnamese. " +
                "Do not address children by names or aliases; use 'con' or imaginary animal characters. " +
                "Riddles need distinguishing clues giving one clear answer, not merely a red sweet fruit. " +
                "Each riddle must provide at least two distinctive observable clues. " +
                "Every clue must literally fit the answer's real shape or parts; color and season alone are not distinctive. " +
                "Do not explain floating merely by low weight or air in leaf veins; use a different familiar idea if unsure. " +
                "Avoid sunflower sun-tracking trivia and whale water-spout questions: they often spread misconceptions. " +
                "For identification questions in any genre, give clues that distinguish the answer from other familiar objects. " +
                "Prefer distinctive object functions or familiar animal features over fruit/flower guessing. " +
                "Red peel, sweetness and many small seeds do not uniquely identify an apple. " +
                "Do not define an object by an optional shape: pencil cases can have different shapes. " +
                "Learn from previousReviewCorrections: replace the flawed idea instead of repeating it. " +
                "Correct previousValidationFailure using its item index/code and the length/fiction/safety rules below. " +
                "Each prompt and answer must be 14-135 and 8-135 characters respectively for ages 4-5, " +
                "with at most 23 prompt words and 28 answer words. For ages 6-10 the character limits are 14-190 and 8-190. " +
                "topic is a lowercase ASCII slug of 3-32 characters matching [a-z][a-z0-9-]{2,31}. " +
                "englishWord is 2-30 lowercase ASCII letters, spaces or hyphens; meaningVi is 2-75 characters. " +
                "The combined answer plus ' Từ tiếng Anh: '+englishWord+' nghĩa là '+meaningVi+'.' must fit within 240 characters. " +
                "For ages 6-7: directly observable comparisons and causes with familiar objects, one easy reasoning step. " +
                "Avoid scientific mechanisms, technical biology/astronomy terms and abstract metaphors at ages 6-7. " +
                "Ages 8-10: richer why/how reasoning in familiar everyday situations, but no specialist jargon. " +
                "For 16 items aim for 6 imaginative STORY, 4 ENGLISH, 4 concrete QUIZ and 2 unambiguous RIDDLE lessons. " +
                "All Vietnamese, friendly and concise for TTS. Output JSON object only.")
        val textField=JSONObject().put("type","string")
        val row=JSONObject().put("type","object")
            .put("properties",JSONObject()
                .put("prompt",textField).put("answer",textField)
                .put("englishWord",textField).put("meaningVi",textField)
                .put("topic",textField)
                .put("genre",JSONObject().put("type","string")
                    .put("enum",JSONArray(listOf("QUIZ","STORY","RIDDLE","ENGLISH"))))
                .put("fictional",JSONObject().put("type","boolean")))
            .put("required",JSONArray(listOf("prompt","answer","englishWord","meaningVi",
                "topic","genre","fictional")))
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
            require(v.length()==7)
            val focus=CreativeLessonPack.focusAt(audience,i)
            try { CreativeLessonPack.Lesson(v.getString("prompt").trim(),
                v.getString("answer").trim(),v.getString("englishWord").trim().lowercase(),
                v.getString("meaningVi").trim(),v.getString("topic").trim().lowercase(),
                CreativeLessonPack.Genre.valueOf(v.getString("genre")),
                focus,CreativeLessonPack.ageForFocus(focus,sauAge,ongAge),v.getBoolean("fictional"))
            } catch (failure: IllegalArgumentException) {
                val code=failure.message.orEmpty()
                if(code.matches(Regex("LESSON_[A-Z_]{1,60}"))) {
                    creativeValidationCorrection=JSONObject().put("index",i).put("code",code)
                        .put("targetAge",CreativeLessonPack.ageForFocus(focus,sauAge,ongAge))
                        .put("promptMinChars",14).put("answerMinChars",8)
                        .put("maxChars",if(CreativeLessonPack.ageForFocus(focus,sauAge,ongAge)<=5)135 else 190)
                    throw IllegalArgumentException(code+"_AT_ITEM_"+i)
                }
                throw failure
            }
        }
        val validated=CreativeLessonPack.validate(entries,audience,sauAge,ongAge,
            recentFingerprints)
        creativeValidationCorrection=null
        // Second pass: reject packs with apparent contradictions, wrong answers,
        // misleading English translations or questions too complex for target ages.
        // This improves quality but does NOT mathematically guarantee truth.
        val reviewLessons=validated.lessons().mapIndexed { index,it ->
            JSONObject().put("index",index).put("targetAge",it.targetAge()).put("genre",it.genre().name)
                .put("prompt",it.prompt()).put("answer",it.answer())
                .put("englishWord",it.englishWord()).put("meaningVi",it.meaningVi()).put("fictional",it.fictional())
        }
        val issueCodes=listOf("AGE_TOO_HARD","FACT_ERROR","ANSWER_MISMATCH","TRANSLATION_ERROR",
            "AMBIGUOUS_RIDDLE","MISSING_FICTION_LABEL","MISSING_DIACRITICS","SAFETY")
        val issueSchema=JSONObject().put("type","object").put("properties",JSONObject()
            .put("index",JSONObject().put("type","integer"))
            .put("code",JSONObject().put("type","string").put("enum",JSONArray(issueCodes)))
            .put("reason",JSONObject().put("type","string")))
            .put("required",JSONArray(listOf("index","code","reason"))).put("additionalProperties",false)
        val reviewSchema=JSONObject().put("type","object")
            .put("properties",JSONObject()
                .put("approved",JSONObject().put("type","boolean"))
                .put("issues",JSONObject().put("type","array").put("maxItems",5).put("items",issueSchema)))
            .put("required",JSONArray(listOf("approved","issues")))
            .put("additionalProperties",false)
        for(batch in reviewLessons.chunked(4)) {
            val batchIndices=batch.map { it.getInt("index") }.toSet()
            val reviewInput=JSONObject().put("lessons",JSONArray(batch))
            val review=JSONObject(invoke(
                "You are a strict independent Vietnamese education quality checker for ages 4-10. " +
                "Audit each of these at most four lessons carefully; do not skim. Return the original indices supplied. " +
                "Reject the entire pack if ANY answer is factually inaccurate, logically inconsistent " +
                "with its question, misleading, translated incorrectly, age-inappropriate, " +
                "frightening or unsafe. Fiction is acceptable ONLY if explicitly fictional. " +
                "Reject Vietnamese missing diacritics, ambiguous riddles with several equally valid answers, " +
                "or a vocabulary word whose meaning disagrees with the answer. " +
                "Check every question premise and every riddle clue against its answer, including real object parts. " +
                "Color and flowering season alone cannot uniquely identify a flower. " +
                "A chair does not have a table on top. Floating is not explained merely by low weight or air in leaf veins. " +
                "Mature sunflower heads do not track the sun all day. Whale blows are exhaled air and condensed vapor, " +
                "not water jets expelled from inside the whale. Reject contrary factual claims. " +
                "Identification clues must distinguish the proposed answer; two wings and singing do not identify one bird species. " +
                "Check precise vocabulary: cat means con mèo, kitten means mèo con, whale means cá voi. " +
                "Reject a whale story paired with fish vocabulary, or cat translated specifically as mèo con. " +
                "Check literal vocabulary: acorn is hạt sồi, chestnut is hạt dẻ, marigold is hoa cúc vạn thọ, " +
                "glasses is kính mắt, magnifying glass is kính lúp. " +
                "Check equal translation specificity: fish is con cá, goldfish is cá vàng, goat is con dê, mountain goat is dê núi. " +
                "An ENGLISH prompt asking specifically for cá vàng must teach goldfish rather than fish. " +
                "Red peel, sweetness and many small seeds do not uniquely identify an apple. " +
                "Do not present an optional object shape as universal; pencil cases come in multiple shapes. " +
                "For ages 4-5 reject abstract explanations such as chlorophyll, photosynthesis, condensation or light speed. " +
                "Prefer familiar concrete ideas. Check each targetAge independently. " +
                "For ages 6-7 use familiar observable ideas; ages 8-10 may use simple everyday why/how reasoning. " +
                "The spoken resolution includes answer followed by englishWord and meaningVi. " +
                "An ENGLISH question asking for an English word should have a full Vietnamese answer naming that word, " +
                "not just a bare word. Do not claim an object-identification prompt asks for English when it does not. " +
                "Do not invent facts or flaws. Return JSON {approved:boolean,issues:[{index,code,reason}]} only. " +
                "If approved=true, issues must be empty. If false, identify up to five concrete item indices and issue codes. " +
                "Each reason must be one concise English sentence (10-160 characters; Vietnamese spelling may be quoted) explaining the specific flaw " +
                "so the author can replace it; include the correct spelling when identifying a typo. No URLs or extra fields.",
                reviewInput.toString(),0.1,4096,reviewSchema,
                requirePackConsent=true,requireCreativeConsent=true))
            require(review.length()==2)
            val issues=review.getJSONArray("issues")
            require(issues.length()<=5)
            val corrections=JSONArray()
            val codes=mutableSetOf<String>()
            for(index in 0 until issues.length()) {
                val issue=issues.getJSONObject(index)
                val rawReason=issue.getString("reason")
                require(!rawReason.contains("http",ignoreCase=true)) { "CREATIVE_REVIEW_REASON_URL" }
                val compactReason=rawReason.map { if(Character.isISOControl(it)) ' ' else it }
                    .joinToString("").replace(Regex("\\s+")," ").trim().take(160)
                val reason=compactReason.takeIf { it.length>=10 }
                    ?: "Replace the flawed idea identified by the review."
                require(issue.length()==3 && issue.getInt("index") in batchIndices && issue.getString("code") in issueCodes)
                val lesson=reviewLessons[issue.getInt("index")]
                corrections.put(JSONObject().put("code",issue.getString("code")).put("reason",reason).put("previousLesson",lesson))
                codes.add(issue.getString("code"))
            }
            if (!review.getBoolean("approved")) {
                require(issues.length()>0)
                creativeCorrections=corrections
                throw IOException("AI_CONTENT_REVIEW_REJECTED_"+codes.joinToString("_"))
            }
            require(issues.length()==0)
        }
        creativeCorrections=JSONArray()
        return validated
    }

    /** Optional cloud input requires independent parent consent; no raw audio or GPS is sent. */
    @JvmOverloads
    fun childReply(question: String, sourceFact: String, spoken: String,
                   age: Int = 4, dayPeriod: DayPeriod = DayPeriod.current()): KidsAiReply {
        check(prefs.enabled && prefs.cloudChildReply)
        val safe = KidsAiSafety.childCloudInput(spoken) ?: throw IOException("CHILD_TEXT_BLOCKED")
        require(age in 4..10 && KidsAiSafety.safe(question,150) && sourceFact.length in 20..240)
        val input = JSONObject().put("question",question)
            .put("verifiedFact",sourceFact).put("childShortReply",safe)
            .put("targetAge",age).put("dayPeriod",dayPeriod.label())
        val system = "Respond as a friendly Vietnamese educational capybara for the requested age 4-10. " +
            "Never claim an unknown fact or physical location; only verifiedFact is reference. " +
            "Do not ask name, address, school or other private info. " +
            "If child's question needs outside knowledge, admit uncertainty. " +
            "Adapt to age and dayPeriod without claiming observed light or weather. " +
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
