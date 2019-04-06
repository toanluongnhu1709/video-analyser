package com.boclips.videoanalyser.infrastructure.videoindexer.resources

import com.boclips.events.types.CaptionsFormat
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexerException
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoResourceToAnalysedVideoConverter.convert
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VideoResourceToAnalysedVideoConverterTest {

    companion object {
        private val keywords = listOf(
                KeywordResource(
                        id = 0,
                        text = "keyword",
                        confidence = 0.6,
                        language = "es-ES",
                        instances = listOf(
                                TimeRangeResource(
                                        start = "0:01:28.298",
                                        end = "0:01:29.376"
                                )
                        )
                )
        )

        private val topics = listOf(
                TopicResource(
                        id = 0,
                        name = "Topic name",
                        referenceId = "Parent topic/Child topic",
                        confidence = 0.85,
                        language = "pl-PL",
                        instances = listOf(
                                TimeRangeResource(
                                        start = "0:00:00",
                                        end = "0:01:14.376"
                                )
                        )
                )
        )

        private val transcript = listOf(
                TranscriptItemResource(
                        id = 0,
                        text = "First line.",
                        confidence = 0.9,
                        speakerId = 0,
                        language = "en-US",
                        instances = emptyList()
                ),
                TranscriptItemResource(
                        id = 1,
                        text = "Second line.",
                        confidence = 0.9,
                        speakerId = 0,
                        language = "en-US",
                        instances = emptyList()
                )
        )
        private val insights = VideoInsightsResource(
                sourceLanguage = "en-GB",
                keywords = keywords,
                topics = topics,
                transcript = transcript

        )

        private val video = VideoIndexItemResource(externalId = "video-id", insights = insights)

        private val captions = "The caption text".toByteArray()
    }

    @Test
    fun `converts video id`() {
        val videoResource = VideoResource(index = VideoIndexResource(videos = listOf(video)), captions = captions)

        val analysedVideo = convert(videoResource)

        assertThat(analysedVideo.videoId).isEqualTo("video-id")
    }

    @Test
    fun `converts language`() {
        val videoResource = VideoResource(index = VideoIndexResource(videos = listOf(video)), captions = captions)

        val analysedVideo = convert(videoResource)
        assertThat(analysedVideo.language).isEqualTo("en-GB")
    }

    @Test
    fun `converts transcript`() {
        val videoResource = VideoResource(index = VideoIndexResource(videos = listOf(video)), captions = captions)

        val analysedVideo = convert(videoResource)

        assertThat(analysedVideo.transcript).isEqualTo("First line. Second line.")
    }

    @Test
    fun `converts captions`() {
        val videoResource = VideoResource(index = VideoIndexResource(videos = listOf(video)), captions = captions)

        val analysedVideo = convert(videoResource)

        assertThat(analysedVideo.captions.content).isEqualTo("The caption text")
        assertThat(analysedVideo.captions.format).isEqualTo(CaptionsFormat.VTT)
    }

    @Test
    fun `converts topics`() {
        val videoResource = VideoResource(index = VideoIndexResource(videos = listOf(video)), captions = captions)

        val analysedVideo = convert(videoResource)

        val topic = analysedVideo.topics.first()
        assertThat(topic.name).isEqualTo("Child topic")
        assertThat(topic.language).isEqualTo("pl-PL")
        assertThat(topic.parent.name).isEqualTo("Parent topic")
        assertThat(topic.parent.language).isEqualTo("pl-PL")
        assertThat(topic.parent.parent).isNull()
        assertThat(topic.confidence).isEqualTo(0.85)
        assertThat(topic.segments.first().startSecond).isEqualTo(0)
        assertThat(topic.segments.first().endSecond).isEqualTo(74)
    }

    @Test
    fun `converts keywords`() {
        val videoResource = VideoResource(index = VideoIndexResource(videos = listOf(video)), captions = captions)

        val analysedVideo = convert(videoResource)

        val topic = analysedVideo.keywords.first()
        assertThat(topic.name).isEqualTo("keyword")
        assertThat(topic.language).isEqualTo("es-ES")
        assertThat(topic.confidence).isEqualTo(0.6)
        assertThat(topic.segments.first().startSecond).isEqualTo(88)
        assertThat(topic.segments.first().endSecond).isEqualTo(89)
    }

    @Test
    fun `throws when index is not set`() {
        val videoResource = VideoResource(index = null, captions = captions)

        val exception = assertThrows<VideoIndexerException> {
            convert(videoResource)
        }
        assertThat(exception.message).isEqualTo("No video index")
    }

    @Test
    fun `throws when videos are not set`() {
        val videoResource = VideoResource(index = VideoIndexResource(null), captions = captions)

        val exception = assertThrows<VideoIndexerException> {
            convert(videoResource)
        }
        assertThat(exception.message).isEqualTo("No videos in the index")
    }

    @Test
    fun `throws when videos are empty`() {
        val videoResource = VideoResource(index = VideoIndexResource(emptyList()), captions = captions)

        val exception = assertThrows<VideoIndexerException> {
            convert(videoResource)
        }
        assertThat(exception.message).isEqualTo("Expecting 1 video in the index, found 0.")
    }

    @Test
    fun `throws when more then one video is present`() {
        val videoResource = VideoResource(index = VideoIndexResource(listOf(video, video)), captions = captions)

        val exception = assertThrows<VideoIndexerException> {
            convert(videoResource)
        }
        assertThat(exception.message).isEqualTo("Expecting 1 video in the index, found 2.")
    }

    @Test
    fun `throws when video id is not set`() {
        val videoResource = createVideoResource(videoId = null)

        val exception = assertThrows<VideoIndexerException> {
            convert(videoResource)
        }
        assertThat(exception.message).isEqualTo("No video id")
    }

    @Test
    fun `throws when video insights are not set`() {
        val videoResource = createVideoResource(insights = null)

        val exception = assertThrows<VideoIndexerException> {
            convert(videoResource)
        }
        assertThat(exception.message).isEqualTo("No video insights")
    }

    @Test
    fun `throws when language is not set`() {
        val videoResource = createVideoResource(insights = insights.copy(sourceLanguage = null))

        val exception = assertThrows<VideoIndexerException> {
            convert(videoResource)
        }
        assertThat(exception.message).isEqualTo("No video language")
    }

    @Test
    fun `returns empty topics when they are not set`() {
        val videoResource = createVideoResource(insights = insights.copy(topics = null))

        val analysedVideo = convert(videoResource)
        assertThat(analysedVideo.topics).isEmpty()
    }

    @Test
    fun `returns empty keywords when they are not set`() {
        val videoResource = createVideoResource(insights = insights.copy(keywords = null))

        val analysedVideo = convert(videoResource)
        assertThat(analysedVideo.keywords).isEmpty()
    }

    @Test
    fun `throws when transcripts are not set`() {
        val videoResource = createVideoResource(insights = insights.copy(transcript = null))

        val exception = assertThrows<VideoIndexerException> {
            convert(videoResource)
        }
        assertThat(exception.message).isEqualTo("No video transcript")
    }

    @Test
    fun `throws when captions are not set`() {
        val videoResource = createVideoResource(captions = null)

        val exception = assertThrows<VideoIndexerException> {
            convert(videoResource)
        }
        assertThat(exception.message).isEqualTo("No video captions")
    }

    private fun createVideoResource(
            videoId: String? = "123",
            insights: VideoInsightsResource? = VideoResourceToAnalysedVideoConverterTest.insights,
            captions: ByteArray? = VideoResourceToAnalysedVideoConverterTest.captions
    ): VideoResource {
        return VideoResource(index = VideoIndexResource(listOf(VideoIndexItemResource(externalId = videoId, insights = insights))), captions = captions)
    }


}

