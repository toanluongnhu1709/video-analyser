package com.boclips.videoanalyser.infrastructure.videoindexer.resources

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TimeParserTest {

    @Test
    fun parse() {
        assertThat(TimeParser.parseToSeconds("0:00:00")).isEqualTo(0)
        assertThat(TimeParser.parseToSeconds("0:00:22.45")).isEqualTo(22)
        assertThat(TimeParser.parseToSeconds("0:21:58.999")).isEqualTo(1318)
        assertThat(TimeParser.parseToSeconds("1:00:00.000")).isEqualTo(3600)
    }
}