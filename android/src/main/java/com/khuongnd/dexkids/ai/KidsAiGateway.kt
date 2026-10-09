package com.khuongnd.dexkids.ai

import android.content.Context
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.HttpsURLConnection

data class KidsQuiz(val question: String, val answer: String, val followUp: String)
data class KidsAiReply(val text: String, val nextQuestion: String)

/** Fail closed: AI can only draft phrasing, never replace sourced facts or infer vehicle location. */
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
    private val calls = AtomicInteger(0)
    private val maxCallsPerInstance = 20

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
    private fun invoke(system: String, input: String, temperature: Double, maxTokens: Int): String {
        check(Looper.myLooper() != Looper.getMainLooper()) { "No network on UI thread" }
        check(prefs.enabled && calls.incrementAndGet() <= maxCallsPerInstance)
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
                            .put("responseMimeType","application/json"))
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
                    check(bytes.size <= 8192)
                    conn.outputStream.use { it.write(bytes) }
                    val status = conn.responseCode
                    if (status !in 200..299) {
                        if (status in listOf(408,429,500,502,503,504)) {
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
                if (e.message != "AI_RETRYABLE" && e !is java.net.SocketTimeoutException)
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
        val raw = invoke(system,prompt.toString(),0.6,1600)
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
        val output = JSONObject(invoke(system,input.toString(),0.35,220))
        val answer = output.getString("reply").trim()
        val follow = output.getString("followUp").trim()
        require(KidsAiSafety.safe(answer,200) && KidsAiSafety.safe(follow,150))
        return KidsAiReply(answer,follow)
    }
}
