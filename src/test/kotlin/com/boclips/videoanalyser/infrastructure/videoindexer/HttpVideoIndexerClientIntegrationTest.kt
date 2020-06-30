package com.boclips.videoanalyser.infrastructure.videoindexer

import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexItemResource
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexResourceParser
import com.boclips.videoanalyser.presentation.PublishAnalysedVideoLinkFactory
import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.Resource
import java.util.*

class HttpVideoIndexerClientIntegrationTest(
        @Autowired val restTemplateBuilder: RestTemplateBuilder,
        @Autowired val videoIndexerProperties: VideoIndexerProperties,
        @Autowired val videoIndexerTokenProvider: VideoIndexerTokenProvider,
        @Autowired val publishAnalysedVideoLinkFactory: PublishAnalysedVideoLinkFactory,
        @Autowired val videoIndexResourceParser: VideoIndexResourceParser,
        @Value("classpath:videoindexer/responses/videoUpload.json") val videoUploadResponseResource: Resource,
        @Value("classpath:videoindexer/responses/videoIndex.json") val videoIndexResponseResource: Resource
) : AbstractSpringIntegrationTest() {

    lateinit var videoIndexer: HttpVideoIndexerClient

    @BeforeEach
    fun setUp() {
        videoIndexer = HttpVideoIndexerClient(
                restTemplate = restTemplateBuilder.build(),
                properties = videoIndexerProperties,
                videoIndexerTokenProvider = videoIndexerTokenProvider,
                publishAnalysedVideoLinkFactory = publishAnalysedVideoLinkFactory,
                videoIndexResourceParser = videoIndexResourceParser
        )
    }

    @Test
    fun `isIndexed true when id lookup successful`() {
        val videoId = "video-id-1234"
        stubLookupByExternalId(videoId, "video-indexer-id")

        val isIndexed = videoIndexer.isIndexed(videoId)

        assertThat(isIndexed).isTrue()
    }

    @Test
    fun `isIndexed false when id lookup NOT successful`() {
        val isIndexed = videoIndexer.isIndexed("unknown-video-id")

        assertThat(isIndexed).isFalse()
    }

    @Test
    fun submit() {
        wireMockServer.stubFor(post(urlPathEqualTo("/northeurope/Accounts/test-account/Videos"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(videoUploadResponseResource.inputStream.readBytes())
                )
        )

        videoIndexer.submitVideo("video1", "https://cdnapisec.example.com/v/1", language = null)

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/northeurope/Accounts/test-account/Videos"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .withQueryParam("name", equalTo("video1"))
                .withQueryParam("videoUrl", equalTo("https://cdnapisec.example.com/v/1"))
                .withQueryParam("externalUrl", equalTo("https://cdnapisec.example.com/v/1"))
                .withQueryParam("externalId", equalTo("video1"))
                .withQueryParam("callbackUrl", equalTo("https://video-analyser.test-boclips.com/v1/videos/video1/publish_analysed_video"))
                .withQueryParam("language", equalTo("auto"))
                .withQueryParam("indexingPreset", equalTo("AudioOnly"))
                .withQueryParam("streamingPreset", equalTo("NoStreaming"))
                .withQueryParam("privacy", equalTo("Private"))
        )
    }

    @Test
    fun `submit with language`() {
        wireMockServer.stubFor(post(urlPathEqualTo("/northeurope/Accounts/test-account/Videos"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(videoUploadResponseResource.inputStream.readBytes())
                )
        )

        videoIndexer.submitVideo("video1", "https://cdnapisec.example.com/v/1", language = Locale.ENGLISH)

        wireMockServer.verify(postRequestedFor(urlPathEqualTo("/northeurope/Accounts/test-account/Videos"))
                .withQueryParam("language", equalTo("en-US"))
        )
    }

    @Test
    fun `submit throws when there is an error`() {
        wireMockServer.stubFor(post(urlPathEqualTo("/northeurope/Accounts/test-account/Videos"))
                .willReturn(
                        aResponse().withStatus(400).withBody("This is a test error")
                )
        )

        val exception = assertThrows<VideoIndexerException> {
            videoIndexer.submitVideo("123", "http://videos.com/1", language = null)
        }

        assertThat(exception).hasMessage("Failed to submit video 123 to Video Indexer")
    }

    @Test
    fun getVideo() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        wireMockServer.stubFor(get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/Index"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(videoIndexResponseResource.inputStream.readBytes())
                )
        )

        wireMockServer.stubFor(get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/Captions"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .withQueryParam("format", equalTo("vtt"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/octet-stream").withBody("contents of vtt file".toByteArray())
                )
        )

        val resource = videoIndexer.getVideo(videoId)

        assertThat(resource.index?.videos?.first()?.insights?.sourceLanguage).isEqualTo("en-US")
        assertThat(resource.index?.videos?.first()?.state).isEqualTo("Processed")
        assertThat(resource.captions).isEqualTo("contents of vtt file".toByteArray())
    }

    @Test
    fun `getVideo when processing returns null captions`() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        val response = String(videoIndexResponseResource.inputStream.readBytes()).replace(VideoIndexItemResource.STATE_PROCESSED, "Processing")
        wireMockServer.stubFor(get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/Index"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(response)
                )
        )

        val resource = videoIndexer.getVideo(videoId)

        assertThat(resource.index?.videos?.first()?.insights?.sourceLanguage).isEqualTo("en-US")
        assertThat(resource.index?.videos?.first()?.state).isEqualTo("Processing")
        assertThat(resource.captions).isNull()
    }

    @Test
    fun deleteVideo() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        wireMockServer.stubFor(delete(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId"))
                .willReturn(aResponse().withStatus(204))
        )

        videoIndexer.deleteVideo(videoId)

        wireMockServer.verify(1, deleteRequestedFor(
                urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId")
        ).withQueryParam("accessToken", equalTo("test-access-token")))
    }

    @Test
    fun `unsuccesful delete does not throw`() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        wireMockServer.stubFor(delete(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId"))
            .willReturn(aResponse().withStatus(500))
        )


        videoIndexer.deleteVideo(videoId)

        wireMockServer.verify(1, deleteRequestedFor(
            urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId")
        ).withQueryParam("accessToken", equalTo("test-access-token")))
    }

    @Test
    fun deleteSourceFile() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        wireMockServer.stubFor(delete(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/SourceFile"))
                .willReturn(aResponse().withStatus(204))
        )

        videoIndexer.deleteSourceFile(videoId)

        wireMockServer.verify(1, deleteRequestedFor(
                urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/SourceFile")
        ).withQueryParam("accessToken", equalTo("test-access-token")))
    }

    fun stubLookupByExternalId(videoId: String, microsoftVideoId: String) {
        wireMockServer.stubFor(get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/GetIdByExternalId"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .withQueryParam("externalId", equalTo(videoId))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"$microsoftVideoId\"")
                )
        )
    }
}
