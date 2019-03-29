package com.boclips.videoanalyser.infrastructure.videoindexer.resources

object TimeParser {
    fun parseToSeconds(time: String): Int {
        return time.split(':')
                .map { it.split('.').first() }
                .map { it.toInt() }
                .fold(0) { t, i -> 60*t+i }
    }
}