package com.boclips.videoanalyser.testsupport.fakes

import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexerTokenProvider
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexItemResource
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexResource
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoInsightsResource
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoResource
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

    @Bean
    @Primary
    fun fakeVideoIndexerTokenProvider(): FakeVideoIndexerTokenProvider {
        return FakeVideoIndexerTokenProvider()
    }
}

data class IndexerVideo(val videoUrl: String, val language: Locale?, val sourceFileAvailable: Boolean)

class FakeVideoIndexer : VideoIndexer {
    private val submittedVideos = mutableMapOf<String, IndexerVideo>()

    override fun isIndexed(videoId: String): Boolean {
        return submittedVideos.containsKey(videoId)
    }

    override fun submitVideo(videoId: String, videoUrl: String, language: Locale?) {
        submittedVideos[videoId] = IndexerVideo(videoUrl = videoUrl, language = language, sourceFileAvailable = true)
    }

    override fun getVideo(videoId: String): VideoResource {
        if (!submittedVideos.containsKey(videoId)) {
            throw RuntimeException("no such video")
        }

        val video = VideoIndexItemResource(
            state = VideoIndexItemResource.STATE_PROCESSED,
            externalId = videoId,
            insights = VideoInsightsResource(
                sourceLanguage = "en-GB",
                keywords = emptyList(),
                topics = emptyList(),
                transcript = emptyList()
            )
        )

        return VideoResource(index = VideoIndexResource(videos = listOf(video)), captions = ByteArray(0))
    }

    override fun deleteSourceFile(videoId: String) {
        submittedVideos[videoId]?.let { video ->
            submittedVideos[videoId] = video.copy(sourceFileAvailable = false)
        }
    }

    override fun deleteVideo(videoId: String) {
        submittedVideos.remove(videoId)
    }

    fun clear() {
        submittedVideos.clear()
    }

    fun submittedVideo(videoId: String): IndexerVideo? = submittedVideos[videoId]
}

class FakeVideoIndexerTokenProvider : VideoIndexerTokenProvider {
    override fun getToken(): String {
        return "test-access-token"
    }
}
