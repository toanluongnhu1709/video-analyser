package com.boclips.videoanalyser.application

import com.boclips.eventtypes.Topics.ANALYSED_VIDEO_IDS_SUBSCRIPTION
import com.boclips.videoanalyser.config.messaging.Topics
import com.boclips.videoanalyser.domain.VideoAnalyserService
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder

class PublishAnalysedVideo(
        private val topics: Topics,
        private val videoAnalyserService: VideoAnalyserService
) {
    @StreamListener(ANALYSED_VIDEO_IDS_SUBSCRIPTION)
    fun execute(videoId: String) {
        val video = videoAnalyserService.getVideo(videoId)

        topics.analysedVideos().send(MessageBuilder.withPayload(video).build())
    }
}
