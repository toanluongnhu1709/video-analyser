package com.boclips.videoanalyser.application

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.EventBus
import com.boclips.eventbus.events.video.RetryVideoAnalysisRequested
import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.infrastructure.Delayer
import com.boclips.videoanalyser.infrastructure.videoindexer.CouldNotGetVideoAnalysisException
import mu.KLogging
import kotlin.random.Random

class RetryVideoAnalysis(
    private val videoAnalyserService: VideoAnalyserService,
    private val analyseVideo: AnalyseVideo,
    private val delayer: Delayer,
    private val eventBus: EventBus
) {

    companion object : KLogging() {
        private const val MAX_RETRY = 3
    }


    @BoclipsEventListener
    fun execute(retryVideoAnalysisRequest: RetryVideoAnalysisRequested) {
        val videoId = retryVideoAnalysisRequest.videoId
        logger.info { "Video $videoId received to retry analysis (language: ${retryVideoAnalysisRequest.language?.toLanguageTag() ?: "detect"})" }

        try {
            if (videoAnalyserService.isAnalysed(videoId)) {
                videoAnalyserService.deleteVideo(videoId)
                waitUntilVideoIsDeleted(videoId)
            }
            analyseVideo.execute(
                VideoAnalysisRequested(
                    videoId,
                    retryVideoAnalysisRequest.videoUrl,
                    retryVideoAnalysisRequest.language
                )
            )
        } catch (e: CouldNotGetVideoAnalysisException) {
            if (e.becauseOfThirdPartyLimits) {
                logger.warn(e) { "Re-publishing VideoAnalysisRequested event for video: $videoId" }
                delayer.delay(Random.nextLong(60000)) {
                    eventBus.publish(retryVideoAnalysisRequest)
                }
            }
            return
        } catch (e: Exception) {
            logger.warn(e) { "Retry analysis of video $videoId failed and will not be retried." }
        }
    }

    private fun waitUntilVideoIsDeleted(videoId: String) {
        var retries = 0

        while (videoAnalyserService.isAnalysed(videoId) && retries < MAX_RETRY) {
            logger.info { "Waiting for video $videoId to be removed from the index attempt: ${retries + 1}" }
            retries++

            if (retries < MAX_RETRY) {
                Thread.sleep(1000)
            } else {
                logger.warn { "Video $videoId still not removed from the index, giving up retry attempt" }
                throw RuntimeException()
            }
        }
    }
}
