package com.boclips.videoanalyser.infrastructure.videoindexer

interface VideoIndexer {

    fun submitVideo(videoId: String, videoUrl: String)
    fun getVideoIndex(videoId: String): VideoIndex
}

class VideoIndexerException(message: String) : Exception(message)