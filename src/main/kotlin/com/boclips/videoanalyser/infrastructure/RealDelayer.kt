package com.boclips.videoanalyser.infrastructure

import mu.KLogging

class RealDelayer : Delayer {
    companion object : KLogging()

    override fun delay(seconds: Int, onComplete: () -> Unit) {
        logger.info { "Thread ${Thread.currentThread().id} is having a little rest" }
        Thread.sleep(seconds.toLong() * 1000)
        logger.info { "Thread ${Thread.currentThread().id} is waking up" }
        onComplete()
    }
}
