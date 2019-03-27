package com.boclips.videoanalyser.infrastructure.videoindexer

data class Topic(val name: String)

data class VideoIndex(var videoId: String, var keywords: List<String>, var topics: List<Topic>, @Suppress("ArrayInDataClass") var vttCaptions: ByteArray)
