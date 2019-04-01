package com.boclips.videoanalyser.infrastructure.videoindexer

import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexResourceParser
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoResource
import com.boclips.videoanalyser.presentation.PublishAnalysedVideoLinkFactory
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

class HttpVideoIndexerClient(
        private val restTemplate: RestTemplate,
        private val properties: VideoIndexerProperties,
        private val videoIndexerTokenProvider: VideoIndexerTokenProvider,
        private val publishAnalysedVideoLinkFactory: PublishAnalysedVideoLinkFactory,
        private val videoIndexResourceParser: VideoIndexResourceParser
) : VideoIndexer {
    companion object : KLogging()

    override fun submitVideo(videoId: String, videoUrl: String) {
        logger.info { "Submitting $videoId to the Video Indexer" }

        val videosUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos" +
                "?accessToken={accessToken}" +
                "&name={videoId}" +
                "&externalId={videoId}" +
                "&videoUrl={videoUrl}" +
                "&externalUrl={videoUrl}" +
                "&callbackUrl={callbackUrl}" +
                "&language=auto" +
                "&indexingPreset=AudioOnly" +
                "&privacy=Private"

        val urlParams = mapOf(
                "accessToken" to getToken(),
                "videoId" to videoId,
                "videoUrl" to videoUrl,
                "callbackUrl" to publishAnalysedVideoLinkFactory.forVideo(videoId)
        )

        try {
            restTemplate.postForEntity(videosUrl, "", String::class.java, urlParams)
            logger.info { "Video $videoId submitted to Video Indexer" }
        } catch(e: HttpStatusCodeException) {
            logger.error(e.responseBodyAsString)
            throw VideoIndexerException("Failed to submit video $videoId to Video Indexer")
        }
    }

    override fun isIndexed(videoId: String): Boolean {
        return resolveId(videoId) != null
    }

    private fun resolveId(videoId: String): String? {
        val externalIdUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos/GetIdByExternalId" +
                "?accessToken={accessToken}" +
                "&externalId={externalId}"

        val urlParams = mapOf("accessToken" to getToken(), "externalId" to videoId)

        val microsoftId = try {
            restTemplate.getForEntity(externalIdUrl, String::class.java, urlParams).body?.replace("\"", "").orEmpty()
        } catch(e: HttpStatusCodeException) {
            if(e.statusCode == HttpStatus.NOT_FOUND) {
                logger.info { "Video $videoId not known by Video Indexer" }
                return null
            }

            logger.error(e.responseBodyAsString)
            throw VideoIndexerException("Failed to resolve Video Indexer id for $videoId")
        }
        logger.info { "Resolved Video Indexer id for $videoId: $microsoftId" }

        return microsoftId
    }

    override fun getVideo(videoId: String): VideoResource {

        val microsoftId = resolveId(videoId) ?: throw VideoIndexerException("Video $videoId not known by Video Indexer")

        val getVideoIndexUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos/$microsoftId/Index" +
                "?accessToken={accessToken}"

        val getVideoCaptionsUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos/$microsoftId/Captions" +
                "?accessToken={accessToken}" +
                "&format=vtt"

        logger.debug { "GETting $getVideoIndexUrl" }
        val response = try {
                restTemplate.getForEntity(getVideoIndexUrl, String::class.java, mapOf("accessToken" to getToken())).body
        } catch(e: HttpStatusCodeException) {
            logger.error(e.responseBodyAsString)
            throw VideoIndexerException("Failed to fetch video $videoId from Video Indexer")
        }

        val captionsResponse = try {
            restTemplate.getForEntity(getVideoCaptionsUrl, ByteArray::class.java, mapOf("accessToken" to getToken())).body
        } catch(e: HttpStatusCodeException) {
            logger.error(e.responseBodyAsString)
            throw VideoIndexerException("Failed to fetch captions of video $videoId from Video Indexer")
        }

        val videoIndexResource = videoIndexResourceParser.parse(response!!)

        return VideoResource(index = videoIndexResource, captions = captionsResponse!!)
    }

    private fun getToken(): String {
        return videoIndexerTokenProvider.getToken()
    }
}
