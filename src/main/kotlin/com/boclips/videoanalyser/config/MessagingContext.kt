package com.boclips.videoanalyser.config

import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

@Configuration
@EnableBinding(Topics::class, Subscriptions::class)
class MessagingContext

interface Topics {

    @Output(ANALYSED_VIDEO_IDS)
    fun analysedVideoIds(): MessageChannel

    @Output(ANALYSED_VIDEOS)
    fun analysedVideos(): MessageChannel

    companion object {
        const val ANALYSED_VIDEO_IDS = "analysed-video-ids-topic"
        const val ANALYSED_VIDEOS = "analysed-videos-topic"
    }
}

interface Subscriptions {

    @Input(ANALYSED_VIDEO_IDS)
    fun analysedVideoIds(): SubscribableChannel

    @Input(VIDEOS_TO_ANALYSE)
    fun videosToAnalyse(): SubscribableChannel

    companion object {
        const val ANALYSED_VIDEO_IDS = "analysed-video-ids-subscription"
        const val VIDEOS_TO_ANALYSE = "videos-to-analyse-subscription"
    }

}
