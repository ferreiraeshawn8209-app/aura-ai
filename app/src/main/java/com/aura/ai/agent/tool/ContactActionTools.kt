package com.aura.ai.agent.tool

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal data class ContactPhoneMatch(
    val contactId: Long,
    val displayName: String,
    val number: String,
)

internal sealed interface ContactLookup {
    data class Matches(val values: List<ContactPhoneMatch>) : ContactLookup
    data class InvalidQuery(val reason: String) : ContactLookup
    data object PermissionDenied : ContactLookup
    data object NoMatches : ContactLookup
    data class MultipleContacts(val names: List<String>) : ContactLookup
    data class MultipleNumbers(val name: String) : ContactLookup
    data class Failed(val reason: String) : ContactLookup
}

/** Queries only when Android's existing Contacts runtime permission is granted. */
internal class ContactDirectory(private val context: Context) {
    fun search(query: String): ContactLookup {
        val safeQuery = ContactActionPolicy.query(query)
            ?: return ContactLookup.InvalidQuery("Use at least two letters; wildcard characters are not allowed.")
        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return ContactLookup.PermissionDenied
        }
        return try {
            val matches = mutableListOf<ContactPhoneMatch>()
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                ),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$safeQuery%"),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC",
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext() && matches.size < ContactActionPolicy.MAX_MATCHES) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)?.trim().orEmpty()
                    val number = cursor.getString(numberColumn)?.trim().orEmpty()
                    if (name.isNotBlank() && number.isNotBlank()) matches += ContactPhoneMatch(id, name, number)
                }
            }
            if (matches.isEmpty()) ContactLookup.NoMatches
            else ContactLookup.Matches(ContactActionPolicy.distinct(matches))
        } catch (_: SecurityException) {
            ContactLookup.PermissionDenied
        } catch (e: Exception) {
            ContactLookup.Failed(e.message ?: "Contact lookup failed")
        }
    }

    fun findUniquePhone(query: String): ContactLookup {
        val result = search(query)
        if (result !is ContactLookup.Matches) return result
        return ContactActionPolicy.uniquePhone(result.values, query)
    }
}

/** Validation and contact-selection rules isolated from Android provider code. */
internal object ContactActionPolicy {
    const val MAX_MATCHES = 50

    fun query(raw: String): String? {
        val value = raw.trim()
        return value.takeIf {
            it.length in 2..80 && it.none { ch -> ch == '%' || ch == '_' || ch == '\\' || ch.isISOControl() }
        }
    }

    fun distinct(values: List<ContactPhoneMatch>): List<ContactPhoneMatch> =
        values.distinctBy { Triple(it.contactId, it.displayName, AndroidActionInputPolicy.normalizePhone(it.number)) }

    fun namesAndCounts(values: List<ContactPhoneMatch>): List<Pair<String, Int>> =
        distinct(values).groupBy { it.contactId to it.displayName }
            .map { (contact, matches) -> contact.second to matches.mapNotNull { AndroidActionInputPolicy.normalizePhone(it.number) }.distinct().size }
            .sortedBy { it.first.lowercase() }

    fun uniquePhone(values: List<ContactPhoneMatch>, query: String): ContactLookup {
        val exact = values.filter { it.displayName.equals(query.trim(), ignoreCase = true) }
        val candidates = distinct(if (exact.isNotEmpty()) exact else values)
        val contacts = candidates.distinctBy { it.contactId }
        if (contacts.size > 1) return ContactLookup.MultipleContacts(contacts.map { it.displayName }.distinct())
        val numbers = contacts.flatMap { contact -> candidates.filter { it.contactId == contact.contactId } }
            .distinctBy { AndroidActionInputPolicy.normalizePhone(it.number) }
            .filter { AndroidActionInputPolicy.normalizePhone(it.number) != null }
        if (numbers.isEmpty()) return ContactLookup.NoMatches
        if (numbers.size > 1) return ContactLookup.MultipleNumbers(contacts.first().displayName)
        return ContactLookup.Matches(numbers)
    }
}

