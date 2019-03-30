package com.boclips.videoanalyser.presentation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PublishAnalysedVideoLinkFactoryTest {

    @Test
    fun `creates a link for a video`() {

        val factory = PublishAnalysedVideoLinkFactory("https://example.com")

        val callback = factory.forVideo("123")

        assertThat(callback).isEqualTo("https://example.com/v1/videos/123/publish_analysed_video")
    }
}
