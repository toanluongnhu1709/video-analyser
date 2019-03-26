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

    @Test
    fun `update process`() {
        mockMvc.perform(post("/v1/videos/123/check_indexing_progress?id=msid&state=Processed"))
                .andExpect(status().isOk)
    }
}
