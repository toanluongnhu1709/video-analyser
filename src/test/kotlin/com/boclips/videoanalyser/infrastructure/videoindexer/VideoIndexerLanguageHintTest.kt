package com.boclips.videoanalyser.infrastructure.videoindexer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class VideoIndexerLanguageHintTest {

    @Test
    fun `normalizes to values supported by Video Indexer`() {
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.ENGLISH)).isEqualTo("en-US")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.UK)).isEqualTo("en-US")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.US)).isEqualTo("en-US")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.SIMPLIFIED_CHINESE)).isEqualTo("zh-Hans")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.CHINESE)).isEqualTo("zh-Hans")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.FRENCH)).isEqualTo("fr-FR")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.GERMAN)).isEqualTo("de-DE")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.ITALIAN)).isEqualTo("it-IT")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.JAPANESE)).isEqualTo("ja-JP")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.KOREAN)).isEqualTo("ko-KR")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.forLanguageTag("es"))).isEqualTo("es-ES")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.forLanguageTag("ar"))).isEqualTo("ar-EG")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.forLanguageTag("hi"))).isEqualTo("hi-IN")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.forLanguageTag("ru"))).isEqualTo("ru-RU")
        assertThat(VideoIndexerLanguageHint.fromLocale(Locale.forLanguageTag("pt"))).isEqualTo("pt-BR")
    }

    @Test
    fun `throws when language is not supported`() {
        assertThrows<VideoIndexerException> {
            VideoIndexerLanguageHint.fromLocale(Locale.forLanguageTag("pl-PL"))
        }
    }
}
