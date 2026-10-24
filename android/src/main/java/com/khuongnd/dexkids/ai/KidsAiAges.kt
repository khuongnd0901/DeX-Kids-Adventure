package com.khuongnd.dexkids.ai

import com.khuongnd.dexkids.ParentSettings

/** Immutable per-request snapshot. Shared content must suit the younger learner. */
internal data class KidsAiAges(val sau: Int, val ong: Int) {
    init { require(sau in 4..7 && ong in 4..7) }
    val shared: Int get() = minOf(sau, ong)
    fun instruction() = "Sâu age $sau, Ong age $ong; shared BOTH content targets age $shared."
    companion object {
        fun from(settings: ParentSettings) = KidsAiAges(settings.sauAge, settings.ongAge)
    }
}
