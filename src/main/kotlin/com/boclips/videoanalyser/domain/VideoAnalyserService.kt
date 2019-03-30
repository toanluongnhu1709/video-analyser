package com.boclips.videoanalyser.domain

import com.boclips.eventtypes.AnalysedVideo

interface VideoAnalyserService {

    fun submitVideo(videoId: String, videoUrl: String)

    fun getVideo(videoId: String): AnalysedVideo
}
