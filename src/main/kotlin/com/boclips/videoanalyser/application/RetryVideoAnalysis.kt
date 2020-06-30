package com.boclips.videoanalyser.application

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.video.RetryVideoAnalysisRequested
import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videoanalyser.domain.VideoAnalyserService
import mu.KLogging

class RetryVideoAnalysis(
    private val videoAnalyserService: VideoAnalyserService,
    private val analyseVideo: AnalyseVideo
) {

    companion object : KLogging() {
        private const val MAX_RETRY = 3
    }


    @BoclipsEventListener
    fun execute(request: RetryVideoAnalysisRequested) {
        val videoId = request.videoId
        logger.info { "Video $videoId received to retry analysis (language: ${request.language?.toLanguageTag() ?: "detect"})" }

        try {
            if (videoAnalyserService.isAnalysed(videoId)) {
                videoAnalyserService.deleteVideo(videoId)
                waitUntilVideoIsDeleted(videoId)
            }
            analyseVideo.execute(VideoAnalysisRequested(videoId, request.videoUrl, request.language))
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
