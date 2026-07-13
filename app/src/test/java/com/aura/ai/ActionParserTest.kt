package com.aura.ai.domain.action

import com.aura.ai.data.model.AuraAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionParserTest {

    @Test
    fun `parse plain text returns Reply action`() {
        val input = "Hello, I'm AURA! How can I help you today?"
        val (action, reply) = ActionParser.parse(input)
        assertTrue(action is AuraAction.Reply)
        assertEquals(input, (action as AuraAction.Reply).text)
        assertTrue(reply == null)
    }

    @Test
    fun `parse make_call action`() {
        val input = """
            Sure, calling your mum now!
            ```json
            {
              "action": "make_call",
              "contact": "Mom",
              "reply": "Calling Mom now!"
            }
            ```
        """.trimIndent()
        val (action, reply) = ActionParser.parse(input)
        assertTrue(action is AuraAction.MakeCall)
        assertEquals("Mom", (action as AuraAction.MakeCall).contact)
        assertEquals("Calling Mom now!", reply)
    }

    @Test
    fun `parse send_sms action`() {
        val input = """
            ```json
            {
              "action": "send_sms",
              "contact": "John",
              "message": "I'll be 10 minutes late.",
              "reply": "Message sent to John."
            }
            ```
        """.trimIndent()
        val (action, reply) = ActionParser.parse(input)
        assertTrue(action is AuraAction.SendSms)
        val sms = action as AuraAction.SendSms
        assertEquals("John", sms.contact)
        assertEquals("I'll be 10 minutes late.", sms.message)
        assertEquals("Message sent to John.", reply)
    }

    @Test
    fun `parse set_reminder action`() {
        val input = """
            ```json
            {
              "action": "set_reminder",
              "title": "Doctor appointment",
              "description": "Annual check-up",
              "datetime": "2025-08-01 09:00",
              "reply": "Reminder set for August 1st at 9 AM."
            }
            ```
        """.trimIndent()
        val (action, reply) = ActionParser.parse(input)
        assertTrue(action is AuraAction.SetReminder)
        val reminder = action as AuraAction.SetReminder
        assertEquals("Doctor appointment", reminder.title)
        assertEquals("Annual check-up", reminder.description)
        assertTrue(reminder.epochMillis > 0)
        assertNotNull(reply)
    }

    @Test
    fun `parse launch_app action`() {
        val input = """
            ```json
            {
              "action": "launch_app",
              "app_name": "Spotify",
              "reply": "Opening Spotify for you."
            }
            ```
        """.trimIndent()
        val (action, reply) = ActionParser.parse(input)
        assertTrue(action is AuraAction.LaunchApp)
        assertEquals("Spotify", (action as AuraAction.LaunchApp).appName)
        assertEquals("Opening Spotify for you.", reply)
    }

    @Test
    fun `parse unknown action returns Reply`() {
        val input = """
            ```json
            {
              "action": "unknown_action",
              "reply": "I don't know that action."
            }
            ```
        """.trimIndent()
        val (action, _) = ActionParser.parse(input)
        assertTrue(action is AuraAction.Reply)
    }

    @Test
    fun `parse malformed JSON returns Reply`() {
        val input = """
            ```json
            { this is not valid json }
            ```
        """.trimIndent()
        val (action, _) = ActionParser.parse(input)
        assertTrue(action is AuraAction.Reply)
    }
}
