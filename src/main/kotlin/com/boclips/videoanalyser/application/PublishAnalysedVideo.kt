package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.config.AnalysedVideosTopic
import com.boclips.videoanalyser.domain.VideoAnalyserService
import org.springframework.messaging.support.MessageBuilder

class PublishAnalysedVideo(
        private val analysedVideosTopic: AnalysedVideosTopic,
        private val videoAnalyserService: VideoAnalyserService
) {
    fun execute(videoId: String) {
        val video = videoAnalyserService.getVideo(videoId)

        analysedVideosTopic.output().send(MessageBuilder.withPayload(video).build())
    }
}
