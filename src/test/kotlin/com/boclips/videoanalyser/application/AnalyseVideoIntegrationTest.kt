package com.boclips.videoanalyser.application

import com.boclips.eventtypes.VideoToAnalyse
import com.boclips.videoanalyser.config.VideosToAnalyseTopic
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
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

    @Autowired
    lateinit var videosToAnalyseTopic: VideosToAnalyseTopic

    lateinit var videoToAnalyse: VideoToAnalyse

    @BeforeEach
    fun setUp() {
        videoToAnalyse = VideoToAnalyse.builder()
                .videoId("1")
                .videoUrl("http://vid.eo/1.mp4")
                .build()
    }

    @Test
    fun `videos to analyse published as events are submitted to video indexer`() {
        videosToAnalyseTopic.input().send(MessageBuilder.withPayload(videoToAnalyse).build())

        assertThat(fakeVideoIndexer.submittedVideo("1")).isEqualTo("http://vid.eo/1.mp4")
    }

    @Test
    fun `video indexer exceptions are handled`() {
        val videoIndexer = mock<VideoIndexer>()

        whenever(videoIndexer.submitVideo(any(), any())).thenThrow(RuntimeException("something went wrong"))

        val analyseVideo = AnalyseVideo(videoIndexer)

        assertThatCode { analyseVideo.execute(videoToAnalyse) }.doesNotThrowAnyException()
    }
}
