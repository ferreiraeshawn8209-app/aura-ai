package com.aura.ai.domain.usecase

import android.content.Context
import android.telephony.SmsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SendSmsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Sends an SMS [message] to [number].
     * Requires [android.Manifest.permission.SEND_SMS].
     */
    fun execute(number: String, message: String): Result<Unit> = runCatching {
        val sanitized = number.filter { it.isDigit() || it == '+' }
        @Suppress("DEPRECATION")
        val smsManager: SmsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
        val parts = smsManager.divideMessage(message)
        if (parts.size == 1) {
            smsManager.sendTextMessage(sanitized, null, message, null, null)
        } else {
            smsManager.sendMultipartTextMessage(sanitized, null, parts, null, null)
        }
    }
}
