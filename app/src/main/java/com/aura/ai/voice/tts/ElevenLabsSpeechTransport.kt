package com.aura.ai.voice.tts

import com.aura.voice.CloudSpeechRecognitionProvider
import com.aura.voice.CloudSynthesisRequest
import com.aura.voice.CloudTextToSpeechTransport
import com.aura.voice.VoiceDescriptor
import com.aura.voice.VoiceProviderType
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * ElevenLabs TTS transport. The API key is supplied at request time from
 * encrypted app storage; it is never embedded in source, URLs, or logs.
 */
class ElevenLabsSpeechTransport(
    private val apiKey: () -> String?,
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build(),
) : CloudTextToSpeechTransport {
    private val calls = ConcurrentHashMap.newKeySet<Call>()
    @Volatile private var closed = false

    override val isAvailable: Boolean
        get() = !closed && !apiKey().isNullOrBlank()

    override fun getVoices(callback: (List<VoiceDescriptor>) -> Unit) {
        val key = apiKey()?.trim()?.takeIf(String::isNotEmpty)
        if (closed || key == null) {
            callback(emptyList())
            return
        }
        loadVoicePage(key, null, mutableListOf(), 0, callback)
    }

    private fun loadVoicePage(
        key: String,
        pageToken: String?,
        collected: MutableList<VoiceDescriptor>,
        page: Int,
        callback: (List<VoiceDescriptor>) -> Unit,
    ) {
        if (closed || page >= MAX_VOICE_PAGES) {
            callback(collected.toList())
            return
        }
        val url = endpoint("v2", "voices").newBuilder()
            .addQueryParameter("page_size", "100")
            .apply { pageToken?.let { addQueryParameter("next_page_token", it) } }
            .build()
        val request = Request.Builder()
            .url(url)
            .header(API_KEY_HEADER, key)
            .get()
            .build()
        enqueue(request, onFailure = { callback(collected.toList()) }) { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                callback(collected.toList())
                return@enqueue
            }
            val root = runCatching { Json.parseToJsonElement(body).jsonObject }.getOrNull()
            val voices = runCatching { root?.get("voices")?.jsonArray }.getOrNull()
            if (voices != null) {
                for (voiceElement in voices) {
                    val voice = runCatching { voiceElement.jsonObject }.getOrNull() ?: continue
                    val voiceId = voice["voice_id"]?.jsonPrimitive?.contentOrNull
                        ?.takeIf(String::isNotBlank) ?: continue
                    val labels = runCatching { voice["labels"]?.jsonObject }.getOrNull() ?: JsonObject(emptyMap())
                    val gender = labels["gender"]?.jsonPrimitive?.contentOrNull.orEmpty().normalGender()
                    val accent = labels["accent"]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)
                    val language = labels["language"]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)
                    collected += VoiceDescriptor(
                        id = "$PROVIDER_ID:$voiceId",
                        name = voice["name"]?.jsonPrimitive?.contentOrNull
                            ?.ifBlank { "ElevenLabs voice" } ?: "ElevenLabs voice",
                        provider = VoiceProviderType.CLOUD,
                        languageTag = language.toLanguageTag(accent),
                        providerId = PROVIDER_ID,
                        isNetworkRequired = true,
                        supportsPitch = false,
                        metadata = buildMap {
                            put("gender", gender)
                            accent?.let { put("accent", it) }
                            voice["category"]?.jsonPrimitive?.contentOrNull
                                ?.takeIf(String::isNotBlank)?.let { put("category", it) }
                        },
                    )
                }
            }
            val next = root?.get("next_page_token")?.jsonPrimitive?.contentOrNull
                ?.takeIf { it.isNotBlank() }
            if (root?.get("has_more")?.jsonPrimitive?.booleanOrNull == true && next != null) {
                loadVoicePage(key, next, collected, page + 1, callback)
            } else {
                callback(collected.toList())
            }
        }
    }

    override fun synthesize(request: CloudSynthesisRequest, callback: (Result<ByteArray>) -> Unit) {
        val key = apiKey()?.trim()?.takeIf(String::isNotEmpty)
        val voiceId = request.settings.voiceId
            ?.takeIf { it.startsWith("$PROVIDER_ID:") }
            ?.removePrefix("$PROVIDER_ID:")
            ?.takeIf(String::isNotBlank)
        if (closed || key == null || voiceId == null) {
            callback(Result.failure(IllegalStateException("ElevenLabs voice is not configured.")))
            return
        }
        val json = buildJsonObject {
            put("text", request.text)
            put("model_id", MULTILINGUAL_MODEL)
            put(
                "voice_settings",
                buildJsonObject {
                    put("stability", 0.5)
                    put("similarity_boost", 0.8)
                    put("style", request.settings.normalized().styleExaggeration)
                    put("use_speaker_boost", true)
                },
            )
        }.toString()
        val url = endpoint("v1", "text-to-speech", voiceId).newBuilder()
            .addQueryParameter("output_format", OUTPUT_FORMAT)
            .build()
        val requestBody = json.toRequestBody(JSON_MEDIA_TYPE)
        val httpRequest = Request.Builder()
            .url(url)
            .header(API_KEY_HEADER, key)
            .header("Accept", "audio/mpeg")
            .post(requestBody)
            .build()
        enqueue(
            httpRequest,
            onFailure = { callback(Result.failure(IOException("ElevenLabs network request failed."))) },
        ) { response ->
            if (!response.isSuccessful) {
                callback(Result.failure(IOException("ElevenLabs TTS returned HTTP ${response.code}.")))
                return@enqueue
            }
            val audio = response.body?.bytes()
            if (audio == null || audio.isEmpty()) {
                callback(Result.failure(IOException("ElevenLabs returned an empty audio response.")))
            } else {
                callback(Result.success(audio))
            }
        }
    }

    private fun endpoint(vararg segments: String): HttpUrl {
        val root = baseUrl.toHttpUrlOrNullCompat()
            ?: error("ElevenLabs base URL is invalid.")
        return root.newBuilder().apply { segments.forEach(::addPathSegment) }.build()
    }

    private fun enqueue(request: Request, onFailure: () -> Unit, consume: (Response) -> Unit) {
        val call = client.newCall(request)
        if (closed) {
            call.cancel()
            onFailure()
            return
        }
        calls += call
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                calls -= call
                if (!closed) onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                calls -= call
                response.use {
                    if (!closed) consume(it)
                }
            }
        })
    }

    override fun close() {
        closed = true
        calls.forEach(Call::cancel)
        calls.clear()
    }

    private fun String.normalGender(): String = when (lowercase()) {
        "male", "man" -> "Male"
        "female", "woman" -> "Female"
        else -> "Voice"
    }

    private fun String?.toLanguageTag(accent: String?): String {
        val language = this.orEmpty().lowercase()
        val accentText = accent.orEmpty().lowercase()
        return when {
            language.startsWith("de") -> "de-AT"
            "austrian" in accentText -> "en-AT"
            language.startsWith("en") -> "en"
            else -> "en"
        }
    }

    private fun String.toHttpUrlOrNullCompat(): HttpUrl? = runCatching { toHttpUrl() }.getOrNull()

    companion object {
        const val PROVIDER_ID = "elevenlabs"
        const val API_KEY_SECRET = "elevenlabs_api_key"
        const val API_KEY_HEADER = "xi-api-key"
        const val MULTILINGUAL_MODEL = "eleven_multilingual_v2"
        const val OUTPUT_FORMAT = "mp3_44100_128"
        const val DEFAULT_BASE_URL = "https://api.elevenlabs.io/"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val MAX_VOICE_PAGES = 10
    }
}
