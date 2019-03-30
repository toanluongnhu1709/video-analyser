package com.boclips.videoanalyser.infrastructure.videoindexer

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

class HttpVideoIndexerTokenProvider(
        private val restTemplate: RestTemplate,
        private val properties: VideoIndexerProperties
) : VideoIndexerTokenProvider {
    override fun getToken(): String {
        return doGetToken()
    }

    private fun doGetToken(): String {
        HttpVideoIndexerClient.logger.info { "Requesting a Video Indexer token" }
        val tokenUrl = "${properties.apiBaseUrl}/auth/northeurope/Accounts/${properties.accountId}/AccessToken?allowEdit=true"
        val headers = HttpHeaders().apply { set("Ocp-Apim-Subscription-Key", properties.subscriptionKey) }
        val response = restTemplate.exchange(tokenUrl, HttpMethod.GET, HttpEntity("", headers), String::class.java)
        val token = response.body?.replace("\"", "").orEmpty()
        HttpVideoIndexerClient.logger.info { "Received a Video Indexer token: ${token.substring(0, 6)}*************" }
        return token
    }
}
