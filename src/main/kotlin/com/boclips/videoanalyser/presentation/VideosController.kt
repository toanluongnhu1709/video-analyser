package com.boclips.videoanalyser.presentation

import mu.KLogging
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/videos")
class VideosController {

    companion object : KLogging()

    @PostMapping("/{videoId}/check_indexing_progress")
    fun checkIndexingProgress(@PathVariable videoId: String, @RequestParam id: String, @RequestParam state: String) {
        logger.info { "Checking for indexing progress in video $videoId: VideoIndexer id $id, state $state" }
    }
}
