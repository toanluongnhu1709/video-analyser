package com.boclips.videoanalyser.application

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.EventBus
import com.boclips.videoanalyser.domain.VideoAnalyserService
import mu.KLogging

class PublishVideoAnalysed(
        private val eventBus: EventBus,
        private val videoAnalyserService: VideoAnalyserService
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun execute(videoIndexed: VideoIndexed) {
        val videoId = videoIndexed.videoId!!
        logger.info { "Requesting analysed video $videoId" }
        val video = try {
            videoAnalyserService.getVideo(videoId)
        } catch (e: Exception) {
            logger.warn(e) { "Request of analysed video $videoId failed. Deleting." }
            videoAnalyserService.deleteVideo(videoId)
            return
        }

        logger.info { "Publishing analysed video $videoId" }
        eventBus.publish(video)

        logger.info { "Deleting source file of analysed video $videoId" }
        try {
            videoAnalyserService.deleteSourceFile(videoId)
        } catch (e: Exception) {
            logger.error(e) { "Error deleting source file for video $videoId" }
        }
    }
}
