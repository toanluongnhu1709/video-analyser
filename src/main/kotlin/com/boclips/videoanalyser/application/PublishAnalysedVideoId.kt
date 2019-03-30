package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.config.AnalysedVideoIdsTopic
import org.springframework.messaging.support.MessageBuilder

class PublishAnalysedVideoId(
        private val analysedVideoIdsTopic: AnalysedVideoIdsTopic
) {

    fun execute(videoId: String) {
        analysedVideoIdsTopic.output().send(MessageBuilder.withPayload(videoId).build())
    }
}
