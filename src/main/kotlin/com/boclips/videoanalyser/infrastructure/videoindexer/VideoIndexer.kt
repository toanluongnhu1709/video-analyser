package com.boclips.videoanalyser.infrastructure.videoindexer

import com.boclips.videoanalyser.infrastructure.videoindexer.resources.VideoResource
import java.util.*

interface VideoIndexer {

    fun isIndexed(videoId: String): Boolean

    fun submitVideo(videoId: String, videoUrl: String, language: Locale?)

    fun getVideo(videoId: String): VideoResource?

    fun deleteSourceFile(videoId: String)

    fun deleteVideo(videoId: String)
}

