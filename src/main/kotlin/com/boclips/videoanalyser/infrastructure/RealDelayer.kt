package com.boclips.videoanalyser.infrastructure

class RealDelayer : Delayer {
    override fun delay(seconds: Int, onComplete: () -> Unit) {
        Thread.sleep(seconds.toLong() * 1000)
        onComplete()
    }
}
