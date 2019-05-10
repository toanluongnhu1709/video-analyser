package com.boclips.videoanalyser.application

import com.boclips.events.config.Subscriptions
import com.boclips.events.config.Topics
import com.boclips.events.types.VideoAnalysisRequested
import com.boclips.videoanalyser.domain.VideoAnalyserService
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder

class AnalyseVideo(
        private val videoAnalyserService: VideoAnalyserService,
        private val topics: Topics
) {
    companion object : KLogging()

    @StreamListener(Subscriptions.VIDEO_ANALYSIS_REQUESTED)
    fun execute(videoAnalysisRequested: VideoAnalysisRequested) {
        val videoId = videoAnalysisRequested.videoId

        logger.info { "Video $videoId received to analyse (language: ${videoAnalysisRequested.language?.toLanguageTag() ?: "detect"})" }

        val alreadyAnalysed = try {
            videoAnalyserService.isAnalysed(videoId)
        } catch(e: Exception) {
            logger.warn(e) { "Check if video $videoId is already analysed failed and will not be retried." }
            return
        }

        if(alreadyAnalysed) {
            logger.info { "Video $videoId has already been analysed. Enqueuing it to be retrieved." }
            topics.videoIndexed().send(MessageBuilder.withPayload(videoId).build())
            return
        }

        try {
            videoAnalyserService.submitVideo(videoId, videoAnalysisRequested.videoUrl, videoAnalysisRequested.language)
        } catch (e: Exception) {
            logger.warn(e) { "Submission of video $videoId to the analyser failed and will not be retried." }
        }
    }
}
