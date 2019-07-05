package com.boclips.videoanalyser.domain

import com.boclips.events.types.video.VideoAnalysed
import java.util.*

interface VideoAnalyserService {

    fun isAnalysed(videoId: String): Boolean

    fun submitVideo(videoId: String, videoUrl: String, language: Locale?)

    fun getVideo(videoId: String): VideoAnalysed

    fun deleteSourceFile(videoId: String)
}
