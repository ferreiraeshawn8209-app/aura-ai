# Aura Voice Architecture

## Boundaries

```text
Android Aura application
  ├─ owns UI, lifecycle, permissions, and AI/reasoning
  ├─ calls AuraVoiceSubsystem
  └─ optionally supplies cloud transports

Aura voice core
  ├─ provider-neutral data models
  ├─ DefaultAuraVoiceSubsystem
  ├─ state machine and generation guards
  ├─ voice settings persistence contract
  └─ provider selection

Provider adapters
  ├─ Android SpeechRecognizer
  ├─ Android TextToSpeech
  ├─ injected cloud speech transport
  ├─ injected cloud TTS transport + audio player
  └─ future local/custom providers
```

The core has no dependency on an Android Activity, Fragment, Compose UI, HTTP
client, database, or AI SDK. Android-specific code is limited to the adapter
package and the `SharedPreferencesVoiceSettingsStore`.

## Event and state flow

```text
startListening
  -> LISTENING
  -> interim SpeechRecognitionResult* (still LISTENING)
  -> final SpeechRecognitionResult (PROCESSING)
  -> app reasons outside this module
  -> speak
  -> SPEAKING
  -> completed -> IDLE

SPEAKING + user starts listening
  -> stop current synthesis
  -> INTERRUPTED
  -> IDLE
  -> LISTENING
```

Provider callbacks can arrive after an operation has been cancelled. The core
uses monotonically increasing recognition and synthesis generations to ignore
stale callbacks. This prevents an old cloud response or Android callback from
moving a newer operation back to `ERROR`, `IDLE`, or `SPEAKING`.

## Ownership rules

### Core owns

- the public API;
- provider availability and selection;
- configured provider id selection when several providers share a type;
- state transitions;
- stale callback suppression;
- persisted voice settings;
- stopping recognition before synthesis and stopping synthesis on barge-in.

### The Android application owns

- `RECORD_AUDIO` permission and runtime permission UX;
- foreground/background policy;
- microphone indicators;
- lifecycle scoping and calling `close()`;
- sending final transcripts to Aura's reasoning system;
- detecting a user interruption if using a custom wake-word/VAD layer;
- UI presentation.

### A cloud transport owns

- authenticated server communication;
- request serialization and streaming;
- token refresh through the host's secure integration;
- provider-specific response parsing;
- audio format selection for the injected player.

The transport must never put a provider key in an APK. A recommended design is
Android -> Aura backend -> provider, with short-lived app authorization on the
Android side.

## Personality separation

`AuraVoiceProfile` contains `personalityId` plus `VoiceSettings`, but no
reasoning-model identifier. Personality text and AI model configuration belong
to the host application. A voice change therefore only updates the voice
settings store and does not invalidate an AI conversation or model selection.

## Threading

The core serializes its own state with a monitor. Providers may be asynchronous.
Host UI updates should be marshalled to the UI scope by the host app if its
observer is not already on the main thread. The built-in Android providers use
the Android main looper for platform calls.

## Failure policy

No provider is silently replaced with fake behavior. If no selected provider is
available, the operation is rejected with a structured `VoiceError` and state
becomes `ERROR`. The host can choose another configured provider, request
permissions, or show a recovery action.