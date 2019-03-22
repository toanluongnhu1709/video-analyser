package com.boclips.videoanalyser.testsupport.fakes

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test", "fake-video-indexer")
abstract class AbstractSpringIntegrationTest {

    @Autowired
    lateinit var fakeVideoIndexer: FakeVideoIndexer

    @BeforeEach
    fun resetState() {
        fakeVideoIndexer.clear()
    }

}
