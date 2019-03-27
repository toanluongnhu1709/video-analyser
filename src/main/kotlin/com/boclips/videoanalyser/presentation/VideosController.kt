package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import mu.KLogging
import org.springframework.web.bind.annotation.*

@RestController
class VideosController(val videoIndexer: VideoIndexer) {

    companion object : KLogging() {
        const val INDEXING_PROGRESS_PATH_TEMPLATE = "/v1/videos/{videoId}/check_indexing_progress"
    }

    @PostMapping(INDEXING_PROGRESS_PATH_TEMPLATE)
    fun checkIndexingProgress(@PathVariable videoId: String) {
        logger.info { "Checking for indexing progress in video $videoId" }

        val videoIndex = videoIndexer.getVideoIndex(videoId)

        logger.info { "Video index keywords: ${videoIndex.keywords}, video index topics: ${videoIndex.topics}" }
    }
}

