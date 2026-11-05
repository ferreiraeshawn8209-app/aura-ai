package com.aura.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aura.ai.ui.T1000App
import com.aura.ai.ui.theme.AuraTheme
import com.aura.ai.voice.WakeWordEvents
import com.aura.ai.voice.WakeWordService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var wakeWordEvents: WakeWordEvents

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

    private fun dispatchWakeIfNeeded(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra(WakeWordService.EXTRA_WOKEN, false) == true) {
            intent.removeExtra(WakeWordService.EXTRA_WOKEN)
            wakeWordEvents.dispatch()
        }
    }
}
