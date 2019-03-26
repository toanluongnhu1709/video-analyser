package com.boclips.videoanalyser.infrastructure.videoindexer

import com.boclips.videoanalyser.presentation.IndexingProgressCallbackFactory
import mu.KLogging
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
                "?name={externalId}" +
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

    private fun submitUrlVariables(videoId: String, videoUrl: String) = mapOf(
            "externalId" to videoId,
            "videoUrl" to videoUrl,
            "callbackUrl" to indexingProgressCallbackFactory.forVideo(videoId),
            "language" to "auto",
            "indexingPreset" to "AudioOnly",
            "privacy" to "Private"
    )
}
