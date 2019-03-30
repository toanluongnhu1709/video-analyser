package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.config.Topics
import org.springframework.messaging.support.MessageBuilder

class PublishAnalysedVideoId(private val topics: Topics) {

    fun execute(videoId: String) {
        topics.analysedVideoIds().send(MessageBuilder.withPayload(videoId).build())
    }
}
