package com.aura.ai.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MakeCallUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Opens the system dialer; the user confirms the call there. */
    fun execute(number: String): Result<Unit> = runCatching {
        val sanitized = number.filter { it.isDigit() || it == '+' }
        require(sanitized.count { it.isDigit() } in 7..15) { "A valid phone number is required." }
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", sanitized, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
