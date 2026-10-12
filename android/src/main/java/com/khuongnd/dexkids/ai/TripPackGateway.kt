package com.khuongnd.dexkids.ai

import com.khuongnd.dexkids.story.CreativeLessonPack
import com.khuongnd.dexkids.story.EntertainmentDirector
import com.khuongnd.dexkids.story.OfflineLearningCatalog
import com.khuongnd.dexkids.story.TripQuestionPack

/** Narrow boundary for deterministic orchestration QA; production uses KidsAiGateway. */
internal interface TripPackGateway {
    fun beginCreativeCycle() {}
    fun ready(): Boolean
    @Throws(java.io.IOException::class)
    fun generateTripPack(catalog: OfflineLearningCatalog, audience: EntertainmentDirector.Audience,
                         recentIds: List<String>): TripQuestionPack
    @Throws(java.io.IOException::class)
    fun generateCreativePack(audience: EntertainmentDirector.Audience, sauAge: Int, ongAge: Int,
                             recentFingerprints: List<String>, recentQuestions: List<String>): CreativeLessonPack
}
