package com.boclips.videoanalyser.application

import com.boclips.events.config.Subscriptions
import com.boclips.events.config.Topics
import com.boclips.videoanalyser.domain.VideoAnalyserService
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder

class PublishVideoAnalysed(
        private val topics: Topics,
        private val videoAnalyserService: VideoAnalyserService
) {
    companion object : KLogging()

    @StreamListener(Subscriptions.VIDEO_INDEXED)
    fun execute(videoId: String) {
        logger.info { "Requesting analysed video $videoId" }
        val video = try {
            videoAnalyserService.getVideo(videoId)
        } catch(e: Exception) {
            logger.warn(e) { "Request of analysed video $videoId failed and will not be retried." }
            return
        }

        logger.info { "Publishing analysed video $videoId" }
        topics.videoAnalysed().send(MessageBuilder.withPayload(video).build())

        logger.info { "Deleting source file of analysed video $videoId"}
        try {
            videoAnalyserService.deleteSourceFile(videoId)
        } catch(e: Exception) {
            logger.error(e) { "Error deleting source file for video $videoId" }
        }
    }
}
