package com.boclips.videoanalyser.infrastructure.videoindexer

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import java.time.Duration
import java.time.Instant

data class Token(val value: String, val expires: Instant) {
    fun hasExpired(): Boolean {
        return Instant.now().isAfter(expires)
    }
}

class HttpVideoIndexerTokenProvider(
        private val restTemplate: RestTemplate,
        private val properties: VideoIndexerProperties
) : VideoIndexerTokenProvider {

    private var token = Token("", expires = Instant.MIN)

    @Synchronized
    override fun getToken(): String {
        if(token.hasExpired()) {
            HttpVideoIndexerClient.logger.info { "Requesting a Video Indexer token" }
            token = doGetToken()
            HttpVideoIndexerClient.logger.info { "Received a Video Indexer token: ${token.value.substring(0, 5)}..." }
        } else {
            HttpVideoIndexerClient.logger.info { "Re-using the existing Video Indexer token: ${token.value.substring(0, 5)}..." }
        }
        return token.value
    }

    private fun doGetToken(): Token {
        val tokenWillExpire = Instant.now().plus(Duration.ofMinutes(properties.tokenTtlMinutes.toLong()))
        val tokenUrl = "${properties.apiBaseUrl}/auth/northeurope/Accounts/${properties.accountId}/AccessToken?allowEdit=true"
        val headers = HttpHeaders().apply { set("Ocp-Apim-Subscription-Key", properties.subscriptionKey) }
        val response = restTemplate.exchange(tokenUrl, HttpMethod.GET, HttpEntity("", headers), String::class.java)
        val token = response.body?.replace("\"", "").orEmpty()
        return Token(token, tokenWillExpire)
    }
}
