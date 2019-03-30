package com.boclips.videoanalyser.config.application

import com.boclips.videoanalyser.application.AnalyseVideo
import com.boclips.videoanalyser.application.PublishAnalysedVideo
import com.boclips.videoanalyser.application.PublishAnalysedVideoId
import com.boclips.videoanalyser.config.messaging.Topics
import com.boclips.videoanalyser.domain.VideoAnalyserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationContext(
        private val topics: Topics,
        private val videoAnalyserService: VideoAnalyserService
) {

    @Bean
    fun analyseVideo(): AnalyseVideo {
        return AnalyseVideo(videoAnalyserService, topics)
    }

    @Bean
    fun publishAnalysedVideo(): PublishAnalysedVideo {
        return PublishAnalysedVideo(topics, videoAnalyserService)
    }

    @Bean
    fun publishAnalysedVideoId(): PublishAnalysedVideoId {
        return PublishAnalysedVideoId(topics)
    }
}
