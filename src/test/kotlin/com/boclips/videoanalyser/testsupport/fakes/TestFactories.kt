package com.boclips.videoanalyser.testsupport.fakes

import com.boclips.eventbus.events.video.VideoAnalysed

object TestFactories {


    fun createVideoAnalysed(): VideoAnalysed {
        return VideoAnalysed()
    }
}
