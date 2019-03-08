package com.boclips.videoanalyser.domain

import com.boclips.videoanalyser.config.Streams
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service

@Service
class VideoAnalyser {

    @StreamListener(Streams.VIDEOS_TO_ANALYSE_INPUT)
    fun analyseVideo(video: Video) {
        print("!!!!!!!!!!!")
        println(video)
    }


}