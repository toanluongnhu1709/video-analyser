package com.boclips.videoanalyser.application

import com.boclips.eventbus.EventBus
import mu.KLogging

class PublishVideoIndexed(private val eventBus: EventBus) {

    companion object : KLogging()

    fun execute(videoId: String) {
        logger.info { "Video $videoId has been analysed" }
        eventBus.publish(VideoIndexed(videoId = videoId))
    }
}
