package com.boclips.videoanalyser.infrastructure

import com.boclips.videoanalyser.config.Streams
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output

interface VideosToAnalyseChannels {

    @Input(Streams.VIDEOS_TO_ANALYSE_INPUT)
    fun inboundVideosToAnalyse(): SubscribableChannel

    @Output(Streams.VIDEOS_TO_ANALYSE_OUTPUT)
    fun outboundVideosToAnalyse(): MessageChannel
}