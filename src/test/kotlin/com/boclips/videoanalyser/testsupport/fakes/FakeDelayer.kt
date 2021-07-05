package com.boclips.videoanalyser.testsupport.fakes

import com.boclips.videoanalyser.infrastructure.Delayer

class FakeDelayer : Delayer {
    var sleepyTime: Long = 0
    var callback: () -> Unit = {}

    override fun delay(seconds: Int, onComplete: () -> Unit) {
        sleepyTime = seconds * 1000L
        callback = onComplete
    }

    override fun delay(milliseconds: Long, onComplete: () -> Unit) {
        sleepyTime = milliseconds
        callback = onComplete
    }

    fun advance(seconds: Int) {
        sleepyTime -= seconds * 1000L
        if (sleepyTime <= 0) {
            callback()
        }
    }
}
