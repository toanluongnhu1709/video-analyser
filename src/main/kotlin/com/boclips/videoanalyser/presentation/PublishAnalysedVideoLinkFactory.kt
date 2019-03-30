package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.presentation.VideosController.Companion.PUBLISH_ANALYSED_VIDEO_PATH_TEMPLATE
import org.springframework.web.util.UriComponentsBuilder

class PublishAnalysedVideoLinkFactory(private val baseUrl: String) {

    fun forVideo(videoId: String): String {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path(PUBLISH_ANALYSED_VIDEO_PATH_TEMPLATE)
                .buildAndExpand(mapOf("videoId" to videoId))
                .toUriString()
    }
}
