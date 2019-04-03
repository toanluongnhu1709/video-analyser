package com.boclips.videoanalyser.infrastructure

import com.boclips.events.types.AnalysedVideo
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoResourceToAnalysedVideoConverter

class VideoIndexerAnalyserService(private val videoIndexer: VideoIndexer) : VideoAnalyserService {

    override fun isAnalysed(videoId: String): Boolean {
        return videoIndexer.isIndexed(videoId)
    }

    override fun submitVideo(videoId: String, videoUrl: String) {
        videoIndexer.submitVideo(videoId, videoUrl)
    }

    override fun getVideo(videoId: String): AnalysedVideo {
        val videoResource = videoIndexer.getVideo(videoId)
        return VideoResourceToAnalysedVideoConverter.convert(videoResource)
    }

}
