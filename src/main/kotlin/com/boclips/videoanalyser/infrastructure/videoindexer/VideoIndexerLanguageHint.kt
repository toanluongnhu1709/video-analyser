package com.boclips.videoanalyser.infrastructure.videoindexer

import java.util.*

object VideoIndexerLanguageHint {

    private val codeByLanguage = mapOf(
            "en" to "en-US",
            "zh" to "zh-Hans",
            "es" to "es-ES",
            "fr" to "fr-FR",
            "ar" to "ar-EG",
            "de" to "de-DE",
            "it" to "it-IT",
            "ja" to "ja-JP",
            "hi" to "hi-IN",
            "ko" to "ko-KR",
            "ru" to "ru-RU",
            "pt" to "pt-BR"
    )

    fun fromLocale(locale: Locale): String {
        return codeByLanguage[locale.language] ?: throw VideoIndexerException("Language ${locale.toLanguageTag()} not supported")
    }
}
