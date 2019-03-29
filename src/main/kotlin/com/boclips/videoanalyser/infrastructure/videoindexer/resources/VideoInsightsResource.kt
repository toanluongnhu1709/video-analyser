package com.boclips.videoanalyser.infrastructure.videoindexer.resources

data class VideoInsightsResource(
        var sourceLanguage: String? = null,
        var keywords: List<KeywordResource>? = null,
        var topics: List<TopicResource>? = null,
        var transcript: List<TranscriptItemResource>? = null
)