package com.boclips.videoanalyser.application

import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.messaging.support.MessageBuilder
import java.lang.RuntimeException

class PublishAnalysedVideoIntegrationTest : AbstractSpringIntegrationTest() {

    @Test
    fun `videos are received from the video indexer and published`() {
        subscriptions.analysedVideoIds().send(MessageBuilder.withPayload("1234").build())

        val message = messageCollector.forChannel(topics.analysedVideos()).poll()

        Assertions.assertThat(message.payload.toString()).contains("1234")
        Assertions.assertThat(message.payload.toString()).contains("en-GB")
    }

    @Test
    fun `exceptions when getting videos are handled`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.getVideo(any())).thenThrow(RuntimeException("something went wrong"))

        val publishAnalysedVideo = PublishAnalysedVideo(topics, videoAnalyserService)

        Assertions.assertThatCode { publishAnalysedVideo.execute("video id") }.doesNotThrowAnyException()
    }
}
