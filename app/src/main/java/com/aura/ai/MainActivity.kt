package com.aura.ai

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.aura.ai.data.settings.SettingsRepository
import com.aura.ai.ui.T1000App
import com.aura.ai.ui.theme.AuraTheme
import com.aura.ai.voice.WakeWordEvents
import com.aura.ai.voice.WakeWordService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var wakeWordEvents: WakeWordEvents
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dispatchWakeIfNeeded(intent)
        enableEdgeToEdge()
        setContent {
            AuraTheme {
                T1000App()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchWakeIfNeeded(intent)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            val enabled = settingsRepository.settings.first().wakeWordEnabled
            val microphoneGranted = ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
            if (enabled && microphoneGranted) WakeWordService.start(this@MainActivity)
        }
    }

    private fun dispatchWakeIfNeeded(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra(WakeWordService.EXTRA_WOKEN, false) == true) {
            intent.removeExtra(WakeWordService.EXTRA_WOKEN)
            val transcript = intent.getStringExtra(WakeWordService.EXTRA_WAKE_TRANSCRIPT)
            intent.removeExtra(WakeWordService.EXTRA_WAKE_TRANSCRIPT)
            wakeWordEvents.dispatch(transcript)
        }
    }
}
