package com.boclips.videoanalyser.config.application

import com.boclips.videoanalyser.infrastructure.videoindexer.*
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexResourceParser
import com.boclips.videoanalyser.presentation.IndexingProgressCallbackFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class InfrastructureContext(
        private val restTemplateBuilder: RestTemplateBuilder,
        private val objectMapper: ObjectMapper
) {

    @Profile("!fake-video-indexer")
    @Bean
    fun videoIndexer(videoIndexerProperties: VideoIndexerProperties, indexingProgressCallbackFactory: IndexingProgressCallbackFactory): VideoIndexer {
        return HttpVideoIndexerClient(restTemplateBuilder.build(), videoIndexerProperties, indexingProgressCallbackFactory, videoIndexResourceParser())
    }

    @Bean
    fun videoIndexResourceParser(): VideoIndexResourceParser {
        return VideoIndexResourceParser(objectMapper)
    }
}
