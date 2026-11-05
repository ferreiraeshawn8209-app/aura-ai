# Voice Providers

## Android SpeechRecognizer

Implemented by `AndroidSpeechRecognizerProvider`.

- Uses the device's installed Android recognition service.
- Produces interim and final transcripts.
- Returns confidence when Android provides it.
- May require `android.permission.RECORD_AUDIO`.
- May require network access depending on the installed recognition service and
  `SpeechInputConfig.preferOffline`.
- Does not require a provider API key in Aura.

The Android adapter reports device/service errors as `VoiceError`; it does not
invent a transcript when no match is returned.

## Android TextToSpeech

Implemented by `AndroidTextToSpeechProvider`.

- Uses the device's installed TTS engine and installed voice catalog.
- Exposes voice ids, language, network requirement, quality, and latency
  metadata where Android provides them.
- Applies speaking rate and pitch within Android's supported range.
- Does not require a provider API key.

Installed voices can differ by device image and user-installed TTS engine.

## Cloud speech recognition

`CloudSpeechRecognitionProvider` is the provider implementation. The host
injects `CloudSpeechTransport`, which is responsible for the actual provider
SDK or HTTPS implementation.

Required transport responsibilities:

- send `SpeechInputConfig` and microphone audio through an authenticated
  channel;
- emit interim/final `SpeechRecognitionResult` values;
- map provider failures to `VoiceError`;
- close streaming sessions promptly on `stopListening`;
- keep provider secrets on a trusted server or managed secret store.

No cloud vendor is hardcoded into this module. The transport may target a
vendor such as Google Cloud Speech-to-Text, Azure Speech, Amazon Transcribe,
or a self-hosted recognizer, but the vendor's credentials and SDK belong in
the transport/backend layer.

## Cloud TTS

`CloudTextToSpeechProvider` is the provider implementation. It injects:

- `CloudTextToSpeechTransport` for voice catalog and synthesis;
- `CloudAudioPlayer` for playback on Android.

Required transport responsibilities:

- list voice descriptors;
- send text, language, voice id, rate, and pitch to the provider;
- return audio bytes or a structured failure;
- use server-side credentials or a managed connection;
- avoid logging text or audio that the product considers sensitive.

The module does not pretend that bytes are speech. The transport must return
real provider audio, and the audio player must report actual playback events.

## Local/future providers

Implement the existing provider interfaces with `type = LOCAL` or
`type = CUSTOM`. A local recognizer may honor `preferOffline`; a local TTS
provider can expose a local catalog. No core changes are required as long as
the provider reports honest availability and structured failures.

## Provider selection

`VoiceSettings.provider` is a configuration preference. The subsystem chooses a
matching available provider id first, then another provider of the requested
type, then another available provider only when the preferred provider is not
available. Applications that require strict provider pinning should provide
only the allowed provider in the constructor or add a policy provider wrapper.