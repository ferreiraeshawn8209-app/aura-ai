# Aura project status

Updated: 2026-11-05
Branch: `integration/aura-voice-consolidation`

## Current delivery

- Integrated the reusable `Aura-Voice-Subsystem` Android library into the existing Kotlin app and wired its recognizer/speech providers into chat and settings.
- Added a continuous in-app listening session: one start, automatic listen after each response, stop button, and tap-to-interrupt while speaking.
- Persisted the selected Android TTS voice ID and exposed installed voices with a masculine/feminine presentation label and preview.
- Changed the wake phrase to “Yo, yo, yo” and connected wake events to the chat voice session.
- Repaired existing Android compilation problems in dependency injection, Room migration, tool types, transcript parsing, and resources. Reduced HTTP logging to headers and redact authorization values.
- The selected TTS voice is kept fixed; no automatic alternate voice is selected. The desired Austrian English voice is not bundled: it will appear only if an installed Android TTS engine exposes it. No ElevenLabs voice or “voice 3” identity is connected in this build.

## Verification state (2026-11-05)

- Latest `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` passed: 19 app tests and 2 voice-library tests, 0 failures.
- Final APK from this checkout is package `com.aura.ai`, version `1.0.0` / code `1`, SHA-256 `49CA00A7945EE1F1B0C82B58385C3F325E7292E9B5092DF630C25FFAF56A1EFD`. The earlier build (`BE2AFB...`) was installed in place; the final rebuild includes the last no-partial-reply correction and is not yet installed.
- Same-signature in-place update of the earlier build to the Honor succeeded; app data was backed up and not cleared. That installed APK's pulled hash matched `BE2AFB...`, and app launch was observed without a fatal exception. The phone then disconnected from ADB; final APK installation and live tests are pending reconnection.
- Local PC Ollama `GET http://192.168.8.18:11434/api/tags` returned HTTP 200 and listed `qwen3:1.7b` and `qwen3:4b-q4_K_M`; a direct Qwen 1.7B chat request returned `LOCAL_QWEN_OK` (cold start 41.8 seconds). The Honor went offline from ADB before phone-originated Ollama and live speech checks; those remain pending.
- The active speech recognizer now requests `en-ZA`; partial/final transcripts, confidence where provided, and recognition failure context are shown in chat diagnostics. Simple spoken arithmetic has a deterministic tested path. The mic button enters listening immediately; ordinary partial LLM output is withheld so a failed response cannot leave a spoken “I'm on it” as the apparent final answer.
- The currently persisted TTS selection in the pre-update app data was Android voice ID `ar-language` (Arabic), not an Austrian voice. This build has only Android TTS wired; no authorized Austrian voice provider/voice ID is configured. The selected voice was preserved rather than silently replaced.
- Cloud-first reasoning, automatic local/cloud fallback/recovery, and backend health routing are not implemented. `AgentOrchestrator` still calls `OllamaRepository` directly; no cloud provider endpoint/credential/usage cap is configured in this checkout.
- The current standby recognizer is Android `SpeechRecognizer`, not a dedicated on-device wake-word engine. Barge-in is button-controlled; acoustic interruption/VAD and OS-generated microphone sounds are not verified or controlled by the app.

## Latest voice acknowledgement change

- Spoken requests classified as actions now wait for exactly one `I'll be back` spoken through the currently selected TTS provider before tools execute. Typed requests and ordinary spoken questions do not receive the acknowledgement.
- Final model text is sanitized to remove leading duplicate or legacy acknowledgements, including “I'm on it.” Voice settings are unchanged.
- Automated policy/sanitizer tests pass. The rebuilt APK has not been installed or voice-tested because the Honor is not connected to ADB. The branch still has no cloud fallback, and the saved Android voice ID `ar-language` is not the requested Austrian voice.

## Repository integration

The eight-repository inventory and compatibility review is recorded in `AURA_INTEGRATION_HANDOVER.md`. `Aura-Voice-Subsystem` is the compatible native library; the other reviewed projects are separate web/backend or singing-voice products and are not direct Android modules. The separately promised eight-repository audit attachment has not arrived in this session.

## Guardrails

This work is confined to the integration branch. The user approved and received an in-place phone update after data backup and package/signature verification. No merge, production deployment, or replacement of the main checkout has occurred.


## Spoken-action gate continuation — 2026-11-05

- Added a tested gate to the existing agent execution flow: one `I'll be back` acknowledgement must finish before actions execute; if the selected voice cannot complete it, the action is not run. Ordinary questions bypass the acknowledgement.
- `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` succeeded (75 tasks up-to-date). Current XML reports: 22 tests, 0 failures/errors/skips, 8 suites. Existing APK package/version/signer verified by metadata and signature check; SHA-256 `5FAF6738A7DA194EF3274813552D6E557D7F723D8EB51AD8CCC48ECEB3123B89`. No install.
- Phone remains disconnected; no device voice/Ollama/cloud/WhatsApp test. Cloud fallback remains missing; Austrian voice is not configured. Personal `device-backups/` is excluded from source control.


## Fresh automated rerun — 2026-11-05

- Forced `:app:testDebugUnitTest :aura-voice:testDebugUnitTest --rerun-tasks`: passed in 10m02s; 50 tasks executed. Reports: 22 tests passed, 0 failures/errors/skips across 8 suites. The separate app assemble task succeeded earlier but was up-to-date. No phone install or device acceptance.
