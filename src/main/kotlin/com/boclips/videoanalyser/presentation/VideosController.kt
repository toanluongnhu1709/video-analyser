package com.boclips.videoanalyser.presentation

import com.boclips.events.types.VideoAnalysisRequested
import com.boclips.videoanalyser.application.AnalyseVideo
import com.boclips.videoanalyser.application.PublishVideoIndexed
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class VideosController(
        val analyseVideo: AnalyseVideo,
        val publishAnalysedVideoId: PublishVideoIndexed
) {
    companion object {
        const val VIDEO_PATH_TEMPLATE = "/v1/videos/{videoId}"
        const val PUBLISH_ANALYSED_VIDEO_PATH_TEMPLATE = "$VIDEO_PATH_TEMPLATE/publish_analysed_video"
    }

    @PostMapping("$VIDEO_PATH_TEMPLATE/analyse")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun analyse(@PathVariable videoId: String, @RequestParam videoUrl: String) {
        analyseVideo.execute(VideoAnalysisRequested.builder().videoId(videoId).videoUrl(videoUrl).build())
    }

    @PostMapping(PUBLISH_ANALYSED_VIDEO_PATH_TEMPLATE)
    fun publishAnalysedVideo(@PathVariable videoId: String) {
        publishAnalysedVideoId.execute(videoId)
    }
}

