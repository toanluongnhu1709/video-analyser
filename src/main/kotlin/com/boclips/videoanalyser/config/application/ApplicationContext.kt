package com.boclips.videoanalyser.config.application

import com.boclips.eventbus.EventBus
import com.boclips.videoanalyser.application.AnalyseVideo
import com.boclips.videoanalyser.application.PublishVideoAnalysed
import com.boclips.videoanalyser.application.PublishVideoIndexed
import com.boclips.videoanalyser.application.RetryVideoAnalysis
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.infrastructure.Delayer
import com.boclips.videoanalyser.infrastructure.RealDelayer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationContext(
    private val eventBus: EventBus,
    private val videoAnalyserService: VideoAnalyserService
) {
    @Bean
    fun delayer(): Delayer = RealDelayer()

    @Bean
    fun analyseVideo(): AnalyseVideo {
        return AnalyseVideo(videoAnalyserService, eventBus, delayer())
    }

    @Bean
    fun retryVideoAnalysis(
        analyseVideo: AnalyseVideo
    ): RetryVideoAnalysis {
        return RetryVideoAnalysis(videoAnalyserService, analyseVideo, delayer(), eventBus)
    }

    @Bean
    fun publishAnalysedVideo(): PublishVideoAnalysed {
        return PublishVideoAnalysed(eventBus, videoAnalyserService, delayer())
    }

    @Bean
    fun publishAnalysedVideoId(): PublishVideoIndexed {
        return PublishVideoIndexed(eventBus)
    }
}
