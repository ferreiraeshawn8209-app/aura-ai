package com.aura.ai.agent.tool

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLEncoder
import javax.inject.Inject

/**
 * Opens a WhatsApp chat with a prefilled draft. The user must review and tap
 * Send in WhatsApp; this tool never sends a message automatically.
 */
class WhatsAppDraftTool @Inject constructor(
    @ApplicationContext private val context: Context,
) : Tool {
    override val name = "prepare_whatsapp_message"
    override val description =
        "Prepare a WhatsApp message in a chat for the user to review and manually send. Never sends automatically."
    override val parameters = mapOf(
        "phone" to "Recipient's international phone number, including country code",
        "message" to "Message text to place into the WhatsApp draft",
    )

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val phone = normalizePhone(args["phone"].orEmpty())
            ?: return ToolResult(false, "", "A valid international phone number is required.")
        val message = args["message"].orEmpty().trim()
        if (message.isBlank()) return ToolResult(false, "", "Message text is required.")

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(buildUri(phone, message))).apply {
            setPackage(WHATSAPP_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            if (intent.resolveActivity(context.packageManager) == null) {
                ToolResult(false, "", "WhatsApp is not installed or cannot open a chat link.")
            } else {
                context.startActivity(intent)
                ToolResult(true, "WhatsApp opened with a draft. Review the recipient and message, then tap Send yourself.")
            }
        } catch (e: Exception) {
            ToolResult(false, "", "Could not open WhatsApp: ${e.message ?: "launch failed"}")
        }
    }

    companion object {
        private const val WHATSAPP_PACKAGE = "com.whatsapp"

        internal fun normalizePhone(input: String): String? {
            val trimmed = input.trim()
            if (trimmed.any { !(it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')') }) {
                return null
            }
            val digits = trimmed.filter(Char::isDigit)
            return digits.takeIf { it.length in 7..15 }
        }

        internal fun buildUri(phone: String, message: String): String {
            val encoded = URLEncoder.encode(message, Charsets.UTF_8.name()).replace("+", "%20")
            return "https://wa.me/$phone?text=$encoded"
        }
    }
}
