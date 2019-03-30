package com.boclips.videoanalyser.config.messaging

import com.boclips.eventtypes.Topics.ANALYSED_VIDEOS_TOPIC
import com.boclips.eventtypes.Topics.ANALYSED_VIDEO_IDS_TOPIC
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel

interface Topics {

    @Output(ANALYSED_VIDEO_IDS_TOPIC)
    fun analysedVideoIds(): MessageChannel

    @Output(ANALYSED_VIDEOS_TOPIC)
    fun analysedVideos(): MessageChannel

}
