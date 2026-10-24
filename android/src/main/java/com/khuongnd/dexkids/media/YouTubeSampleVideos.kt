package com.khuongnd.dexkids.media

import com.khuongnd.dexkids.story.EntertainmentDirector.Audience

/** Optional manual starter links, never parent approval or cached eligibility. */
object YouTubeSampleVideos {
    /** Only empty prompts for known story IDs; parent edits and every other field stay intact. */
    fun withSuggestedRecall(videos: List<YouTubeVideo>): List<YouTubeVideo> {
        val suggestions = pending().associateBy { it.videoId() }
        return videos.map { video ->
            val recall = suggestions[video.videoId()]?.recall().orEmpty()
            if (video.recall().isNotBlank() || recall.isBlank()) video
            else YouTubeVideo(video.videoId(), video.title(), video.audience(), video.minAge(),
                video.maxAge(), video.plannedSeconds(), video.approved(), video.enabled(), recall)
        }
    }

    fun pending(): List<YouTubeVideo> = listOf(
        YouTubeVideo("uWXvGSMrwXc", "Dê đen và dê trắng", Audience.BOTH, 4, 7, 300, false, false, "Con nhớ dê đen và dê trắng đã gặp chuyện gì?\nKhi thấy sợ, con có thể nhờ người lớn nào giúp đỡ?"),
        YouTubeVideo("LIKg2CYTp4Y", "Cáo, thỏ và gà trống", Audience.BOTH, 4, 7, 600, false, false, "Bạn thỏ trong truyện cần được giúp chuyện gì?\nCon có thể làm gì khi thấy một người bạn buồn?"),
        YouTubeVideo("TXN0gMj2GZs", "Rùa và thỏ", Audience.BOTH, 4, 7, 120, false, false, "Con nhớ rùa và thỏ đã làm gì trong cuộc thi?\nKhi làm một việc khó, con sẽ làm gì để không bỏ dở?"),
        YouTubeVideo("Tfkf3NStsx4", "Nhổ củ cải", Audience.BOTH, 4, 7, 240, false, false, "Nhờ đâu mọi người nhổ được củ cải?\nKhi cần giúp đỡ, con sẽ nhờ bạn như thế nào?"),
        YouTubeVideo("82bWFKrP8rg", "Chú đỗ con", Audience.BOTH, 4, 7, 180, false, false, "Con nhớ những ai đã giúp chú đỗ con lớn lên?\nNếu trồng một hạt đỗ, con muốn chăm sóc nó thế nào?"),
        YouTubeVideo("TgUeLqOpj5Y", "Cây táo", Audience.BOTH, 4, 7, 120, false, false, "Con nhớ ai đã chăm sóc cây táo trong truyện?\nCon sẽ làm gì để chăm sóc một cây nhỏ?"),
        YouTubeVideo("-pII-it4wrA", "Giọt nước tí xíu", Audience.BOTH, 4, 7, 240, false, false, "Con hãy kể lại chuyến đi của giọt nước tí xíu.\nCon có thể làm gì để dùng nước tiết kiệm?"),
        YouTubeVideo("7hKzVZfuwz0", "Chiếc ô của thỏ con", Audience.BOTH, 4, 7, 120, false, false, "Trong truyện, thỏ con đã dùng gì để che mưa?\nNếu bạn chưa có ô, con có thể giúp bạn thế nào?"),
        YouTubeVideo("RuRjL37Xl4I", "Gấu con chia quà", Audience.BOTH, 4, 7, 240, false, false, "Gấu con đã chia quà cho những ai?\nNếu có quà để chia, con sẽ làm sao để không bỏ sót ai?")
    )
}
