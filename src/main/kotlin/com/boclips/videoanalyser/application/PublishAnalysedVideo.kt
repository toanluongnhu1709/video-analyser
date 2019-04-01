package com.boclips.videoanalyser.application

import com.boclips.eventtypes.Topics.ANALYSED_VIDEO_IDS_SUBSCRIPTION
import com.boclips.videoanalyser.config.messaging.Topics
import com.boclips.videoanalyser.domain.VideoAnalyserService
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder

class PublishAnalysedVideo(
        private val topics: Topics,
        private val videoAnalyserService: VideoAnalyserService
) {
    companion object : KLogging()

    @StreamListener(ANALYSED_VIDEO_IDS_SUBSCRIPTION)
    fun execute(videoId: String) {
        logger.info { "Requesting analysed video $videoId" }
        val video = try {
            videoAnalyserService.getVideo(videoId)
        } catch(e: Exception) {
            logger.warn(e) { "Request of analysed video $videoId failed and will not be retried." }
            return
        }

        logger.info { "Publishing analysed video $videoId" }
        topics.analysedVideos().send(MessageBuilder.withPayload(video).build())
    }
}
