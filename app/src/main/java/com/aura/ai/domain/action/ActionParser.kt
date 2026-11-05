package com.aura.ai.domain.action

import com.aura.ai.data.model.AuraAction
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Parses an assistant response and extracts a structured [AuraAction].
 *
 * AURA uses a function-call convention via a JSON code block. If the response
 * contains a ```json block with an "action" key, it is parsed as an action.
 * Otherwise the full text is returned as a plain [AuraAction.Reply].
 *
 * Expected JSON shape:
 * ```json
 * {
 *   "action": "make_call" | "send_sms" | "set_reminder" | "launch_app",
 *   "contact": "Mom",            // for call / sms
 *   "number": "+15551234567",    // optional resolved number
 *   "message": "I'll be late",  // for sms
 *   "title": "Doctor appt",     // for reminder
 *   "description": "...",       // for reminder (optional)
 *   "datetime": "2024-09-15 09:00",  // for reminder (yyyy-MM-dd HH:mm)
 *   "app_name": "Spotify",      // for launch_app
 *   "package_name": "com.spotify.music", // for launch_app (optional)
 *   "reply": "Calling Mom now!" // spoken reply to user
 * }
 * ```
 */
object ActionParser {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun parse(response: String): Pair<AuraAction, String?> {
        val jsonBlock = extractJsonBlock(response) ?: return AuraAction.Reply(response) to null
        return try {
            val json = Json.parseToJsonElement(jsonBlock).jsonObject
            fun field(name: String): String? = json[name]?.jsonPrimitive?.contentOrNull
            val replyText = field("reply").orEmpty().ifBlank { null }
            val action = when (field("action")) {
                "make_call" -> AuraAction.MakeCall(
                    contact = field("contact") ?: error("Missing contact"),
                    number = field("number").ifBlankToNull()
                )
                "send_sms" -> AuraAction.SendSms(
                    contact = field("contact") ?: error("Missing contact"),
                    number = field("number").ifBlankToNull(),
                    message = field("message") ?: error("Missing message")
                )
                "set_reminder" -> {
                    val datetime = field("datetime") ?: error("Missing datetime")
                    val epochMillis = runCatching { dateFormat.parse(datetime)?.time }
                        .getOrNull() ?: (System.currentTimeMillis() + 60_000)
                    AuraAction.SetReminder(
                        title = field("title") ?: error("Missing title"),
                        description = field("description").orEmpty(),
                        epochMillis = epochMillis
                    )
                }
                "launch_app" -> AuraAction.LaunchApp(
                    appName = field("app_name") ?: error("Missing app name"),
                    packageName = field("package_name").ifBlankToNull()
                )
                else -> AuraAction.Reply(replyText ?: response)
            }
            action to replyText
        } catch (e: Exception) {
            AuraAction.Reply(response) to null
        }
    }

    private fun extractJsonBlock(text: String): String? {
        val fencedJson = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```", RegexOption.IGNORE_CASE)
            .find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?.takeIf { it.startsWith("{") && it.endsWith("}") }
        if (fencedJson != null) return fencedJson

        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start >= 0 && end > start) text.substring(start, end + 1).trim() else null
    }

    private fun String?.ifBlankToNull(): String? = this?.takeIf { it.isNotBlank() }
}
