package com.boclips.videoanalyser.infrastructure.videoindexer

import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder

class HttpVideoIndexerTokenProviderTest(
        @Autowired val restTemplateBuilder: RestTemplateBuilder,
        @Autowired val videoIndexerProperties: VideoIndexerProperties
) : AbstractSpringIntegrationTest() {

    lateinit var videoIndexerTokenProvider: VideoIndexerTokenProvider

    @BeforeEach
    fun setUp() {
        wireMockServer.stubFor(get(urlPathEqualTo("/auth/northeurope/Accounts/test-account/AccessToken"))
                .withQueryParam("allowEdit", equalTo("true"))
                .withHeader("Ocp-Apim-Subscription-Key", equalTo("test-subscription-key"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"test-access-token\"")
                )
        )

        videoIndexerTokenProvider = HttpVideoIndexerTokenProvider(restTemplateBuilder.build(), videoIndexerProperties)
    }

    @Test
    fun `fetches token from the auth api`() {
        val token = videoIndexerTokenProvider.getToken()

        assertThat(token).isEqualTo("test-access-token")
    }
}
