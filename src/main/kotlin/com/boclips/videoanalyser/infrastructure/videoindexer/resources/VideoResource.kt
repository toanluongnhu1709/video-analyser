package com.boclips.videoanalyser.infrastructure.videoindexer.resources

data class VideoResource(var index: VideoIndexResource? = null, var captions: ByteArray? = null)