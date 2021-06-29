package com.boclips.videoanalyser.infrastructure

import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexer
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoIndexerAnalyserServiceTest {

    @Test
    fun `do not throw when Video Indexer does not return the video`() {
        val videoIndexer = mock<VideoIndexer>()
        whenever(videoIndexer.getVideo(any())).thenReturn(null)
        val videoAnalyserService = VideoIndexerAnalyserService(videoIndexer = videoIndexer)

        assertThat(videoAnalyserService.getVideo("blabla")).isNull()
    }
}
