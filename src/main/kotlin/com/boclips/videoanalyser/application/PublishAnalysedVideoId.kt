package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.config.messaging.Topics
import mu.KLogging
import org.springframework.messaging.support.MessageBuilder

class PublishAnalysedVideoId(private val topics: Topics) {

    companion object : KLogging()

    fun execute(videoId: String) {
        logger.info { "Video $videoId has been analysed" }
        topics.analysedVideoIds().send(MessageBuilder.withPayload(videoId).build())
    }
}
