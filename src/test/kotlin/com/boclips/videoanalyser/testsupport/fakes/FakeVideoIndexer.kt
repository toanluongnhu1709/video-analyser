package com.boclips.videoanalyser.testsupport.fakes

import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexItemResource
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexResource
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoInsightsResource
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoResource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

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

    override fun getVideoIndex(videoId: String): VideoResource {
        val video = VideoIndexItemResource(
                externalId = videoId,
                insights = VideoInsightsResource(
                        sourceLanguage = "en-GB",
                        keywords = emptyList(),
                        topics = emptyList(),
                        transcript = emptyList()
                ))

        return VideoResource(index = VideoIndexResource(videos = listOf(video)), captions = ByteArray(0))
    }

    fun clear() {
        submittedVideos.clear()
    }

    fun submittedVideo(videoId: String): String? = submittedVideos[videoId]

}
