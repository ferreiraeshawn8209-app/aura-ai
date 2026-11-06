package com.aura.ai.agent

import com.aura.ai.voice.wake.WakePhraseMatcher
import java.time.Clock
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Answers simple local-time questions without waiting for a model or network. */
internal object SimpleTimeIntent {
    private val questions = listOf(
        Regex("^\\s*(?:what(?:'s| is)\\s+the\\s+time|what\\s+time\\s+is\\s+it)(?:\\s+now)?[?.!\\s]*$", RegexOption.IGNORE_CASE),
        Regex("^\\s*(?:tell\\s+me\\s+the\\s+time|current\\s+time|time\\s+please)(?:\\s+please)?[?.!\\s]*$", RegexOption.IGNORE_CASE),
    )

    fun answer(
        text: String,
        clock: Clock = Clock.systemDefaultZone(),
        locale: Locale = Locale.getDefault(),
    ): String? {
        val request = WakePhraseMatcher.commandAfterWake(text) ?: text
        if (questions.none { it.matches(request) }) return null
        val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(locale)
            .format(clock.instant().atZone(clock.zone))
        return "It's $time."
    }
}
