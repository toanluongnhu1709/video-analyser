package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.config.Subscriptions
import com.boclips.videoanalyser.config.Topics
import com.boclips.videoanalyser.domain.VideoAnalyserService
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder

class PublishAnalysedVideo(
        private val topics: Topics,
        private val videoAnalyserService: VideoAnalyserService
) {
    @StreamListener(Subscriptions.ANALYSED_VIDEO_IDS)
    fun execute(videoId: String) {
        val video = videoAnalyserService.getVideo(videoId)

        topics.analysedVideos().send(MessageBuilder.withPayload(video).build())
    }
}
