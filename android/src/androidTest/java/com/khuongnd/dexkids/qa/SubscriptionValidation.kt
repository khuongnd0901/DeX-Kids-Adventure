package com.khuongnd.dexkids.qa

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import com.khuongnd.dexkids.ai.*
import com.khuongnd.dexkids.auth.*
import com.khuongnd.dexkids.story.*
import com.khuongnd.dexkids.story.adventure.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Controlled synthetic transport and isolated storage; zero account inference. */
object SubscriptionValidation {
    @JvmStatic fun run(r: LifecycleValidationRunner, result: Bundle) {
        val base=r.targetContext
        val directory=File(base.cacheDir,"t075-isolated").apply { mkdirs() }
        val isolated=object:ContextWrapper(base) {
            override fun getApplicationContext():Context=this
            override fun getFilesDir()=directory
            override fun getNoBackupFilesDir()=directory
            override fun getSharedPreferences(name:String,mode:Int)=base.getSharedPreferences("qa-t075-$name",mode)
        }
        try {
            secureStore(isolated)
            gateway(isolated)
            result.putString("t075_storage", "PASS: real Android Keystore AES-GCM roundtrip; tokens absent from raw record; logout preserves registration and unrelated sentinel")
            result.putString("t075_features", "PASS: actual gateway parsers with controlled transport: POI/trip/creative generation and batch review; story author/regenerate/review; ages5/4; invalid JSON/schema and consent/late response rejection. No live inference")
            settingsUi(r, result)
        } finally {
            listOf("kids_ai_settings_v1","kids_chatgpt_subscription_v1","sentinel").forEach { isolated.getSharedPreferences(it,0).edit().clear().commit() }
            directory.deleteRecursively()
            val keys=java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if(keys.containsAlias("qa-t075-subscription")) keys.deleteEntry("qa-t075-subscription")
        }
    }
    private fun secureStore(c:Context) {
        val untouched=c.getSharedPreferences("sentinel",0)
        untouched.edit().putString("old_saved_data","unchanged").commit()
        val store=ChatGptSessionStore(c,"qa_t075_session","qa-t075-subscription")
        val host=store.hostId();store.issued("oaiapp_synthetic")
        store.save(ChatGptSession("oaiapp_synthetic","synthetic-sub","","id","synthetic-access-token-075","synthetic-refresh-token-075","resource.invoke chatgpt.tokens.use.direct",Long.MAX_VALUE))
        check(store.load()!!.accessToken=="synthetic-access-token-075")
        val raw=File(c.noBackupFilesDir,"qa_t075_session.enc").readBytes().toString(Charsets.ISO_8859_1)
        check(!raw.contains("synthetic-access-token") && !raw.contains("synthetic-refresh-token"))
        check(ChatGptAuthManager(store,revoke={}).logout())
        check(store.load()==null && store.hostId()==host && store.registration()!!.getString("client_id")=="oaiapp_synthetic")
        check(untouched.getString("old_saved_data",null)=="unchanged")
        val settings=ChatGptSettings(c)
        check(!settings.enabled && settings.models("a").isEmpty())
        settings.saveCatalog("a",listOf(ChatGptModelRepository.Model("catalog-model","Synthetic model")))
        settings.select("a","catalog-model")
        check(settings.models("other-account").isEmpty())
        rejects {settings.select("a","not-in-catalog")}
    }
    private fun rejects(block:()->Unit) {var rejected=false;try{block()}catch(_:Exception){rejected=true};check(rejected)}
    private fun gateway(c:Context) {
        val prefs=KidsAiSettings(c)
        prefs.enabled=true;prefs.automaticPacks=true;prefs.creativePacks=true
        var calls=0;var malformed=false;var lateRevoke=false
        val fake=object:KidsAiTransport {
            override fun ready()=true
            override fun model()="synthetic-account-model"
            override fun request(system:String,input:String,schema:JSONObject?,maxRequestBytes:Int,consent:()->Boolean):String {
                check(consent());calls++
                if(lateRevoke)prefs.enabled=false
                if(malformed)return "{}"
                if(system.startsWith("Select IDs")) {
                    val p=JSONObject(input);val quiz=p.getJSONArray("quizOptions");val word=p.getJSONArray("wordOptions")
                    val candidates=(0 until quiz.length()).map{quiz.getJSONArray(it)}.filter{it.getInt(2)<=4}.groupBy{it.getString(1)}
                    val selected=candidates.values.take(6).flatMap{it.take(3)}
                    check(selected.size==18)
                    return JSONObject().put("items",JSONArray(selected.mapIndexed { i,q ->
                        val w=(0 until word.length()).map{word.getJSONArray(it)}.first{it.getString(1)==q.getString(1) && it.getInt(2)<=4}
                        JSONObject().put("quizId",q.getString(0)).put("wordId",w.getString(0)).put("focus",EntertainmentDirector.Audience.values()[i%3].name)
                    })).toString()
                }
                if(system.startsWith("You prepare short"))return JSONObject().put("questions",JSONArray((0..9).map{ i ->
                    JSONObject().put("question","Câu hỏi số $i, con thấy cây xanh thế nào?").put("factIndex",0).put("followUp","Con cùng tưởng tượng vào ${DayPeriod.current().label()} nhé.")
                })).toString()
                if(system.startsWith("You are an expert"))return JSONObject().put("items",JSONArray((0..19).map{ i ->
                    val genre=CreativeLessonPack.Genre.values()[i%4]
                    JSONObject().put("prompt",(if(genre==CreativeLessonPack.Genre.STORY) "Trong chuyện tưởng tượng lần " else "Trong tranh lần ")+"75 số $i, hoa có màu gì?")
                        .put("answer","Bông hoa có màu đỏ trong bức tranh.").put("englishWord","flower").put("meaningVi","bông hoa")
                        .put("topic","topic${i%4}").put("genre",genre.name).put("fictional",genre==CreativeLessonPack.Genre.STORY)
                })).toString()
                if(system.startsWith("You are a strict independent")) {
                    val rows=JSONObject(input).getJSONArray("lessons")
                    return JSONObject().put("verdicts",JSONArray((0 until rows.length()).map {i->JSONObject().put("id",rows.getJSONObject(i).getString("id")).put("verdict","ACCEPT").put("code","OK")})).toString()
                }
                if(system.startsWith("Write safe gentle"))return StoryEpisodeCodec.toJson(StoryWorldFixtures.showcase(StoryEpisode.Audience.BOTH))
                if(system.startsWith("Rewrite ONLY"))return input
                if(system.startsWith("Independently review"))return "{\"approved\":true}"
                if(system.startsWith("Respond as a friendly"))return "{\"reply\":\"Con cùng nhìn bông hoa nhé.\",\"followUp\":\"Con thích màu nào hơn?\"}"
                error("Unexpected synthetic request")
            }
        }
        val gateway=KidsAiGateway(c,fake)
        val fact="Cây xanh có nhiều chiếc lá màu xanh."
        val base=KidsQuiz("Con thấy chiếc lá có màu gì?",fact,"Con cùng nhìn chiếc lá nhé.")
        check(gateway.generate("osm:node:123",fact,base,4).size==10)
        val catalog=c.assets.open("learning/offline-learning.tsv").use {OfflineLearningCatalog.parse(it)}
        check(gateway.generateTripPack(catalog,EntertainmentDirector.Audience.BOTH,emptyList()).items().size==18)
        val pack=gateway.generateCreativePack(EntertainmentDirector.Audience.BOTH,5,4,emptyList(),emptyList())
        check(pack.lessons().size in 16..20 && pack.lessons().all{it.targetAge()==if(it.focus()==EntertainmentDirector.Audience.SAU)5 else 4})
        val author=StoryAuthor(c){s,i,review ->gateway.storyRequest(s,i,review)}
        val story=author.generate("Giúp bạn","Biết chia sẻ",StoryEpisode.Audience.BOTH)
        check(story.sauAge()==5 && story.ongAge()==4)
        check(author.regenerateScene(story,0).scenes()==story.scenes());author.review(story)
        val before=calls
        rejects {gateway.childReply(base.question,fact,"Màu xanh")};check(calls==before)
        prefs.cloudChildReply=true
        check(gateway.childReply(base.question,fact,"Màu xanh").text.isNotBlank())
        val beforePrivate=calls
        rejects {gateway.childReply(base.question,fact,"Tên con là Bé")};check(calls==beforePrivate)
        malformed=true;rejects {gateway.generateTripPack(catalog,EntertainmentDirector.Audience.BOTH,emptyList())};malformed=false
        lateRevoke=true;rejects {gateway.generate("osm:node:123",fact,base,4)}
        val after=calls;rejects {gateway.generate("osm:node:123",fact,base,4)};check(calls==after)
    }
    private fun all(view:View):List<View> = listOf(view)+if(view is ViewGroup)(0 until view.childCount).flatMap{all(view.getChildAt(it))}else emptyList()
    private fun settingsUi(r:LifecycleValidationRunner,result:Bundle) {
        val auth=ChatGptAuthManager.get(r.targetContext)
        if(auth.connected()) {result.putString("t075_ui","NOT VERIFIED: existing owner session preserved");return}
        var activity:Activity?=null
        try {
            activity=r.startActivitySync(Intent(r.targetContext,ChatGptSubscriptionActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            r.runOnMainSync {
                val root=activity!!.window.decorView
                check(all(root).filterIsInstance<TextView>().any{it.text.contains("Disconnected")})
                val test=all(root).filterIsInstance<Button>().single{it.text.startsWith("Test inference")}
                check(test.performClick())
            }
            android.os.SystemClock.sleep(600)
            r.runOnMainSync {check(all(activity!!.window.decorView).filterIsInstance<TextView>().any{it.text.contains("Disconnected")})}
            auth.failure(ChatGptFailure("SESSION_EXPIRED"))
            r.runOnMainSync {
                activity!!.javaClass.getDeclaredMethod("updateStatus").apply{isAccessible=true}.invoke(activity)
                check(all(activity!!.window.decorView).filterIsInstance<TextView>().any{it.text.contains("Expired")})
            }
            auth.failure(ChatGptFailure("QUOTA_LIMITED"))
            r.runOnMainSync {
                activity!!.javaClass.getDeclaredMethod("updateStatus").apply{isAccessible=true}.invoke(activity)
                check(all(activity!!.window.decorView).filterIsInstance<TextView>().any{it.text.contains("Hết hạn mức")})
            }
            result.putString("t075_ui","PASS: actual subscription settings displays Disconnected; harmless test refuses without session. Expired/quota rendered from controlled auth states (no live account error claim)")
        } finally {r.runOnMainSync{activity?.finish()};auth.failure(ChatGptFailure("NOT_CONNECTED"))}
    }
}
