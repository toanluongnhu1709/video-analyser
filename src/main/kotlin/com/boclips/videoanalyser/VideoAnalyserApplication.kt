package com.boclips.videoanalyser

import com.boclips.eventbus.EnableBoclipsEvents
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableBoclipsEvents
class VideoAnalyserApplication

fun main(args: Array<String>) {
	runApplication<VideoAnalyserApplication>(*args)
}
