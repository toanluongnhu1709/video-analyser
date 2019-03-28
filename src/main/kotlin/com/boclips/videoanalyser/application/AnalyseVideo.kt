package com.boclips.videoanalyser.application

import com.boclips.eventtypes.VideoToAnalyse
import com.boclips.videoanalyser.config.VideosToAnalyseTopic
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import mu.KLogging
import org.springframework.cloud.stream.annotation.StreamListener
import java.lang.Exception

class AnalyseVideo(private val videoIndexer: VideoIndexer) {

    companion object : KLogging()

    @StreamListener(VideosToAnalyseTopic.INPUT)
    fun execute(videoToAnalyse: VideoToAnalyse) {
        try {
            videoIndexer.submitVideo(videoToAnalyse.videoId, videoToAnalyse.videoUrl)
        } catch(e: Exception) {
            logger.error { "Submission of video ${videoToAnalyse.videoId} to Video Indexer failed and will not be retried." }
        }
    }
}
