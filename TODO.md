# TODO — Aura AI / T1000

Work against the existing Android app; do not rebuild already-working modules.

## Immediate: validate baseline and recent changes

- [ ] Run `./gradlew testDebugUnitTest assembleDebug` on Java 17 + Android SDK; fix every compilation/test failure.
- [ ] Add regression tests for memory tool API wiring, model malformed responses, cancellation, and saved chat restoration/search.
- [ ] Fix `AgentOrchestrator.parseVerdict` to reject missing or malformed verification instead of treating it as success.
- [ ] Verify `ChatViewModel` interruption resets state and persists only complete turns; decide whether interrupted partial replies should be kept as transcript.

## User-facing Android functionality

- [ ] Build transcript search/export UI on `ConversationRepository.searchMessages`; avoid clearing history without confirmation.
- [ ] Implement a clear user-approved memory capture flow. Current memory is Room-persistent, but the “remember” tool has no explicit UI confirmation gate; only call it when the user explicitly requested remembering.
- [ ] Wire wake-word preference to foreground-service lifecycle with runtime microphone/notification permission UX; evaluate a true on-device keyword spotter and battery impact.
- [ ] Add device action tools (launch app/call/SMS/reminder) only behind strict argument validation, just-in-time Android permission checks, and explicit confirmation for calls/messages. WhatsApp has no general-purpose send API; use its supported intents and hand off to the user rather than bypassing consent.
- [ ] Connect chat context windows to saved conversations/memories deliberately; cap history size and avoid adding private data by default.
- [ ] Add accessible states, TalkBack descriptions, and device/emulator tests for voice, permission denial, and background restrictions.

## AI providers and safety

- [ ] Keep Ollama as a configurable local/LAN backend and add health/model errors in the chat UI.
- [ ] Add cloud inference providers only as opt-in configured services; use Android Keystore-backed credentials, redact logs, and document data sent off-device.
- [ ] Require verifier JSON to be valid and grounded in tool evidence; never infer that a failed/empty tool succeeded.
- [ ] Keep untrusted model output constrained to allowlisted typed tools; do not evaluate generated code or shell commands in the app.
- [ ] Replace coding/debugging mocks with a reviewable patch workflow. Tests must run in a sandbox; application of edits requires the user's explicit approval.

## Windows integration (not present yet)

- [ ] Decide whether Windows means a standalone desktop client, an Android companion bridge to a Windows Ollama host, or both.
- [ ] Define authenticated local-network protocol, pairing UX, encryption, replay protection, and permission model before exposing Windows control.
- [ ] Start with read-only device/host status and user-approved launch actions; never enable arbitrary shell execution or unattended file edits.
- [ ] Add a separate Windows build/test pipeline and document supported Windows versions after implementation.

## Release readiness

- [ ] Resolve duplicate/legacy architecture and database migration coverage without destructive fallbacks.
- [ ] Add privacy disclosure, data export/deletion controls, and security review.
- [ ] Configure signed release only after explicit production approval; release workflow is currently a placeholder.
