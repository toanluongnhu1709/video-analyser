package com.boclips.videoanalyser.infrastructure.videoindexer.resources

import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import java.nio.charset.StandardCharsets.UTF_8

class VideoIndexResourceParserIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var videoIndexResourceParser: VideoIndexResourceParser

    @Value("classpath:videoindexer/responses/videoIndex.json")
    lateinit var videoIndexJsonResource: Resource

    lateinit var videoIndexJsonString: String

    @BeforeEach
    fun setUp() {
        videoIndexJsonString = videoIndexJsonResource.inputStream.readAllBytes().toString(UTF_8)
    }

    @Test
    fun `parses external id`() {
        val videoIndex = videoIndexResourceParser.parse(videoIndexJsonString)
        val externalId = videoIndex.videos?.first()?.externalId

        assertThat(externalId).isEqualTo("123")
    }

    @Test
    fun `parses source language`() {
        val videoIndex = videoIndexResourceParser.parse(videoIndexJsonString)
        val insights = videoIndex.videos?.first()?.insights!!

        assertThat(insights.sourceLanguage).isEqualTo("en-US")
    }

    @Test
    fun `parses transcript`() {
        val videoIndex = videoIndexResourceParser.parse(videoIndexJsonString)
        val insights = videoIndex.videos?.first()?.insights!!
        val transcript = insights.transcript!!
        val transcriptItem = transcript.find { it.id == 5 }

        assertThat(transcriptItem?.text).isEqualTo("who said they would. The 7%")
        assertThat(transcriptItem?.confidence).isEqualTo(0.9316)
        assertThat(transcriptItem?.speakerId).isEqualTo(1)
        assertThat(transcriptItem?.language).isEqualTo("en-US")
        assertThat(transcriptItem?.instances).hasSize(1)
        assertThat(transcriptItem?.instances?.first()?.start).isEqualTo("0:00:29.35")
        assertThat(transcriptItem?.instances?.first()?.end).isEqualTo("0:00:31.61")
        assertThat(transcriptItem?.instances?.first()?.adjustedStart).isEqualTo("0:00:29.35")
        assertThat(transcriptItem?.instances?.first()?.adjustedEnd).isEqualTo("0:00:31.61")
    }

    @Test
    fun `parses topics`() {
        val videoIndex = videoIndexResourceParser.parse(videoIndexJsonString)
        val insights = videoIndex.videos?.first()?.insights!!
        val topic = insights.topics?.first()!!

        assertThat(topic.id).isEqualTo(0)
        assertThat(topic.name).isEqualTo("Health and Wellbeing")
        assertThat(topic.referenceId).isEqualTo("Health")
        assertThat(topic.referenceType).isEqualTo("VideoIndexer")
        assertThat(topic.iptcName).isEqualTo("Health")
        assertThat(topic.confidence).isEqualTo(0.8254)
        assertThat(topic.iabName).isEqualTo("Medical Health")
        assertThat(topic.language).isEqualTo("en-US")
        assertThat(topic.instances).hasSize(1)
        assertThat(topic.instances?.first()?.start).isEqualTo("0:00:00")
        assertThat(topic.instances?.first()?.end).isEqualTo("0:01:14.376")
        assertThat(topic.instances?.first()?.adjustedStart).isEqualTo("0:00:00")
        assertThat(topic.instances?.first()?.adjustedEnd).isEqualTo("0:01:14.376")
    }

    @Test
    fun `parses keywords`() {
        val videoIndex = videoIndexResourceParser.parse(videoIndexJsonString)
        val insights = videoIndex.videos?.first()?.insights!!
        val keyword = insights.keywords?.first()!!

        assertThat(keyword.id).isEqualTo(0)
        assertThat(keyword.text).isEqualTo("genetic testing")
        assertThat(keyword.confidence).isEqualTo(0.9975)
        assertThat(keyword.language).isEqualTo("en-US")
        assertThat(keyword.instances).hasSize(4)
        assertThat(keyword.instances?.last()?.start).isEqualTo("0:00:37.55")
        assertThat(keyword.instances?.last()?.end).isEqualTo("0:00:42")
        assertThat(keyword.instances?.last()?.adjustedStart).isEqualTo("0:00:37.55")
        assertThat(keyword.instances?.last()?.adjustedEnd).isEqualTo("0:00:42")
    }
}