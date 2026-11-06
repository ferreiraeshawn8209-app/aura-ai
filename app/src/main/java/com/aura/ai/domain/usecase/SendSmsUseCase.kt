package com.aura.ai.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SendSmsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Opens a prefilled SMS draft; the user reviews and sends it in their messaging app. */
    fun execute(number: String, message: String): Result<Unit> = runCatching {
        val sanitized = number.filter { it.isDigit() || it == '+' }
        require(sanitized.count { it.isDigit() } in 7..15) { "A valid phone number is required." }
        require(message.isNotBlank()) { "Message text is required." }
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", sanitized, null)).apply {
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
