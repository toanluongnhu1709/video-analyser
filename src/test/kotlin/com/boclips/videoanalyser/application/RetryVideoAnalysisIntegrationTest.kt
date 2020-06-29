package com.boclips.videoanalyser.application

import com.boclips.eventbus.events.video.RetryVideoAnalysisRequested
import com.boclips.eventbus.events.video.VideoAnalysisRequested
import com.boclips.videoanalyser.domain.VideoAnalyserService
import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class RetryVideoAnalysisIntegrationTest(
    @Autowired val videoAnalysisService: VideoAnalyserService
) : AbstractSpringIntegrationTest() {

    lateinit var videoAnalysisRequested: VideoAnalysisRequested
    lateinit var retryVideoAnalysisRequested: RetryVideoAnalysisRequested

    @BeforeEach
    fun setUp() {
        videoAnalysisRequested = VideoAnalysisRequested
            .builder()
            .videoId("1")
            .videoUrl("http://vid.eo/1.mp4")
            .language(Locale.ENGLISH)
            .build()

        retryVideoAnalysisRequested = RetryVideoAnalysisRequested
            .builder()
            .videoId("1")
            .videoUrl("http://vid.eo/1.mp4")
            .language(Locale.ENGLISH)
            .build()
    }

    @Test
    fun `able to reanalyse a video with missing source file`() {
        eventBus.publish(videoAnalysisRequested)
        videoAnalysisService.deleteSourceFile("1")

        val videoBeforeRetry = fakeVideoIndexer.submittedVideo("1");
        eventBus.publish(retryVideoAnalysisRequested)
        val videoAfterRetry = fakeVideoIndexer.submittedVideo("1");

        assertThat(videoBeforeRetry?.sourceFileAvailable).isFalse()
        assertThat(videoAfterRetry?.sourceFileAvailable).isTrue()
    }

}
