package com.aura.ai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test for permission-related UI flows.
 *
 * Full permission-grant/denial scenarios require a device or emulator with
 * UIAutomator; the tests here verify the basic launch-and-display contract.
 * Use the Accompanist `PermissionState` API in UI tests for finer-grained
 * permission UI assertions.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appBar_displaysAuraTitle() {
        composeTestRule.onNodeWithText("AURA AI").assertIsDisplayed()
    }

    @Test
    fun chatInput_isDisplayed() {
        composeTestRule.onNodeWithText("Ask AURA anything…").assertIsDisplayed()
    }
}
