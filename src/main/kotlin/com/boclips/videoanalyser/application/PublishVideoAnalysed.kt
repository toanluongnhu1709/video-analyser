package com.boclips.videoanalyser.application

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.EventBus
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.infrastructure.Delayer
import com.boclips.videoanalyser.infrastructure.videoindexer.CouldNotGetVideoAnalysisException
import mu.KLogging
import kotlin.random.Random

class PublishVideoAnalysed(
    private val eventBus: EventBus,
    private val videoAnalyserService: VideoAnalyserService,
    private val delayer: Delayer
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun execute(videoIndexed: VideoIndexed) {
        val videoId = videoIndexed.videoId!!
        logger.info { "Requesting analysed video $videoId" }
        val video = try {
            videoAnalyserService.getVideo(videoId)
        } catch (e: CouldNotGetVideoAnalysisException) {
            if (e.becauseOfThirdPartyLimits) {
                logger.warn(e) { "Repeating event for video: $videoId" }
                delayer.delay(Random.nextLong(60000)) {
                    eventBus.publish(VideoIndexed(videoId = videoId))
                }
            }
            return
        } catch (e: Exception) {
            logger.warn(e) { "Request of analysed video $videoId failed. Deleting." }
            videoAnalyserService.deleteVideo(videoId)
            return
        }

        if (video == null) {
            logger.info { "NOT publishing video $videoId because it is not ready." }
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
