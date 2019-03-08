package com.boclips.videoanalyser

import com.boclips.videoanalyser.infrastructure.VideosToAnalyseChannels
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding

@SpringBootApplication
@EnableBinding(VideosToAnalyseChannels::class)
class VideoAnalyserApplication

fun main(args: Array<String>) {
	runApplication<VideoAnalyserApplication>(*args)
}
