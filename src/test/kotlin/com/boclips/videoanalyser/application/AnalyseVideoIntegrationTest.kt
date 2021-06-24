package com.boclips.videoanalyser.application

import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
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

        val analyseVideo = AnalyseVideo(videoAnalyserService)

        assertThatCode { analyseVideo.execute(videoAnalysisRequested) }.doesNotThrowAnyException()
    }

    @Test
    fun `video indexer exceptions during lookup are handled`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.isAnalysed(any())).thenThrow(RuntimeException("something went wrong"))

        val analyseVideo = AnalyseVideo(videoAnalyserService)

        assertThatCode { analyseVideo.execute(videoAnalysisRequested) }.doesNotThrowAnyException()
    }
}
