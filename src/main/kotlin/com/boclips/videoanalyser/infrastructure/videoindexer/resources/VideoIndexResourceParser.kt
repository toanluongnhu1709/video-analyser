package com.boclips.videoanalyser.infrastructure.videoindexer.resources

import com.fasterxml.jackson.databind.ObjectMapper

class VideoIndexResourceParser(private val objectMapper: ObjectMapper) {

    fun parse(json: String): VideoIndexResource {
        return objectMapper.readValue(json, VideoIndexResource::class.java).copy(raw = json)
    }
}
