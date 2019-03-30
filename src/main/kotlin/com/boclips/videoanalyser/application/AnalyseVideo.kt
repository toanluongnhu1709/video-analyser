package com.boclips.videoanalyser.application

import com.boclips.eventtypes.VideoToAnalyse
import com.boclips.videoanalyser.config.Subscriptions
import com.boclips.videoanalyser.domain.VideoAnalyserService
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener

class AnalyseVideo(private val videoAnalyserService: VideoAnalyserService) {

    companion object : KLogging()

    @StreamListener(Subscriptions.VIDEOS_TO_ANALYSE)
    fun execute(videoToAnalyse: VideoToAnalyse) {
        try {
            videoAnalyserService.submitVideo(videoToAnalyse.videoId, videoToAnalyse.videoUrl)
        } catch (e: Exception) {
            logger.error { "Submission of video ${videoToAnalyse.videoId} to Video Indexer failed and will not be retried." }
        }
    }
}
