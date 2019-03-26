package com.boclips.videoanalyser.presentation

import mu.KLogging
import org.springframework.web.bind.annotation.*

@RestController
class VideosController {

    companion object : KLogging() {
        const val INDEXING_PROGRESS_PATH_TEMPLATE = "/v1/videos/{videoId}/check_indexing_progress"
    }

    @PostMapping(INDEXING_PROGRESS_PATH_TEMPLATE)
    fun checkIndexingProgress(@PathVariable videoId: String, @RequestParam id: String, @RequestParam state: String) {
        logger.info { "Checking for indexing progress in video $videoId: VideoIndexer id $id, state $state" }
    }
}