class SearchContactsTool @Inject constructor(
    @ApplicationContext private val context: Context,
) : Tool {
    private val directory by lazy { ContactDirectory(context) }

    override val name = "search_contacts"
    override val description = "Search contacts by name. Returns names and saved-number counts only, not phone numbers. Requires Contacts permission."
    override val parameters = mapOf("query" to "At least two letters from the contact's name")

    override suspend fun execute(args: Map<String, String>): ToolResult = when (val result = directory.search(args["query"].orEmpty())) {
        is ContactLookup.Matches -> {
            val results = ContactActionPolicy.namesAndCounts(result.values)
            ToolResult(true, results.joinToString("\n") { (name, count) -> "$name ($count saved number${if (count == 1) "" else "s"})" })
        }
        is ContactLookup.InvalidQuery -> ToolResult(false, "", result.reason)
        ContactLookup.PermissionDenied -> ToolResult(false, "", "Contacts access is off. Grant Aura Contacts permission in Android app settings, then try again.")
        ContactLookup.NoMatches -> ToolResult(true, "No matching contacts with a phone number were found.")
        is ContactLookup.MultipleContacts -> ToolResult(false, "", "More than one contact matched: ${result.names.joinToString()}. Ask for a more specific name.")
        is ContactLookup.MultipleNumbers -> ToolResult(false, "", "${result.name} has multiple numbers. Open Contacts and choose the desired number before retrying.")
        is ContactLookup.Failed -> ToolResult(false, "", "Could not search contacts: ${result.reason}")
    }
}

class PrepareContactCallTool @Inject constructor(
    @ApplicationContext private val context: Context,
) : Tool {
    private val directory by lazy { ContactDirectory(context) }

    override val name = "prepare_contact_call"
    override val description = "Open the dialer for a uniquely matched contact. The user reviews and taps Call; Aura does not place calls directly."
    override val parameters = mapOf("contact_name" to "Exact or distinctive contact name")

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val name = args["contact_name"].orEmpty().trim()
        return when (val result = directory.findUniquePhone(name)) {
            is ContactLookup.Matches -> {
                val phone = AndroidActionInputPolicy.normalizePhone(result.values.single().number)!!
                launch(context, Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null)), "Dialer opened for ${result.values.single().displayName}. Review the number and tap Call.")
            }
            else -> result.asToolFailure()
        }
    }
}

class PrepareContactSmsTool @Inject constructor(
    @ApplicationContext private val context: Context,
) : Tool {
    private val directory by lazy { ContactDirectory(context) }

    override val name = "prepare_contact_sms"
    override val description = "Prepare an SMS for a uniquely matched contact. The user reviews and taps Send; Aura never sends automatically."
    override val parameters = mapOf(
        "contact_name" to "Exact or distinctive contact name",
        "message" to "Message text to prepare for review",
    )

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val name = args["contact_name"].orEmpty().trim()
        val message = args["message"].orEmpty().trim()
        if (message.isBlank()) return ToolResult(false, "", "Message text is required.")
        return when (val result = directory.findUniquePhone(name)) {
            is ContactLookup.Matches -> {
                val phone = AndroidActionInputPolicy.normalizePhone(result.values.single().number)!!
                val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", phone, null)).apply {
                    putExtra("sms_body", message)
                }
                launch(context, intent, "Messaging app opened with a draft for ${result.values.single().displayName}. Review it and tap Send yourself.")
            }
            else -> result.asToolFailure()
        }
    }
}

private fun ContactLookup.asToolFailure(): ToolResult = when (this) {
    is ContactLookup.InvalidQuery -> ToolResult(false, "", reason)
    ContactLookup.PermissionDenied -> ToolResult(false, "", "Contacts access is off. Grant Aura Contacts permission in Android app settings, then try again.")
    ContactLookup.NoMatches -> ToolResult(false, "", "No matching contact with a valid phone number was found.")
    is ContactLookup.MultipleContacts -> ToolResult(false, "", "More than one contact matched: ${names.joinToString()}. Ask for a more specific name.")
    is ContactLookup.MultipleNumbers -> ToolResult(false, "", "$name has multiple numbers. Open Contacts and choose the desired number before retrying.")
    is ContactLookup.Failed -> ToolResult(false, "", "Could not search contacts: $reason")
    is ContactLookup.Matches -> ToolResult(false, "", "Contact selection is ambiguous.")
}

private fun launch(context: Context, intent: Intent, success: String): ToolResult = try {
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (intent.resolveActivity(context.packageManager) == null) ToolResult(false, "", "No installed app can handle this action.")
    else {
        context.startActivity(intent)
        ToolResult(true, success)
    }
} catch (e: Exception) {
    ToolResult(false, "", "Could not open the requested app: ${e.message ?: "launch failed"}")
}
