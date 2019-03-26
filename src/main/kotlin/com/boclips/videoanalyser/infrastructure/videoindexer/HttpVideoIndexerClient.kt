package com.boclips.videoanalyser.infrastructure.videoindexer

import com.boclips.videoanalyser.presentation.IndexingProgressCallbackFactory
import mu.KLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

data class VideoResponse(var id: String? = null)

class HttpVideoIndexerClient(
        private val restTemplate: RestTemplate,
        private val properties: VideoIndexerProperties,
        private val indexingProgressCallbackFactory: IndexingProgressCallbackFactory
) : VideoIndexer {

    companion object : KLogging()

    override fun submitVideo(videoId: String, videoUrl: String): String {
        logger.info { "Submitting $videoUrl to the Video Indexer" }

        val videosUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos" +
                "?accessToken={accessToken}" +
                "&name={externalId}" +
                "&externalId={externalId}" +
                "&videoUrl={videoUrl}" +
                "&externalUrl={videoUrl}" +
                "&callbackUrl={callbackUrl}" +
                "&language={language}" +
                "&indexingPreset={indexingPreset}" +
                "&privacy={privacy}"
        val urlParams = submitUrlVariables(videoId, videoUrl)

        val response = restTemplate.postForEntity(videosUrl, "", VideoResponse::class.java, urlParams).body

        return response?.id.orEmpty()
    }

    fun getToken(): String {
        val tokenUrl = "${properties.apiBaseUrl}/auth/northeurope/Accounts/${properties.accountId}/AccessToken"
        val headers = HttpHeaders().apply { set("Ocp-Apim-Subscription-Key", properties.subscriptionKey) }
        val response = restTemplate.exchange(tokenUrl, HttpMethod.GET, HttpEntity("", headers), String::class.java)
        return response.body?.replace("\"", "").orEmpty()
    }

    private fun submitUrlVariables(videoId: String, videoUrl: String) = mapOf(
            "accessToken" to getToken(),
            "externalId" to videoId,
            "videoUrl" to videoUrl,
            "callbackUrl" to indexingProgressCallbackFactory.forVideo(videoId),
            "language" to "auto",
            "indexingPreset" to "AudioOnly",
            "privacy" to "Private"
    )
}
