package com.boclips.videoanalyser.application

import com.boclips.eventbus.BoclipsEvent

@BoclipsEvent("VIDEO_ANALYSER_VIDEO_INDEXED")
data class VideoIndexed(
    var videoId: String? = null
)
