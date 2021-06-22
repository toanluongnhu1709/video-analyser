package com.boclips.videoanalyser.testsupport.fakes

import com.boclips.videoanalyser.infrastructure.Delayer

class FakeDelayer : Delayer {
    var sleepyTime: Int = 0
    var callback: () -> Unit = {}

    override fun delay(seconds: Int, onComplete: () -> Unit) {
        sleepyTime = seconds
        callback = onComplete
    }

    fun advance(seconds: Int) {
        sleepyTime = sleepyTime - seconds
        if (sleepyTime <= 0) {
            callback()
        }
    }
}
