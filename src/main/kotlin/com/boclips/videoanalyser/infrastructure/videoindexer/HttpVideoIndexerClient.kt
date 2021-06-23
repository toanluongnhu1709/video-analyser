package com.boclips.videoanalyser.infrastructure.videoindexer

import com.boclips.videoanalyser.infrastructure.Delayer
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexItemResource
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoIndexResourceParser
import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoResource
import com.boclips.videoanalyser.presentation.PublishAnalysedVideoLinkFactory
import mu.KLogging
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.*

class HttpVideoIndexerClient(
        private val restTemplate: RestTemplate,
        private val properties: VideoIndexerProperties,
        private val videoIndexerTokenProvider: VideoIndexerTokenProvider,
        private val publishAnalysedVideoLinkFactory: PublishAnalysedVideoLinkFactory,
        private val videoIndexResourceParser: VideoIndexResourceParser,
        private val delayer: Delayer
) : VideoIndexer {
    companion object : KLogging()

    override fun submitVideo(videoId: String, videoUrl: String, language: Locale?) {
        logger.info { "Submitting $videoId to the Video Indexer" }

        val videosUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos" +
                "?accessToken={accessToken}" +
                "&name={videoId}" +
                "&externalId={videoId}" +
                "&videoUrl={videoUrl}" +
                "&externalUrl={videoUrl}" +
                "&callbackUrl={callbackUrl}" +
                "&language={language}" +
                "&indexingPreset=AudioOnly" +
                "&streamingPreset=NoStreaming" +
                "&privacy=Private"

        val urlParams = params()
                .plus("videoId" to videoId)
                .plus("videoUrl" to videoUrl)
                .plus("language" to (language?.let(VideoIndexerLanguageHint::fromLocale) ?: "auto"))
                .plus("callbackUrl" to publishAnalysedVideoLinkFactory.forVideo(videoId))

        try {
            restTemplate.postForEntity(videosUrl, "", String::class.java, urlParams)
            logger.info { "Video $videoId submitted to Video Indexer" }
        } catch (e: HttpStatusCodeException) {
            if (e.statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                val parsedSeconds : String? = ".*Try again in ([0-9]+) seconds.*".toRegex().let { pattern ->
                    e.message?.let { pattern.matchEntire(it) }
                        ?.groups
                        ?.get(1)
                        ?.value
                }
                val seconds = parsedSeconds?.toInt() ?: 10
                logger.info { "Received request to delay analysing video $videoId further processing by $seconds seconds"}
                delayer.delay(seconds) {
                    logger.info { "Finished delaying for video $videoId, resubmitting." }
                    submitVideo(videoId, videoUrl, language)
                }
            } else {
                logger.error { "Unhandled HTTP status code ${e.statusCode} with body: ${e.responseBodyAsString}" }
                throw VideoIndexerException("Failed to submit video $videoId to Video Indexer")
            }
        }
    }

    override fun isIndexed(videoId: String): Boolean {
        return resolveId(videoId) != null
    }

    private fun resolveId(videoId: String): String? {
        val externalIdUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos/GetIdByExternalId" +
                "?accessToken={accessToken}" +
                "&externalId={videoId}"

        val urlParams = params().plus("videoId" to videoId)

        val microsoftId = try {
            restTemplate.getForEntity(externalIdUrl, String::class.java, urlParams).body?.replace("\"", "").orEmpty()
        } catch (e: HttpStatusCodeException) {
            if (e.statusCode == HttpStatus.NOT_FOUND) {
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

        val videoIndexUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos/$microsoftId/Index" +
                "?accessToken={accessToken}"

        val videoCaptionsUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos/$microsoftId/Captions" +
                "?accessToken={accessToken}" +
                "&format=vtt"

        logger.debug { "GETting $videoIndexUrl" }
        val response = try {
            restTemplate.getForEntity(videoIndexUrl, String::class.java, params()).body
        } catch (e: HttpStatusCodeException) {
            logger.error(e.responseBodyAsString)
            throw VideoIndexerException("Failed to fetch video $videoId from Video Indexer")
        }
        val videoIndexResource = videoIndexResourceParser.parse(response!!)
        val state = videoIndexResource.videos?.first()?.state
        if(state != VideoIndexItemResource.STATE_PROCESSED) {
            return VideoResource(index = videoIndexResource, captions = null)
        }

        val captionsResponse = try {
            restTemplate.getForEntity(videoCaptionsUrl, ByteArray::class.java, params()).body
        } catch (e: HttpStatusCodeException) {
            logger.error(e.responseBodyAsString)
            throw VideoIndexerException("Failed to fetch captions of video $videoId from Video Indexer")
        }


        return VideoResource(index = videoIndexResource, captions = captionsResponse!!)
    }

    override fun deleteVideo(videoId: String) {
        val microsoftId = resolveId(videoId) ?: return

        val videoUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos/$microsoftId" +
                "?accessToken={accessToken}"

        logger.info { "Deleting video $videoId" }
        try {
            val response = restTemplate.exchange(videoUrl, HttpMethod.DELETE, null, Unit.javaClass, params())

            when (response.statusCode.is2xxSuccessful) {
                true -> logger.info { "Video $videoId deleted" }
                else -> logger.warn { "Video $videoId deletion failed with error ${response.statusCode} ${response.body}" }
            }
        } catch (ex: RestClientException) {
            logger.warn { "Video $videoId deletion failed with error ${ex.message}" }
        }
    }


    override fun deleteSourceFile(videoId: String) {
        val microsoftId = resolveId(videoId) ?: throw VideoIndexerException("Video $videoId not known by Video Indexer")

        val sourceFileUrl = "${properties.apiBaseUrl}/northeurope/Accounts/${properties.accountId}/Videos/$microsoftId/SourceFile" +
                "?accessToken={accessToken}"

        logger.info { "Deleting source file of video $videoId" }
        restTemplate.delete(sourceFileUrl, params())
        logger.info { "Source file of video $videoId deleted" }
    }

    private fun params(): Map<String, Any> {
        return mapOf("accessToken" to videoIndexerTokenProvider.getToken())
    }
}
