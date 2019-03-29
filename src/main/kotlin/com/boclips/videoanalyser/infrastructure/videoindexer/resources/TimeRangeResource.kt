package com.boclips.videoanalyser.infrastructure.videoindexer.resources

data class TimeRangeResource(
        var start: String? = null,
        var end: String? = null,
        var adjustedStart: String? = null,
        var adjustedEnd: String? = null
)