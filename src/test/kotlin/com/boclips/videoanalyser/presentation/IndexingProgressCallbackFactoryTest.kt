package com.boclips.videoanalyser.presentation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IndexingProgressCallbackFactoryTest {

    @Test
    fun `creates a callback link for a video`() {

        val factory = IndexingProgressCallbackFactory("https://example.com")

        val callback = factory.forVideo("123")

        assertThat(callback).isEqualTo("https://example.com/v1/videos/123/check_indexing_progress")
    }
}
