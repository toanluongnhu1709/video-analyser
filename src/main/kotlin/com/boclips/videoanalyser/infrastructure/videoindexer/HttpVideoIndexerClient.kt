package com.boclips.videoanalyser.infrastructure.videoindexer

import mu.KLogging
import org.springframework.web.client.RestTemplate

data class VideoResponse(var id: String? = null)

class HttpVideoIndexerClient(
        private val restTemplate: RestTemplate,
        private val properties: VideoIndexerProperties
) : VideoIndexer {

    companion object : KLogging()

    override fun submitVideo(videoId: String, videoUrl: String): String {
        logger.info { "Submitting $videoUrl to the Video Indexer" }

        val videosUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos" +
                "?name={externalId}" +
                "&externalId={externalId}" +
                "&videoUrl={videoUrl}" +
                "&externalUrl={videoUrl}" +
                "&language={language}" +
                "&indexingPreset={indexingPreset}" +
                "&privacy={privacy}"
        val urlParams = submitUrlVariables(videoId, videoUrl)

        val response = restTemplate.postForEntity(videosUrl, "", VideoResponse::class.java, urlParams).body

        return response?.id.orEmpty()
    }

    private fun submitUrlVariables(videoId: String, videoUrl: String) = mapOf(
            "externalId" to videoId,
            "videoUrl" to videoUrl,
            "language" to "auto",
            "indexingPreset" to "AudioOnly",
            "privacy" to "Private"
    )
}
