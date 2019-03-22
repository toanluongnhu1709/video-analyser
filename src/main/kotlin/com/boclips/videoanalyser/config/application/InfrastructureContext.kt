package com.boclips.videoanalyser.config.application

import com.boclips.videoanalyser.infrastructure.videoindexer.HttpVideoIndexerClient
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class InfrastructureContext {

    @Profile("!fake-video-indexer")
    @Bean
    fun videoIndexer(): VideoIndexer {
        return HttpVideoIndexerClient()
    }
}
