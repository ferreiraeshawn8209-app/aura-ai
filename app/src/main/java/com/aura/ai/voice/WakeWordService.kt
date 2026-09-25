package com.aura.ai.voice

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.aura.ai.MainActivity
import com.aura.ai.R
import com.aura.ai.T1000Application
import dagger.hilt.android.AndroidEntryPoint

/**
 * A lightweight, always-on wake-phrase listener implemented with the platform
 * recognizer in a restart loop. This is a pragmatic, dependency-free approach:
 * it recognizes short utterances and matches the phrase locally. For a
 * production-grade always-on hotword you would swap in a dedicated on-device
 * wake-word engine (e.g. Porcupine) behind this same service contract — the rest
 * of the app doesn't change.
 */
@AndroidEntryPoint
class WakeWordService : LifecycleService() {

    private var recognizer: SpeechRecognizer? = null
    private var listening = false

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        startRecognizer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val tapIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, T1000Application.CHANNEL_WAKE)
            .setContentTitle("T1000 is listening")
            .setContentText("Say \"${getString(R.string.wake_phrase)}\" to wake me")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setContentIntent(tapIntent)
            .build()
    }

    private fun startRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this) || listening) return
        listening = true
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(WakeListener())
            startListening(recognizerIntent())
        }
    }

    private fun recognizerIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    private fun restartSoon() {
        recognizer?.destroy()
        recognizer = null
        listening = false
        // Small backoff to avoid a hot loop when the recognizer errors repeatedly.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mainExecutor.execute { startRecognizer() }
        } else {
            startRecognizer()
        }
    }

    private fun onPhraseDetected() {
        val launch = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_WOKEN, true)
        }
        startActivity(launch)
    }

    private fun matches(text: String): Boolean {
        val phrase = getString(R.string.wake_phrase).lowercase()
        val normalized = text.lowercase()
        // Tolerant match: allow "hey t1000", "hey t 1000", "hey terminator", etc.
        return normalized.contains(phrase) ||
            (normalized.contains("hey") && normalized.contains("1000")) ||
            (normalized.contains("hey") && normalized.contains("t 1000"))
    }

    private inner class WakeListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onError(error: Int) = restartSoon()

        override fun onPartialResults(partialResults: Bundle?) {
            val hit = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.any { matches(it) } == true
            if (hit) {
                onPhraseDetected()
                restartSoon()
            }
        }

        override fun onResults(results: Bundle?) {
            val hit = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.any { matches(it) } == true
            if (hit) onPhraseDetected()
            restartSoon()
        }
    }

    override fun onDestroy() {
        recognizer?.destroy()
        recognizer = null
        listening = false
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 4242
        const val EXTRA_WOKEN = "woken"

        fun start(context: Context) {
            val intent = Intent(context, WakeWordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, WakeWordService::class.java))
        }
    }
}
