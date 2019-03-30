package com.boclips.videoanalyser.config.application

import com.boclips.videoanalyser.presentation.PublishAnalysedVideoLinkFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PresentationContext {

    @Value("\${public.url}")
    lateinit var publicUrl: String

    @Bean
    fun publishAnalysedVideoLinkFactory(): PublishAnalysedVideoLinkFactory {
        return PublishAnalysedVideoLinkFactory(publicUrl)
    }

}
