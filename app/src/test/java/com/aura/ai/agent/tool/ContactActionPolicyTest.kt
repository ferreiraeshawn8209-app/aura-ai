package com.aura.ai.agent.tool

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactActionPolicyTest {
    @Test
    fun restrictsBlankShortWildcardAndControlCharacterQueries() {
        assertNull(ContactActionPolicy.query(" "))
        assertNull(ContactActionPolicy.query("A"))
        assertNull(ContactActionPolicy.query("%"))
        assertNull(ContactActionPolicy.query("Jo_n"))
        assertEquals("Jordan", ContactActionPolicy.query(" Jordan "))
    }

    @Test
    fun exactNameWinsOverPartialMatchesAndReturnsOnePhoneLocally() {
        val result = ContactActionPolicy.uniquePhone(
            listOf(
                ContactPhoneMatch(1, "Jordan", "+27 82 123 4567"),
                ContactPhoneMatch(2, "Jordan Work", "+27 82 999 0000"),
            ),
            "Jordan",
        ) as ContactLookup.Matches
        assertEquals(1, result.values.single().contactId)
    }

    @Test
    fun refusesAmbiguousContactsAndMultipleNumbers() {
        val sameName = ContactActionPolicy.uniquePhone(
            listOf(
                ContactPhoneMatch(1, "Jordan", "+27 82 123 4567"),
                ContactPhoneMatch(2, "Jordan", "+27 82 999 0000"),
            ),
            "Jordan",
        )
        assertTrue(sameName is ContactLookup.MultipleContacts)

        val multipleNumbers = ContactActionPolicy.uniquePhone(
            listOf(
                ContactPhoneMatch(1, "Jordan", "+27 82 123 4567"),
                ContactPhoneMatch(1, "Jordan", "+27 83 555 1234"),
            ),
            "Jordan",
        )
        assertTrue(multipleNumbers is ContactLookup.MultipleNumbers)
    }

    @Test
    fun contactSearchSummaryDoesNotIncludeNumbers() {
        val summary = ContactActionPolicy.namesAndCounts(
            listOf(
                ContactPhoneMatch(1, "Jordan", "+27 82 123 4567"),
                ContactPhoneMatch(1, "Jordan", "+27 83 555 1234"),
                ContactPhoneMatch(1, "Jordan", "+27 82 123 4567"),
            ),
        )
        assertEquals(listOf("Jordan" to 2), summary)
        assertEquals(1, ContactActionPolicy.distinct(listOf(
            ContactPhoneMatch(1, "Jordan", "+27 82 123 4567"),
            ContactPhoneMatch(1, "Jordan", "+27 82 123 4567"),
        )).size)
    }
}
