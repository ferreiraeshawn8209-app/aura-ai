package com.aura.ai.agent

import java.time.Clock
import java.util.Locale

/** Deterministic answers that remain available when no model backend is configured. */
internal object LocalAnswerIntent {
    fun answer(
        request: String,
        clock: Clock = Clock.systemDefaultZone(),
        locale: Locale = Locale.getDefault(),
    ): String? = SimpleTimeIntent.answer(request, clock, locale)
        ?: SimpleMathIntent.answer(request)
}
