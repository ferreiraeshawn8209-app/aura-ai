# Voice verification

Updated: 2026-11-05

## Automated checks (2026-11-05)

| Check | Result |
|---|---|
| Existing app tests | 14 passed. |
| `SimpleMathIntentTest` | 2 passed; wake phrase plus spoken “five plus five” returns `10`. |
| `DefaultAuraVoiceSubsystemTest` | 2 passed. Confirms selected-provider behavior and no silent provider switch. |
| Debug APK build | `:app:assembleDebug` passed; APK SHA-256 is recorded in `AURA_INTEGRATION_HANDOVER.md`. |

Combined: 16 app tests and 2 voice-library tests passed, 0 failures. The tests do not exercise Android microphone behavior, background wake, spoken TTS, acoustic interruptions, or live cloud fallback.

## Manual device checks

Verified on the Honor 90 Lite:

- An earlier build from this same branch installed in place after backing up app data. Package, version and signer matched; pulled installed APK hash matched that earlier build. Launch was observed without a fatal exception.
- The final APK adds the no-partial-response correction after that install and is not yet installed because ADB disconnected.

Still pending on the physical phone (ADB disconnected before speech testing):

- Wake phrase recognition and opening Aura from the background.
- Speech capture/transcript in the active app.
- A complete spoken answer after an “I’m on it” acknowledgment.
- Multi-turn conversation and recovery after long pauses.
- Interrupting TTS and resuming recognition.
- Same voice after app restart, and Austrian-accent voice availability on the device.
- Local Ollama connectivity from the phone (PC endpoint itself responds and lists both Qwen models).

Do not mark pending checks as passing until tested on the actual phone. The user previously reported “I'm on it” followed by a misunderstanding. The direct chat path no longer exposes partial model output as a completed answer, but the real-phone voice flow still needs acceptance testing.
