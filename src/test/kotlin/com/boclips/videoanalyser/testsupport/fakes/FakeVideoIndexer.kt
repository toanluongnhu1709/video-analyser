package com.boclips.videoanalyser.testsupport.fakes

import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndex
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.util.*

@Profile("fake-video-indexer")
@Configuration
class FakeVideoIndexerConfiguration {

    @Bean
    @Primary
    fun fakeVideoIndexer(): FakeVideoIndexer {
        return FakeVideoIndexer()
    }
}

class FakeVideoIndexer : VideoIndexer {
    private val submittedVideos = mutableMapOf<String, String>()

    override fun submitVideo(videoId: String, videoUrl: String) {
        submittedVideos[videoId] = videoUrl
    }

    override fun getVideoIndex(videoId: String): VideoIndex {
        return VideoIndex(videoId = videoId, keywords = emptyList(), topics = emptyList(), vttCaptions = ByteArray(0))
    }

    fun clear() {
        submittedVideos.clear()
    }

    fun submittedVideo(videoId: String): String? = submittedVideos[videoId]

}
