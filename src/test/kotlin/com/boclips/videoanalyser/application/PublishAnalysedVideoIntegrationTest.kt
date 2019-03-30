package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.messaging.support.MessageBuilder

class PublishAnalysedVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Test
    fun `videos are received from the video indexer and published`() {
        subscriptions.analysedVideoIds().send(MessageBuilder.withPayload("1234").build())

        val message = messageCollector.forChannel(topics.analysedVideos()).poll()

        Assertions.assertThat(message.payload.toString()).contains("1234")
        Assertions.assertThat(message.payload.toString()).contains("en-GB")
    }
}
