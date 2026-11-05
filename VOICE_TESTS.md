# Voice verification

Updated: 2026-11-05

## Automated checks

| Check | Result |
|---|---|
| `ActionParserTest` | 7 passed. |
| `OkHttpRedactionTest` | 1 passed. |
| `OllamaModelsTest` | 2 passed. |
| `DefaultAuraVoiceSubsystemTest` | 2 passed. Confirms selected provider behavior and no silent provider switch. |
| Debug APK build | `:app:assembleDebug` passed; APK SHA-256 is recorded in `AURA_INTEGRATION_HANDOVER.md`. |

Combined: 12 unit tests passed, 0 failures. The tests cover parser/provider models/redaction/voice selection, not Android microphone behavior or a live Ollama request.

## Manual device checks

Not yet verified on a physical phone in this integration worktree:

- Wake phrase recognition and opening Aura from the background.
- Speech capture/transcript in the active app.
- A complete spoken answer after an “I’m on it” acknowledgment.
- Multi-turn conversation and recovery after long pauses.
- Interrupting TTS and resuming recognition.
- Same voice after app restart, and Austrian-accent voice availability on the device.
- Local Ollama connectivity from the phone.

Do not mark these as passing until tested on the actual phone. The user previously reported that Aura sometimes only says “I’m on it,” and later says it did not understand; this remains an unresolved device-level issue.
