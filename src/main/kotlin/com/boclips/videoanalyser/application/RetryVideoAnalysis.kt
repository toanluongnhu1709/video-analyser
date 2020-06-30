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

    companion object : KLogging()

    @BoclipsEventListener
    fun execute(request: RetryVideoAnalysisRequested) {
        val videoId = request.videoId

        AnalyseVideo.logger.info { "Video $videoId received to retry analysis (language: ${request.language?.toLanguageTag() ?: "detect"})" }

        try {
            if (videoAnalyserService.isAnalysed(videoId)) {
                videoAnalyserService.deleteVideo(videoId)
            }
            analyseVideo.execute(VideoAnalysisRequested(videoId, request.videoUrl, request.language))
        } catch (e: Exception) {
            AnalyseVideo.logger.warn(e) { "Retry analysis of video $videoId failed and will not be retried." }
        }
    }
}
