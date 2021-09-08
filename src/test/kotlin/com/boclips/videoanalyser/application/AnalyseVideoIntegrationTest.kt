package com.boclips.videoanalyser.application

import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.infrastructure.videoindexer.CouldNotGetVideoAnalysisException
import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.fakes.FakeDelayer
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        eventBus.publish(videoAnalysisRequested)

        assertThat(fakeVideoIndexer.submittedVideo("1")?.videoUrl).isEqualTo("http://vid.eo/1.mp4")
        assertThat(fakeVideoIndexer.submittedVideo("1")?.language).isEqualTo(Locale.ENGLISH)
    }

    @Test
    fun `videos NOT published as analysed if not indexed yet`() {
        eventBus.publish(videoAnalysisRequested)

        assertThat(eventBus.countEventsOfType(VideoIndexed::class.java)).isEqualTo(0)
    }

    @Test
    fun `videos do NOT get submitted to video indexer if already indexed`() {
        fakeVideoIndexer.submitVideo("1", "http://old.url", language = null)

        eventBus.publish(videoAnalysisRequested)

        assertThat(fakeVideoIndexer.submittedVideo("1")?.videoUrl).isEqualTo("http://old.url")
    }

    @Test
    fun `video indexer exceptions during submission are handled`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.submitVideo(any(), any(), any())).thenThrow(RuntimeException("something went wrong"))

        val analyseVideo = AnalyseVideo(videoAnalyserService, eventBus, FakeDelayer())

        assertThatCode { analyseVideo.execute(videoAnalysisRequested) }.doesNotThrowAnyException()
    }

    @Test
    fun `video indexer exceptions during lookup are handled`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.isAnalysed(any())).thenThrow(RuntimeException("something went wrong"))

        val analyseVideo = AnalyseVideo(videoAnalyserService, eventBus, FakeDelayer())

        assertThatCode { analyseVideo.execute(videoAnalysisRequested) }.doesNotThrowAnyException()
    }

    @Test
    fun `video indexer will still request videos if it can't tell if they've been analysed`() {
        val videoAnalyserService = mock<VideoAnalyserService>()
        whenever(videoAnalyserService.isAnalysed(any())).thenThrow(RuntimeException("something went wrong"))

        val analyseVideo = AnalyseVideo(videoAnalyserService, eventBus, FakeDelayer())
        analyseVideo.execute(VideoAnalysisRequested.builder().videoId("a-good-video").videoUrl("blah").build())

        verify(videoAnalyserService, times(1)).submitVideo(videoId = eq("a-good-video"), videoUrl = eq("blah"), language = anyOrNull())
    }

    @Test
    fun `third party repeatable exceptions re-publishes AnalyseVideoRequest event`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.isAnalysed(any())).thenThrow(CouldNotGetVideoAnalysisException(becauseOfThirdPartyLimits = true))

        val analyseVideo = AnalyseVideo(videoAnalyserService, eventBus, FakeDelayer())

        assertThatCode { analyseVideo.execute(videoAnalysisRequested) }.doesNotThrowAnyException()
    }


    @Test
    fun `third party repeatable exceptions re-publish VideoIndexed event`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.getVideo(any())).thenThrow(CouldNotGetVideoAnalysisException(becauseOfThirdPartyLimits = true))

        val delayer = FakeDelayer()
        val publishAnalysedVideo = PublishVideoAnalysed(eventBus, videoAnalyserService, delayer)

        Assertions.assertThatCode { publishAnalysedVideo.execute(VideoIndexed("video id")) }.doesNotThrowAnyException()
        eventBus.clearState()
        delayer.advance(61)
        val message = eventBus.getEventOfType(VideoIndexed::class.java)
        assertThat(message.videoId).isEqualTo("video id")
    }
}
