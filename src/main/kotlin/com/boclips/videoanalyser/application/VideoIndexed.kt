package com.boclips.videoanalyser.application

import com.boclips.eventbus.BoclipsEvent

@BoclipsEvent("video-analyser-video-indexed")
data class VideoIndexed(
    var videoId: String? = null
)
