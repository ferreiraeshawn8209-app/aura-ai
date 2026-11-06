package com.aura.ai.agent.tool

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Opens a uniquely matching installed launcher app; it does not install apps. */
class OpenAppTool @Inject constructor(
    @ApplicationContext private val context: Context,
) : Tool {
    override val name = "open_app"
    override val description = "Open an installed phone app by its visible name. Does not install or change apps."
    override val parameters = mapOf("app_name" to "Visible name of the installed app to open")

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val requested = args["app_name"].orEmpty().trim()
        if (requested.isBlank()) return ToolResult(false, "", "app_name is required")
        val matches = runCatching {
            context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .mapNotNull { app ->
                    val label = context.packageManager.getApplicationLabel(app).toString()
                    if (label.equals(requested, ignoreCase = true)) app.packageName to label else null
                }
        }.getOrElse { return ToolResult(false, "", "Could not search installed apps: ${it.message ?: "unknown error"}") }
        if (matches.isEmpty()) return ToolResult(false, "", "No installed app named '$requested' was found.")
        if (matches.size > 1) {
            return ToolResult(false, "", "More than one app is named '$requested': ${matches.joinToString { it.second }}. Specify the exact app.")
        }
        val (packageName, label) = matches.single()
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return ToolResult(false, "", "$label is installed but has no launch screen.")
        return start(context, launchIntent, "$label opened.")
    }
}

/** Opens an HTTP(S) page in the user's browser. */
class OpenWebsiteTool @Inject constructor(
    @ApplicationContext private val context: Context,
) : Tool {
    override val name = "open_website"
    override val description = "Open an HTTP or HTTPS website in the user's browser."
    override val parameters = mapOf("url" to "Website address, for example https://example.com")

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val url = AndroidActionInputPolicy.webUrl(args["url"].orEmpty())
            ?: return ToolResult(false, "", "Provide a valid website address using HTTP or HTTPS.")
        return start(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)), "Opened $url in the browser.")
    }
}

/** Brings up the system dialer with a number filled in; the user places the call. */
class PreparePhoneCallTool @Inject constructor(
    @ApplicationContext private val context: Context,
) : Tool {
    override val name = "prepare_phone_call"
    override val description = "Open the phone dialer with a number ready. The user reviews and taps Call; Aura does not place calls directly."
    override val parameters = mapOf("phone" to "Phone number to prepare in the dialer")

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val phone = AndroidActionInputPolicy.normalizePhone(args["phone"].orEmpty())
            ?: return ToolResult(false, "", "A valid phone number is required.")
        return start(context, Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null)), "Dialer opened with the number ready. Review it and tap Call to place the call.")
    }
}

/** Opens the default SMS app with a recipient and draft; the user taps Send. */
class PrepareSmsTool @Inject constructor(
    @ApplicationContext private val context: Context,
) : Tool {
    override val name = "prepare_sms"
    override val description = "Prepare an SMS in the default messaging app. The user reviews and taps Send; Aura never sends automatically."
    override val parameters = mapOf(
        "phone" to "Recipient phone number",
        "message" to "Message text to prepare for review",
    )

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val phone = AndroidActionInputPolicy.normalizePhone(args["phone"].orEmpty())
            ?: return ToolResult(false, "", "A valid phone number is required.")
        val message = args["message"].orEmpty().trim()
        if (message.isBlank()) return ToolResult(false, "", "Message text is required.")
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", phone, null)).apply {
            putExtra("sms_body", message)
        }
        return start(context, intent, "Messaging app opened with an SMS draft. Review the recipient and text, then tap Send yourself.")
    }
}

private fun start(context: Context, intent: Intent, success: String): ToolResult = try {
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (intent.resolveActivity(context.packageManager) == null) {
        ToolResult(false, "", "No installed app can handle this action.")
    } else {
        context.startActivity(intent)
        ToolResult(true, success)
    }
} catch (e: Exception) {
    ToolResult(false, "", "Could not open the requested app: ${e.message ?: "launch failed"}")
}
