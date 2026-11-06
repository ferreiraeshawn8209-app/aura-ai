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

1. Reconnect/unlock the Honor and install the latest APK in place. Test a spoken action: hear exactly one “I'll be back” before execution, then confirm the action and final response; also test that “what is five plus five?” gets an immediate answer without the acknowledgement.
2. On the phone, verify selected voice is unchanged and the acknowledgement uses it. Do not claim Austrian T-800 voice until the actual installed voice matches; current persisted Android ID is `ar-language`.
3. On the phone's Settings screen, test the saved PC endpoint `http://192.168.8.18:11434`, discover both Qwen models, and confirm one responds. The PC endpoint and local generation passed from Windows.
4. Configure and implement a real cloud provider only after an endpoint and credential are available to this app. Add encrypted key storage, usage limits, failover/recovery/offline tests; current code has no cloud chat transport.
5. Replace platform standby recognition with a maintained on-device wake-word/VAD engine and test interruption, echo suppression, false activations, and a five-minute conversation on the Honor.
6. Publish the verified changes and handover on `integration/aura-voice-consolidation`; keep `main` untouched.


## Spoken-action gate follow-up — 2026-11-05

- Record the code commit SHA here and in AI_HANDOVER.md/PROJECT_STATUS.md after commit; keep `device-backups/` unstaged.
- When the Honor reconnects, test the exact spoken action sequence and confirm acknowledgement completes before tool execution; separately test an ordinary question.
- Highest outstanding implementation: add configured cloud-first model routing with tested Ollama fallback/recovery, then wire secure settings and verify without logging credentials.
- The current `ar-language` TTS ID is not the requested Austrian male voice. Preserve it until an authorized compatible voice and its identity are configured; then test preview, persistence, and spoken turns on-device.


## Commit record — 2026-11-05

- Code and test commit: `b93d67d (rebased commit; previous local hash ec7a312 was replaced during safe integration)` (`Gate spoken actions on acknowledgement completion`) on `integration/aura-voice-consolidation`. Production `main` unchanged. `device-backups/` remains untracked and was excluded.


## Updated implementation checkpoint — 2026-11-06

Completed in code and JVM verification:

- Cloud-first OpenAI-compatible chat routing with Ollama fallback, timeout handling, 30-second failure cooldown, recovery retry, encrypted key input, and chat-trace backend status.
- WhatsApp draft preparation through the installed standard WhatsApp app; user must review and send manually.
- 32 app/voice unit tests pass, 0 failures/errors/skips, 11 suites. Debug APK assembled and signature/package verified; details are in AI_HANDOVER.md.

Next implementation and acceptance:

1. Add a user-configurable daily cloud request/spend limit; current per-request cloud output is capped at 768 tokens, but no daily budget exists.
2. Configure the user's provider endpoint/model/key in Aura Settings (key stays on-device). Run actual cloud success, failure-to-Ollama, cooldown, and recovery checks; do not infer them from mocks.
3. When Honor reconnects, request authorization for this exact APK before installing. Preserve/verify existing package, signer, and app data; test voice, wake, interruption, phone-to-PC Ollama, and WhatsApp draft opening. Sending must remain a user-confirmed tap.
4. Verify the one selected TTS voice. Current saved ID is ar-language, not Austrian. Do not label the Austrian voice complete until the licensed engine and voice ID are available and tested.
5. Keep main unchanged; update handover with the actual phone/provider results, unresolved items, and commit IDs.


## Commit record — 2026-11-06

- Cloud/local routing, WhatsApp draft tool, tests, and updated project docs committed as 45eaf56dc6caf629927c6ba0e23319e2f2351d11 (Add cloud fallback and WhatsApp draft actions) on integration/aura-voice-consolidation.
- Prior spoken-action gate is b93d67d after rebasing on the latest remote audit commits. Branch has not been merged to main. The debug APK is not installed; Honor tests remain pending.
## Implementation update — 2026-11-06

- Implemented the configurable daily cloud inference request cap (60/day default; 1–1,000 setting; atomic persisted UTC-day accounting; local Ollama fallback after the cap). This is a request-count cap, not a cost/spend cap. Added quota and fallback tests.
- Latest Gradle verification passed: :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug, 12m20s, 35 tests/0 failures/0 errors/0 skipped across 12 suites. APK assembled and identity/signature verified; details in AI_HANDOVER.md.
- Next: finish code/doc commit and branch push, confirm the remote branch head; then, only after phone reconnects and approval for this exact build, test actual voice/wake/interruption, phone-to-PC Ollama, WhatsApp draft opening, and configured cloud operation. Do not install while disconnected. Austrian voice remains unavailable/unverified. Do not merge into main.
## Quota implementation commit — 2026-11-06

- Code and the current handover records committed as f4a2e0a9d5cb50c2ee97f084f9e6209e3e2593b6 on integration/aura-voice-consolidation. Verify branch publication and continue with live provider and phone acceptance only when resources reconnect.
## Branch publication confirmation — 2026-11-06

The implementation and handover are published on integration/aura-voice-consolidation at remote commit 8c332e49254fa4d40a372794202bf8ca61c1e2e1. Main stays unchanged. Device testing remains pending until the phone reconnects.
## Offline voice work — 2026-11-06

- Fixed a code-level recognizer leak/conflict: listening turns now replace and destroy the previous Android SpeechRecognizer, stale callbacks are invalidated, and permission/setup failures are surfaced. Added JVM tests for stale callback protection.
- Build and automated result: 39 tests passed, 0 failures/errors/skips, 14 suites; APK rebuilt and signature/package verified. APK hash recorded in AI_HANDOVER.md. Physical phone tests were intentionally not run.
- Next: commit/publish this repair, then after phone reconnect and approval, test microphone open/close across repeated turns, wake phrase, accurate transcript, TTS response and interruption. Continue cloud-provider live checks only after configuration and Ollama recovery testing when its service is available.
