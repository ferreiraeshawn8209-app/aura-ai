# Voice verification

Updated: 2026-11-05

## Automated checks (2026-11-05)

| Check | Result |
|---|---|
| App unit tests | 19 passed, including math, acknowledgement policy and response sanitization. |
| `DefaultAuraVoiceSubsystemTest` | 2 passed. Confirms selected-provider behavior and no silent provider switch. |
| Debug APK build | `:app:assembleDebug` passed; APK SHA-256 is recorded in `AURA_INTEGRATION_HANDOVER.md`. |

Combined: 19 app tests and 2 voice-library tests passed, 0 failures. The tests do not exercise Android microphone behavior, background wake, actual spoken acknowledgement, acoustic interruptions, or live cloud fallback.

## Manual device checks

Verified on the Honor 90 Lite:

- An earlier build from this same branch installed in place after backing up app data. Package, version and signer matched; pulled installed APK hash matched that earlier build. Launch was observed without a fatal exception.
- The latest APK also includes the spoken pre-action acknowledgement change and is not installed because ADB is disconnected.

Still pending on the physical phone (ADB is currently disconnected):

- Wake phrase recognition and opening Aura from the background.
- Speech capture/transcript in the active app.
- A complete spoken answer after an “I’m on it” acknowledgment.
- Multi-turn conversation and recovery after long pauses.
- Interrupting TTS and resuming recognition.
- Same voice after app restart, and Austrian-accent voice availability on the device.
- Local Ollama connectivity from the phone (PC endpoint itself responds and lists both Qwen models).
- Exact single “I'll be back” spoken before an actionable voice command, followed by the task executing once.

Do not mark pending checks as passing until tested on the actual phone. The user previously reported “I'm on it” followed by a misunderstanding. The direct chat path no longer exposes partial model output as a completed answer, but the real-phone voice flow still needs acceptance testing.
