package com.aura.ai.ui.screen

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.aura.ai.util.VoiceHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Root screen that gates the chat UI behind runtime permission requests.
 * Non-critical permissions (contacts, calendar, SMS, phone) are requested
 * upfront but the user can proceed without them — AURA will degrade gracefully.
 *
 * **Testing note**: Runtime permission grant/denial flows require a real device
 * or emulator with UIAutomator. Basic launch and UI-display assertions are
 * covered in [MainActivityTest]. Full permission acceptance/rejection scenarios
 * should be validated via instrumentation tests using the UIAutomator API or
 * Accompanist's `PermissionState` testing utilities.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val voiceHelper = remember { VoiceHelper(context) }

    // Request all sensitive permissions at once. The user can deny some and
    // AURA will still work for the features they do have access to.
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    )

    // Launch permission request on first composition
    if (!permissionsState.allPermissionsGranted && !permissionsState.shouldShowRationale) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    // Always show the chat screen — permissions are handled per-action
    ChatScreen(voiceHelper = voiceHelper)
}
