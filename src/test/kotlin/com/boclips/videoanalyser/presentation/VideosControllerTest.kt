package com.boclips.videoanalyser.presentation

import com.boclips.videoanalyser.testsupport.fakes.AbstractSpringIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class VideosControllerTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var indexingProgressCallbackFactory: IndexingProgressCallbackFactory

    @Test
    fun `update process`() {

        val callback = indexingProgressCallbackFactory.forVideo("123")

        mockMvc.perform(post("$callback?id=msid&state=Processed"))
                .andExpect(status().isOk)
    }
}

