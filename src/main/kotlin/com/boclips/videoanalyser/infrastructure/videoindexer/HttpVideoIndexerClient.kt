package com.boclips.videoanalyser.infrastructure.videoindexer

import com.boclips.videoanalyser.presentation.IndexingProgressCallbackFactory
import mu.KLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import java.lang.Exception

data class VideoUploadResponse(var id: String? = null)

class HttpVideoIndexerClient(
        private val restTemplate: RestTemplate,
        private val properties: VideoIndexerProperties,
        private val indexingProgressCallbackFactory: IndexingProgressCallbackFactory
) : VideoIndexer {

    companion object : KLogging()

    override fun submitVideo(videoId: String, videoUrl: String): String {
        return try {
            doSubmitVideo(videoId = videoId, videoUrl = videoUrl)
        }
        catch (e: Exception) {
            logger.error(e) { "Raised exception from doSubmitVideo" }
            ""
        }
    }

    private fun doSubmitVideo(videoId: String, videoUrl: String): String {
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

        val response = restTemplate.postForEntity(videosUrl, "", VideoUploadResponse::class.java, urlParams)
        logger.info { "Made post request to MS Video Indexer and received status code ${response.statusCode}" }
        return response.body?.id.orEmpty()
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
        logger.info { "Requesting a Video Indexer token" }
        val tokenUrl = "${properties.apiBaseUrl}/auth/northeurope/Accounts/${properties.accountId}/AccessToken"
        val headers = HttpHeaders().apply { set("Ocp-Apim-Subscription-Key", properties.subscriptionKey) }
        val response = restTemplate.exchange(tokenUrl, HttpMethod.GET, HttpEntity("", headers), String::class.java)
        val token = response.body?.replace("\"", "").orEmpty()
        logger.info { "Received a Video Indexer token: ${token.substring(0, 6)}*************" }
        return token
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
