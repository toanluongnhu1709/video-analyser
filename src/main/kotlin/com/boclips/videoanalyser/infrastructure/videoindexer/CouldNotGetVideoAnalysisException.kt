package com.boclips.videoanalyser.infrastructure.videoindexer

import java.lang.RuntimeException

class CouldNotGetVideoAnalysisException(val becauseOfThirdPartyLimits: Boolean) : RuntimeException()
