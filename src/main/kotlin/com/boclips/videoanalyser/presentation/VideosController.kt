package com.boclips.videoanalyser.presentation

import com.boclips.eventtypes.VideoToAnalyse
import com.boclips.videoanalyser.application.AnalyseVideo
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class VideosController(
        val videoIndexer: VideoIndexer,
        val analyseVideo: AnalyseVideo
) {
    companion object : KLogging() {
        const val VIDEO_PATH_TEMPLATE = "/v1/videos/{videoId}"
        const val INDEXING_PROGRESS_PATH_TEMPLATE = "$VIDEO_PATH_TEMPLATE/check_indexing_progress"
    }

    @PostMapping("$VIDEO_PATH_TEMPLATE/analyse")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun analyse(@PathVariable videoId: String, @RequestParam videoUrl: String) {
        analyseVideo.execute(VideoToAnalyse.builder().videoId(videoId).videoUrl(videoUrl).build())
    }

    @PostMapping(INDEXING_PROGRESS_PATH_TEMPLATE)
    fun checkIndexingProgress(@PathVariable videoId: String) {
        logger.info { "Checking for indexing progress in video $videoId" }

        val videoIndex = videoIndexer.getVideoIndex(videoId)

        logger.info { "Video index keywords: ${videoIndex.keywords}, video index topics: ${videoIndex.topics}, video index captions: ${videoIndex.vttCaptions}" }
    }
}

