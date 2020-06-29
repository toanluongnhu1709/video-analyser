package com.boclips.videoanalyser.config.application

import com.boclips.eventbus.EventBus
import com.boclips.videoanalyser.application.AnalyseVideo
import com.boclips.videoanalyser.application.PublishVideoAnalysed
import com.boclips.videoanalyser.application.PublishVideoIndexed
import com.boclips.videoanalyser.application.RetryVideoAnalysis
import com.boclips.videoanalyser.domain.VideoAnalyserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationContext(
    private val eventBus: EventBus,
    private val videoAnalyserService: VideoAnalyserService
) {

    @Bean
    fun analyseVideo(): AnalyseVideo {
        return AnalyseVideo(videoAnalyserService, eventBus)
    }

    @Bean
    fun retryVideoAnalysis(
        analyseVideo: AnalyseVideo
    ): RetryVideoAnalysis {
        return RetryVideoAnalysis(videoAnalyserService, analyseVideo)
    }

    @Bean
    fun publishAnalysedVideo(): PublishVideoAnalysed {
        return PublishVideoAnalysed(eventBus, videoAnalyserService)
    }

    @Bean
    fun publishAnalysedVideoId(): PublishVideoIndexed {
        return PublishVideoIndexed(eventBus)
    }
}
