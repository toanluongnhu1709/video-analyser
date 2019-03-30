package com.boclips.videoanalyser.application

import com.boclips.eventtypes.VideoToAnalyse
import com.boclips.videoanalyser.config.VideosToAnalyseTopic
import com.boclips.videoanalyser.domain.VideoAnalyserService
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener

class AnalyseVideo(private val videoAnalyserService: VideoAnalyserService) {

    companion object : KLogging()

    @StreamListener(VideosToAnalyseTopic.INPUT)
    fun execute(videoToAnalyse: VideoToAnalyse) {
        try {
            videoAnalyserService.submitVideo(videoToAnalyse.videoId, videoToAnalyse.videoUrl)
        } catch (e: Exception) {
            logger.error { "Submission of video ${videoToAnalyse.videoId} to Video Indexer failed and will not be retried." }
        }
    }
}
