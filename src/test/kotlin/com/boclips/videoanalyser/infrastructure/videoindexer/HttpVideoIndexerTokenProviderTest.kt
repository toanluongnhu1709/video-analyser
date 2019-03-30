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

    @BeforeEach
    fun setUp() {
        wireMockServer.stubFor(get(urlPathEqualTo("/auth/northeurope/Accounts/test-account/AccessToken"))
                .withQueryParam("allowEdit", equalTo("true"))
                .withHeader("Ocp-Apim-Subscription-Key", equalTo("test-subscription-key"))
                .willReturn(
                        aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("\"test-access-token\"")
                )
        )
    }

    private fun tokenProvider(ttlMinutes: Int): HttpVideoIndexerTokenProvider {
        return HttpVideoIndexerTokenProvider(restTemplateBuilder.build(), videoIndexerProperties.copy(tokenTtlMinutes = ttlMinutes))
    }

    @Test
    fun `fetches token from the auth api`() {
        val tokenProvider = tokenProvider(ttlMinutes = 1)

        val token = tokenProvider.getToken()

        assertThat(token).isEqualTo("test-access-token")
    }

    @Test
    fun `caches the token for subsequent calls`() {
        val tokenProvider = tokenProvider(ttlMinutes = 1)

        tokenProvider.getToken()
        tokenProvider.getToken()

        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/auth/northeurope/Accounts/test-account/AccessToken")))
    }

    @Test
    fun `re-fetches token periodically`() {
        val tokenProvider = tokenProvider(ttlMinutes = 0)

        tokenProvider.getToken()
        tokenProvider.getToken()

        wireMockServer.verify(2, getRequestedFor(urlPathEqualTo("/auth/northeurope/Accounts/test-account/AccessToken")))
    }
}
