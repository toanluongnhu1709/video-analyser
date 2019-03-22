package com.boclips.videoanalyser.config

import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.SubscribableChannel

@Configuration
@EnableBinding(VideosToAnalyseTopic::class)
class MessagingContext

interface VideosToAnalyseTopic {

    @Input(VideosToAnalyseTopic.INPUT)
    fun input(): SubscribableChannel

    companion object {
        const val INPUT = "videos-to-analyse"
    }
}
