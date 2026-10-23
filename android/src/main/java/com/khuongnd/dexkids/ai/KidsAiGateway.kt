package com.khuongnd.dexkids.ai

import android.content.Context
import com.khuongnd.dexkids.story.DayPeriod
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.khuongnd.dexkids.story.EntertainmentDirector
import com.khuongnd.dexkids.story.OfflineLearningCatalog
import com.khuongnd.dexkids.story.TripQuestionPack
import com.khuongnd.dexkids.story.CreativeLessonPack

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

class KidsAiGateway @JvmOverloads constructor(context: Context,
    private val subscription: KidsAiTransport = KidsSubscriptionAiClient(context)) : TripPackGateway {
    private val prefs = KidsAiSettings(context)
    // Only validated AI-authored text, held in this worker's gateway; never child input or disk cache.
    private var reviewedPool = com.khuongnd.dexkids.story.ReviewedLessonPool()
    private var creativeBatch = 0
    private var responseModel = ""
    override fun beginCreativeCycle() {
        reviewedPool = com.khuongnd.dexkids.story.ReviewedLessonPool()
        creativeBatch = 0
    }

    override fun ready(): Boolean = subscription.ready()

    /** Business consent stays here; transport owns OAuth, catalog and terminal SSE checks. */
    private fun invoke(system: String, input: String,
                       responseSchema: JSONObject? = null, requirePackConsent: Boolean = false,
                       requireChildConsent: Boolean = false,
                       requireCreativeConsent: Boolean = false): String {
        check(Looper.myLooper() != Looper.getMainLooper()) { "No network on UI thread" }
        fun consent() = prefs.enabled &&
            (!requirePackConsent || prefs.automaticPacks) &&
            (!requireCreativeConsent || prefs.creativePacks) &&
            (!requireChildConsent || prefs.cloudChildReply)
        check(consent() && !Thread.currentThread().isInterrupted) { "AI_DISABLED" }
        val text = subscription.request(system, input, responseSchema,
            if (requireCreativeConsent) 49_152 else 8192, ::consent)
        check(consent() && !Thread.currentThread().isInterrupted) { "AI_DISABLED" }
        responseModel = "CHATGPT_SUBSCRIPTION:" + subscription.model()
        return text
    }

    /** Parent-only story authoring uses the same subscription, consent and limiter. */
    fun storyRequest(system: String, input: String, review: Boolean = false): String {
        require(input.length <= com.khuongnd.dexkids.story.adventure.StoryEpisodeCodec.MAX_JSON_CHARS && system.length <= 6000)
        return invoke(system,input,
            requireCreativeConsent=true)
    }

    /** Generates ten or more questions about *only* an already sourced POI fact pack. */
    @JvmOverloads
    fun generate(poiId: String, sourceFact: String, base: KidsQuiz, age: Int,
                 dayPeriod: DayPeriod = DayPeriod.current()): List<KidsQuiz> {
        require(poiId.matches(Regex("osm:(node|way|relation):[1-9][0-9]{0,15}")))
        require(sourceFact.length in 20..240 && age in 4..7)
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
        val system = "You prepare short, kind, safe age-adjusted educational quizzes for ages 4-7 in Vietnamese. " +
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
        val raw = invoke(system,prompt.toString(),schema)
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
                .put("minItems",20).put("maxItems",20).put("items",row)))
            .put("required",JSONArray(listOf("items"))).put("additionalProperties",false)
        val raw = invoke("Select IDs only, never invent IDs. Return JSON {items:[{quizId,wordId,focus}]} " +
            "with 15-20 unique vetted ID pairs; aim for 18.",options.toString(),
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
        require(sauAge in 4..7 && ongAge in 4..7)
        val batchId = ++creativeBatch
        require(batchId <= 6) { "CREATIVE_ATTEMPTS_EXHAUSTED" }
        val schedule=JSONArray((0 until 20).map { index ->
            val focus=CreativeLessonPack.focusAt(audience,index)
            JSONObject().put("index",index).put("focus",focus.name)
                .put("targetAge",CreativeLessonPack.ageForFocus(focus,sauAge,ongAge))
        })
        val input = JSONObject()
            .put("audience",audience.name).put("sauAge",sauAge).put("ongAge",ongAge)
            .put("lessonSchedule",schedule)
            .put("avoidQuestionExamples",JSONArray(recentQuestions.takeLast(10)))
            .put("acceptedTopics",JSONArray(reviewedPool.accepted().map { it.lesson().topic() }))
            .put("acceptedQuestionFingerprints",JSONArray(reviewedPool.accepted().map { CreativeLessonPack.fingerprint(it.lesson().prompt()) }))
            .put("requirements","Invent exactly 20 completely NEW lessons, no finite catalog. Fill missing topics and genres. " +
                "Use at least 4 different safe topics and at least 3 genres QUIZ/STORY/RIDDLE/ENGLISH. " +
                "At least 2 explicitly imaginary STORY items; set fictional=true for each STORY. " +
                "Every STORY prompt must explicitly contain 'tưởng tượng', 'cổ tích' or 'hư cấu' so children hear the fiction label. " +
                "For BOTH rotate focus SAU, ONG, BOTH each consecutive card, " +
                "with ages sauAge, ongAge, min(sauAge,ongAge). SOLO focus stays audience. " +
                "Follow all 20 entries of lessonSchedule in order; write each lesson for that entry's targetAge. " +
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
                "Each prompt and answer must be 14-135 and 8-135 characters respectively for ages 4-5, " +
                "with at most 23 prompt words and 28 answer words. For ages 6-7 the character limits are 14-190 and 8-190. " +
                "topic is a lowercase ASCII slug of 3-32 characters matching [a-z][a-z0-9-]{2,31}. " +
                "englishWord is 2-30 lowercase ASCII letters, spaces or hyphens; meaningVi is 2-75 characters. " +
                "The combined answer plus ' Từ tiếng Anh: '+englishWord+' nghĩa là '+meaningVi+'.' must fit within 240 characters. " +
                "For ages 6-7: directly observable comparisons and causes with familiar objects, one easy reasoning step. " +
                "Avoid scientific mechanisms, technical biology/astronomy terms and abstract metaphors at ages 6-7. " +
                "Only generate for ages 4-7; reject content aimed at ages 8-10. " +
                "For 16 items aim for 6 imaginative STORY, 4 ENGLISH, 4 concrete QUIZ and 2 unambiguous RIDDLE lessons. Before returning JSON, silently self-check every lesson for accurate facts, suitable age, unambiguous answer, translation, diacritics, novelty and safety; replace any flawed item. " +
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
                .put("minItems",20).put("maxItems",20).put("items",row)))
            .put("required",JSONArray(listOf("items"))).put("additionalProperties",false)
        val system="You are an expert Vietnamese preschool/primary teacher and creative children's writer. " +
            "Write original, coherent, emotionally safe, strictly age-adapted lessons for ages 4-7. " +
            "Avoid unverifiable facts; be scientifically accurate and internally consistent. " +
            "No child data, no real-time location knowledge, no claims the vehicle is near anything. " +
            "For a STORY use an imaginary setup as the prompt and a fictional ending as answer. " +
            "For other genres ask an actual question ending in ?. " +
            "Keep questions short and answers clear; vocabulary translated correctly. Independently self-check each item before returning it; replace questionable content. JSON only."
        val response=JSONObject(invoke(system,input.toString(),schema,
            requirePackConsent=true,requireCreativeConsent=true))
        val generationModel=responseModel
        require(response.length()==1)
        val arr=response.getJSONArray("items")
        require(arr.length()==20)
        val entries=(0 until arr.length()).mapNotNull { i ->
            try {
                val v=arr.getJSONObject(i)
                require(v.length()==7)
                val focus=CreativeLessonPack.focusAt(audience,i)
                val lesson=CreativeLessonPack.Lesson(v.getString("prompt").trim(),
                    v.getString("answer").trim(),v.getString("englishWord").trim().lowercase(),
                    v.getString("meaningVi").trim(),v.getString("topic").trim().lowercase(),
                    CreativeLessonPack.Genre.valueOf(v.getString("genre")),
                    focus,CreativeLessonPack.ageForFocus(focus,sauAge,ongAge),v.getBoolean("fictional"))
                com.khuongnd.dexkids.story.ReviewedLessonPool.Candidate("b${batchId}q$i",lesson)
            } catch (_: IllegalArgumentException) { null }
              catch (_: org.json.JSONException) { null }
        }.distinctBy { CreativeLessonPack.fingerprint(it.lesson().prompt()) }
            .filter { c -> CreativeLessonPack.fingerprint(c.lesson().prompt()) !in recentFingerprints &&
                reviewedPool.accepted().none { it.lesson().prompt()==c.lesson().prompt() } }
        require(entries.isNotEmpty()) { "CREATIVE_NO_VALID_CANDIDATES" }
        val reviewLessons=entries.map { c ->
            val it=c.lesson()
            JSONObject().put("id",c.id()).put("targetAge",it.targetAge()).put("focus",it.focus().name)
                .put("genre",it.genre().name).put("prompt",it.prompt()).put("answer",it.answer())
                .put("englishWord",it.englishWord()).put("meaningVi",it.meaningVi()).put("fictional",it.fictional())
        }
        val issueSchema=JSONObject().put("type","object").put("properties",JSONObject()
            .put("id",JSONObject().put("type","string"))
            .put("verdict",JSONObject().put("type","string").put("enum",JSONArray(listOf("ACCEPT","REJECT"))))
            .put("code",JSONObject().put("type","string").put("enum",JSONArray(CreativeBatchReview.codes))))
            .put("required",JSONArray(listOf("id","verdict","code"))).put("additionalProperties",false)
        val reviewSchema=JSONObject().put("type","object")
            .put("properties",JSONObject().put("verdicts",JSONObject().put("type","array").put("items",issueSchema)))
            .put("required",JSONArray(listOf("verdicts"))).put("additionalProperties",false)
        run {
            val reviewInput=JSONObject().put("lessons",JSONArray(reviewLessons))
            val review=invoke(
                "You are a strict independent Vietnamese education quality checker for ages 4-7. " +
                "Audit ALL supplied lessons individually; return every original ID exactly once. " +
                "REJECT an item if its answer is factually inaccurate, logically inconsistent " +
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
                "For ages 6-7 use familiar observable ideas and simple reasoning; ages above 7 are not supported. " +
                "The spoken resolution includes answer followed by englishWord and meaningVi. " +
                "An ENGLISH question asking for an English word should have a full Vietnamese answer naming that word, " +
                "not just a bare word. Do not claim an object-identification prompt asks for English when it does not. " +
                "Do not invent facts or flaws. Return JSON {verdicts:[{id,verdict,code}]} only. " +
                "Each verdict is ACCEPT or REJECT. ACCEPT requires code OK; REJECT requires an issue code from the schema. " +
                "Include every supplied ID exactly once, with no extra IDs or fields.",
                reviewInput.toString(),reviewSchema,
                requirePackConsent=true,requireCreativeConsent=true)
            val parsed=CreativeBatchReview.parse(review)
            val reviewed=entries.map { com.khuongnd.dexkids.story.ReviewedLessonPool.Candidate(
                it.id(),it.lesson(),generationModel,responseModel) }
            reviewedPool.merge(reviewed,parsed,audience,sauAge,ongAge,recentFingerprints)
        }
        return reviewedPool.complete(audience,sauAge,ongAge,recentFingerprints)
    }

    /** Optional cloud input requires independent parent consent; no raw audio or GPS is sent. */
    @JvmOverloads
    fun childReply(question: String, sourceFact: String, spoken: String,
                   age: Int = 4, dayPeriod: DayPeriod = DayPeriod.current()): KidsAiReply {
        check(prefs.enabled && prefs.cloudChildReply)
        val safe = KidsAiSafety.childCloudInput(spoken) ?: throw IOException("CHILD_TEXT_BLOCKED")
        require(age in 4..7 && KidsAiSafety.safe(question,150) && sourceFact.length in 20..240)
        val input = JSONObject().put("question",question)
            .put("verifiedFact",sourceFact).put("childShortReply",safe)
            .put("targetAge",age).put("dayPeriod",dayPeriod.label())
        val system = "Respond as a friendly Vietnamese educational capybara for the requested age 4-7. " +
            "Never claim an unknown fact or physical location; only verifiedFact is reference. " +
            "Do not ask name, address, school or other private info. " +
            "If child's question needs outside knowledge, admit uncertainty. " +
            "Adapt to age and dayPeriod without claiming observed light or weather. " +
            "Only output JSON {\"reply\":string,\"followUp\":string}. " +
            "Each field one short sentence; do not repeat child personal text."
        val output = JSONObject(invoke(system,input.toString(),
            requireChildConsent=true))
        val answer = output.getString("reply").trim()
        val follow = output.getString("followUp").trim()
        require(KidsAiSafety.safe(answer,200) && KidsAiSafety.safe(follow,150))
        return KidsAiReply(answer,follow)
    }
}
