package com.boclips.videoanalyser.application

import com.boclips.events.config.Topics
import mu.KLogging
import org.springframework.messaging.support.MessageBuilder

class PublishVideoIndexed(private val topics: Topics) {

    companion object : KLogging()

    fun execute(videoId: String) {
        logger.info { "Video $videoId has been analysed" }
        topics.videoIndexed().send(MessageBuilder.withPayload(videoId).build())
    }
}
