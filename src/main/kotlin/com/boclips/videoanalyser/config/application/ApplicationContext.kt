package com.boclips.videoanalyser.config.application

import com.boclips.videoanalyser.application.AnalyseVideo
import com.boclips.videoanalyser.application.PublishAnalysedVideo
import com.boclips.videoanalyser.config.AnalysedVideosTopic
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationContext(
        private val analysedVideosTopic: AnalysedVideosTopic,
        private val videoAnalyserService: VideoAnalyserService
) {

    @Bean
    fun analyseVideo(): AnalyseVideo {
        return AnalyseVideo(videoAnalyserService)
    }

    @Bean
    fun publishAnalysedVideo() : PublishAnalysedVideo {
        return PublishAnalysedVideo(
                analysedVideosTopic,
                videoAnalyserService
        )
    }
}
