package com.boclips.videoanalyser.infrastructure.videoindexer.resources

data class TopicResource(
        var id: Int? = null,
        var name: String? = null,
        var referenceId: String? = null,
        var referenceType: String? = null,
        var iptcName: String? = null,
        var confidence: Double? = null,
        var iabName: String? = null,
        var language: String? = null,
        var instances: List<TimeRangeResource>? = null
)