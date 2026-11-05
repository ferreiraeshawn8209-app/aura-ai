# Aura Voice API

Package: `com.aura.voice`

The API uses callbacks so the host Android application can use its existing
coroutine, lifecycle, and networking choices. Provider implementations must
deliver callbacks on a thread safe for the host app; the built-in Android
adapters deliver platform callbacks on Android's main thread.

## `AuraVoiceSubsystem`

```kotlin
interface AuraVoiceSubsystem : Closeable {
    fun startListening(
        config: SpeechInputConfig = SpeechInputConfig(),
        listener: SpeechRecognitionListener,
    ): VoiceOperationResult

    fun stopListening(): VoiceOperationResult
    fun speak(text: String, listener: SpeechSynthesisListener): VoiceOperationResult
    fun stopSpeaking(): VoiceOperationResult
    fun getVoices(callback: (List<VoiceDescriptor>) -> Unit)
    fun selectVoice(voiceId: String, callback: (VoiceOperationResult) -> Unit = {})
    fun conversationState(): ConversationStateSnapshot
    fun observeState(observer: ConversationStateObserver): Closeable
    fun voiceSettings(): VoiceSettings
    fun updateVoiceSettings(settings: VoiceSettings): VoiceOperationResult
    fun markProcessing(): VoiceOperationResult
    fun interrupt(): VoiceOperationResult
}
```

### `startListening`

Starts the selected available `SpeechRecognitionProvider`.

- Interim results have `isFinal = false`.
- The final result has `isFinal = true`.
- `confidence` may be `null` when the provider does not report confidence.
- `languageTag` may be `null` when the provider does not report the detected
  language.
- Provider failures arrive as a result with a non-null `error`.
- The subsystem moves to `PROCESSING` after a final result. The app's
  reasoning layer remains outside this module.

`SpeechInputConfig.preferOffline` is a provider hint, not a guarantee of local
processing.

### `speak`

Speaks non-blank text using the provider matching persisted voice settings.
The subsystem moves to `SPEAKING` until completion, error, or interruption.
Starting a new listen operation stops current synthesis first.

### `stopSpeaking` and `interrupt`

Both stop current audio. The subsystem emits `INTERRUPTED` and then `IDLE`.
`interrupt` also stops recognition if it is active. Use it when the product
detects a barge-in or when the user taps a stop control.

### `getVoices` and `selectVoice`

Voice enumeration is asynchronous because cloud and some local providers load
catalogs asynchronously. `getVoices` aggregates the catalogs of available
providers. `selectVoice` validates the id against those catalogs, then persists:

- provider
- provider id (when more than one provider of a type is configured)
- voice id
- language
- speaking rate
- pitch

The selected voice does not alter, reload, or replace the AI reasoning model.

`updateVoiceSettings` persists language, speaking rate, pitch, provider, and
voice selection without changing the reasoning model.

### Conversation states

| State | Meaning |
| --- | --- |
| `IDLE` | No active recognition or synthesis. |
| `LISTENING` | Recognition is active or an interim transcript is arriving. |
| `PROCESSING` | A final transcript is ready for Aura's reasoning layer. |
| `SPEAKING` | TTS is producing or playing the response. |
| `ERROR` | The last operation failed; inspect `ConversationStateSnapshot.error`. |
| `INTERRUPTED` | A stop/barge-in was received. The subsystem immediately returns to `IDLE`. |

## Provider contracts

Implement `SpeechRecognitionProvider` for any recognizer and
`TextToSpeechProvider` for any synthesizer. Providers must not know about
Aura's reasoning model or personality. The subsystem owns provider selection
and state transitions.

## Error behavior

Errors are structured with `VoiceErrorCode`, a human-readable message, and a
recoverability hint. Callers should use the code for product behavior and the
message for diagnostics/UI. Do not display provider exception details that
could contain credentials or request headers.