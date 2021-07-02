package com.boclips.videoanalyser.infrastructure.videoindexer.resources

import com.boclips.eventbus.domain.video.*
import com.boclips.eventbus.events.video.*
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexerException
import mu.KLogging
import org.apache.logging.log4j.util.Strings
import java.nio.charset.StandardCharsets
import java.util.*

object VideoResourceToAnalysedVideoConverter {
    private val log = KLogging().logger

    fun convert(videoResource: VideoResource): VideoAnalysed {
        val index = videoResource.index ?: throw VideoIndexerException("No video index")
        val videos = index.videos ?: throw VideoIndexerException("No videos in the index")
        if (videos.size != 1) throw VideoIndexerException("Expecting 1 video in the index, found ${videos.size}.")
        val video = videos.first()

        val videoId = video.externalId ?: throw VideoIndexerException("No video id")

        val insights = video.insights ?: throw VideoIndexerException("No video insights")

        val language = insights.sourceLanguage ?: throw VideoIndexerException("No video language")

        val keywordsResources = insights.keywords.orEmpty()
        val keywords = keywordsResources.map(this::convertKeyword)

        val topicResources = insights.topics.orEmpty()
        val topics = topicResources.map(this::convertTopic)

        if (insights.transcript == null) {
            log.warn { "No video transcript" }
        }

        val transcriptResources = insights.transcript ?: emptyList()
        val transcript = transcriptResources.map { it.text }.joinToString(" ")

        if (videoResource.captions == null) {
            log.warn { "No video captions" }
        }

        val captions = videoResource.captions ?: Strings.EMPTY.toByteArray()

        return VideoAnalysed.builder()
            .videoId(videoId)
            .language(Locale.forLanguageTag(language))
            .transcript(transcript)
            .captions(
                Captions.builder()
                    .content(String(captions, StandardCharsets.UTF_8))
                    .language(Locale.forLanguageTag(language))
                    .format(CaptionsFormat.VTT)
                    .autoGenerated(true)
                    .build()
            )
            .topics(topics)
            .keywords(keywords)
            .build()
    }

    private fun convertKeyword(keywordResource: KeywordResource): VideoAnalysedKeyword {
        val segments = convertTimeRanges(keywordResource.instances!!)

        return VideoAnalysedKeyword.builder()
            .name(keywordResource.text!!)
            .language(Locale.forLanguageTag(keywordResource.language!!))
            .confidence(keywordResource.confidence!!)
            .segments(segments)
            .build()
    }

    private fun convertTopic(topicResource: TopicResource): VideoAnalysedTopic {
        val segments = convertTimeRanges(topicResource.instances!!)
        return parseTopicReferenceId(topicResource.referenceId!!, Locale.forLanguageTag(topicResource.language!!)).toBuilder()
            .segments(segments)
            .confidence(topicResource.confidence!!)
            .build()
    }

    private fun parseTopicReferenceId(referenceId: String, language: Locale): VideoAnalysedTopic {
        return referenceId
            .split('/')
            .fold(null) { parentTopic: VideoAnalysedTopic?, topicName: String ->
                VideoAnalysedTopic.builder()
                    .language(language)
                    .name(topicName)
                    .confidence(1.0)
                    .segments(emptyList())
                    .parent(parentTopic)
                    .build()
            }!!
    }

    private fun convertTimeRanges(instances: List<TimeRangeResource>): List<TimeSegment> {
        return instances.map(this::convertTimeRange)
    }

    private fun convertTimeRange(instance: TimeRangeResource): TimeSegment {
        return TimeSegment.builder()
            .startSecond(TimeParser.parseToSeconds(instance.start!!))
            .endSecond(TimeParser.parseToSeconds(instance.end!!))
            .build()
    }
}
