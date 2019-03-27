package com.boclips.videoanalyser.infrastructure.videoindexer

interface VideoIndexer {

    fun submitVideo(videoId: String, videoUrl: String): String
    fun getVideoIndex(videoId: String): VideoIndex
}
