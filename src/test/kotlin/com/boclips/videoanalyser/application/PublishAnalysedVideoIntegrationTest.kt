package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.config.AnalysedVideosTopic
import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.messaging.support.MessageBuilder


class PublishAnalysedVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var publishAnalysedVideo : PublishAnalysedVideo

    @Autowired
    lateinit var messageCollector: MessageCollector

    @Autowired
    lateinit var analysedVideosTopic: AnalysedVideosTopic

    @Test
    fun `videos are received from the video indexer and published`() {
        publishAnalysedVideo.execute(videoId = "1234")

        val message = messageCollector.forChannel(analysedVideosTopic.output()).poll()

        Assertions.assertThat(message.payload.toString()).contains("1234")
        Assertions.assertThat(message.payload.toString()).contains("en-GB")
    }
}