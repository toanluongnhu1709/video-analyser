package com.boclips.videoanalyser.infrastructure.videoindexer

import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder

class HttpVideoIndexerClientIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var restTemplateBuilder: RestTemplateBuilder

    lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
        wireMockServer.start()
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun submit() {
        val properties = VideoIndexerProperties(
                apiBaseUrl = wireMockServer.baseUrl(),
                accountId = "account1"
        )

        wireMockServer.stubFor(post(urlPathEqualTo("/northeurope/Accounts/account1/Videos"))
                .withQueryParam("videoUrl", equalTo("https://cdnapisec.example.com/v/1"))
                .withQueryParam("name", equalTo("video1"))
                .withQueryParam("externalId", equalTo("video1"))
                .withQueryParam("externalUrl", equalTo("https://cdnapisec.example.com/v/1"))
                .withQueryParam("language", equalTo("auto"))
                .withQueryParam("indexingPreset", equalTo("AudioOnly"))
                .withQueryParam("privacy", equalTo("Private"))
                .willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(responseBody)
        ))

        val videoIndexer = HttpVideoIndexerClient(restTemplate = restTemplateBuilder.build(), properties = properties)

        val videoIndexerId = videoIndexer.submitVideo("video1", "https://cdnapisec.example.com/v/1")

        assertThat(videoIndexerId).isEqualTo("3a9220459b")
    }

    val responseBody = """
        {
          "accountId": "account1",
          "id": "3a9220459b",
          "partition": null,
          "externalId": "video1",
          "metadata": null,
          "name": "SampleVideo_1280x720_2mb222",
          "description": null,
          "created": "2018-04-25T16:29:43.0992053+00:00",
          "lastModified": "2018-04-25T16:29:43.1929511+00:00",
          "lastIndexed": "2018-04-25T16:29:43.1929511+00:00",
          "privacyMode": "Private",
          "userName": "SampleUserName",
          "isOwned": true,
          "isBase": true,
          "state": "Uploaded",
          "processingProgress": "",
          "durationInSeconds": 13,
          "thumbnailVideoId": "3a9220459b",
          "thumbnailId": "00000000-0000-0000-0000-000000000000",
          "social": {
            "likedByUser": false,
            "likes": 0,
            "views": 0
          },
          "searchMatches": [],
          "indexingPreset": "Default",
          "streamingPreset": "Default",
          "sourceLanguage": "En-US"
        }
    """.trimIndent()
}
