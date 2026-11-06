package com.aura.ai.voice.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.aura.voice.CloudAudioPlayer
import com.aura.voice.VoiceError
import com.aura.voice.VoiceErrorCode
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/** Plays short-lived cloud TTS audio from app-private cache and deletes it afterwards. */
class AndroidCloudAudioPlayer(context: Context) : CloudAudioPlayer, Closeable {
    private val cacheDir = context.applicationContext.cacheDir
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lock = Any()
    private var active: Playback? = null
    @Volatile private var closed = false

    override fun play(
        audio: ByteArray,
        onStarted: () -> Unit,
        onCompleted: () -> Unit,
        onStopped: () -> Unit,
        onError: (VoiceError) -> Unit,
    ): Closeable {
        if (closed) {
            onError(VoiceError(VoiceErrorCode.PROVIDER_UNAVAILABLE, "Voice playback is closed."))
            return Closeable { }
        }
        val file = File(cacheDir, "aura-tts-${UUID.randomUUID()}.mp3")
        try {
            file.writeBytes(audio)
        } catch (_: IOException) {
            file.delete()
            onError(VoiceError(VoiceErrorCode.AUDIO_UNAVAILABLE, "Could not prepare voice audio."))
            return Closeable { }
        }
        val playback = Playback(file, onStarted, onCompleted, onStopped, onError)
        synchronized(lock) {
            active?.stop()
            active = playback
        }
        playback.prepare()
        return playback
    }

    override fun stop() {
        synchronized(lock) { active }?.stop()
    }

    override fun close() {
        closed = true
        stop()
    }

    private inner class Playback(
        private val file: File,
        private val onStarted: () -> Unit,
        private val onCompleted: () -> Unit,
        private val onStopped: () -> Unit,
        private val onError: (VoiceError) -> Unit,
    ) : Closeable {
        private val ended = AtomicBoolean(false)
        private var player: MediaPlayer? = null

        fun prepare() {
            mainHandler.post {
                if (ended.get() || closed) {
                    finish(PlaybackEnd.STOPPED)
                    return@post
                }
                try {
                    val mediaPlayer = MediaPlayer()
                    player = mediaPlayer
                    mediaPlayer.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                    )
                    mediaPlayer.setOnPreparedListener { prepared ->
                        if (ended.get()) {
                            finish(PlaybackEnd.STOPPED)
                        } else {
                            runCatching { prepared.start() }
                                .onSuccess { onStarted() }
                                .onFailure { finish(PlaybackEnd.ERROR) }
                        }
                    }
                    mediaPlayer.setOnCompletionListener { finish(PlaybackEnd.COMPLETED) }
                    mediaPlayer.setOnErrorListener { _, _, _ ->
                        finish(PlaybackEnd.ERROR)
                        true
                    }
                    mediaPlayer.setDataSource(file.absolutePath)
                    mediaPlayer.prepareAsync()
                } catch (_: Exception) {
                    finish(PlaybackEnd.ERROR)
                }
            }
        }

        fun stop() = finish(PlaybackEnd.STOPPED)

        override fun close() = stop()

        private fun finish(end: PlaybackEnd) {
            if (!ended.compareAndSet(false, true)) return
            val release = {
                player?.let { media ->
                    if (end == PlaybackEnd.STOPPED) runCatching { media.stop() }
                    runCatching { media.reset() }
                    media.release()
                }
                player = null
                file.delete()
                synchronized(lock) { if (active === this) active = null }
                when (end) {
                    PlaybackEnd.COMPLETED -> onCompleted()
                    PlaybackEnd.STOPPED -> onStopped()
                    PlaybackEnd.ERROR -> onError(
                        VoiceError(VoiceErrorCode.AUDIO_UNAVAILABLE, "Could not play the selected cloud voice."),
                    )
                }
            }
            if (Looper.myLooper() == Looper.getMainLooper()) release() else mainHandler.post(release)
        }
    }

    private enum class PlaybackEnd { COMPLETED, STOPPED, ERROR }
}
