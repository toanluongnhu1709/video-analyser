package com.boclips.videoanalyser.application

import com.boclips.eventtypes.Topics.VIDEOS_TO_ANALYSE_SUBSCRIPTION
import com.boclips.eventtypes.VideoToAnalyse
import com.boclips.videoanalyser.domain.VideoAnalyserService
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener

class AnalyseVideo(private val videoAnalyserService: VideoAnalyserService) {

    companion object : KLogging()

    @StreamListener(VIDEOS_TO_ANALYSE_SUBSCRIPTION)
    fun execute(videoToAnalyse: VideoToAnalyse) {
        try {
            videoAnalyserService.submitVideo(videoToAnalyse.videoId, videoToAnalyse.videoUrl)
        } catch (e: Exception) {
            logger.error { "Submission of video ${videoToAnalyse.videoId} to Video Indexer failed and will not be retried." }
        }
    }
}
