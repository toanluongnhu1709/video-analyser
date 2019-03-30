package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.config.AnalysedVideoIdsSubscription
import com.boclips.videoanalyser.config.AnalysedVideosTopic
import com.boclips.videoanalyser.domain.VideoAnalyserService
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder

class PublishAnalysedVideo(
        private val analysedVideosTopic: AnalysedVideosTopic,
        private val videoAnalyserService: VideoAnalyserService
) {
    @StreamListener(AnalysedVideoIdsSubscription.INPUT)
    fun execute(videoId: String) {
        val video = videoAnalyserService.getVideo(videoId)

        analysedVideosTopic.output().send(MessageBuilder.withPayload(video).build())
    }
}
