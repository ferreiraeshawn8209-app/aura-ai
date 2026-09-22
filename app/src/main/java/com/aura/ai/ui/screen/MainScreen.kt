package com.aura.ai.ui.screen

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.aura.ai.util.VoiceHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Root screen. Text chat remains available without sensitive permissions;
 * feature-specific permissions are requested only when the feature is used.
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

    val microphonePermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    // Keep permission prompts tied to the feature that needs them. Chat remains
    // usable for text even when voice permission is denied.
    ChatScreen(
        voiceHelper = voiceHelper,
        onRequestMicrophonePermission = {
            if (microphonePermission.status.isGranted) {
                true
            } else {
                microphonePermission.launchPermissionRequest()
                false
            }
        }
    )
}
