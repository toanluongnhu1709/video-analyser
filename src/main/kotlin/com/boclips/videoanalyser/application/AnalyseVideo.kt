package com.boclips.videoanalyser.application

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videoanalyser.domain.VideoAnalyserService
import mu.KLogging

class AnalyseVideo(
    private val videoAnalyserService: VideoAnalyserService
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun execute(videoAnalysisRequested: VideoAnalysisRequested) {
        val videoId = videoAnalysisRequested.videoId

        logger.info { "Video $videoId received to analyse (language: ${videoAnalysisRequested.language?.toLanguageTag() ?: "detect"})" }

        val alreadyAnalysed = try {
            videoAnalyserService.isAnalysed(videoId)
        } catch (e: Exception) {
            logger.warn(e) { "Check if video $videoId is already analysed failed and will not be retried." }
            return
        }

        if (alreadyAnalysed) {
            logger.info { "Video $videoId has already been analysed." }
            return
        } else {
            try {
                videoAnalyserService.submitVideo(videoId, videoAnalysisRequested.videoUrl, videoAnalysisRequested.language)
            } catch (e: Exception) {
                logger.warn(e) { "Submission of video $videoId to the analyser failed and will not be retried." }
            }
        }
    }
}
