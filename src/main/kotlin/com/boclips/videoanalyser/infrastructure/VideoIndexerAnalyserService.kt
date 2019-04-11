package com.boclips.videoanalyser.infrastructure

import com.boclips.events.types.AnalysedVideo
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoResourceToAnalysedVideoConverter
import java.util.*

class VideoIndexerAnalyserService(private val videoIndexer: VideoIndexer) : VideoAnalyserService {
    override fun isAnalysed(videoId: String): Boolean {
        return videoIndexer.isIndexed(videoId)
    }

    override fun submitVideo(videoId: String, videoUrl: String, language: Locale?) {
        videoIndexer.submitVideo(videoId, videoUrl, language)
    }

    override fun getVideo(videoId: String): AnalysedVideo {
        val videoResource = videoIndexer.getVideo(videoId)
        return VideoResourceToAnalysedVideoConverter.convert(videoResource)
    }

    override fun deleteSourceFile(videoId: String) {
        videoIndexer.deleteSourceFile(videoId)
    }

}
