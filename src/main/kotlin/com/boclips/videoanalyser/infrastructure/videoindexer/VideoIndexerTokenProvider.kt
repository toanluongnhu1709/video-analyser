package com.boclips.videoanalyser.infrastructure.videoindexer

interface VideoIndexerTokenProvider {
    fun getToken(): String
}

