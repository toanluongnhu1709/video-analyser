package com.boclips.videoanalyser

import com.boclips.events.spring.EnableBoclipsEvents
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableBoclipsEvents(appName = "video-analyser")
class VideoAnalyserApplication

fun main(args: Array<String>) {
	runApplication<VideoAnalyserApplication>(*args)
}
