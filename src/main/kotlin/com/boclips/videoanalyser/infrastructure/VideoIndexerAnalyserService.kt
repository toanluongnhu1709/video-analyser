package com.boclips.videoanalyser.infrastructure

import com.boclips.eventbus.events.video.VideoAnalysed
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexerException
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexItemResource
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoResourceToAnalysedVideoConverter
import mu.KLogging
import java.util.*

class VideoIndexerAnalyserService(private val videoIndexer: VideoIndexer) : VideoAnalyserService {

    companion object : KLogging()

    override fun isAnalysed(videoId: String): Boolean {
        return videoIndexer.isIndexed(videoId)
    }

    override fun submitVideo(videoId: String, videoUrl: String, language: Locale?) {
        videoIndexer.submitVideo(videoId, videoUrl, language)
    }

    override fun getVideo(videoId: String): VideoAnalysed? {
        val videoResource = videoIndexer.getVideo(videoId) ?: return null

        try {
            return VideoResourceToAnalysedVideoConverter.convert(videoResource)
        } catch (e: VideoIndexerException) {
            logger.error(e) { "Failed to parse video indexer response for video $videoId with body:\n${videoResource.index?.raw}" }
            throw VideoIndexerException("Failed to get video $videoId")
        }
    }

    override fun deleteSourceFile(videoId: String) {
        videoIndexer.deleteSourceFile(videoId)
    }

    override fun deleteVideo(videoId: String) {
        videoIndexer.deleteVideo(videoId)
    }
}
