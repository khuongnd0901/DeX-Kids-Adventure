package com.khuongnd.dexkids.qa

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import com.khuongnd.dexkids.ParentSettings
import com.khuongnd.dexkids.ai.*
import com.khuongnd.dexkids.story.*
import com.khuongnd.dexkids.story.adventure.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File

/** Inspects real prompt/example/validator/store boundaries with synthetic transport only. */
object ConfiguredAiAgesValidation {
    @JvmStatic fun run(r:LifecycleValidationRunner,result:Bundle) {
        val base=r.targetContext
        val directory=File(base.cacheDir,"qa-t075-configured-ages").apply{mkdirs()}
        val context=object:ContextWrapper(base) {
            override fun getApplicationContext():Context=this
            override fun getFilesDir()=directory
            override fun getSharedPreferences(name:String,mode:Int)=base.getSharedPreferences("qa-t075-ages-$name",mode)
        }
        try {
            val settings=ParentSettings(context)
            val flags=KidsAiSettings(context)
            flags.enabled=true;flags.automaticPacks=true;flags.creativePacks=true;flags.cloudChildReply=true
            val legacy=StoryWorldFixtures.showcase(StoryEpisode.Audience.BOTH)
            val original=StoryEpisodeCodec.toJson(legacy)
            val author=StoryAuthor(context){system,input,review ->
                prompt(system,settings.sauAge,settings.ongAge)
                val payload=JSONObject(input)
                val example=if(system.startsWith("Write safe"))payload.getJSONObject("example")else payload
                check(example.getInt("sauAge")==settings.sauAge && example.getInt("ongAge")==settings.ongAge)
                if(review)"{\"approved\":true}"else example.toString()
            }
            var tripCalls=0
            val gateway=KidsAiGateway(context,object:KidsAiTransport {
                override fun ready()=true
                override fun model()="synthetic-age-model"
                override fun request(system:String,input:String,schema:JSONObject?,maxRequestBytes:Int,consent:()->Boolean):String {
                    check(consent())
                    val p=JSONObject(input)
                    if(system.startsWith("Select IDs")) {
                        tripCalls++
                        check(p.getInt("sauAge")==settings.sauAge && p.getInt("ongAge")==settings.ongAge && p.getInt("sharedAge")==minOf(settings.sauAge,settings.ongAge))
                        check(p.getString("rules").contains("SAU (age ${settings.sauAge}), ONG (age ${settings.ongAge}), BOTH (age ${minOf(settings.sauAge,settings.ongAge)})"))
                        val quizzes=p.getJSONArray("quizOptions");val words=p.getJSONArray("wordOptions")
                        return JSONObject().put("items",JSONArray((0 until 18).map {i->
                            check(quizzes.getJSONArray(i).length()==4)
                            JSONObject().put("quizId",quizzes.getJSONArray(i).getString(0)).put("wordId",words.getJSONArray(i).getString(0))
                                .put("focus",EntertainmentDirector.Audience.values()[i%3].name)
                        })).toString()
                    }
                    if(system.startsWith("You are an expert")) {
                        prompt(system,settings.sauAge,settings.ongAge)
                        val schedule=p.getJSONArray("lessonSchedule")
                        for(i in 0 until schedule.length()) {
                            val focus=EntertainmentDirector.Audience.valueOf(schedule.getJSONObject(i).getString("focus"))
                            check(schedule.getJSONObject(i).getInt("targetAge")==settings.ageForFocus(focus))
                        }
                        return JSONObject().put("items",JSONArray((0..19).map {i ->
                            val genre=CreativeLessonPack.Genre.values()[i%4]
                            JSONObject().put("prompt",(if(genre==CreativeLessonPack.Genre.STORY)"Trong chuyện tưởng tượng lần " else "Trong tranh lần ")+"76 số $i, hoa có màu gì?")
                                .put("answer","Bông hoa có màu đỏ trong bức tranh.").put("englishWord","flower").put("meaningVi","bông hoa")
                                .put("topic","topic${i%4}").put("genre",genre.name).put("fictional",genre==CreativeLessonPack.Genre.STORY)
                        })).toString()
                    }
                    if(system.startsWith("You are a strict independent")) {
                        prompt(system,settings.sauAge,settings.ongAge)
                        check(p.getInt("sauAge")==settings.sauAge && p.getInt("ongAge")==settings.ongAge)
                        val rows=p.getJSONArray("lessons")
                        for(i in 0 until rows.length())check(rows.getJSONObject(i).getInt("targetAge")==settings.ageForFocus(EntertainmentDirector.Audience.valueOf(rows.getJSONObject(i).getString("focus"))))
                        return JSONObject().put("verdicts",JSONArray((0 until rows.length()).map{i->JSONObject().put("id",rows.getJSONObject(i).getString("id")).put("verdict","ACCEPT").put("code","OK")})).toString()
                    }
                    if(system.startsWith("Respond as a friendly")) {
                        val age=settings.ageForFocus(EntertainmentDirector.Audience.fromId(settings.audienceMode))
                        check(p.getInt("targetAge")==age && system.contains("requested age $age."))
                        return "{\"reply\":\"Con cùng nhìn bông hoa nhé.\",\"followUp\":\"Con thích màu nào hơn?\"}"
                    }
                    error("Unexpected request")
                }
            })
            val catalog=catalog(7)
            for(pair in listOf(7 to 6,4 to 7,6 to 4,5 to 4)) {
                settings.sauAge=pair.first;settings.ongAge=pair.second;settings.audienceMode="BOTH"
                val created=author.generate("Giúp bạn","Biết chia sẻ",StoryEpisode.Audience.BOTH)
                check(created.sauAge()==pair.first && created.ongAge()==pair.second && created.learningAge()==minOf(pair.first,pair.second))
                val regenerated=author.regenerateScene(legacy,0)
                check(regenerated.sauAge()==pair.first && regenerated.ongAge()==pair.second && regenerated.scenes()==legacy.scenes())
                author.review(legacy)
                check(StoryEpisodeCodec.toJson(legacy)==original)
                check(gateway.generateTripPack(catalog,EntertainmentDirector.Audience.BOTH,emptyList()).items().size==18)
                gateway.beginCreativeCycle()
                val creative=gateway.generateCreativePack(EntertainmentDirector.Audience.BOTH,pair.first,pair.second,emptyList(),emptyList())
                check(creative.lessons().all{it.targetAge()==settings.ageForFocus(it.focus())})
                gateway.childReply("Con thấy bông hoa có màu gì?","Bông hoa trong tranh có màu xanh lá.","Màu xanh")
            }
            settings.sauAge=7;settings.ongAge=6
            var wrongCalls=0
            rejects("AI_AGE_MISMATCH") {StoryAuthor(context){_,_,_->wrongCalls++;original}.generate("Giúp bạn","Biết chia sẻ",StoryEpisode.Audience.BOTH)}
            check(wrongCalls==1)
            var changedCalls=0
            rejects("AI_AGES_CHANGED") {StoryAuthor(context){_,input,_->
                changedCalls++;settings.ongAge=4;JSONObject(input).getJSONObject("example").toString()
            }.generate("Giúp bạn","Biết chia sẻ",StoryEpisode.Audience.BOTH)}
            check(changedCalls==1)
            settings.sauAge=6;settings.ongAge=4
            val legacyCatalog=catalog(6)
            val pack=gateway.generateTripPack(legacyCatalog,EntertainmentDirector.Audience.BOTH,emptyList())
            TripPackStore(context,legacyCatalog,EntertainmentDirector.Audience.BOTH).swap(pack)
            val file=File(directory,"trip-packs-v1-both.json");val bytes=file.readBytes()
            settings.sauAge=7;settings.ongAge=6
            check(TripPackStore(context,legacyCatalog,EntertainmentDirector.Audience.BOTH).load()==null)
            check(file.readBytes().contentEquals(bytes))
            val beforeUnavailable=tripCalls
            rejects("AI_AGE_CONTENT_UNAVAILABLE"){gateway.generateTripPack(legacyCatalog,EntertainmentDirector.Audience.BOTH,emptyList())}
            check(tripCalls==beforeUnavailable)
            result.putString("configured_ai_ages","PASS: config pairs7/6,4/7,6/4,5/4 drive story generation/example/regenerate/review and trip/creative generation+review+default child-reply prompts; shared=min; model-age mismatch/config-change rejected; old episode unchanged; incompatible cached card ages rejected without deleting file. Synthetic transport only; zero live inference")
        } finally {
            for(name in listOf("parent_settings","kids_ai_settings_v1","trip-pack-state-v1"))context.getSharedPreferences(name,0).edit().clear().commit()
            directory.deleteRecursively()
        }
    }
    private fun prompt(system:String,sau:Int,ong:Int) {
        check(system.contains("Sâu age $sau, Ong age $ong; shared BOTH content targets age ${minOf(sau,ong)}."))
    }
    private fun rejects(code:String?,action:()->Unit) {
        var failure:Exception?=null;try{action()}catch(e:Exception){failure=e}
        check(failure!=null && (code==null || failure.message==code)) {"Expected rejection $code"}
    }
    private fun catalog(maxAge:Int):OfflineLearningCatalog {
        val text=StringBuilder("# dexkids-offline-learning-v1\nid\tkind\ttopic\tmin_age\tmax_age\tprompt_vi\tanswer_vi\tenglish\n")
        val topics=listOf("sea","city","road","nature","weather","animal","garden","safety")
        repeat(24){i->
            val id="${topics[i%8]}-$i"
            text.append("quiz-$id\tQUIZ\t${topics[i%8]}\t4\t$maxAge\tCon thử nói điều số $i có đúng không?\tMột câu trả lời được biên tập nhé.\t\n")
            text.append("word-$id\tWORD\t${topics[i%8]}\t4\t$maxAge\tMình cùng học từ số $i tiếng Anh nhé!\tCây xanh trong khu vườn nhé.\ttree\n")
        }
        return OfflineLearningCatalog.parse(ByteArrayInputStream(text.toString().toByteArray()))
    }
}
