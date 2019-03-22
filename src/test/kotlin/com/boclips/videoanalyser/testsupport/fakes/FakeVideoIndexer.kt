package com.boclips.videoanalyser.testsupport.fakes

import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
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

    private val submittedVideos = mutableSetOf<String>()

    override fun submitVideo(url: String) {
        submittedVideos.add(url)
    }

    fun clear() {
        submittedVideos.clear()
    }

    fun submittedVideos(): Set<String> = submittedVideos

}
