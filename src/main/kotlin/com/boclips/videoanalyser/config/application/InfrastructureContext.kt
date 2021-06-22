package com.boclips.videoanalyser.config.application

import com.boclips.videoanalyser.infrastructure.RealDelayer
import com.boclips.videoanalyser.infrastructure.videoindexer.*
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexResourceParser
import com.boclips.videoanalyser.presentation.PublishAnalysedVideoLinkFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class InfrastructureContext(
        private val restTemplateBuilder: RestTemplateBuilder,
        private val objectMapper: ObjectMapper,
        private val videoIndexerProperties: VideoIndexerProperties
) {

    @Profile("!fake-video-indexer")
    @Bean
    fun videoIndexer(publishAnalysedVideoLinkFactory: PublishAnalysedVideoLinkFactory): VideoIndexer {
        return HttpVideoIndexerClient(
            restTemplateBuilder.build(),
            videoIndexerProperties,
            videoIndexerTokenProvider(),
            publishAnalysedVideoLinkFactory,
            videoIndexResourceParser(),
            RealDelayer()
        )
    }

    @Profile("!fake-video-indexer")
    @Bean
    fun videoIndexerTokenProvider(): VideoIndexerTokenProvider {
        return HttpVideoIndexerTokenProvider(restTemplateBuilder.build(), videoIndexerProperties)
    }

    @Bean
    fun videoIndexResourceParser(): VideoIndexResourceParser {
        return VideoIndexResourceParser(objectMapper)
    }
}
