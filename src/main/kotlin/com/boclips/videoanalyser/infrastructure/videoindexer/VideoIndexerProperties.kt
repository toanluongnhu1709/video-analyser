package com.boclips.videoanalyser.infrastructure.videoindexer

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "microsoft.videoindexer")
data class VideoIndexerProperties(
        var apiBaseUrl: String = "https://api.videoindexer.ai",
        var accountId: String = "",
        var subscriptionKey: String = ""
)
