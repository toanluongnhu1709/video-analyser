package com.boclips.videoanalyser.config.application

import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.infrastructure.VideoIndexerAnalyserService
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainContext(private val videoIndexer: VideoIndexer) {

    @Bean
    fun videoAnalyserService(): VideoAnalyserService {
        return VideoIndexerAnalyserService(videoIndexer)
    }
}
