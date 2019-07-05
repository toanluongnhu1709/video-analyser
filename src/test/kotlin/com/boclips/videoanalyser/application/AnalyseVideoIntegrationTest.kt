package com.boclips.videoanalyser.application

import com.boclips.events.types.video.VideoAnalysisRequested
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.messaging.support.MessageBuilder
import java.lang.RuntimeException
import java.util.*

class AnalyseVideoIntegrationTest : AbstractSpringIntegrationTest() {

    lateinit var videoAnalysisRequested: VideoAnalysisRequested

    @BeforeEach
    fun setUp() {
        videoAnalysisRequested = VideoAnalysisRequested.builder()
                .videoId("1")
                .videoUrl("http://vid.eo/1.mp4")
                .language(Locale.ENGLISH)
                .build()
    }

    @Test
    fun `videos get submitted to video indexer if not indexed yet`() {
        subscriptions.videoAnalysisRequested().send(MessageBuilder.withPayload(videoAnalysisRequested).build())

        assertThat(fakeVideoIndexer.submittedVideo("1")?.videoUrl).isEqualTo("http://vid.eo/1.mp4")
        assertThat(fakeVideoIndexer.submittedVideo("1")?.language).isEqualTo(Locale.ENGLISH)
    }

    @Test
    fun `videos NOT published as analysed if not indexed yet`() {
        subscriptions.videoAnalysisRequested().send(MessageBuilder.withPayload(videoAnalysisRequested).build())

        val analysedVideoIdMessage = messageCollector.forChannel(topics.videoIndexed()).poll()

        assertThat(analysedVideoIdMessage).isNull()
    }

    @Test
    fun `videos do NOT get submitted to video indexer if already indexed`() {
        fakeVideoIndexer.submitVideo("1", "http://old.url", language = null)

        subscriptions.videoAnalysisRequested().send(MessageBuilder.withPayload(videoAnalysisRequested).build())

        assertThat(fakeVideoIndexer.submittedVideo("1")?.videoUrl).isEqualTo("http://old.url")
    }

    @Test
    fun `video id immediately published as analysed if already indexed`() {
        fakeVideoIndexer.submitVideo("1", "http://old.url", language = null)

        subscriptions.videoAnalysisRequested().send(MessageBuilder.withPayload(videoAnalysisRequested).build())

        val analysedVideoIdMessage = messageCollector.forChannel(topics.videoIndexed()).poll()

        assertThat(analysedVideoIdMessage.payload.toString()).isEqualTo("1")
    }

    @Test
    fun `video indexer exceptions during submission are handled`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.submitVideo(any(), any(), any())).thenThrow(RuntimeException("something went wrong"))

        val analyseVideo = AnalyseVideo(videoAnalyserService, topics)

        assertThatCode { analyseVideo.execute(videoAnalysisRequested) }.doesNotThrowAnyException()
    }

    @Test
    fun `video indexer exceptions during lookup are handled`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.isAnalysed(any())).thenThrow(RuntimeException("something went wrong"))

        val analyseVideo = AnalyseVideo(videoAnalyserService, topics)

        assertThatCode { analyseVideo.execute(videoAnalysisRequested) }.doesNotThrowAnyException()
    }
}
