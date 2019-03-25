package com.boclips.videoanalyser.testsupport.fakes

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

    override fun submitVideo(videoId: String, videoUrl: String): String {
        val id = UUID.randomUUID().toString()
        submittedVideos[id] = videoUrl
        return id
    }

    fun clear() {
        submittedVideos.clear()
    }

    fun submittedVideos(): Set<String> = submittedVideos.values.toSet()

}
