package com.boclips.videoanalyser.infrastructure.videoindexer

import com.boclips.videoanalyser.presentation.IndexingProgressCallbackFactory
import mu.KLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

data class VideoUploadResponse(var id: String? = null)

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

        val response = restTemplate.postForEntity(videosUrl, "", VideoUploadResponse::class.java, urlParams).body

        return response?.id.orEmpty()
    }

    override fun getVideoIndex(videoId: String): VideoIndex {
        val externalIdUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos/GetIdByExternalId" +
                "?accessToken={accessToken}" +
                "&externalId={externalId}"

        val urlParams = mapOf("accessToken" to getToken(), "externalId" to videoId)
        val microsoftId = restTemplate.getForEntity(externalIdUrl, String::class.java, urlParams).body?.replace("\"", "").orEmpty()

        val getVideoIndexUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos/$microsoftId" +
                "?accessToken={accessToken}"

        val getVideoCaptionsUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos/$microsoftId/Captions" +
                "?accessToken={accessToken}" +
                "&format=vtt"

        val response = restTemplate.getForEntity(getVideoIndexUrl, VideoIndexResource::class.java, mapOf("accessToken" to getToken())).body

        val captionsResponse = restTemplate.getForEntity(getVideoCaptionsUrl, ByteArray::class.java, mapOf("accessToken" to getToken())).body

        val keywords = response?.videos?.firstOrNull()?.insights?.keywords?.mapNotNull { it.text }.orEmpty()

        val topics = response?.videos?.firstOrNull()?.insights?.topics?.map { Topic(name = it.name!!) }.orEmpty()

        return VideoIndex(videoId = videoId, keywords = keywords, topics = topics, vttCaptions = captionsResponse!!)
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

data class VideoIndexResource(var videos: List<VideoResource>? = null)

data class VideoResource(var insights: VideoInsightsResource? = null)

data class VideoInsightsResource(var keywords: List<KeywordResource>? = null, var topics: List<TopicResource>? = null)

data class KeywordResource(var text: String? = null)

data class TopicResource(var name: String? = null)
