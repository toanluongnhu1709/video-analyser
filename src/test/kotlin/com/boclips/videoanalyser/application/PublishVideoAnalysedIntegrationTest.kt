package com.boclips.videoanalyser.application

import com.boclips.eventbus.events.video.VideoAnalysed
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.infrastructure.videoindexer.CouldNotGetVideoAnalysisException
import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.fakes.FakeDelayer
import com.boclips.videoanalyser.testsupport.fakes.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class PublishVideoAnalysedIntegrationTest : AbstractSpringIntegrationTest() {

    @Test
    fun `videos are received from the video indexer and published`() {
        fakeVideoIndexer.submitVideo("1234", "http://example.com", language = null)

        eventBus.publish(VideoIndexed(videoId = "1234"))

        val message = eventBus.getEventOfType(VideoAnalysed::class.java)

        assertThat(message.videoId).isEqualTo("1234")
        assertThat(message.language).isEqualTo(Locale("en", "GB"))
    }

    @Test
    fun `exceptions when getting videos are handled`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.getVideo(any())).thenThrow(RuntimeException("something went wrong"))

        val publishAnalysedVideo = PublishVideoAnalysed(eventBus, videoAnalyserService, FakeDelayer())

        Assertions.assertThatCode { publishAnalysedVideo.execute(VideoIndexed("video id")) }.doesNotThrowAnyException()
    }

    @Test
    fun `third party repeatable exceptions send VideoIndexed event`() {
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

    @Test
    fun `source files get deleted in video indexer`() {
        fakeVideoIndexer.submitVideo("1234", "http://example.com", language = null)

        eventBus.publish(VideoIndexed("1234"))

        assertThat(fakeVideoIndexer.submittedVideo("1234")?.sourceFileAvailable).isFalse()
    }

    @Test
    fun `exceptions when deleting source files are handled`() {
        val videoAnalyserService = mock<VideoAnalyserService>()

        whenever(videoAnalyserService.getVideo(any())).thenReturn(TestFactories.createVideoAnalysed())
        whenever(videoAnalyserService.deleteSourceFile(any())).thenThrow(RuntimeException("something went wrong"))

        val publishAnalysedVideo = PublishVideoAnalysed(eventBus, videoAnalyserService, FakeDelayer())

        Assertions.assertThatCode { publishAnalysedVideo.execute(VideoIndexed("video id")) }.doesNotThrowAnyException()
    }
}
