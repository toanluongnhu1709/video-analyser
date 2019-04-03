package com.boclips.videoanalyser.application

import com.boclips.events.types.VideoToAnalyse
import com.boclips.videoanalyser.config.messaging.Subscriptions
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import java.lang.RuntimeException

class AnalyseVideoIntegrationTest : AbstractSpringIntegrationTest() {

    lateinit var videoToAnalyse: VideoToAnalyse

    @BeforeEach
    fun setUp() {
        videoToAnalyse = VideoToAnalyse.builder()
                .videoId("1")
                .videoUrl("http://vid.eo/1.mp4")
                .build()
    }

    @Test
    fun `videos get submitted to video indexer if not indexed yet`() {
        subscriptions.videosToAnalyse().send(MessageBuilder.withPayload(videoToAnalyse).build())

        assertThat(fakeVideoIndexer.submittedVideo("1")).isEqualTo("http://vid.eo/1.mp4")
    }

    @Test
    fun `videos NOT published as analysed if not indexed yet`() {
        subscriptions.videosToAnalyse().send(MessageBuilder.withPayload(videoToAnalyse).build())

        val analysedVideoIdMessage = messageCollector.forChannel(topics.analysedVideoIds()).poll()

        assertThat(analysedVideoIdMessage).isNull()
    }

    @Test
    fun `videos do NOT get submitted to video indexer if already indexed`() {
        fakeVideoIndexer.submitVideo("1", "http://old.url")

        subscriptions.videosToAnalyse().send(MessageBuilder.withPayload(videoToAnalyse).build())

        assertThat(fakeVideoIndexer.submittedVideo("1")).isEqualTo("http://old.url")
    }

    @Test
    fun `video id immediately published as analysed if already indexed`() {
        fakeVideoIndexer.submitVideo("1", "http://old.url")

        subscriptions.videosToAnalyse().send(MessageBuilder.withPayload(videoToAnalyse).build())

        val analysedVideoIdMessage = messageCollector.forChannel(topics.analysedVideoIds()).poll()

        assertThat(analysedVideoIdMessage.payload.toString()).isEqualTo("1")
    }

    @Test
    fun `video indexer exceptions during submission are handled`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.submitVideo(any(), any())).thenThrow(RuntimeException("something went wrong"))

        val analyseVideo = AnalyseVideo(videoAnalyserService, topics)

        assertThatCode { analyseVideo.execute(videoToAnalyse) }.doesNotThrowAnyException()
    }

    @Test
    fun `video indexer exceptions during lookup are handled`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.isAnalysed(any())).thenThrow(RuntimeException("something went wrong"))

        val analyseVideo = AnalyseVideo(videoAnalyserService, topics)

        assertThatCode { analyseVideo.execute(videoToAnalyse) }.doesNotThrowAnyException()
    }
}
