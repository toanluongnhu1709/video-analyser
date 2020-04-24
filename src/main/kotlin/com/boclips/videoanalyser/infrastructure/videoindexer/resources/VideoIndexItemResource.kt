package com.boclips.videoanalyser.infrastructure.videoindexer.resources

data class VideoIndexItemResource(
        var state: String? = null,
        var externalId: String? = null,
        var insights: VideoInsightsResource? = null
) {
    companion object {
        final val STATE_PROCESSED = "Processed"
    }
}
