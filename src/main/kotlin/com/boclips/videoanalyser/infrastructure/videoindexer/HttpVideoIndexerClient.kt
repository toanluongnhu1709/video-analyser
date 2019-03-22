package com.boclips.videoanalyser.infrastructure.videoindexer

import mu.KLogging

class HttpVideoIndexerClient : VideoIndexer {

    companion object : KLogging()

    override fun submitVideo(url: String) {
        logger.info { "When implemented, will submit $url to the Video Indexer" }
    }
}
