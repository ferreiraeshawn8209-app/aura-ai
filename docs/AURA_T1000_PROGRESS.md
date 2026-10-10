# Unified Aura / T1000 progress — 10 October 2026

## Source of truth

`ferreiraeshawn8209-app/aura-ai`, `main`, is the Android foundation. Its history already contains `c956816` (Amalgamate T1000 into Aura AI). `spokdot777-dot/T1000` is reference material for selective comparison, not a second application to develop. `aura-core` is a separate server component of the same product; keep its Brain server-side behind CoreService/CoreRepository.

## Review findings and route

1. Stabilize the merged Android build, local Ollama connection, and manual voice first.
2. Bridge Aura's existing calls, SMS, reminders and app launching into the registry used by the visible agent. `agent.tool.ToolRegistry` currently registers memory, skills, clock and calculator only; `agent.core.AgentPipeline` is a separate execution path. Consolidate behind one execution boundary without discarding working device use cases.
3. Wire the wake-word preference to the foreground service and microphone permission flow. The present switch only writes a preference; wake detection is not handled by MainActivity. Resolve recognizer/TTS ownership and restart backoff before claiming hands-free operation.
4. Inspect and align the Android CoreService DTOs with the actual Aura Core task/control routes. Core defaults to `https://core.invalid/` and is not connected through the visible navigation.
5. Add additional features after build, device actions and voice work. Finance/browser/coding/trading repositories include mocks; these do not demonstrate working integrations. DJ playback must be checked against actual audio playback before being advertised.

## Repairs in codex/t1000-connection-repair

- Add missing Ollama APIs and shared Json dependency bindings.
- Remove self-dependent repository provider aliases.
- Repair malformed memory-tool import/types, JSON escaping, duplicate converter annotation, chat Flow.first import and Android greeting apostrophe.
- Register task_history with its DAO and enum converters; add a version 1 to 2 migration preserving existing tables.
- Enable configurable local HTTP endpoints with network-hop checks that reject public HTTP, including redirects. Core/public servers must use HTTPS.
- Validate endpoint before connection; retain saved URL on failed tests; clear unavailable model selection, and select automatically when exactly one model exists.
- Remove body logging from the streaming client and allow slow model loading up to the settings maximum.
- Explain Wi-Fi, emulator and USB endpoint choices.
- Restore/verify the Gradle 8.7 wrapper using a pinned SHA256; run tests and upload APK in GitHub Actions.

## Verification and limitations

Local full build attempt failed while downloading Gradle: `java.net.SocketException: Network is unreachable`. This environment also has no Android SDK. No APK or phone execution has been verified here. The retrieved historical claims of earlier build success are not validation of this patch.

Local checks passed for XML parsing, migration SQL preservation, wrapper integrity and diff whitespace. HTTP policy regression tests were added but could not run locally because javac is absent. Full Kotlin compilation, Hilt/Room generation, instrumentation migration and device tests remain required.

## Resume

Run the branch CI build, address compiler failures, then test on Shawn's Honor 90 Lite with his actual installed local model. USB: `adb reverse tcp:11434 tcp:11434`, endpoint `http://127.0.0.1:11434`. Wi-Fi: use the PC's current IPv4 and configure Ollama to listen on the LAN interface. Do not hard-code the old `192.168.8.2` address or an assumed model name. Continue with device-tool adapters and unified voice lifecycle.
