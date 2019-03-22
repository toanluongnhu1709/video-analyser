package com.boclips.videoanalyser.infrastructure.videoindexer

interface VideoIndexer {

    fun submitVideo(url: String)
}
