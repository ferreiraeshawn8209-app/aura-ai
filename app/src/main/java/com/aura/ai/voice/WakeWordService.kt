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
    private var consecutiveErrors = 0
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
        if (!SpeechRecognizer.isRecognitionAvailable(this) || listening) return
        listening = true
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(WakeListener())
            startListening(recognizerIntent())
        }
    }

    private fun recognizerIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-ZA")
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    private fun restartSoon(error: Int? = null) {
        if (stopping) return
        if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
            stopSelf()
            return
        }
        recognizer?.destroy()
        recognizer = null
        listening = false
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

    private inner class WakeListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onError(error: Int) = restartSoon(error)

        override fun onPartialResults(partialResults: Bundle?) {
            // Wait for a final utterance: triggering on the partial wake phrase
            // used to cut off the question spoken immediately after it.
        }

        override fun onResults(results: Bundle?) {
            val transcript = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull(WakePhraseMatcher::matches)
            if (transcript != null) {
                onPhraseDetected(transcript)
                stopSelf()
            } else {
                restartSoon()
            }
        }
    }

    override fun onDestroy() {
        stopping = true
        handler.removeCallbacks(restart)
        recognizer?.destroy()
        recognizer = null
        listening = false
        super.onDestroy()
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
