package com.boclips.videoanalyser.infrastructure

import java.lang.RuntimeException

class VideoHasInvalidStateException(val videoId: String?, val state: String?) : RuntimeException()
