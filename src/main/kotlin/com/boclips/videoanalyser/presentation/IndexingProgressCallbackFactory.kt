package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.presentation.VideosController.Companion.INDEXING_PROGRESS_PATH_TEMPLATE
import org.springframework.web.util.UriComponentsBuilder

class IndexingProgressCallbackFactory(private val baseUrl: String) {

    fun forVideo(videoId: String): String {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path(INDEXING_PROGRESS_PATH_TEMPLATE)
                .buildAndExpand(mapOf("videoId" to videoId))
                .toUriString()
    }
}
