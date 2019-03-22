package com.boclips.videoanalyser.config.application

import com.boclips.videoanalyser.application.AnalyseVideo
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationContext(private val videoIndexer: VideoIndexer) {

    @Bean
    fun analyseVideo(): AnalyseVideo {
        return AnalyseVideo(videoIndexer)
    }
}
