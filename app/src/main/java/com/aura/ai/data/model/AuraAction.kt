package com.aura.ai.data.model

/**
 * Represents an action that AURA should perform on behalf of the user.
 * The AI response is parsed to detect these structured actions.
 */
sealed class AuraAction {
    /** Make a phone call to [number] (or contact name). */
    data class MakeCall(val contact: String, val number: String? = null) : AuraAction()

    /** Send an SMS to [contact] with [message]. */
    data class SendSms(val contact: String, val number: String? = null, val message: String) : AuraAction()

    /** Create a calendar reminder. */
    data class SetReminder(
        val title: String,
        val description: String = "",
        val epochMillis: Long
    ) : AuraAction()

    /** Launch an installed application by its [packageName] or a search [appName]. */
    data class LaunchApp(val appName: String, val packageName: String? = null) : AuraAction()

    /** Plain conversational reply — no device action needed. */
    data class Reply(val text: String) : AuraAction()
}
