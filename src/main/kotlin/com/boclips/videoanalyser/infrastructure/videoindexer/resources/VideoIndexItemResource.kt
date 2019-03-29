package com.boclips.videoanalyser.infrastructure.videoindexer.resources

data class VideoIndexItemResource(
        var externalId: String? = null,
        var insights: VideoInsightsResource? = null
)