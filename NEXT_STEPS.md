# Aura: remote-first development queue (2026-11-05)

**Owner unavailable for phone/PC testing. Do not block software development on device access.** Work on `integration/aura-voice-consolidation`; do not merge `main` without approval. Preserve app data, recordings, models, voice choice, and checkpoints.

## Mandatory execution order: AUDIT AND INTEGRATION FIRST

1. Audit all eight named repositories and the current Aura integration branch, documenting reusable components, dependencies, duplicates, incompatibilities, and missing features.
2. Integrate suitable code into the protected integration branch, retaining the existing architecture and avoiding wholesale merges of unrelated applications.
3. Build and run automated tests; fix regressions and document evidence.
4. Only after the audit and integration, proceed with additional cloud routing, TTS, and voice refinements below.
5. Defer physical-device tests until the owner is available. Do not block other work on device access.

## Work that can proceed without the phone

1. Implement a provider-neutral inference router with cloud-first routing, local Ollama fallback, timeouts, circuit breaker/retry limits, health checks, and recovery to cloud. No invented credentials; provider adapters should be configurable and fail safely if unconfigured.
2. Add unit/integration tests with mocked transports for cloud success, cloud timeout, 401/429, local fallback, recovery, both providers unavailable, cancellation, and no duplicate TTS responses. Run Gradle tests and APK build in an available build environment; report exactly what ran.
3. Audit the spoken action state machine: say exactly one `I'll be back` only before spoken actions, wait for TTS completion before executing, and ensure normal questions respond directly. Test interruption and state transitions without a phone.
4. Audit selected TTS persistence and prevent silent switching. Do not claim Austrian T-800 voice until a licensed voice provider and matching voice ID are configured and verified.
5. Audit eight repository integrations for reusable code and API contracts; do not merge unrelated web projects wholesale. Prioritize compatible Android voice functionality and well-scoped backend adapters.
6. Improve transcript logging and error observability without recording private speech or secrets by default. Maintain a readable history and handover with exact commit SHAs, tests, and unresolved blockers.

## Deferred until the Honor or PC is available

- Install latest signed APK in place; verify package/certificate and preserve app data.
- Test microphone permissions, wake detection, live transcript, audio playback, selected Austrian voice, barge-in, Android mic tones, and multi-turn sessions.
- Verify actual phone-to-PC Ollama connection and cloud credentials, if supplied.

**Acceptance rule:** A successful compilation or mocked test is not proof of a working physical-device experience. Do not claim completion until both automated and device acceptance pass.

## Execution handoff

Continue the queue above in a code-capable workspace without waiting for the owner to return to the computer. Record progress and blockers in `AURA_INTEGRATION_HANDOVER.md` after each meaningful change. If a provider key or license is missing, build/test the interface with mocks and flag the integration gate rather than stopping all other tasks.
