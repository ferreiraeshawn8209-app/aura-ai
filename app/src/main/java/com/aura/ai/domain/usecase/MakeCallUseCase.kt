package com.aura.ai.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MakeCallUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Initiates a phone call to [number].
     * Requires [android.Manifest.permission.CALL_PHONE].
     */
    fun execute(number: String): Result<Unit> = runCatching {
        val sanitized = number.filter { it.isDigit() || it == '+' }
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$sanitized"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
