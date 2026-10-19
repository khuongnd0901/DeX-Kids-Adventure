package com.khuongnd.dexkids.media

import com.khuongnd.dexkids.story.EntertainmentDirector.Audience

/** Optional manual starter links, never parent approval or cached eligibility. */
object YouTubeSampleVideos {
    fun pending(): List<YouTubeVideo> = listOf(
        YouTubeVideo("uWXvGSMrwXc", "Dê đen và dê trắng", Audience.BOTH, 4, 7, 300, false, false, ""),
        YouTubeVideo("LIKg2CYTp4Y", "Cáo, thỏ và gà trống", Audience.BOTH, 4, 7, 600, false, false, ""),
        YouTubeVideo("TXN0gMj2GZs", "Rùa và thỏ", Audience.BOTH, 4, 7, 120, false, false, "")
    )
}
