package com.boclips.videoanalyser.application

import com.boclips.events.config.Subscriptions
import com.boclips.events.config.Topics
import com.boclips.events.types.VideoToAnalyse
import com.boclips.videoanalyser.domain.VideoAnalyserService
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder

class AnalyseVideo(
        private val videoAnalyserService: VideoAnalyserService,
        private val topics: Topics
) {
    companion object : KLogging()

    @StreamListener(Subscriptions.VIDEOS_TO_ANALYSE)
    fun execute(videoToAnalyse: VideoToAnalyse) {
        val videoId = videoToAnalyse.videoId

        logger.info { "Video $videoId received to analyse (language: ${videoToAnalyse.language?.toLanguageTag() ?: "detect"})" }

        val alreadyAnalysed = try {
            videoAnalyserService.isAnalysed(videoId)
        } catch(e: Exception) {
            logger.warn(e) { "Check if video $videoId is already analysed failed and will not be retried." }
            return
        }

        if(alreadyAnalysed) {
            logger.info { "Video $videoId has already been analysed. Enqueuing it to be retrieved." }
            topics.analysedVideoIds().send(MessageBuilder.withPayload(videoId).build())
            return
        }

        try {
            videoAnalyserService.submitVideo(videoId, videoToAnalyse.videoUrl, videoToAnalyse.language)
        } catch (e: Exception) {
            logger.warn(e) { "Submission of video $videoId to the analyser failed and will not be retried." }
        }
    }
}
