package com.boclips.videoanalyser.testsupport.fakes

import com.boclips.videoanalyser.config.messaging.Subscriptions
import com.boclips.videoanalyser.config.messaging.Topics
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterEach
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

    @Autowired
    lateinit var topics: Topics

    @Autowired
    lateinit var subscriptions: Subscriptions

    @Autowired
    lateinit var messageCollector: MessageCollector

    lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun resetState() {
        fakeVideoIndexer.clear()
        messageCollector.forChannel(topics.analysedVideoIds()).clear()
        messageCollector.forChannel(topics.analysedVideos()).clear()
    }

    @BeforeEach
    fun startWireMockServer() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(57407))
        wireMockServer.start()
    }

    @AfterEach
    fun stopWireMockServer() {
        wireMockServer.stop()
    }


}
