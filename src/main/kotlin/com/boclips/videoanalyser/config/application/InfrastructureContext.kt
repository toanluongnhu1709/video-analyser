package com.boclips.videoanalyser.config.application

import com.boclips.videoanalyser.infrastructure.videoindexer.HttpVideoIndexerClient
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexerProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class InfrastructureContext(private val restTemplateBuilder: RestTemplateBuilder) {

    @Profile("!fake-video-indexer")
    @Bean
    fun videoIndexer(videoIndexerProperties: VideoIndexerProperties): VideoIndexer {
        return HttpVideoIndexerClient(restTemplateBuilder.build(), videoIndexerProperties)
    }
}
