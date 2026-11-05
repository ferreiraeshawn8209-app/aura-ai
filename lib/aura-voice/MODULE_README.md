# Aura Module C — Voice and Conversation System

This directory contains Aura's reusable voice subsystem. It is an Android
library source module, not an Android application and not a phone-telephony
implementation.

## What is included

- Provider-neutral Kotlin contracts for speech recognition and text to speech.
- `DefaultAuraVoiceSubsystem`, which coordinates recognition, synthesis,
  interruption, persistence, and the `IDLE`/`LISTENING`/`PROCESSING`/
  `SPEAKING`/`ERROR`/`INTERRUPTED` lifecycle.
- Native Android adapters for `SpeechRecognizer` and `TextToSpeech`.
- Cloud provider adapters that accept an injected transport. They do not make
  network calls or contain credentials.
- A `SharedPreferencesVoiceSettingsStore` that keeps voice configuration
  separate from Aura's reasoning model and personality logic.
- Android consumption guidance in `ANDROID_INTEGRATION.md`.

## Important non-goals

This module does not:

- fake transcripts, audio, or generated speech;
- choose or implement Aura's AI/reasoning model;
- expose a provider API key to the client;
- provide phone calls, telephony, or a chat UI.

## Source layout

```text
src/main/kotlin/com/aura/voice/
  VoiceModels.kt
  VoiceProviders.kt
  DefaultAuraVoiceSubsystem.kt
  CloudProviders.kt
  android/
    AndroidSpeechRecognizerProvider.kt
    AndroidTextToSpeechProvider.kt
    SharedPreferencesVoiceSettingsStore.kt
```

The module targets Android API 26+ and uses the host app's Kotlin/Android
Gradle plugin versions. If this repo is not used as a Gradle composite, copy
the `lib/aura-voice` directory into the Aura Android repository as a library
module and follow `ANDROID_INTEGRATION.md`.

## Quick construction

```kotlin
val speech = AndroidSpeechRecognizerProvider(context)
val tts = AndroidTextToSpeechProvider(context)
val settings = SharedPreferencesVoiceSettingsStore(context)

val voice = DefaultAuraVoiceSubsystem(
    speechProviders = listOf(speech),
    textToSpeechProviders = listOf(tts),
    settingsStore = settings,
)
```

The app owns the reasoning loop. The usual sequence is:

1. `startListening(...)`
2. receive interim/final `SpeechRecognitionResult` values
3. hand a final transcript to Aura's reasoning layer
4. call `speak(responseText, ...)`
5. call `stopSpeaking()` or `interrupt()` if the user begins speaking

See `API.md` for the complete contract and `ARCHITECTURE.md` for lifecycle
and ownership details.