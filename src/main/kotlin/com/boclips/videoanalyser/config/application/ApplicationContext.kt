package com.boclips.videoanalyser.config.application

import com.boclips.events.config.Topics
import com.boclips.videoanalyser.application.AnalyseVideo
import com.boclips.videoanalyser.application.PublishVideoAnalysed
import com.boclips.videoanalyser.application.PublishVideoIndexed
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
    fun publishAnalysedVideo(): PublishVideoAnalysed {
        return PublishVideoAnalysed(topics, videoAnalyserService)
    }

    @Bean
    fun publishAnalysedVideoId(): PublishVideoIndexed {
        return PublishVideoIndexed(topics)
    }
}
