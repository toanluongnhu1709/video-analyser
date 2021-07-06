package com.boclips.videoanalyser.application

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.infrastructure.Delayer
import com.boclips.videoanalyser.infrastructure.videoindexer.CouldNotGetVideoAnalysisException
import mu.KLogging
import kotlin.random.Random

class AnalyseVideo(
    private val videoAnalyserService: VideoAnalyserService,
    private val eventBus: EventBus,
    private val delayer: Delayer

) {
    companion object : KLogging()

    @BoclipsEventListener
    fun execute(videoAnalysisRequested: VideoAnalysisRequested) {
        val videoId = videoAnalysisRequested.videoId

        logger.info { "Video $videoId received to analyse (language: ${videoAnalysisRequested.language?.toLanguageTag() ?: "detect"})" }

        val alreadyAnalysed = try {
            videoAnalyserService.isAnalysed(videoId)
        } catch (e: CouldNotGetVideoAnalysisException) {
            if (e.becauseOfThirdPartyLimits) {
                logger.warn(e) { "Re-publishing VideoAnalysisRequested event for video: $videoId" }
                delayer.delay(Random.nextLong(60000)) {
                    eventBus.publish(videoAnalysisRequested)
                }
            }
            return
        } catch (e: Exception) {
            logger.warn(e) { "Check if video $videoId is already analysed failed and will not be retried." }
            return
        }

        if (alreadyAnalysed) {
            logger.info { "Video $videoId has already been analysed. Enqueuing it to be retrieved." }
            eventBus.publish(VideoIndexed(videoId = videoId))
            return
        }

        try {
            videoAnalyserService.submitVideo(videoId, videoAnalysisRequested.videoUrl, videoAnalysisRequested.language)
        } catch (e: Exception) {
            logger.warn(e) { "Submission of video $videoId to the analyser failed and will not be retried." }
        }
    }
}
