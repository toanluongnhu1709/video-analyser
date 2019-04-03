package com.boclips.videoanalyser.infrastructure.videoindexer.resources

import com.boclips.events.types.AnalysedVideo
import com.boclips.videoanalyser.infrastructure.videoindexer.VideoIndexerException
import java.nio.charset.StandardCharsets

object VideoResourceToAnalysedVideoConverter {

    fun convert(videoResource: VideoResource): AnalysedVideo {
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

        val transcriptResources = insights.transcript ?: throw VideoIndexerException("No video transcript")
        val transcript = transcriptResources.map { it.text }.joinToString(" ")

        val captions = videoResource.captions ?: throw VideoIndexerException("No video captions")

        return AnalysedVideo.builder()
                .videoId(videoId)
                .language(language)
                .transcript(transcript)
                .captions(AnalysedVideo.Captions.builder()
                        .content(String(captions, StandardCharsets.UTF_8))
                        .format(AnalysedVideo.CaptionsFormat.VTT)
                        .build()
                )
                .topics(topics)
                .keywords(keywords)
                .build()
    }

    private fun convertKeyword(keywordResource: KeywordResource): AnalysedVideo.Assigned<AnalysedVideo.Keyword> {
        val keyword = AnalysedVideo.Keyword.builder()
                .name(keywordResource.text!!)
                .language(keywordResource.language!!)
                .build()
        val segments = convertTimeRanges(keywordResource.instances!!)

        return AnalysedVideo.Assigned.builder<AnalysedVideo.Keyword>()
                .value(keyword)
                .confidence(keywordResource.confidence!!)
                .segments(segments)
                .build()
    }

    private fun convertTopic(topicResource: TopicResource): AnalysedVideo.Assigned<AnalysedVideo.Topic> {
        val topic = parseTopicReferenceId(topicResource.referenceId!!, topicResource.language!!)
        val segments = convertTimeRanges(topicResource.instances!!)
        return AnalysedVideo.Assigned.builder<AnalysedVideo.Topic>()
                .confidence(topicResource.confidence!!)
                .value(topic)
                .segments(segments)
                .build()
    }

    private fun parseTopicReferenceId(referenceId: String, language: String): AnalysedVideo.Topic {
        return referenceId
                .split('/')
                .fold(null) { parentTopic: AnalysedVideo.Topic?, topicName: String ->
                    AnalysedVideo.Topic.builder()
                            .language(language)
                            .name(topicName)
                            .parent(parentTopic)
                            .build()
                }!!
    }

    private fun convertTimeRanges(instances: List<TimeRangeResource>): List<AnalysedVideo.TimeSegment> {
        return instances.map(this::convertTimeRange)
    }

    private fun convertTimeRange(instance: TimeRangeResource): AnalysedVideo.TimeSegment {
        return AnalysedVideo.TimeSegment.builder()
                .startSecond(TimeParser.parseToSeconds(instance.start!!))
                .endSecond(TimeParser.parseToSeconds(instance.end!!))
                .build()
    }
}