package com.boclips.videoanalyser.infrastructure.videoindexer.resources

data class TranscriptItemResource(
        var id: Int? = null,
        var text: String? = null,
        var confidence: Double? = null,
        var speakerId: Int? = null,
        var language: String? = null,
        var instances: List<TimeRangeResource>? = null
)