package com.aura.ai.voice

import android.app.Notification
import android.app.PendingIntent
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.aura.ai.MainActivity
import com.aura.ai.R
import com.aura.ai.AuraApplication
import com.aura.ai.voice.wake.WakePhraseMatcher
import com.aura.ai.voice.wake.WakeRecognizerPolicy
import com.aura.ai.voice.wake.WakeWordRequestGate
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
    private val handler = Handler(Looper.getMainLooper())
    private var stopping = false
    private var forceSystemRecognizer = false
    private var activeRecognizerOnDevice = false
    private var consecutiveErrors = 0
    private val requestGate = WakeWordRequestGate()
    private val restart = Runnable { if (!stopping) startRecognizer() }

    override fun onCreate() {
        super.onCreate()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }
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
        return NotificationCompat.Builder(this, AuraApplication.CHANNEL_WAKE)
            .setContentTitle("T1000 is listening")
            .setContentText("Say \"${getString(R.string.wake_phrase)}\" to wake me")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setContentIntent(tapIntent)
            .build()
    }

    private fun startRecognizer() {
        if (stopping || listening) return
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            scheduleRestart(SpeechRecognizer.ERROR_CLIENT)
            return
        }

        listening = true
        val requestId = requestGate.begin()
        val next = try {
            createWakeRecognizer()
        } catch (_: SecurityException) {
            requestGate.invalidate()
            listening = false
            stopSelf()
            return
        } catch (_: RuntimeException) {
            restartSoon(requestId, SpeechRecognizer.ERROR_CLIENT)
            return
        }

        recognizer = next
        try {
            next.setRecognitionListener(WakeListener(requestId))
            next.startListening(recognizerIntent())
        } catch (_: SecurityException) {
            requestGate.invalidate()
            listening = false
            destroyRecognizer()
            stopSelf()
        } catch (_: RuntimeException) {
            restartSoon(requestId, SpeechRecognizer.ERROR_CLIENT)
        }
    }

    private fun recognizerIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-ZA")
        // Prefer local standby recognition to avoid sending ambient wake audio
        // to a network service. Android recognizers may ignore this hint.
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    private fun createWakeRecognizer(): SpeechRecognizer {
        val onDeviceAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(this)
        if (!forceSystemRecognizer && WakeRecognizerPolicy.useOnDeviceRecognizer(Build.VERSION.SDK_INT, onDeviceAvailable)) {
            try {
                activeRecognizerOnDevice = true
                return SpeechRecognizer.createOnDeviceSpeechRecognizer(this)
            } catch (_: RuntimeException) {
                // Some platform providers can report availability and still
                // fail creation. Preserve wake availability via system fallback.
            }
        }
        activeRecognizerOnDevice = false
        return SpeechRecognizer.createSpeechRecognizer(this)
    }

    private fun restartSoon(requestId: Long, error: Int? = null) {
        if (!requestGate.isCurrent(requestId)) return
        requestGate.invalidate()
        destroyRecognizer()
        listening = false
        scheduleRestart(error)
    }

    private fun scheduleRestart(error: Int?) {
        if (stopping) return
        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            stopSelf()
            return
        }
        handler.removeCallbacks(restart)
        consecutiveErrors = if (error == null) 0 else (consecutiveErrors + 1).coerceAtMost(5)
        val delayMs = if (error == null) 800L else (1000L * (1 shl (consecutiveErrors - 1))).coerceAtMost(16_000L)
        handler.postDelayed(restart, delayMs)
    }

    private fun onPhraseDetected(transcript: String) {
        val launch = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_WOKEN, true)
            putExtra(EXTRA_WAKE_TRANSCRIPT, transcript)
        }
        startActivity(launch)
    }

    private inner class WakeListener(private val requestId: Long) : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onError(error: Int) {
            val languageUnsupported = error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
                error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE
            if (WakeRecognizerPolicy.shouldFallbackAfterLanguageError(activeRecognizerOnDevice, languageUnsupported)) {
                forceSystemRecognizer = true
                activeRecognizerOnDevice = false
            }
            restartSoon(requestId, error)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // Wait for a final utterance: triggering on the partial wake phrase
            // used to cut off the question spoken immediately after it.
        }

        override fun onResults(results: Bundle?) {
            if (!requestGate.isCurrent(requestId)) return
            val transcript = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull(WakePhraseMatcher::matches)
            if (transcript != null) {
                requestGate.invalidate()
                listening = false
                onPhraseDetected(transcript)
                stopSelf()
            } else {
                restartSoon(requestId)
            }
        }
    }

    override fun onDestroy() {
        stopping = true
        requestGate.invalidate()
        handler.removeCallbacks(restart)
        destroyRecognizer()
        listening = false
        super.onDestroy()
    }

    private fun destroyRecognizer() {
        val active = recognizer ?: return
        recognizer = null
        try {
            active.cancel()
        } catch (_: RuntimeException) {
        }
        try {
            active.destroy()
        } catch (_: RuntimeException) {
        }
    }

    companion object {
        const val NOTIFICATION_ID = 4242
        const val EXTRA_WOKEN = "woken"
        const val EXTRA_WAKE_TRANSCRIPT = "wake_transcript"

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
