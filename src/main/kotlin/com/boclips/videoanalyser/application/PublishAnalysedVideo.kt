package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.config.AnalysedVideosTopic
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoResourceToAnalysedVideoConverter
import org.springframework.messaging.support.MessageBuilder

class PublishAnalysedVideo(
        private val analysedVideosTopic: AnalysedVideosTopic,
        private val videoIndexer: VideoIndexer
) {

    fun execute(videoId: String) {
        val videoResource = videoIndexer.getVideoIndex(videoId = videoId)

        val analysedVideo = VideoResourceToAnalysedVideoConverter.convert(videoResource)

        analysedVideosTopic.output().send(MessageBuilder.withPayload(analysedVideo).build())
    }
}