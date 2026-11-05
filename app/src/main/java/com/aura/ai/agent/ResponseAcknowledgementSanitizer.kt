package com.aura.ai.agent

/** Prevent model-generated filler acknowledgements from duplicating the spoken pre-action line. */
internal object ResponseAcknowledgementSanitizer {
    private val leadingAcknowledgement = Regex(
        "^\\s*(?:i['’]?m\\s+on\\s+it|i['’]?ll\\s+be\\s+back|affirmative|roger\\s+that|command\\s+received)[.!,:;\\s\\-–—]*",
        RegexOption.IGNORE_CASE,
    )

    fun removeLeadingAcknowledgement(text: String): String =
        text.replaceFirst(leadingAcknowledgement, "").trim()
}
