package com.khuongnd.dexkids.ai

import com.khuongnd.dexkids.story.ReviewedLessonPool
import org.json.JSONObject

/** Strict whole-response parsing; no partial approval on malformed reviewer output. */
object CreativeBatchReview {
    val codes = listOf("OK", "AGE_TOO_HARD", "FACT_ERROR", "ANSWER_MISMATCH", "TRANSLATION_ERROR",
        "AMBIGUOUS_RIDDLE", "MISSING_FICTION_LABEL", "MISSING_DIACRITICS", "SAFETY")

    @JvmStatic fun parse(text: String): List<ReviewedLessonPool.Verdict> {
        val root = JSONObject(text)
        require(root.length() == 1)
        val rows = root.getJSONArray("verdicts")
        require(rows.length() in 1..20)
        return (0 until rows.length()).map { i ->
            val row = rows.getJSONObject(i)
            require(row.length() == 3)
            val verdict = row.getString("verdict")
            val code = row.getString("code")
            require(verdict in listOf("ACCEPT", "REJECT") && code in codes)
            require((verdict == "ACCEPT") == (code == "OK"))
            ReviewedLessonPool.Verdict(row.getString("id"), verdict == "ACCEPT")
        }
    }
}
