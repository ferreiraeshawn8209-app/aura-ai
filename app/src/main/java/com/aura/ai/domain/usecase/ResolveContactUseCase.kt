package com.aura.ai.domain.usecase

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ResolveContactUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Looks up a phone number for [contactName] in the device's contacts.
     * Returns null if not found or permission was not granted.
     */
    fun execute(contactName: String): String? {
        val resolver: ContentResolver = context.contentResolver
        val name = contactName.trim()
        var cursor: Cursor? = null
        return try {
            cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$name%"),
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                cursor.getString(numberIdx)
            } else null
        } catch (e: SecurityException) {
            null
        } finally {
            cursor?.close()
        }
    }
}
