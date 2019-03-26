package com.boclips.videoanalyser.config.application

import com.boclips.videoanalyser.presentation.IndexingProgressCallbackFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PresentationContext {

    @Value("\${publicUrl}")
    lateinit var publicUrl: String

    @Bean
    fun indexingProgressCallbackFactory(): IndexingProgressCallbackFactory {
        return IndexingProgressCallbackFactory(publicUrl)
    }

}
