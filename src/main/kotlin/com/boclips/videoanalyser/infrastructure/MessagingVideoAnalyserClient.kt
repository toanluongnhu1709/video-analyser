package com.boclips.videoanalyser.infrastructure

import com.boclips.videoanalyser.domain.Video
import com.boclips.videoanalyser.domain.VideoAnalyserClient
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.util.MimeTypeUtils

@Service
class MessagingVideoAnalyserClient(val videosToAnalyseChannels: VideosToAnalyseChannels) : VideoAnalyserClient {

    override fun analyseVideo(video: Video) {
        println("Sending a video...")
        val messageChannel = videosToAnalyseChannels.outboundVideosToAnalyse()
        println(messageChannel.toString())
        val sent = messageChannel.send(MessageBuilder
                .withPayload(video)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build())
        println(sent)
    }
}