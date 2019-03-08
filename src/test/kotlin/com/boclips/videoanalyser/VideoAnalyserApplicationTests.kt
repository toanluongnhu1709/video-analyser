package com.boclips.videoanalyser

import com.boclips.videoanalyser.domain.Video
import com.boclips.videoanalyser.domain.VideoAnalyserClient
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class VideoAnalyserApplicationTests {

	@Autowired
	lateinit var videoAnalyserClient: VideoAnalyserClient

	@Test
	fun send() {
		videoAnalyserClient.analyseVideo(Video("be more dog"))
	}

	@Test
	fun receive() {
		Thread.sleep(10000)
	}
}
