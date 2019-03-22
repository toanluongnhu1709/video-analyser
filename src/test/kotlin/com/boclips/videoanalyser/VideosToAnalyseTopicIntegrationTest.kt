package com.boclips.videoanalyser

import com.boclips.eventtypes.VideoToAnalyse
import com.boclips.videoanalyser.config.VideosToAnalyseTopic
import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder

class VideosToAnalyseTopicIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videosToAnalyseTopic: VideosToAnalyseTopic

    @Test
    fun `videos to analyse are submitted to video indexer`() {
        val videoToAnalyse = VideoToAnalyse.builder()
                .videoId("1")
                .videoUrl("http://vid.eo/1.mp4")
                .build()

        videosToAnalyseTopic.input().send(MessageBuilder.withPayload(videoToAnalyse).build())

        assertThat(fakeVideoIndexer.submittedVideos()).contains("http://vid.eo/1.mp4")
    }
}
