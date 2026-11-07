package com.aura.ai.voice.tts

import com.aura.voice.CloudSynthesisRequest
import com.aura.voice.VoiceProviderType
import com.aura.voice.VoiceSettings
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ElevenLabsSpeechTransportTest {
    @Test
    fun loadsVoiceNamesGenderAndAccentWithoutLosingTheVoiceId() {
        val server = MockWebServer()
        server.enqueue(
            MockResponse().setBody(
                """{"voices":[{"voice_id":"voice-3","name":"Austrian Baritone","category":"professional","labels":{"gender":"male","accent":"Austrian","language":"English"}}],"has_more":false}""",
            ),
        )
        server.start()
        val transport = ElevenLabsSpeechTransport(apiKey = { "test-key" }, baseUrl = server.url("/").toString())
        try {
            val result = AtomicReference<List<com.aura.voice.VoiceDescriptor>>()
            val complete = CountDownLatch(1)
            transport.getVoices { result.set(it); complete.countDown() }

            assertTrue(complete.await(5, TimeUnit.SECONDS))
            val voice = result.get().single()
            assertEquals("elevenlabs:voice-3", voice.id)
            assertEquals("Male", voice.metadata["gender"])
            assertEquals("Austrian", voice.metadata["accent"])
            assertEquals("en-AT", voice.languageTag)
            assertEquals("test-key", server.takeRequest().getHeader("xi-api-key"))
        } finally {
            transport.close()
            server.shutdown()
        }
    }

    @Test
    fun synthesizesWithThePersistedVoiceAndRestrictedKeyHeader() {
        val server = MockWebServer()
        val audio = "fake-mp3-data".toByteArray()
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "audio/mpeg")
                .setBody(Buffer().write(audio)),
        )
        server.start()
        val transport = ElevenLabsSpeechTransport(apiKey = { "secret-test-key" }, baseUrl = server.url("/").toString())
        try {
            val result = AtomicReference<Result<ByteArray>>()
            val complete = CountDownLatch(1)
            transport.synthesize(
                CloudSynthesisRequest(
                    text = "All systems operational.",
                    settings = VoiceSettings(
                        provider = VoiceProviderType.CLOUD,
                        providerId = ElevenLabsSpeechTransport.PROVIDER_ID,
                        voiceId = "elevenlabs:voice-3",
                        styleExaggeration = 0.7f,
                    ),
                ),
            ) { result.set(it); complete.countDown() }

            assertTrue(complete.await(5, TimeUnit.SECONDS))
            assertTrue(result.get().getOrThrow().contentEquals(audio))
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/v1/text-to-speech/voice-3?output_format=mp3_44100_128", request.path)
            assertEquals("secret-test-key", request.getHeader("xi-api-key"))
            val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
            assertEquals("All systems operational.", body.getValue("text").jsonPrimitive.contentOrNull)
            assertEquals("eleven_multilingual_v2", body.getValue("model_id").jsonPrimitive.contentOrNull)
            assertEquals(0.7f, body.getValue("voice_settings").jsonObject.getValue("style").jsonPrimitive.content.toFloat(), 0.0001f)
            assertFalse(request.path!!.contains("secret-test-key"))
        } finally {
            transport.close()
            server.shutdown()
        }
    }

    @Test
    fun doesNotSendRequestWithoutSavedKeyAndRedactsProviderErrorBody() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(401).setBody("secret-test-key rejected"))
        server.start()
        var key: String? = null
        val transport = ElevenLabsSpeechTransport(apiKey = { key }, baseUrl = server.url("/").toString())
        try {
            assertFalse(transport.isAvailable)
            val missingResult = AtomicReference<Result<ByteArray>>()
            val missingDone = CountDownLatch(1)
            transport.synthesize(
                CloudSynthesisRequest("Hello", VoiceSettings(voiceId = "elevenlabs:voice-3")),
            ) { missingResult.set(it); missingDone.countDown() }
            assertTrue(missingDone.await(2, TimeUnit.SECONDS))
            assertTrue(runCatching { missingResult.get().getOrThrow() }.isFailure)
            assertEquals(0, server.requestCount)

            key = "secret-test-key"
            val errorResult = AtomicReference<Result<ByteArray>>()
            val errorDone = CountDownLatch(1)
            transport.synthesize(
                CloudSynthesisRequest(
                    "Hello",
                    VoiceSettings(provider = VoiceProviderType.CLOUD, voiceId = "elevenlabs:voice-3"),
                ),
            ) { errorResult.set(it); errorDone.countDown() }
            assertTrue(errorDone.await(5, TimeUnit.SECONDS))
            val error = runCatching { errorResult.get().getOrThrow() }.exceptionOrNull()
            assertTrue(error?.message?.contains("401") == true)
            assertFalse(error?.message?.contains("secret-test-key") == true)
        } finally {
            transport.close()
            server.shutdown()
        }
    }
}

