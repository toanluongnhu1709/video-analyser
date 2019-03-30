package com.boclips.videoanalyser.config

import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

@Configuration
@EnableBinding(VideosToAnalyseTopic::class, AnalysedVideosTopic::class, AnalysedVideoIdsTopic::class, AnalysedVideoIdsSubscription::class)
class MessagingContext

interface VideosToAnalyseTopic {

    @Input(VideosToAnalyseTopic.INPUT)
    fun input(): SubscribableChannel

    companion object {
        const val INPUT = "videos-to-analyse"
    }
}

interface AnalysedVideosTopic {

    @Output(AnalysedVideosTopic.OUTPUT)
    fun output(): MessageChannel

    companion object {
        const val OUTPUT = "analysed-videos"
    }
}

interface AnalysedVideoIdsTopic {

    @Output(AnalysedVideoIdsTopic.OUTPUT)
    fun output(): MessageChannel

    companion object {
        const val OUTPUT = "analysed-video-ids-topic"
    }
}

interface AnalysedVideoIdsSubscription {

    @Input(AnalysedVideoIdsSubscription.INPUT)
    fun input(): SubscribableChannel

    companion object {
        const val INPUT = "analysed-video-ids-subscription"
    }
}
