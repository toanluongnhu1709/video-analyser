package com.boclips.videoanalyser.config.messaging

import com.boclips.eventtypes.Topics.ANALYSED_VIDEO_IDS_SUBSCRIPTION
import com.boclips.eventtypes.Topics.VIDEOS_TO_ANALYSE_SUBSCRIPTION
import org.springframework.cloud.stream.annotation.Input
import org.springframework.messaging.SubscribableChannel

interface Subscriptions {

    @Input(ANALYSED_VIDEO_IDS_SUBSCRIPTION)
    fun analysedVideoIds(): SubscribableChannel

    @Input(VIDEOS_TO_ANALYSE_SUBSCRIPTION)
    fun videosToAnalyse(): SubscribableChannel

}
