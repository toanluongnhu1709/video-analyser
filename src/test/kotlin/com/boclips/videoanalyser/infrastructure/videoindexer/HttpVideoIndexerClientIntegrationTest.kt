package com.boclips.videoanalyser.infrastructure.videoindexer

import com.boclips.videoanalyser.infrastructure.VideoHasInvalidStateException
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexItemResource
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexResourceParser
import com.boclips.videoanalyser.presentation.PublishAnalysedVideoLinkFactory
import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import com.boclips.videoanalyser.testsupport.fakes.FakeDelayer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.stubbing.StubMapping
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
    lateinit var fakeDelayer: FakeDelayer

    @BeforeEach
    fun setUp() {
        fakeDelayer = FakeDelayer()
        videoIndexer = HttpVideoIndexerClient(
            restTemplate = restTemplateBuilder.build(),
            properties = videoIndexerProperties,
            videoIndexerTokenProvider = videoIndexerTokenProvider,
            publishAnalysedVideoLinkFactory = publishAnalysedVideoLinkFactory,
            videoIndexResourceParser = videoIndexResourceParser,
            delayer = fakeDelayer
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
        stubPostWithStatusAndBody(200, videoUploadResponseResource.inputStream.readBytes().toString())

        videoIndexer.submitVideo("video1", "https://cdnapisec.example.com/v/1", language = null)

        wireMockServer.verify(
            postRequestedFor(urlPathEqualTo("/northeurope/Accounts/test-account/Videos"))
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
        stubPostWithStatusAndBody(200, videoUploadResponseResource.inputStream.readBytes().toString())

        videoIndexer.submitVideo("video1", "https://cdnapisec.example.com/v/1", language = Locale.ENGLISH)

        wireMockServer.verify(
            postRequestedFor(urlPathEqualTo("/northeurope/Accounts/test-account/Videos"))
                .withQueryParam("language", equalTo("en-US"))
        )
    }

    @Test
    fun `submit throws when there is an error`() {
        stubPostWithStatusAndBody(400, "This is a test error")

        val exception = assertThrows<VideoIndexerException> {
            videoIndexer.submitVideo("123", "http://videos.com/1", language = null)
        }

        assertThat(exception).hasMessage("Failed to submit video 123 to Video Indexer")
    }

    @Test
    fun `submit retries after a delay specified in a 429 (Too Many Requests) response body`() {
        val rateLimited = stubPostWithStatusAndBody(429, """"{ "statusCode": 429, "message": "Rate limit is exceeded. Try again in 28 seconds." }",""")

        videoIndexer.submitVideo("123", "http://videos.com/1", language = null)

        wireMockServer.verify(1, postRequestedFor(urlPathEqualTo("/northeurope/Accounts/test-account/Videos")))
        fakeDelayer.advance(27)
        wireMockServer.verify(1, postRequestedFor(urlPathEqualTo("/northeurope/Accounts/test-account/Videos")))

        wireMockServer.removeStub(rateLimited)
        stubPostWithStatusAndBody(200, videoUploadResponseResource.inputStream.readBytes().toString())

        fakeDelayer.advance(1)
        wireMockServer.verify(2, postRequestedFor(urlPathEqualTo("/northeurope/Accounts/test-account/Videos")))
    }

    @Test
    fun `submit retries after a default delay if it can't parse the 429 response body`() {
        val rateLimited = stubPostWithStatusAndBody(429, """"{ "statusCode": 429, "message": "Rate limit is poo pants wee bum" }",""")

        videoIndexer.submitVideo("123", "http://videos.com/1", language = null)

        wireMockServer.verify(1, postRequestedFor(urlPathEqualTo("/northeurope/Accounts/test-account/Videos")))
        fakeDelayer.advance(9)
        wireMockServer.verify(1, postRequestedFor(urlPathEqualTo("/northeurope/Accounts/test-account/Videos")))

        wireMockServer.removeStub(rateLimited)

        stubPostWithStatusAndBody(200, videoUploadResponseResource.inputStream.readBytes().toString())

        fakeDelayer.advance(1)
        wireMockServer.verify(2, postRequestedFor(urlPathEqualTo("/northeurope/Accounts/test-account/Videos")))
    }

    @Test
    fun getVideo() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        wireMockServer.stubFor(
            get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/Index"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(videoIndexResponseResource.inputStream.readBytes())
                )
        )

        wireMockServer.stubFor(
            get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/Captions"))
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
    fun `getVideo throws exception when video index has invalid state`() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        val response = String(videoIndexResponseResource.inputStream.readBytes()).replace(VideoIndexItemResource.STATE_PROCESSED, "Processing")
        wireMockServer.stubFor(
            get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/Index"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(response)
                )
        )

        val exception = assertThrows<VideoHasInvalidStateException> {
            videoIndexer.getVideo(videoId)
        }
        assertThat(exception.state).isEqualTo("Processing")
    }

    @Test
    fun `getVideo throws exception with property saying that it was caused by third party limits`() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        wireMockServer.stubFor(
            get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/Index"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .willReturn(
                    aResponse().withStatus(429).withBody("Too many requests")
                )
        )

        val ex = assertThrows<CouldNotGetVideoAnalysisException> {
            videoIndexer.getVideo(videoId)
        }
        assertThat(ex.becauseOfThirdPartyLimits).isTrue()
    }

    @Test
    fun `getCaptions throws exception with property saying that it was caused by third party limits`() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        val response = String(videoIndexResponseResource.inputStream.readBytes())
        wireMockServer.stubFor(
            get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/Index"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(response)
                )
        )

        wireMockServer.stubFor(
            get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/Captions"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .withQueryParam("format", equalTo("vtt"))
                .willReturn(
                    aResponse().withStatus(429).withBody("Too many requests")
                )
        )

        val ex = assertThrows<CouldNotGetVideoAnalysisException> {
            videoIndexer.getVideo(videoId)
        }
        assertThat(ex.becauseOfThirdPartyLimits).isTrue()
    }
    @Test
    fun `getCaptions throws exception with prop saying that it was not caused by third party limits `() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        val response = String(videoIndexResponseResource.inputStream.readBytes())
        wireMockServer.stubFor(
            get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/Index"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(response)
                )
        )

        wireMockServer.stubFor(
            get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/Captions"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .withQueryParam("format", equalTo("vtt"))
                .willReturn(
                    aResponse().withStatus(404).withBody("not found")
                )
        )

        val ex = assertThrows<CouldNotGetVideoAnalysisException> {
            videoIndexer.getVideo(videoId)
        }
        assertThat(ex.becauseOfThirdPartyLimits).isFalse()
    }

    @Test
    fun deleteVideo() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        wireMockServer.stubFor(
            delete(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId"))
                .willReturn(aResponse().withStatus(204))
        )

        videoIndexer.deleteVideo(videoId)

        wireMockServer.verify(
            1,
            deleteRequestedFor(
                urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId")
            ).withQueryParam("accessToken", equalTo("test-access-token"))
        )
    }

    @Test
    fun `unsuccesful delete does not throw`() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        wireMockServer.stubFor(
            delete(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId"))
                .willReturn(aResponse().withStatus(500))
        )

        videoIndexer.deleteVideo(videoId)

        wireMockServer.verify(
            1,
            deleteRequestedFor(
                urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId")
            ).withQueryParam("accessToken", equalTo("test-access-token"))
        )
    }

    @Test
    fun deleteSourceFile() {
        val videoId = "video-id-1234"
        val microsoftVideoId = "ms-id-1234"

        stubLookupByExternalId(videoId, microsoftVideoId)

        wireMockServer.stubFor(
            delete(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/SourceFile"))
                .willReturn(aResponse().withStatus(204))
        )

        videoIndexer.deleteSourceFile(videoId)

        wireMockServer.verify(
            1,
            deleteRequestedFor(
                urlPathEqualTo("/northeurope/Accounts/test-account/Videos/$microsoftVideoId/SourceFile")
            ).withQueryParam("accessToken", equalTo("test-access-token"))
        )
    }

    fun stubLookupByExternalId(videoId: String, microsoftVideoId: String) {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/northeurope/Accounts/test-account/Videos/GetIdByExternalId"))
                .withQueryParam("accessToken", equalTo("test-access-token"))
                .withQueryParam("externalId", equalTo(videoId))
                .willReturn(
                    aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"$microsoftVideoId\"")
                )
        )
    }

    private fun stubPostWithStatusAndBody(status: Int, body: String): StubMapping =
        wireMockServer.stubFor(
            post(urlPathEqualTo("/northeurope/Accounts/test-account/Videos"))
                .willReturn(
                    aResponse().withHeader("Content-Type", "application/json").withStatus(status).withBody(body)
                )
        )
}
