package com.khuongnd.dexkids.qa

import android.content.Context
import android.content.ContextWrapper
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Display
import com.khuongnd.dexkids.KidsSessionControl
import com.khuongnd.dexkids.ParentSettings
import com.khuongnd.dexkids.ai.*
import com.khuongnd.dexkids.story.*
import com.khuongnd.dexkids.story.adventure.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Explicit owner-authorized real subscription requests; isolated business settings and content. */
object SubscriptionLiveValidation {
    @JvmStatic fun run(r: LifecycleValidationRunner, result: Bundle, feature: String): Boolean {
        val base = r.targetContext
        check(Build.MODEL == "SM-F926B" && !KidsSessionControl.active())
        val display = base.getSystemService(DisplayManager::class.java).getDisplay(r.targetDisplay())
        check(r.targetDisplay() > 0 && display?.name == "Desktop" && display.state == Display.STATE_ON)
        require(feature in setOf("poi", "trip", "creative", "child", "story_author", "story_review", "story_regenerate"))
        val root = File(base.cacheDir, "qa-t081-live").apply { mkdirs() }
        // A failed new request must not leave an older successful response looking current.
        root.listFiles()?.filter { it.name.startsWith("$feature-") }?.forEach { check(it.delete()) }
        val directory = File(root, "isolated-$feature").apply { deleteRecursively(); mkdirs() }
        val prefNames = mutableSetOf<String>()
        val context = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir() = directory
            override fun getSharedPreferences(name: String, mode: Int) = base.getSharedPreferences(
                "qa-t081-$feature-$name".also { prefNames.add(it) }, mode)
            override fun getDatabasePath(name: String) = File(directory, name)
            override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase =
                SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory)
            override fun openOrCreateDatabase(name: String, mode: Int, factory: SQLiteDatabase.CursorFactory?, handler: DatabaseErrorHandler?): SQLiteDatabase =
                SQLiteDatabase.openDatabase(getDatabasePath(name).path, factory, SQLiteDatabase.CREATE_IF_NECESSARY, handler)
        }
        val ownerAges = ParentSettings(base)
        val sau = ownerAges.sauAge; val ong = ownerAges.ongAge
        ParentSettings(context).apply { sauAge = sau; ongAge = ong }
        val flags = KidsAiSettings(context).apply {
            enabled = true; automaticPacks = true; creativePacks = true; cloudChildReply = false
        }
        val production = KidsSubscriptionAiClient(base)
        check(production.ready()) { "OWNER_SUBSCRIPTION_NOT_READY" }
        val events = JSONArray()
        val report = JSONObject().put("feature", feature).put("model", production.model())
            .put("sauAge", sau).put("ongAge", ong).put("live", true).put("status", "RUNNING").put("requests", events)
        val started = SystemClock.elapsedRealtime()
        var sequence = 0
        val live = object : KidsAiTransport {
            override fun ready() = production.ready()
            override fun model() = production.model()
            override fun request(system: String, input: String, schema: JSONObject?, maxRequestBytes: Int, consent: () -> Boolean): String {
                check(++sequence <= 8) { "LIVE_TEST_CALL_BUDGET" }
                val phase = when {
                    system.startsWith("You prepare short") -> "poi_generation"
                    system.startsWith("Select IDs") -> "trip_selection"
                    system.startsWith("You are an expert") -> "creative_generation"
                    system.startsWith("You are a strict independent") -> "creative_review"
                    system.startsWith("Respond as a friendly") -> "child_sample_reply"
                    system.startsWith("Write safe gentle") -> "story_author"
                    system.startsWith("Rewrite ONLY") -> "story_regenerate"
                    system.startsWith("Independently review") -> "story_review"
                    else -> error("UNEXPECTED_LIVE_PHASE")
                }
                val event = JSONObject().put("sequence", sequence).put("phase", phase)
                    .put("requestMaxBytes", maxRequestBytes)
                event.put("status", "STARTED")
                events.put(event)
                File(root, "$feature-report.json").writeText(report.toString(2))
                val time = SystemClock.elapsedRealtime()
                try {
                    val text = production.request(system, input, schema, maxRequestBytes, consent)
                    // Only generic test content. OAuth/session/HTTP headers are never exposed here.
                    File(root, "$feature-$sequence-$phase.json").writeText(text)
                    event.put("status", "COMPLETED").put("responseChars", text.length)
                    return text
                } catch (failure: Exception) {
                    event.put("status", "FAILED").put("error", safeFailure(failure)); throw failure
                } finally {
                    event.put("elapsedMs", SystemClock.elapsedRealtime() - time)
                    File(root, "$feature-report.json").writeText(report.toString(2))
                }
            }
        }
        val gateway = KidsAiGateway(context, live)
        fun fixture(): StoryEpisode {
            val saved = File(root, "accepted-generated-story.json")
            if (saved.isFile()) return StoryEpisodeCodec.parse(saved.readText()).also { report.put("storySource", "live_generated") }
            val old = StoryFixtures.educational(StoryEpisode.Audience.BOTH)
            report.put("storySource", "bundled_fixture")
            return StoryEpisode(old.id(), old.schemaVersion(), old.revision(), old.title(), old.audience(), sau, ong,
                old.educationalGoal(), old.synopsis(), old.scenes())
        }
        fun persisted(story: StoryEpisode) {
            StoryEpisodeValidator.validate(story)
            StoryRepository(context).use { repo ->
                val entry = repo.save(story, true, null)
                check(entry.reviewed && !entry.approved)
                check(repo.get(story.id())!!.episode == story)
            }
            report.put("isolatedRepositoryRoundtrip", true).put("automaticallyApproved", false)
        }
        try {
            when (feature) {
                "poi" -> {
                    val narrations = base.assets.open("narration/live-landmarks.tsv").use { OfflineNarrationCatalog.parse(it) }
                    val dialogue = base.assets.open("poi/live-dialogue.tsv").use { PoiDialogueCatalog.parse(it) }
                    val ids = base.assets.open("poi/live-landmarks.tsv").bufferedReader().use { input ->
                        input.lineSequence().drop(2).filter { it.isNotBlank() }.map { it.substringBefore('\t') }.toList()
                    }
                    val id = ids.first { narrations.findByPoiId(it).isPresent && dialogue.find(it).isPresent &&
                        narrations.findByPoiId(it).get().textVi().length in 20..240 }
                    val fact = narrations.findByPoiId(id).get().textVi()
                    val card = dialogue.find(id).get()
                    val original = KidsQuiz(card.quiz(), card.answer(), card.chat())
                    val period = DayPeriod.current()
                    val quizzes = gateway.generate(id, fact, original, minOf(sau, ong), period)
                    val cache = KidsAiQuizCache(context)
                    cache.put(id, minOf(sau, ong), fact, original, quizzes, period)
                    check(cache.get(id, minOf(sau, ong), fact, original, period) == quizzes)
                    report.put("items", quizzes.size).put("sourcePoiId", id).put("factBoundCacheRoundtrip", true)
                }
                "trip" -> {
                    val catalog = base.assets.open("learning/offline-learning.tsv").use { OfflineLearningCatalog.parse(it) }
                    val pack = gateway.generateTripPack(catalog, EntertainmentDirector.Audience.BOTH, emptyList())
                    val store = TripPackStore(context, catalog, EntertainmentDirector.Audience.BOTH, sau, ong)
                    store.swap(pack); check(store.load()!!.pack.items() == pack.items())
                    report.put("items", pack.items().size).put("reviewedIdCacheRoundtrip", true)
                }
                "creative" -> {
                    gateway.beginCreativeCycle()
                    var complete: CreativeLessonPack? = null
                    for (attempt in 1..3) {
                        try { complete = gateway.generateCreativePack(EntertainmentDirector.Audience.BOTH, sau, ong, emptyList(), emptyList()); break }
                        catch (failure: IllegalArgumentException) {
                            if (failure.message != "CREATIVE_REFILL_REQUIRED" || attempt == 3) throw failure
                            report.put("refillAttempts", attempt)
                        }
                    }
                    val pack = complete ?: error("CREATIVE_NO_COMPLETE_PACK")
                    val store = CreativePackStore(context, EntertainmentDirector.Audience.BOTH, sau, ong)
                    store.swap(pack); check(store.load()!!.pack.lessons() == pack.lessons())
                    report.put("items", pack.lessons().size).put("reviewSources", pack.reviewSources().size)
                        .put("independentReviewAndCacheRoundtrip", true)
                }
                "child" -> {
                    val before = sequence
                    val question = "Con thích bông hoa màu gì?"
                    val fact = "Bông hoa trong bức tranh tưởng tượng có màu xanh."
                    var blocked = false
                    try { gateway.childReply(question, fact, "Màu xanh", minOf(sau, ong)) } catch (_: Exception) { blocked = true }
                    check(blocked && sequence == before)
                    flags.cloudChildReply = true // Isolated QA preference only; never the owner's setting.
                    val reply = gateway.childReply(question, fact, "Màu xanh", minOf(sau, ong))
                    check(reply.text.isNotBlank() && reply.nextQuestion.isNotBlank())
                    val after = sequence; blocked = false
                    try { gateway.childReply(question, fact, "Tên con là Bé", minOf(sau, ong)) } catch (_: Exception) { blocked = true }
                    check(blocked && sequence == after)
                    report.put("syntheticTextOnly", true).put("missingConsentBlockedBeforeNetwork", true)
                        .put("privateTextBlockedBeforeNetwork", true)
                }
                "story_author" -> {
                    val author = StoryAuthor(context) { s, i, review -> gateway.storyRequest(s, i, review) }
                    val story = author.generate("Giúp bạn chăm sóc khu vườn tưởng tượng", "Biết chia sẻ và giúp đỡ bạn", StoryEpisode.Audience.BOTH)
                    persisted(story)
                    File(root, "accepted-generated-story.json").writeText(StoryEpisodeCodec.toJson(story))
                    report.put("scenes", story.scenes().size).put("questionCount", story.scenes().count { it.question() != null })
                }
                "story_review" -> {
                    val author = StoryAuthor(context) { s, i, review -> gateway.storyRequest(s, i, review) }
                    val story = fixture(); author.review(story); persisted(story)
                }
                "story_regenerate" -> {
                    val author = StoryAuthor(context) { s, i, review -> gateway.storyRequest(s, i, review) }
                    val story = fixture(); val changed = author.regenerateScene(story, 0); persisted(changed)
                    check(story.scenes().indices.drop(1).all { story.scenes()[it] == changed.scenes()[it] })
                    File(root, "accepted-regenerated-story.json").writeText(StoryEpisodeCodec.toJson(changed))
                    report.put("otherScenesPreserved", true)
                }
            }
            report.put("status", "PASS")
            return true
        } catch (failure: Exception) {
            report.put("status", "FAIL").put("error", safeFailure(failure))
            return false
        } finally {
            report.put("elapsedMs", SystemClock.elapsedRealtime() - started)
            File(root, "$feature-report.json").writeText(report.toString(2))
            result.putString("live_feature_report", report.toString())
            prefNames.forEach { base.deleteSharedPreferences(it) }
            directory.deleteRecursively()
        }
    }
    private fun safeFailure(e: Exception): String {
        val message = e.message.orEmpty()
        return e.javaClass.simpleName + if (message.matches(Regex("[A-Z0-9_ :;=.,-]{1,180}"))) ":$message" else ""
    }
}
