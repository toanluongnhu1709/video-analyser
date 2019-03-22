package com.boclips.videoanalyser.application

import com.boclips.eventtypes.VideoToAnalyse
import com.boclips.videoanalyser.config.VideosToAnalyseTopic
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import org.springframework.cloud.stream.annotation.StreamListener

class AnalyseVideo(private val videoIndexer: VideoIndexer) {

    @StreamListener(VideosToAnalyseTopic.INPUT)
    fun execute(videoToAnalyse: VideoToAnalyse) {
        videoIndexer.submitVideo(videoToAnalyse.videoUrl)
    }
}
