# Android Integration

This module can be consumed by Aura's Android application without any Lovable
UI or web runtime.

## 1. Add the library module

Copy `lib/aura-voice` into the Android repository as a library module, or add
it to a Gradle composite. Use the host app's Kotlin and Android Gradle plugin
versions if they differ from the module's versions.

```kotlin
// settings.gradle.kts
include(":aura-voice")
project(":aura-voice").projectDir = file("../aura-voice")
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":aura-voice"))
}
```

The module targets Android API 26+. It has no network or UI dependency.

## 2. Add microphone permission

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

Request this permission at runtime before calling `startListening`. The module
does not request permissions or hide a denied permission. A denied permission
is returned as `AUDIO_PERMISSION_DENIED`.

## 3. Construct and scope the subsystem

Create one instance scoped to the Android feature/service that owns the
conversation, not one instance per Compose recomposition or button tap.

```kotlin
class AuraVoiceController(context: Context) {
    private val subsystem = DefaultAuraVoiceSubsystem(
        speechProviders = listOf(AndroidSpeechRecognizerProvider(context)),
        textToSpeechProviders = listOf(AndroidTextToSpeechProvider(context)),
        settingsStore = SharedPreferencesVoiceSettingsStore(context),
    )

    fun close() = subsystem.close()
}
```

Call `close()` from the owner when the subsystem is no longer needed.

## 4. Listen and hand off to Aura reasoning

```kotlin
val result = subsystem.startListening(
    config = SpeechInputConfig(
        languageTag = "en-ZA",
        partialResults = true,
        preferOffline = false,
    ),
    listener = object : SpeechRecognitionListener {
        override fun onResult(result: SpeechRecognitionResult) {
            renderTranscript(result.transcript, result.isFinal)
            result.error?.let(::renderVoiceError)

            if (result.isFinal && result.error == null && result.transcript.isNotBlank()) {
                // This is the handoff boundary. The voice module does not
                // select or call Aura's reasoning model.
                sendToAuraReasoning(result.transcript)
            }
        }
    },
)

if (!result.accepted) {
    renderVoiceError(requireNotNull(result.error))
}
```

If the reasoning layer needs to expose a visible processing state before it
receives a final event, it may call `subsystem.markProcessing()`.

## 5. Speak a reasoning response

```kotlin
val result = subsystem.speak(
    text = responseText,
    listener = object : SpeechSynthesisListener {
        override fun onStarted() = Unit
        override fun onCompleted() = Unit
        override fun onStopped() = Unit
        override fun onError(error: VoiceError) = renderVoiceError(error)
    },
)
```

## 6. Handle interruption / barge-in

The safest default is to stop TTS as soon as the host detects that the user
has started speaking:

```kotlin
subsystem.interrupt()
// or subsystem.stopSpeaking() when only synthesis should stop
```

Starting `startListening` while speaking also interrupts the active synthesis.
The host may use Android audio focus, a VAD layer, or a product-specific
barge-in signal. This module does not claim to provide wake-word detection or
telephony.

## 7. Render state

```kotlin
val subscription = subsystem.observeState(
    object : ConversationStateObserver {
        override fun onStateChanged(snapshot: ConversationStateSnapshot) {
            renderAuraVoiceState(snapshot.state, snapshot.error)
        }
    },
)

// subscription.close() when the screen/controller is destroyed
```

Do not infer state from button labels or audio callbacks. Use the structured
state snapshot.

## 8. Voice settings UI

```kotlin
subsystem.getVoices { voices ->
    renderVoiceChoices(voices)
}

subsystem.selectVoice("en-us-x-sfg#female_1-local") { result ->
    if (!result.accepted) renderVoiceError(requireNotNull(result.error))
}
```

To let the user change rate or pitch, persist a new `VoiceSettings` through
the host's settings flow or expose a small settings-store update operation
around the subsystem. These fields are independent from the AI model.

## 9. Cloud credentials

Do not put cloud provider keys in:

- Android source;
- `BuildConfig`;
- resources or assets;
- client-side HTTP headers;
- logs or crash messages.

For cloud providers, inject a `CloudSpeechTransport` or
`CloudTextToSpeechTransport` backed by Aura's authenticated server or a
managed integration. The Android app may hold a short-lived user/session
credential needed to call Aura's own backend, but the provider key belongs
behind that backend boundary.

## 10. Lifecycle and production checklist

- Request and explain `RECORD_AUDIO` permission.
- Decide whether listening is allowed while the app is backgrounded.
- Keep one subsystem instance per conversation owner.
- Observe and display `ERROR` states with recovery actions.
- Stop listening and speaking when the owner is destroyed.
- Test installed Android recognition/TTS services, offline mode, missing
  language packs, denied permissions, airplane mode, and barge-in.
- Avoid logging raw transcripts/audio unless the user has explicitly opted in.