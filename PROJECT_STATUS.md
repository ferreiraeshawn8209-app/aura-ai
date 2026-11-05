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

## Verification state

- Latest full check passed: `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` (12 tests, 0 failures).
- Installable debug APK: `app/build/outputs/apk/debug/app-debug.apk`; SHA-256 is recorded in `AURA_INTEGRATION_HANDOVER.md`.
- Real-device acceptance tests have not been performed in this worktree. A connected Honor was detected previously, but installation would replace the app currently on the phone; no deployment approval was given.
- Cloud-first reasoning, automatic local fallback/recovery, and backend health routing are not implemented. `AgentOrchestrator` currently calls `OllamaRepository` directly. No cloud provider endpoint, credential, usage cap, or fallback can be verified from this checkout.
- Acoustic barge-in / voice activity detection is not implemented. The current app supports button-based interruption. The Android platform standby recognizer is not a production-grade always-on on-device wake-word engine.

## Repository integration

The eight-repository inventory and compatibility review is recorded in `AURA_INTEGRATION_HANDOVER.md`. `Aura-Voice-Subsystem` is the compatible native library; the other reviewed projects are separate web/backend or singing-voice products and are not direct Android modules. The separately promised eight-repository audit attachment has not arrived in this session.

## Guardrails

This work is confined to the integration branch. No merge, production deployment, phone installation, or replacement of the user's main checkout has occurred.
