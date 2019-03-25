package com.boclips.videoanalyser.infrastructure.videoindexer

import mu.KLogging
import org.springframework.web.client.RestTemplate

data class VideoResponse(var id: String? = null)

class HttpVideoIndexerClient(
        private val restTemplate: RestTemplate,
        private val properties: VideoIndexerProperties
) : VideoIndexer {

    companion object : KLogging()

    override fun submitVideo(url: String): String {
        logger.info { "Submitting $url to the Video Indexer" }

        val videosUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos" +
                "?videoUrl={videoUrl}" +
                "&externalUrl={videoUrl}" +
                "&language={language}" +
                "&indexingPreset={indexingPreset}" +
                "&privacy={privacy}"
        val urlParams = submitUrlVariables(url)

        val response = restTemplate.postForEntity(videosUrl, "", VideoResponse::class.java, urlParams).body

        return response?.id.orEmpty()
    }

    private fun submitUrlVariables(videoUrl: String) = mapOf(
            "language" to "auto",
            "videoUrl" to videoUrl,
            "indexingPreset" to "AudioOnly",
            "privacy" to "Private"
    )
}
