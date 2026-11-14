## Continuation entry — 2026-11-05

Repository: `ferreiraeshawn8209-app/aura-ai`; branch: `integration/aura-voice-consolidation`. See `AURA_INTEGRATION_HANDOVER.md` for the full prior integration history.

- Added `SpokenActionGate` to the existing agent execution flow. Spoken action execution waits for one “I'll be back” acknowledgement to finish; if it fails, execution is withheld. Ordinary questions bypass the gate.
- Changed: `app/src/main/java/com/aura/ai/agent/AgentOrchestrator.kt`; added `app/src/main/java/com/aura/ai/agent/SpokenActionGate.kt` and `app/src/test/java/com/aura/ai/SpokenActionGateTest.kt`; updated `AGENTS.md`, `AI_HANDOVER.md`, `PROJECT_STATUS.md`, `AURA_INTEGRATION_HANDOVER.md`, `VOICE_TESTS.md`, `NEXT_STEPS.md`.
- Verification: Gradle app/voice unit test and debug assemble tasks succeeded (75 tasks up-to-date). JUnit reports show 22 tests, 0 failures/errors/skips, across 8 suites.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, package `com.aura.ai`, version 1.0.0/code 1, SHA-256 `5FAF6738A7DA194EF3274813552D6E557D7F723D8EB51AD8CCC48ECEB3123B89`; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- Honor disconnected; no live voice, Ollama/cloud, or WhatsApp test. Cloud failover and the Austrian voice remain incomplete. Do not stage `device-backups/`.
- Pre-change HEAD: `448b96002d32e7474e43bf57002f6009e562d3a1`. Record code commit and follow-up steps here after commit; do not merge to `main`.


## Verification rerun — 2026-11-05

- Forced fresh app and voice-library unit tests: `:app:testDebugUnitTest :aura-voice:testDebugUnitTest --rerun-tasks` completed `BUILD SUCCESSFUL` in 10m02s (50 actionable tasks executed). JUnit XML confirms 22 passed, 0 failures, 0 errors, 0 skipped across 8 suites.
- Separate `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` also completed successfully before the rerun; its 75 tasks were up-to-date. APK identity/hash/signing details remain as above; no install.


## Commit record — 2026-11-05

- Code and test commit: `ec7a31235bcc62f664267a9c7505608e98784805` (`Gate spoken actions on acknowledgement completion`) on `integration/aura-voice-consolidation`. Production `main` unchanged. `device-backups/` remains untracked and was excluded.


## Integration work — 2026-11-06

Repository: ferreiraeshawn8209-app/aura-ai; branch: integration/aura-voice-consolidation. Work stayed in the existing Android app; package identity, database, and selected TTS voice were not changed.

- Added an OpenAI-compatible cloud chat backend and provider router. When enabled and configured, cloud is tried first; cloud errors/timeouts fall back to the existing selected Ollama model. Cloud calls enter a 30-second cooldown after failure, then a later request retries cloud automatically. The chat trace names the backend/fallback. Inference text is buffered before display so a failed cloud stream cannot duplicate partial output.
- Added cloud endpoint/model settings and a masked API-key field. The key is stored using the existing Android encrypted preferences (SecureStorage) and is not logged or embedded in the APK. Cloud routing is off until configured; the user’s actual key/endpoint/model are not available in this checkout.
- Added prepare_whatsapp_message: opens standard WhatsApp with a prefilled wa.me draft. Aura does not send it; the user reviews the recipient/text and taps Send in WhatsApp.
- Added cloud-first/fallback/recovery/cooldown/cancellation, HTTP request/auth/error-redaction, and WhatsApp draft-link tests.
- Changed source files: AgentModels.kt, AgentOrchestrator.kt, SecureStorage.kt, SettingsRepository.kt, AppModule.kt, ToolModule.kt, ChatViewModel.kt, SettingsScreen.kt, SettingsViewModel.kt; added ProviderChatRouter.kt, WhatsAppDraftTool.kt, ProviderChatRouterTest.kt, OpenAiCompatibleChatBackendTest.kt, WhatsAppDraftToolTest.kt. Handover/status/voice-test/next-step files are also updated.
- Verification command: gradlew.bat --no-daemon with Kotlin in-process compilation, running :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug — BUILD SUCCESSFUL in 14m26s; 75 tasks, 17 executed and 58 up-to-date. JUnit XML: 32 tests, 0 failures, 0 errors, 0 skipped, 11 suites. git diff --check passed.
- APK: app/build/outputs/apk/debug/app-debug.apk; package com.aura.ai, version 1.0.0/code 1; size 20,723,798 bytes; SHA-256 0A85F8C5811A5F3F332CB93A5985AD0C775D67AFD4FE56A3CF62283C5FBDA64A. Signature verification passed; signer SHA-256 1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3.
- No install was performed. Honor/ADB is disconnected. No live cloud credential, cloud request, phone-to-PC Ollama call, WhatsApp launch/send, wake/microphone/speech/TTS/interruption test occurred. Ollama is intentionally stopped. The saved Android TTS voice remains ar-language; the requested Austrian voice is still not supplied/configured.
- Limits: the router bounds cloud output to 768 tokens per request and cools down after failure, but a user-configurable daily spend/request budget is not implemented yet. The cloud adapter supports OpenAI-compatible chat-completions, not provider-specific proprietary APIs.
- Next: configure a provider in Aura Settings, test cloud success/failure and Ollama recovery when available, complete phone voice/WhatsApp checks after reconnection and approval for this build, implement a daily cloud budget, and continue wake/barge-in/Austrian TTS work. No merge to main.


## Commit record — 2026-11-06

- Cloud/local routing, WhatsApp draft tool, tests, and updated project docs committed as 45eaf56dc6caf629927c6ba0e23319e2f2351d11 (Add cloud fallback and WhatsApp draft actions) on integration/aura-voice-consolidation.
- Prior spoken-action gate is b93d67d after rebasing on the latest remote audit commits. Branch has not been merged to main. The debug APK is not installed; Honor tests remain pending.


## Branch publication — 2026-11-06

GitHub accepted the integration branch push: origin/integration/aura-voice-consolidation now includes the spoken-action gate and cloud/WhatsApp work through commit b7ba19425b8c0cf6ba2d40cba26f4375049d94bd. Production main remains unchanged. Phone acceptance and live provider tests remain pending.
## Cloud request cap and fresh verification — 2026-11-06

- Added a user-configurable daily cloud inference request cap in Settings (default 60, allowed range 1–1,000). Usage is persisted in the existing DataStore, counted atomically per UTC day, and resets when the UTC date changes. Once the cap is reached, Aura skips cloud for that inference and tries the existing Ollama path with a visible fallback reason. This is a request-count cap, not a currency/spend cap; multi-step agent work can consume more than one inference request.
- Changed: SettingsRepository.kt, SettingsScreen.kt, SettingsViewModel.kt, AgentOrchestrator.kt, ProviderChatRouter.kt, and ProviderChatRouterTest.kt; added CloudDailyQuotaTest.kt. Existing database schema and package identity were not changed.
- Verification command: gradlew.bat --no-daemon -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug — BUILD SUCCESSFUL in 12m20s; 75 tasks, 15 executed and 60 up-to-date. JUnit XML: 35 tests, 0 failures, 0 errors, 0 skipped, 12 suites. git diff --check passed (only CRLF normalization warnings).
- APK: app/build/outputs/apk/debug/app-debug.apk, 20,761,174 bytes; package com.aura.ai, version 1.0.0/code 1; SHA-256 F2B2FC2D00E6028B9DD708385319422411EE668622E26CB2CD9BC3998ECD63E5. apksigner verification passed; signer SHA-256 1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3.
- This is a debug-signed build and has not been installed. Honor/ADB remains disconnected by request; no real-device voice/wake/interruption/WhatsApp/Ollama test was run. Ollama is stopped; cloud credentials are not configured, so provider behavior has only mocked/unit coverage. Austrian TTS is still not configured (existing saved voice remains ar-language).
- Next: inspect final diff, commit code and documentation together on integration/aura-voice-consolidation, publish that branch, then wait for phone reconnection before requesting approval to install this exact APK and completing device checks. Do not merge to main.
## Implementation commit — 2026-11-06

- Daily cloud request cap, tests, and handover updates committed as f4a2e0a9d5cb50c2ee97f084f9e6209e3e2593b6 (Add daily cloud inference request limit) on integration/aura-voice-consolidation. Commit contains the quota implementation and all five project records. No main merge or APK install. device-backups/ remains untracked and excluded.
## GitHub publication — 2026-11-06

The quota implementation and documentation were pushed successfully. origin/integration/aura-voice-consolidation resolves to 8c332e49254fa4d40a372794202bf8ca61c1e2e1. Local branch is equal to origin; main unchanged. Only untracked path is the pre-existing personal device-backups/ directory, excluded from commits.
## Voice recognizer lifecycle repair — 2026-11-06

- Fixed the existing Android speech provider's repeated-turn lifecycle: every new turn now invalidates stale recognition callbacks and destroys the previous SpeechRecognizer before creating another. Stop and close cancel and destroy the active instance. Creation/start permission or runtime failures now become explicit recognition errors instead of escaping the asynchronous main-thread task silently.
- Changed: lib/aura-voice/src/main/kotlin/com/aura/voice/android/AndroidSpeechRecognizerProvider.kt; added lib/aura-voice/src/main/kotlin/com/aura/voice/android/RecognitionRequestGate.kt and lib/aura-voice/src/test/kotlin/com/aura/voice/RecognitionRequestGateTest.kt. No database, package identity, recordings, models, or saved voice settings were modified.
- Verification: gradlew.bat --no-daemon -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug — BUILD SUCCESSFUL in 10m23s; 75 tasks, 16 executed and 59 up-to-date. JUnit XML: 39 tests, 0 failures/errors/skips, 14 suites. New tests cover invalidation of earlier request callbacks on restart and stop. git diff --check passed.
- APK: app/build/outputs/apk/debug/app-debug.apk, 20,761,174 bytes, package com.aura.ai v1.0.0/code 1; SHA-256 C241E0899CB45F23AE74E736B674349DF5DFCF14A826562CB9F7734BC6B41E2C. Signature verification passed; signer SHA-256 1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3.
- Not installed. Phone is disconnected by request. This fix is unit/build verified only; real microphone exclusivity, wake phrase, transcript quality, TTS echo/interruption and multi-turn voice remain pending Honor tests. Wake detection still relies on Android SpeechRecognizer; do not claim a dedicated Vosk hotword engine is verified. Ollama is stopped, provider credentials are not configured, and the selected Android voice remains ar-language rather than Austrian.
- Next: commit source and records on integration/aura-voice-consolidation and push. When the Honor reconnects, request approval for this exact APK before install; back up and verify package/signature first, then run repeated-turn, wake, recovery and barge-in checks. Keep main unchanged.
## Voice lifecycle commit — 2026-11-06

- Recognizer cleanup, stale-callback gate/tests, and the five handover updates committed as e3d86b3215ca67a88f595a77e01dc8946255a1f3 (Release stale Android speech recognizers) on integration/aura-voice-consolidation. Awaiting push; main unchanged. device-backups/ excluded.
## Voice repair published — 2026-11-06

GitHub accepted the speech lifecycle implementation commit e3d86b3215ca67a88f595a77e01dc8946255a1f3 and its handover commit 12af8d08565847d406672fa4741b77c4123f3c32. The verified integration branch head was 12af8d0 at publication check; main remains unchanged. The only untracked path is the pre-existing device-backups/ folder.

## Transcript persistence and search — 2026-11-06

- Integrated the existing Room-backed `ConversationRepository` into the active Agent chat screen. On launch, saved messages load into the existing transcript; completed user and assistant turns are saved through the repository. Sending and wake-triggered sends wait until history has loaded so an early utterance is not discarded.
- Added in-chat transcript search and selectable message text for copy. Search is case-insensitive. Uses the existing conversation table; no database/schema migration, package change, or replacement app.
- Changed source: `app/src/main/java/com/aura/ai/ui/chat/ChatScreen.kt`, `ChatUiState.kt`, `ChatViewModel.kt`; added `TranscriptSearch.kt` and `app/src/test/java/com/aura/ai/TranscriptSearchTest.kt`.
- First Gradle attempt failed before compilation because C: had 0 bytes free. Freed space only from checked, generated Gradle directories under `app/build` (`intermediates`, `kspCaches`, `tmp`); preserved APK outputs, all source, app data, databases, models and `device-backups/`. The host terminal could not write the protected user Gradle wrapper cache, so the successful build ran on authorized BEATK.
- Verification: `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` — `BUILD SUCCESSFUL` in 21m12s; 75 actionable tasks, 46 executed and 29 up-to-date. JUnit XML: 42 tests, 0 failures, 0 errors, 0 skipped, 15 suites. `git diff --check` passed. Existing warnings: Room schema export location unset; Android/Moshi deprecations.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,621,965 bytes; package `com.aura.ai`, version 1.0.0/code 1; SHA-256 `1614E230F6663E00D86D975B5CFE21F9BF84E7F998EADB24C98F3E356F8CB3F1`. `apksigner verify --print-certs` passed; existing Android Debug signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- Not installed. Phone is disconnected by request. No real-device microphone, transcript display, continuous conversation, wake, interruption, speech output, Ollama, cloud, WhatsApp launch/send, or app-data compatibility result is claimed. Cloud needs configured provider credentials; no live provider request was made. The saved TTS voice remains `ar-language`, not Austrian. WhatsApp remains a user-reviewed draft action.
- Current branch: `integration/aura-voice-consolidation`; production `main` unchanged. Record implementation commit after committing; exclude pre-existing untracked `device-backups/`.
- Next: publish the code+handover commit to the integration branch; when Honor reconnects, verify package/signature and backup before asking to install this exact build, then check transcript load/search/copy and run the real-device voice/Ollama/cloud/WhatsApp scenarios. Do not merge to main.

## Transcript implementation commit — 2026-11-06

- Transcript persistence/search/copy source, 3 tests, and required project records are committed as `d0e8e9c` (`Persist and search Aura chat transcripts`) on `integration/aura-voice-consolidation`. Build/test/APK evidence is above. The code commit and documentation commit are published; `main` unchanged. `device-backups/` was excluded.

## Publication verification — 2026-11-06

- Immediately after publishing the code and first handover commits, GitHub remote `origin/integration/aura-voice-consolidation` resolved to `1ae7c1c99562b4c00ca767e2f57ade323202bf68`. `main` remains `4b48185e9f110764938f10548f80a29acda674f7`; no merge. This supersedes the earlier note that publication was pending.

## PC-side Ollama availability check — 2026-11-06

- Direct HTTP check from the authorized BEATK PC to `http://192.168.8.18:11434/api/tags` succeeded with proxy bypassed. Ollama listed `qwen3:1.7b` and `qwen3:4b-q4_K_M`, matching the app's configured default LAN endpoint and requested models.
- The normal shell request was intercepted by the machine's proxy configuration and refused at `127.0.0.1:9`; the direct no-proxy request reached Ollama. I changed no proxy, firewall, Ollama, or model settings and did not run a generation request.
- This verifies PC-to-Ollama HTTP/model discovery only. Honor remains disconnected, so Android-to-PC reachability and model selection are still pending. Cloud is implemented and covered by mock tests; no provider credentials/live cloud request are configured.

## Optional ElevenLabs voice provider — 2026-11-06

- Added an optional ElevenLabs speech transport and private-cache MP3 player to the existing Aura voice subsystem. The existing Android TTS provider remains available and remains the saved selection until the owner explicitly chooses another voice. Voice IDs are preserved as provider-prefixed IDs so Aura does not substitute a different voice silently.
- Added a masked ElevenLabs key field in Voice settings; the key is saved through the existing encrypted SecureStorage and is not compiled into source or URL paths. The screen warns against distributing a build containing a personal key and recommends a server-side proxy/restricted key for shared apps.
- Voice list metadata includes voice name, gender, accent and a language tag; the user can load, select and preview through the existing controls. No key was configured and no live ElevenLabs request/audio playback was performed. No Austrian voice has been confirmed or selected; the existing saved Android voice remains unchanged.
- Changed files: app/src/main/java/com/aura/ai/di/VoiceModule.kt, app/src/main/java/com/aura/ai/ui/settings/SettingsScreen.kt, app/src/main/java/com/aura/ai/ui/settings/SettingsViewModel.kt; added app/src/main/java/com/aura/ai/voice/tts/AndroidCloudAudioPlayer.kt, app/src/main/java/com/aura/ai/voice/tts/ElevenLabsSpeechTransport.kt, app/src/test/java/com/aura/ai/voice/tts/ElevenLabsSpeechTransportTest.kt.
- Build/test history for this change: initial full run reached 41 tests and found 3 failures because Android's stub JSONObject was used in local JVM tests; provider and tests were changed to the project's kotlinx.serialization JSON implementation. One compile attempt then missed the typed JSON put extension import. The next attempt stopped at KSP because C: had no space. Only generated app/build/intermediates, kspCaches and tmp folders were cleared after confirming their exact repo-local paths; app/build/outputs, source, data, and device-backups were preserved.
- Focused regression command :app:testDebugUnitTest --tests com.aura.ai.voice.tts.ElevenLabsSpeechTransportTest passed in 4m04s.
- Final command :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug completed BUILD SUCCESSFUL in 5m21s (75 actionable tasks: 6 executed, 69 up-to-date). JUnit XML totals: 41 tests, 0 failures, 0 errors, 0 skipped across 14 suites. git diff --check passed.
- Fresh debug APK: app/build/outputs/apk/debug/app-debug.apk; 20,638,349 bytes; package com.aura.ai, version 1.0.0/code 1; SHA-256 95EC076E0F424A8C76CB30BAB3BC067F630C6217524F32624366573390B70DE2. APK signature verification passed; signer SHA-256 1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3.
- The APK was not installed. Honor is disconnected as requested. No microphone, wake phrase, live transcript, voice preview, speech interruption, Ollama, cloud reasoning, or WhatsApp device acceptance is claimed from this change. Existing app package, database, user settings, backups and default voice were preserved. Main was not modified.
- Branch: integration/aura-voice-consolidation. Pre-change HEAD: 498e494a526c6e0f5679950c23b505d6906df1d8. Implementation commit ID will be recorded below after commit. Personal device-backups/ remains untracked and excluded.
- Next: after Honor reconnects, verify APK package/signature and back up app data before requesting install approval; configure an owner-controlled ElevenLabs key, load voices, select the intended voice and preview on device; then separately verify repeated spoken turns, wake, interruptions, Android TTS fallback behavior, local Ollama/cloud reasoning and WhatsApp draft review. Do not merge to main.


## Commit record — 2026-11-06

Implementation commit: 3eb89d5cdb1b8c634797c971dcc43527188b317e (Add optional ElevenLabs voice provider) on integration/aura-voice-consolidation. It includes provider code, tests, and project records. Main unchanged; device-backups/ was excluded.

## Branch publication — 2026-11-06

GitHub origin/integration/aura-voice-consolidation was pushed and verified at 50bb1ed4d404a42454f57355931779b12e413bd0. This includes implementation commit 3eb89d5cdb1b8c634797c971dcc43527188b317e and documentation commit 50bb1ed4d404a42454f57355931779b12e413bd0. Main was not updated. The existing device-backups/ folder remains untracked and excluded.

## Wake-listener restart repair — 2026-11-06

- Fixed a lifecycle gap in `ChatViewModel.stopHandsFreeSession()`: after an explicit hands-free conversation ends, Aura now resumes standby wake listening only when the wake-word setting is enabled and Android microphone permission is granted. If starting the wake listener fails, the chat banner reports the failure. This avoids leaving the user's enabled wake phrase silently stopped after a session.
- Added a small pure policy helper and JVM coverage for wake setting and microphone permission; a later guard/test also covers a concurrently active hands-free session.
- Changed files: `app/src/main/java/com/aura/ai/ui/chat/ChatViewModel.kt`; added `app/src/main/java/com/aura/ai/voice/wake/WakeWordResumePolicy.kt` and `app/src/test/java/com/aura/ai/voice/wake/WakeWordResumePolicyTest.kt`. No database, package, settings schema, or user data changes.
- Verification on authorized BEATK PC: `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` — `BUILD SUCCESSFUL` in 9m08s, 75 actionable tasks (17 executed, 58 up-to-date). JUnit XML reports for the app: 42 tests, 0 failures, 0 errors, 0 skipped across 15 suites, including the new wake-resume policy test. No XML test reports were present under `aura-voice/build/test-results`; do not infer additional test counts for that module. Existing compile deprecation warnings remain. `git diff --check` passed before documentation update.
- Fresh debug APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,759,056 bytes, SHA-256 `C3659671B0E42BB488E8CFC3E87075CB74D1106C4B1ABD1E5A84C4EB6F317DA4`. `apksigner verify --print-certs` passed. Package `com.aura.ai`, version 1.0.0/code 1; Android Debug signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- The Honor phone is disconnected. The APK was not installed. This change has only JVM/unit and build verification; standby microphone restart, wake detection, recognition, speech, interruption, Ollama/cloud routing, and WhatsApp actions remain unverified on-device. No physical-device success is claimed.
- Branch `integration/aura-voice-consolidation`, starting HEAD `cfdf931fe92330e4be8c08f0a2299aa2631cc23a` (same as remote before this task). Main unchanged. Existing untracked personal `device-backups/` remains excluded from staging.
- Next steps: when Honor reconnects, verify package/signature and backup before installing the user-approved build. Test stopping a voice session with wake enabled/disabled and permission granted/denied, followed by repeated wake and multi-turn conversation. Continue separate phone checks for transcript, voice, interruption, Ollama/cloud fallback, and WhatsApp draft review.

## Commit and publication — 2026-11-06

- Code, unit test and records committed as `04fd7a4ad8e3927b2f6d39271fe83b4afc57d61c` (`Resume Aura wake listener after voice session`). Push succeeded; `origin/integration/aura-voice-consolidation` verified at the same commit. `main` remains `4b48185e9f110764938f10548f80a29acda674f7`; no merge. Only the pre-existing personal `device-backups/` is untracked.

## Wake restart race guard and final verification — 2026-11-06

- Tightened the restart policy to also require that a new hands-free session is not active at the moment the asynchronous settings/permission check finishes. This prevents a delayed standby restart from taking microphone ownership after the user has already started another conversation. The policy test now covers wake setting, permission, and active-session state.
- Updated files: `app/src/main/java/com/aura/ai/ui/chat/ChatViewModel.kt`, `app/src/main/java/com/aura/ai/voice/wake/WakeWordResumePolicy.kt`, `app/src/test/java/com/aura/ai/voice/wake/WakeWordResumePolicyTest.kt`.
- Re-ran `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` on BEATK: `BUILD SUCCESSFUL` in 8m27s (75 actionable tasks: 14 executed, 61 up-to-date). App XML summary: 42 tests, 0 failures, 0 errors, 0 skipped across 15 suites. `aura-voice:testDebugUnitTest` was up-to-date. Existing deprecation warnings remain.
- Latest APK replaces the earlier APK hash recorded above: `app/build/outputs/apk/debug/app-debug.apk`, 20,862,522 bytes, SHA-256 `3C5BA73F4582281AC2A27BAEB17B3BD008E68F068566438E876CBDB12CAAD511`. `apksigner verify --print-certs` passed; package `com.aura.ai`, version 1.0.0/code 1; Android Debug signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- Honor remains disconnected and this APK was not installed. No physical wake, mic, transcript, voice, interruption, Ollama/cloud or WhatsApp results are claimed. `main` remains unchanged; `device-backups/` remains excluded.
- Next: when the Honor reconnects, verify app data backup and APK identity before installing under the existing approval. Test race scenario (end, immediately restart session) plus wake enabled/disabled and permission denied, then full multi-turn voice and backend/device action coverage.
- Final code/test/handover commit `b33b06dd8832893d8f6429b074933f7b0fce2e70` is published and verified on `origin/integration/aura-voice-consolidation`; `main` remains `4b48185e9f110764938f10548f80a29acda674f7`.

## Cloud-only chat availability repair — 2026-11-06

- Fixed a mismatch between the cloud-first reasoning router and the chat UI gate. The router supported a configured cloud model without a selected Ollama model, but `ChatViewModel` disabled chat before routing. Chat availability now accepts a valid cloud configuration (enabled, URL/model present, encrypted API key saved) or a local model. The header reports cloud-first with Ollama fallback, cloud-only, or local Ollama.
- Added `ChatBackendAvailabilityPolicy` and four unit tests for cloud-only, cloud-first plus local fallback, local-only, and incomplete cloud settings. No settings schema, database, credentials, voice selection, package identity, or personal data changed.
- Changed files: `app/src/main/java/com/aura/ai/ui/chat/ChatBackendAvailabilityPolicy.kt`, `ChatUiState.kt`, `ChatViewModel.kt`, `ChatScreen.kt`, and `app/src/test/java/com/aura/ai/ui/chat/ChatBackendAvailabilityPolicyTest.kt`.
- Verification: focused `:app:testDebugUnitTest --tests com.aura.ai.ui.chat.ChatBackendAvailabilityPolicyTest` passed. Full `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` completed `BUILD SUCCESSFUL`; 50 tests, 0 failures, 0 errors, 0 skipped across 18 XML suites. APK signature passed; existing Compose/Moshi deprecation warnings remain.
- Fresh debug APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,759,062 bytes, SHA-256 `C07912A9B18D9D8827C9ABE427DB23A46B8F3A5F49AED8B69BD55F221647581B`. `apksigner verify --print-certs` passed; package `com.aura.ai`, version 1.0.0/code 1; Android Debug signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- Device acceptance is pending. Honor is disconnected; this APK was not installed. No live voice, interruption, speech recognition, Ollama inference from phone, live cloud request, or WhatsApp launch/draft test was performed. The prior PC-side Ollama `/api/tags` check only confirmed host reachability/model listing.
- Branch `integration/aura-voice-consolidation`; main unchanged. The personal `device-backups/` folder remains untracked and excluded. Commit ID and remote publication will be recorded after commit.
- Next: test chat readiness and cloud-only response with owner-configured credentials, then cloud-first/local fallback and recovery with live providers; after Honor reconnects, verify package/signature and back up accessible app data before installing the approved APK, then test voice, local/cloud route, and WhatsApp draft on device.

## Commit record — 2026-11-06

- Cloud-only readiness implementation, four policy tests, and project records are committed on `integration/aura-voice-consolidation` as `a0a4606` (`Enable cloud-only Aura chat readiness`). Branch started at `63a5f1dcd6b27dd4de0bada84e50a8df06681e16`; `main` remains unchanged. `device-backups/` was excluded.

## Honor install preflight and launch — 2026-11-06

- Wireless ADB reconnected during this turn. Read-only inspection found the installed `com.aura.ai` app at version 1.0.0/code 1 and a separate `com.aura.ai.debug` package; the latter was left untouched. The new APK package is `com.aura.ai` v1.0.0/code 1. Installed APK and new build signatures both verified to the same Android Debug certificate SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- Before update, archived the current debuggable app's private data read-only with `run-as` to ignored `device-backups/com.aura.ai-preinstall-cloud-only-fix-20261106.tar` (997,376 bytes; SHA-256 `B7604B79A17CDFA52937361F7C9DD0E081AC7B6A350C70887C4A458A388F043A`). Pulled the existing base APK to ignored `device-backups/com.aura.ai-installed-base-20261106.apk` (20,662,141 bytes) for signature comparison. Both backup files remain untracked and were not staged.
- With the prior explicit device-test approval and successful backup/signature/package preflight, updated the existing `com.aura.ai` app in place using `adb install -r`; result `Success`. No uninstall, clear-data, or replacement package was used. Started `com.aura.ai/.MainActivity`; Android reported it as top resumed. A recent 600-line logcat scan showed no `FATAL EXCEPTION` or `AndroidRuntime` match. This verifies update/install/launch only.
- New APK SHA-256 `C07912A9B18D9D8827C9ABE427DB23A46B8F3A5F49AED8B69BD55F221647581B`; package/version/signature above. The user has been asked for one live “Yo, yo, yo, what is five plus five?” turn. Transcript/audio outcome is pending; no voice behavior is claimed from launch evidence.
- Integration implementation commit `a0a4606` and records commit `db6d650` were pushed successfully to `origin/integration/aura-voice-consolidation`; `main` was not changed. A subsequent `ls-remote` read was blocked by the PC proxy after the successful push; local tracking status showed no ahead/behind.

## Latest handover publication — 2026-11-06

- Device install/startup documentation commit `1a402459e7e30e046b56ea810b24df8c09dba294` was pushed successfully. GitHub returned `db6d650..1a40245` for `integration/aura-voice-consolidation`; `main` was not changed. This is a push result, not a later `ls-remote` read.

## Continuous listening and barge-in — 2026-11-06

- Added optional recognition while Aura is speaking in an active hands-free session. The voice state model now exposes `LISTENING_AND_SPEAKING`; normal listening still stops existing TTS. Added a transcript policy that filters likely Aura TTS echo and requires two stable, non-filler partials before stopping speech. ChatViewModel starts monitor listening when TTS begins, cancels TTS when user speech is confirmed, submits the final transcript, and resumes listening after applicable errors.
- Changed: `app/src/main/java/com/aura/ai/ui/chat/ChatViewModel.kt`, `lib/aura-voice/src/main/kotlin/com/aura/voice/DefaultAuraVoiceSubsystem.kt`, `VoiceModels.kt`; added `BargeInTranscriptPolicy.kt` and its unit test; expanded `DefaultAuraVoiceSubsystemTest.kt`.
- Focused `:aura-voice:testDebugUnitTest`: `BUILD SUCCESSFUL` (7m24s). Full `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug`: `BUILD SUCCESSFUL` in 19m13s (75 tasks, 16 executed, 59 up-to-date). JUnit XML totals: 56 tests, 0 failures, 0 errors, 0 skipped, 19 suites. `git diff --check` passed before the documentation edits. Remaining compiler output is deprecation warnings in existing Compose/Moshi code.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,862,524 bytes; package `com.aura.ai`, version 1.0.0/code 1; SHA-256 `D9607C66F0835DFD73E1F77FA813BA1463A8E5405C46603A8411426E704048FE`. `apksigner verify --verbose` passed using APK v2; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- No ADB or device test was run, as requested. Full-duplex audio, actual wake/ASR/TTS behavior, interrupt timing, Ollama/cloud calls, and WhatsApp launch remain unverified on the Honor. Ollama was not started and no live provider key/request was used. The existing tested app code contains cloud-first/local Ollama fallback and WhatsApp draft preparation; live provider/device acceptance remains outstanding.
- Branch `integration/aura-voice-consolidation`; base HEAD `f99bd100282e080742263df18ea121ece91261b6`; implementation, tests and records committed as `6e2a221` (`Add_hands_free_interruption_monitoring`). `main` remains `4b48185e9f110764938f10548f80a29acda674f7`. `device-backups/` remains personal/untracked and excluded. Push status will be recorded after publication attempt.
- Next: publish the committed source, tests, and records on this integration branch if network access works. Keep the APK uninstalled. When the Honor reconnects, verify identity/signing and back up existing data before requesting approval to install this exact build, then test repeat turns and interruption. Test real cloud-first/Ollama fallback and recovery plus WhatsApp draft opening separately.

## Publication record — 2026-11-06

Implementation commit `6e2a221` and handover record commit `ac8c9bc` were pushed successfully to `origin/integration/aura-voice-consolidation` (`f99bd10..ac8c9bc`). `main` was not changed. The only untracked path is personal `device-backups/`, excluded from both commits and the push.

## Wake recognizer stale-callback recovery — 2026-11-06

- Hardened the existing `WakeWordService` restart loop. Each recognizer now has a request generation; callbacks from prior recognizers cannot trigger another restart or launch Aura after replacement/stop. Service teardown invalidates callbacks before cancel/destroy. Recognizer creation/listener/start runtime failures now retry with the existing capped backoff; revoked mic permission stops the listener rather than retrying forever.
- Added `WakeWordRequestGate` and two unit tests covering replacement and stop invalidation. Changed `app/src/main/java/com/aura/ai/voice/WakeWordService.kt`; added `app/src/main/java/com/aura/ai/voice/wake/WakeWordRequestGate.kt` and `app/src/test/java/com/aura/ai/voice/wake/WakeWordRequestGateTest.kt`.
- Verification: `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` — `BUILD SUCCESSFUL` in 7m29s, 75 tasks (14 executed, 61 up-to-date). JUnit XML totals: 58 tests, 0 failures, 0 errors, 0 skipped, 20 suites. `git diff --check` passed. Existing Compose, Moshi and test-API deprecation warnings remain.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,862,525 bytes; package `com.aura.ai`, version 1.0.0/code 1; SHA-256 `A242340DC1003BE359FC52F66638FC0884A284DF265A4F09E363054589BABFA4`. `apksigner verify --verbose --print-certs` passed with v2; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- No ADB/device/provider action was taken. Wake phrase capture, service survival, ASR/TTS, full-duplex interruption, Ollama/cloud networking and WhatsApp are still not verified on Honor. This change guards stale callbacks but does not replace Android SpeechRecognizer with a dedicated acoustic hotword engine.
- Branch `integration/aura-voice-consolidation`, starting HEAD `f3406fb911134796fb7356cb08288dd548d0ad5d`; source/tests/records committed as `1eb7027` (`Add_wake_listener_stale_callback_guard`). `main` remains `4b48185e9f110764938f10548f80a29acda674f7`. Exclude `device-backups/`.
- Next: when Honor reconnects, verify app identity and back up existing data before asking approval for this exact APK, then test repeated wake starts/stops and a full spoken turn. Also perform real provider fallback/recovery and WhatsApp draft checks.

## Wake guard publication — 2026-11-06

Implementation `1eb7027` and handover update `86e2f72` were pushed successfully to `origin/integration/aura-voice-consolidation` (`f3406fb..86e2f72`). `main` was not modified. APK remains uninstalled; `device-backups/` excluded.

## Wake-to-conversation microphone handoff — 2026-11-06

- Fixed an Activity lifecycle race: wake-triggered `MainActivity.onStart()` could asynchronously start standby `WakeWordService` again while chat was starting hands-free listening. The Activity now records a pending wake event and skips standby restart for that start; normal launches still restore opted-in wake listening only with microphone permission.
- Added two `WakeWordResumePolicy` tests for ordinary Activity start versus wake-triggered start. Changed `app/src/main/java/com/aura/ai/MainActivity.kt`, `app/src/main/java/com/aura/ai/voice/wake/WakeWordResumePolicy.kt`, and `app/src/test/java/com/aura/ai/voice/wake/WakeWordResumePolicyTest.kt`.
- Verification: `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` — `BUILD SUCCESSFUL` in 8m48s (75 tasks; 14 executed, 61 up-to-date). JUnit XML: 60 tests, 0 failures, 0 errors, 0 skipped, 20 suites. `git diff --check` passed. Existing Compose, Moshi Kapt, and test-API deprecation warnings remain.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,862,621 bytes; package `com.aura.ai`, version 1.0.0/code 1; SHA-256 `9CF7CD5B965D36E964D124877165362C11514B180A8DE957E607E586454B0CB6`. `apksigner verify --verbose --print-certs` passed using v2; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- No ADB/device test was run. The lifecycle policy is unit-tested, but phone microphone handoff, wake detection, response speech, interruption, Ollama/cloud, and WhatsApp remain unverified on Honor. This removes a code path that could race; it does not establish real-device success.
- Branch `integration/aura-voice-consolidation`, starting HEAD `1adad3f061b8fb62ee731ffcc2dc0781be2b7b0a`; code/tests/records committed as `cb04a22` (`Prevent_wake_launch_mic_reclaim`). `main` remains `4b48185e9f110764938f10548f80a29acda674f7`. Exclude `device-backups/`.
- Next: keep this APK uninstalled. When Honor reconnects, back up app data and verify package/version/signer before requesting approval for this exact build; test wake-triggered and ordinary starts, two turns, and interruption. Live provider and WhatsApp tests remain separate.

## Wake handoff publication — 2026-11-06

Implementation `cb04a22` and test-record commit `25e9af6` were pushed successfully to `origin/integration/aura-voice-consolidation` (`1adad3f..25e9af6`). `main` was not changed. APK is uninstalled; personal `device-backups/` remains excluded.

The publication confirmation is recorded as commit `d95ec2a` and was also pushed to the integration branch. It confirms source `cb04a22`, handover `25e9af6`, no `main` merge, and no APK install.

## Agent phone-action tools — 2026-11-06

- Continued work while the Honor was disconnected; no ADB access or APK installation was attempted.
- Exposed safe Android actions in the active agent tool registry: `open_app` (unique exact installed label), `open_website` (HTTP/HTTPS only), `prepare_phone_call` (opens ACTION_DIAL; user taps Call), and `prepare_sms` (opens the default SMS app with a draft; user taps Send). Existing `prepare_whatsapp_message` remains registered and manually reviewed; no message is sent automatically. These tools use Android intents and add no permissions or database changes.
- Changed: `app/src/main/java/com/aura/ai/di/ToolModule.kt`; added `app/src/main/java/com/aura/ai/agent/tool/AndroidActionInputPolicy.kt`, `AndroidActionTools.kt`, and `app/src/test/java/com/aura/ai/agent/tool/AndroidActionInputPolicyTest.kt`; updated `AI_HANDOVER.md`, `PROJECT_STATUS.md`, `VOICE_TESTS.md`, and `NEXT_STEPS.md`.
- Verification: focused AndroidActionInputPolicyTest passed. Full offline invocation `gradlew.bat --offline --no-daemon -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` completed `BUILD SUCCESSFUL` in 2m58s; 75 tasks, 5 executed and 70 up-to-date. JUnit XML totals: 64 tests, 0 failures, 0 errors, 0 skipped across 21 suites. `git diff --check` passed. Initial online-mode invocation failed before compilation because Windows reported insufficient disk space during plugin resolution; offline mode used cached dependencies and passed without deleting any files.
- Fresh debug APK: `app/build/outputs/apk/debug/app-debug.apk`, package `com.aura.ai`, version 1.0.0/code 1, SHA-256 `9cbb5714d088c8e134e8c7044ff48b99194971adbafae1aa6f7b1a959d5a1a2e`. APK v2 signature verification passed; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- Remaining: actual intent resolution and app labels, default-browser behavior, dialer/SMS draft UI, WhatsApp draft, Honor voice/wake/mic/interruption, and phone-to-PC Ollama remain unverified on-device. Cloud/Ollama fallback tests remain automated/mock-only without configured live cloud credentials; no live provider request was made. Austrian TTS is not yet available/configured. The existing app, package identity, database, user data, APK outputs, and backup folder were preserved.
- Branch `integration/aura-voice-consolidation`; pre-change HEAD `fdc3d95950e1e3d839c0177a952fd5598ac66987`; production `main` remains unchanged. Personal `device-backups/` is untracked and excluded. Commit and publication status are recorded below after commit/push.
- Next: keep the APK uninstalled until the Honor reconnects and the exact update has been approved. Then back up app data and verify package/signature before installation, and exercise the real app-open, browser, dialer, SMS/WhatsApp draft, voice, wake, interruption, local Ollama and cloud fallback flows. Obtain/configure a live provider to test actual failover and recovery.

## Commit record — 2026-11-06

- Action tools, tests and initial records are committed as `f5655fa` (`Add-safe-Android-action-tools-to-agent`) on `integration/aura-voice-consolidation`. The follow-up documentation commit records that commit's complete hash and push status. `main` is unchanged; `device-backups/` was excluded.

## Branch publication — 2026-11-06

- GitHub accepted the code commit `f5655fa` and commit-status handover commit `eaeba65`. At that verification point, local and remote resolved to `eaeba65c9d484846c8c1f6c3dc116b483ff5b61b`. The publication record was then committed as `9ec975a` and pushed successfully. Production `main` remains `4b48185e9f110764938f10548f80a29acda674f7`; personal `device-backups/` was excluded.

## Contact lookup and confirmation safety — 2026-11-06

- Added `search_contacts`, `prepare_contact_call`, and `prepare_contact_sms` to the existing agent tool registry. Lookup requires the existing Android `READ_CONTACTS` runtime permission; no new manifest permission was added or granted. Search results expose contact names and saved-number counts, not phone numbers. Calls and SMS open the native dialer/messaging app only after a unique name/number match; the user must tap Call/Send. Ambiguous contacts or multiple saved numbers stop and request clarification.
- Repaired the older `SendMessageUseCase` action path too: `MakeCallUseCase` now uses `ACTION_DIAL`; `SendSmsUseCase` now uses `ACTION_SENDTO` with a draft. User-facing acknowledgement text and the legacy model instruction now make the review step explicit. A source scan found no `ACTION_CALL`, `sendTextMessage`, or `sendMultipartTextMessage` calls in Aura's Android source after this change.
- Changed: `app/src/main/java/com/aura/ai/di/ToolModule.kt`, `app/src/main/java/com/aura/ai/domain/usecase/MakeCallUseCase.kt`, `SendSmsUseCase.kt`, `SendMessageUseCase.kt`; added `app/src/main/java/com/aura/ai/agent/tool/ContactActionTools.kt` and `app/src/test/java/com/aura/ai/agent/tool/ContactActionPolicyTest.kt`; updated `AI_HANDOVER.md`, `PROJECT_STATUS.md`, and `NEXT_STEPS.md`.
- Verification: focused `ContactActionPolicyTest` passed. Full offline app + voice tests + APK assembly passed in 2m23s (75 tasks; 5 executed, 70 up-to-date). JUnit XML totals: 68 passed, 0 failures/errors/skips across 22 suites. `git diff --check` passed. Static source scan for direct call/SMS sending APIs returned no matches.
- Fresh debug APK: `app/build/outputs/apk/debug/app-debug.apk`, package `com.aura.ai`, version 1.0.0/code 1; SHA-256 `4cdfdbc615394e201c0395f7d774b32a1b16ddd7b3c3bed5b8b28a912e7399c7`. APK v2 signature verification passed; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- Not verified on the Honor: Android Contacts permission state/prompt, contact matching against real contacts, dialer/SMS intent targets, app/browser/WhatsApp launch, or voice operation. No direct call or SMS was performed. Honor remains disconnected by instruction. Cloud/Ollama are still automated/mock verified only; no provider credential/request or live Ollama call was used. Austrian TTS remains unconfigured.
- Existing database, package identity, settings and user data were not changed; existing permission declarations were left untouched; `main` and personal `device-backups/` were not changed/staged. Record commit and publication status below.
- Next: keep the APK uninstalled until Honor reconnects and the exact build has explicit install approval. Verify backup/package/signer, then exercise granted/denied Contacts permission, unique/ambiguous contact behavior, user-confirmed dial/SMS drafts, WhatsApp draft, voice, and local/cloud connectivity. Continue offline code integration meanwhile.
- Source/tests/records are committed on `integration/aura-voice-consolidation`; final commit IDs and GitHub publication are listed in the following record after commit.

## Contact action implementation commit — 2026-11-06

- Contact search, contact-based call/SMS drafts, legacy ACTION_DIAL/ACTION_SENDTO safety changes, tests and project records are committed as `055d535a0028713fb3c7ce8d6355b80472b929c1` (`Add-contact-actions-and-safe-confirmation`) on `integration/aura-voice-consolidation`. Production `main` remains unchanged; `device-backups/` was excluded. Push status is recorded after publication.

## Contact actions publication — 2026-11-06

- GitHub accepted the contact implementation commit `055d535a0028713fb3c7ce8d6355b80472b929c1` and handover commit `3d66643` on `integration/aura-voice-consolidation` (`a4228e7..3d66643`). The push succeeded. `main` remains `4b48185e9f110764938f10548f80a29acda674f7`; no phone APK was installed. The personal backup directory remains excluded.

## User-selected shared-folder search — 2026-11-06

- Added a Settings flow using Android's Storage Access Framework folder picker. The user chooses one folder and grants persistent read access; Aura stores that folder URI in the existing DataStore settings file, and Remove access releases the persisted grant. No broad storage permission, Room schema change, or other database change was added.
- Added active agent tools `search_files` and `open_shared_file`. They traverse only the selected tree, bound work to depth 8, 2,000 visited entries and 15 results, ignore dot-prefixed entries, search filenames/relative paths only, and pass a single matched file to a compatible installed app with read-only URI access. Aura does not read file contents, write, rename, move or delete files. Duplicate names require an exact relative path.
- Changed: `app/src/main/java/com/aura/ai/data/settings/SettingsRepository.kt`, `app/src/main/java/com/aura/ai/ui/settings/SettingsViewModel.kt`, `app/src/main/java/com/aura/ai/ui/settings/SettingsScreen.kt`, `app/src/main/java/com/aura/ai/di/ToolModule.kt`; added `app/src/main/java/com/aura/ai/agent/tool/SharedFolderFileTools.kt` and `app/src/test/java/com/aura/ai/agent/tool/SharedFileSearchPolicyTest.kt`; updated `AI_HANDOVER.md`, `PROJECT_STATUS.md`, and `NEXT_STEPS.md`.
- Verification: focused `SharedFileSearchPolicyTest` passed. Full offline app/voice unit tests and APK assembly passed in 2m31s (75 tasks; 4 executed, 71 up-to-date). JUnit XML: 71 passed, 0 failures, 0 errors, 0 skipped across 23 suites. `git diff --check` passed.
- Fresh debug APK: `app/build/outputs/apk/debug/app-debug.apk`, package `com.aura.ai`, version 1.0.0/code 1, 20,821,374 bytes, SHA-256 `ffc0e51f2ae16c02004e142bf325310c2bdff681098e8ed5701ae51613c5309f`. APK v2 signature verified; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- Device checks remain pending: actual folder picker URI grants/revocation, behavior with external providers/large trees, and opening representative file types. Honor remains disconnected; no ADB or install was attempted. No phone-to-PC Ollama or live cloud provider request was made. Existing package, databases and user files were not replaced.
- Next: leave this APK uninstalled until the Honor reconnects and the exact build is approved; back up current app data and verify package/signature first. Then grant a test folder in Aura Settings and test filename search, duplicate-name handling, opening a file, access revocation, and re-selection. Continue the voice/backend/Windows work while the phone is offline.
- File-picker/search implementation, tests, and handover updates are committed as `5883d84284f42c3849378010eb4da637ec207bec` (`Add-user-selected-folder-file-tools`) on `integration/aura-voice-consolidation`. `main` is unchanged; publication status is recorded after push.

## File access branch publication — 2026-11-06

- GitHub accepted file-access implementation commit `5883d84284f42c3849378010eb4da637ec207bec` and handover commit `f800d4e`; push `0a94038..f800d4e` succeeded for `integration/aura-voice-consolidation`. Production `main` remains `4b48185e9f110764938f10548f80a29acda674f7`. APK remains uninstalled; personal `device-backups/` was excluded.

## Offline continuation verification — 2026-11-06

- Continued on `integration/aura-voice-consolidation` while the Honor is disconnected. No ADB command, installation, source edit, database change, or permission change was made. `device-backups/` remains untracked and excluded.
- Ran `gradlew.bat --offline --no-daemon -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug`; Gradle completed `BUILD SUCCESSFUL` in 1m13s (75 tasks, all up-to-date). The existing app and voice-library JUnit XML reports total 71 tests across 23 suites, 0 failures, 0 errors, 0 skipped. This invocation reused test outputs; it did not freshly rerun the tests. `git diff --check` passed.
- Verified the existing debug APK artifact: `app/build/outputs/apk/debug/app-debug.apk`, package `com.aura.ai`, version `1.0.0` / code `1`, SHA-256 `FFC0E51F2AE16C02004E142BF325310C2BDFF681098E8ED5701AE51613C5309F`; APK signature verification passed, signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. It was not installed.
- Existing code already routes cloud-first to Ollama fallback with cooldown/retry, offers Android app/web/call/SMS/contact/folder tools, and opens WhatsApp with a draft for user review. Their automated policy/transport tests are among the report totals; this run made no live provider request and no device intent test. No live local-model, cloud credentials, WhatsApp, microphone, wake, speech, TTS, or interruption test was possible without the phone/services.
- No code change was justified by this verification-only pass. The system wake listener still depends on Android `SpeechRecognizer` rather than a dedicated on-device wake engine. The requested Austrian TTS voice remains unconfigured unless supplied by the installed engine or a configured authorized provider. Cloud/local fallback is automated/mock-tested only; live failover and recovery remain unverified. Honor acceptance remains pending reconnection.
- Git state before this record: HEAD and `origin/integration/aura-voice-consolidation` both `8b6fa40b18f0b65e35b58e01d2e113ecb8704280`; `main` unchanged. This record is documentation-only; commit and publication status are recorded below after publication.
- Next steps: keep the APK uninstalled; when the phone reconnects, verify accessible data backup and exact package/signing compatibility before any separately approved install. Then test actual wake, continuous turns, interruption, voice selection, local Ollama inference/model discovery, cloud failover/recovery and WhatsApp draft opening. Continue source improvements without ADB while disconnected.

## On-device wake recognizer preference — 2026-11-06

- Updated the existing `WakeWordService` to create Android's dedicated on-device speech recognizer on API 31+ when the platform reports one available. If that local recognizer cannot be created, it falls back to Android's regular recognizer. Standby requests now set `EXTRA_PREFER_OFFLINE=true`; the platform is allowed to ignore that hint. Wake phrase matching remains exclusively “Yo, yo, yo”; the app package and app data are unchanged.
- Added pure policy tests covering supported/available, pre-31, and unavailable recognizer cases. Changed `app/src/main/java/com/aura/ai/voice/WakeWordService.kt`; added `app/src/main/java/com/aura/ai/voice/wake/WakeRecognizerPolicy.kt` and `app/src/test/java/com/aura/ai/voice/wake/WakeRecognizerPolicyTest.kt`.
- Verification: the first compile found and then a local extra-brace syntax error was corrected. Final `gradlew.bat --offline --no-daemon -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` completed `BUILD SUCCESSFUL` in 8m28s (75 tasks; 14 executed, 61 up-to-date). App + voice-library XML reports: 73 tests across 24 suites, 0 failures, 0 errors, 0 skipped. The new app tests executed; the voice-library suite was up-to-date. `git diff --check` passed.
- Fresh APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,863,017 bytes; package `com.aura.ai`, version `1.0.0` / code `1`; SHA-256 `A88BF09A91C596752669D3D274065B81F66E2D32511D19F10C96685CD45D4363`. `apksigner verify --print-certs` passed; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- No ADB or phone test was attempted. The Honor's on-device recognizer availability, en-ZA offline language support, acoustic wake reliability, microphone behavior, conversation, TTS voice, interruption, live Ollama/cloud, and WhatsApp actions remain unverified. Android documents `EXTRA_PREFER_OFFLINE` as a hint that a recognizer may ignore; using the dedicated on-device API depends on OS/provider support. `main` remains unchanged; `device-backups/` is excluded. Starting HEAD was `8b6fa40b18f0b65e35b58e01d2e113ecb8704280`; commit and publication state follow after documentation commit.
- Next: once the Honor reconnects, first confirm whether Android reports on-device speech recognition available and has en-ZA resources; then test both standby wake and continuous turns. If unavailable or inaccurate, retain the system fallback and select a dedicated wake-word engine only after its model/dependency and licensing are available.

## Wake recognizer commit — 2026-11-06

- Source, policy tests, and initial handover records committed as `13f17e7` (`Prefer on-device speech for standby wake`) on `integration/aura-voice-consolidation`. Production `main` is unchanged; personal `device-backups/` was not staged. The final handover publication record follows after push verification.

## Wake recognizer publication — 2026-11-06

- GitHub accepted source commit `13f17e7` and handover commit `9cfd5ffd70caea7bbb0d9a56b89bbf5e429736c5` on `integration/aura-voice-consolidation`; remote matched local at `9cfd5ffd70caea7bbb0d9a56b89bbf5e429736c5` after push. `main` remains unchanged. `device-backups/` remains excluded. This publication note is committed after push verification.

## Wake language-pack fallback — 2026-11-06

- Extended the standby recognizer fallback: if a dedicated Android on-device recognizer is active and reports `ERROR_LANGUAGE_NOT_SUPPORTED` or `ERROR_LANGUAGE_UNAVAILABLE`, Aura switches to the system recognizer for the remaining service session instead of retrying the unsupported local language indefinitely. Added a policy test for the language-error gate. Changed `app/src/main/java/com/aura/ai/voice/WakeWordService.kt`, `app/src/main/java/com/aura/ai/voice/wake/WakeRecognizerPolicy.kt`, and `app/src/test/java/com/aura/ai/voice/wake/WakeRecognizerPolicyTest.kt`.
- Verification: full offline app/voice tests and APK assemble passed in 7m43s (`:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug`; 75 tasks, 14 executed and 61 up-to-date). App + voice reports total 74 tests / 24 suites, zero failures/errors/skips. The updated app tests executed; the voice library was up-to-date. `git diff --check` passed.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,863,028 bytes; package `com.aura.ai`, v1.0.0/code 1; SHA-256 `4930072AF2184041BD9482DFC67E8B9012347D5B92538DB46F0631C31BB8E3B6`; signature verified; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- No phone/provider acceptance: Honor disconnected, no ADB/install, no confirmation of on-device en-ZA model availability, wake/audio capture, cloud/Ollama, TTS, barge-in or WhatsApp. Android's generic system recognizer may use network even when offline was requested. Started from `071efd56a11791b3bd4736370f8e52b9675a7c1a`; source and documentation commit follows. `main` unchanged; exclude `device-backups/`.
- Next: on Honor reconnection, verify package/signature/data backup before separately approving the exact APK; test with network disabled/enabled to confirm which recognizer is used and whether en-ZA works offline, then test wake/turn-taking. Keep local fallback behavior if the language pack is unsupported.

## Wake language fallback commit — 2026-11-06

- Implementation, policy test, and initial records committed as `8756b1db708a0d7e49d3c067d0ab38333a08d379` (`Fall back when offline wake language is unavailable`) on `integration/aura-voice-consolidation`. `main` unchanged; `device-backups/` excluded. Push and final handover publication state follow.

## Local memory and prior conversation recall — 2026-11-06

- Fixed long-term memory lookup without altering the database schema: query words are tokenized and bounded, memories are fetched for meaningful terms instead of searching only for the whole question phrase, results are deduplicated/ranked by overlap and importance, and up to three preference/setting/instruction/project memories are kept available alongside relevant matches.
- Added bounded historical exchange recall in the existing chat flow. It selects up to three relevant older user/assistant pairs (max 1,800 characters) from the Room-loaded transcript, then adds them before the recent 12 turns. Both ordinary chat and task `UNDERSTAND` prompts now receive that context, so follow-up/action requests do not lose history at the task-planning boundary.
- Changed `app/src/main/java/com/aura/ai/data/repository/T1000MemoryRepository.kt`, `app/src/main/java/com/aura/ai/agent/AgentPrompts.kt`, `AgentOrchestrator.kt`, and `app/src/main/java/com/aura/ai/ui/chat/ChatViewModel.kt`; added `MemoryRecallPolicy.kt`, `ConversationRecallPolicy.kt`, `MemoryRecallPolicyTest.kt`, `ConversationRecallPolicyTest.kt`, and `AgentPromptContextTest.kt`. No Room/database migration or user-data changes.
- Verification: `gradlew.bat --offline --no-daemon -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` — `BUILD SUCCESSFUL` in 9m56s (75 tasks, 15 executed, 60 up-to-date). Reports: 81 tests across 27 suites, 0 failures, 0 errors, 0 skipped. The seven new policy/prompt tests executed. `git diff --check` passed.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,859,496 bytes; package `com.aura.ai`, version 1.0.0/code 1; SHA-256 `9D46DEB9542FE2F200BDA0DDE98B7875B0F1106834D565FBE8808D0F699F56E5`; signature verified; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- Limitations: retrieval is lexical word overlap with simple plural normalization, not embeddings or semantic search. It only selects transcript turns already loaded by the existing local conversation repository; it does not guarantee recall when the user uses unrelated wording. No device, live model/provider, speech or action test was run. Honor remains disconnected; `main` is unchanged and `device-backups/` excluded. Starting HEAD was `66d56a5e67a8b15899cb170dd078411d7f3b9aa9`; commit/publication state follows.
- Next: retain the bounded local retrieval. When the Honor is available and the exact APK is separately approved, test a saved preference and a project fact after app restart, plus a task follow-up referring to an older exchange. If lexical recall misses common paraphrases, improve it with a tested local synonym/embedding strategy without changing the stored database schema.

## Memory recall publication — 2026-11-06

- Memory and prior-exchange recall implementation, policy/prompt tests, and initial status records are committed as `13b64ed0d788be2881e8375f798fc999b715e45b` (`Recall relevant memories and prior exchanges`) on `integration/aura-voice-consolidation`.
- GitHub accepted the commit on `origin/integration/aura-voice-consolidation`; production `main` was not changed. APK remains uninstalled. `device-backups/` remains untracked and excluded.
- Final verification: offline app and voice unit tests plus debug APK assemble succeeded; 81 tests, 0 failures/errors/skips. APK SHA-256 `9D46DEB9542FE2F200BDA0DDE98B7875B0F1106834D565FBE8808D0F699F56E5`, signer verified. No phone, live model/provider, voice, or action acceptance test was performed.
- Next: when the Honor reconnects, verify memory recall after restart and older-exchange task context; then resume pending device voice, Ollama/cloud failover, TTS, interruption, and WhatsApp checks.

## Offline quick answers for time and arithmetic — 2026-11-06

- Added an exact local-time intent for common phrasings such as “what time is it?” and “tell me the time,” including when preceded by “Yo, yo, yo.” It formats the phone/computer's current local zone and deliberately declines requests for another city or a meeting time. Existing arithmetic answers are kept in the same `LocalAnswerIntent` fast path.
- Moved deterministic answers ahead of model configuration in `AgentOrchestrator`; ChatViewModel now allows only these matched local questions through when no model backend is ready. Other requests continue to require the configured cloud/local backend. This reduces latency and gives basic answers during backend outages; it does not claim general offline reasoning.
- Changed `app/src/main/java/com/aura/ai/agent/AgentOrchestrator.kt` and `app/src/main/java/com/aura/ai/ui/chat/ChatViewModel.kt`; added `LocalAnswerIntent.kt`, `SimpleTimeIntent.kt`, `LocalAnswerIntentTest.kt`, and `SimpleTimeIntentTest.kt`. No database, user data, permissions, or app identity changed.
- Tests: the first PowerShell invocation misparsed the Gradle property; the next reached Gradle but hit a temporary dependency binary-store write error. The corrected Windows Command Prompt invocation succeeded: `gradlew.bat --offline --no-daemon --stacktrace -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest` (`BUILD SUCCESSFUL`, 8m28s). Then full app/voice test and assemble command succeeded (`BUILD SUCCESSFUL`, 2m45s). XML reports: 76 app tests + 10 voice-library tests = 86 total; 0 failures, 0 errors, 0 skipped. App tests were freshly run; voice suite outputs were up-to-date and unchanged by this task. `git diff --check` passed.
- Fresh APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,821,779 bytes; package `com.aura.ai`, version `1.0.0` / code `1`; SHA-256 `D9F08E1338CE993E75F6A7014F5DFA0F1D978DF88595985CEC9A4A320FAF0713`. `apksigner verify --print-certs` passed; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. APK was not installed.
- No ADB or real device/provider tests were run: Honor is disconnected. Wake activation, continuous multi-turn voice, transcripts on phone, interruption, TTS voice/accent, Ollama connection, cloud failover/recovery, and WhatsApp draft remain pending actual device/service verification. Starting HEAD: `b396baf3df81a92d9194b82b6013a8fdfdd924ad`; source and initial documentation commit follows. `main` unchanged; pre-existing `device-backups/` excluded.
- Next: on phone reconnection, after exact-build approval and package/signature/data checks, test spoken time and arithmetic with cloud/Ollama unavailable, then restore backend and verify multi-turn voice, barge-in, selected TTS and WhatsApp draft. Basic local quick answers do not replace those checks.

## Offline quick-answer publication — 2026-11-06

- Source and initial test/build records committed as `bfe0f2ef81da6205f706e487684f9fd2a38730b2` (`Answer basic time questions offline`) on `integration/aura-voice-consolidation` and accepted by `origin`.
- Production `main` is unchanged. APK is local and uninstalled; pre-existing `device-backups/` is excluded. APK hash/signature and the 86-test result are in the implementation record above.
- Phone wake, speech recognition, TTS, interruption, Ollama/cloud, and WhatsApp remain unverified until Honor reconnects. Continue code-only work meanwhile.

## Persistent Android voice tuning controls — 2026-11-06

- Added bounded pitch (0.70–1.30) and speech-rate (0.85–1.15) controls for selected Android voices in Settings. Values save through the existing `VoiceSettingsStore`/SharedPreferences configuration and survive process restarts by design. Voice identity is unchanged. The controls are hidden for cloud voices because that provider controls its own prosody; changing pitch alone does not create an Austrian accent.
- Changed `app/src/main/java/com/aura/ai/ui/settings/SettingsViewModel.kt` and `SettingsScreen.kt`; added `lib/aura-voice/src/main/kotlin/com/aura/voice/VoiceTuningPolicy.kt` and its two unit tests. No database or permission change.
- Verification: `gradlew.bat --offline --no-daemon -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` completed `BUILD SUCCESSFUL` in 13m33s. XML reports: 76 app tests + 12 voice-library tests = 88; 30 suites, 0 failures, 0 errors, 0 skipped. The new tuning policy tests executed. `git diff --check` passed.
- Fresh APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,821,779 bytes; package `com.aura.ai`, version 1.0.0 / code 1; SHA-256 `34770A6C4FB123D9034983CB7FCCEF561C058A84FD28C1EA79C7A74F98DBE5C2`. `apksigner verify --print-certs` passed; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. It was not installed.
- No device test was possible: Honor remains disconnected. Android Settings slider interaction, voice persistence after app restart, preview audio, selected Austrian voice, continuous listening, interruption, Ollama/cloud, and WhatsApp remain unverified on-device/live provider. Starting HEAD `97baa3e4f5b9a148ff2e19e80bd48ad0ba3435b6`; source and handover commit follows. `main` unchanged; exclude `device-backups/`.
- Next: on reconnection and after approval of this exact APK, adjust Android voice pitch/rate, reopen Settings to confirm persistence, preview the exact selected voice, and check it remains the same during multi-turn conversation. Confirm accent through actual voice playback; do not infer it from slider values.

## Voice tuning publication — 2026-11-06

- Android voice-tuning implementation, tests, and initial handover records are committed as `af603cf83fe379e275681b4676b8c298505b39e5` (`Add persistent Android voice tuning controls`) on `integration/aura-voice-consolidation` and accepted by `origin`.
- Production `main` remains unchanged. APK is not installed; `device-backups/` remains untracked and excluded. APK SHA-256 and test totals are in the implementation section above.
- No Honor/settings/preview playback or live AI/action test was run. On reconnection, verify saved tuning and selected voice audibly before making any voice-quality claim.

## Wake-listener quiet-period recovery — 2026-11-06

- Fixed the wake service's restart policy: Android speech-timeout and no-match outcomes are normal idle periods, so they now resume recognition after 800 ms and reset the consecutive-failure counter. Genuine recognizer/provider errors retain exponential backoff from 1 to 16 seconds. This removes the prior case where repeated silence could leave up to 16-second gaps between wake checks.
- Changed `app/src/main/java/com/aura/ai/voice/WakeWordService.kt`; added `app/src/main/java/com/aura/ai/voice/wake/WakeRestartPolicy.kt` and `app/src/test/java/com/aura/ai/voice/wake/WakeRestartPolicyTest.kt`. The wake phrase remains “Yo, yo, yo”; no permissions or app identity changed.
- Verification: `gradlew.bat --offline --no-daemon -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` completed `BUILD SUCCESSFUL` in 10m41s. XML reports: 78 app tests + 12 voice-library tests = 90; 31 suites, 0 failures, 0 errors, 0 skipped. Both new retry-policy tests executed. `git diff --check` passed.
- Fresh APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,877,242 bytes; `com.aura.ai`, v1.0.0/code 1; SHA-256 `C1BD74A4DAC4C51814409C1D16D3206DDE35310AE8EF90B4D742CE229F4EDBB6`. `apksigner verify --print-certs` passed; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. APK is not installed.
- Honor remains disconnected; no ADB or real-device wake, mic, speech, TTS, interruption, Ollama/cloud or WhatsApp test was run. This code test does not prove acoustic wake reliability on the phone. Starting HEAD `be455a66ccaa5b24b972a88b8ec4423ccf792028`; source and record commit follows. `main` unchanged; `device-backups/` excluded.
- Next: when the Honor reconnects and the exact APK has separate install approval, test “Yo, yo, yo” after repeated silence, then multi-turn speech and interruption. Observe whether Android's recognizer itself emits microphone sounds; this restart policy cannot suppress OEM/platform sounds.

## Wake retry recovery publication — 2026-11-06

- Wake idle recovery source/tests and initial records are committed as `abf83a815d7311d153fbf3eb4e347e7394f0585c` (`Keep wake listening responsive after silence`) on `integration/aura-voice-consolidation` and accepted by `origin`.
- Production `main` is unchanged. The APK remains uninstalled; `device-backups/` remains excluded. APK and 90-test details are recorded above.
- Phone wake/acoustic, continuous voice, interruption, TTS identity/accent, local/cloud inference and WhatsApp remain unverified. Continue code-only work while disconnected.

## Conversation intent routing repair — 2026-11-06

- Narrowed the existing conversation-vs-agent classifier. Action routing now requires a command phrase at the start of the utterance (after common polite prefixes or the configured “Yo, yo, yo” wake phrase), with explicit call/reminder patterns. This prevents ordinary questions such as “What does this WhatsApp message mean?”, “What is a message queue?”, “What does this code do?”, and “How do I find a file?” from being sent to the multi-step action planner merely because they contain an action-related word.
- Changed: `app/src/main/java/com/aura/ai/agent/ConversationIntent.kt` and `app/src/test/java/com/aura/ai/ConversationIntentTest.kt`. Existing app, database, user data, voice identity, and package identity were not changed.
- Tests: `:app:testDebugUnitTest` passed after the change. Its XML reports show 78 tests, 0 failures, 0 errors, 0 skipped, across 27 suites. The 12 existing `:aura-voice:testDebugUnitTest` report entries also show 0 failures/errors/skips (4 suites); an additional attempt to rerun that task was blocked before Gradle started by access denied on the Gradle wrapper distribution lock. No voice-library source changed in this task. `git diff --check` passed with only Git's LF-to-CRLF normalization warnings.
- Build: the combined offline test-and-assemble command reached APK packaging but failed during D8 dex merge with a Windows disk-space error. No files were removed. Retrying `:app:assembleDebug` offline with `--max-workers=1` completed `BUILD SUCCESSFUL`; the APK is `app/build/outputs/apk/debug/app-debug.apk`, 20,877,242 bytes, package `com.aura.ai`, version 1.0.0/code 1, SHA-256 `1348A0BC74D1B83C6BF1D013C2500AC105443BB00793946319CAA473A07ED012`. `apksigner verify --print-certs` passed; signer SHA-256 is `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- Device/live status: Honor is disconnected. APK was not installed. No microphone, wake, voice response, interruption, Ollama/cloud provider, WhatsApp, or real-device acceptance test was run. This classifier repair is JVM-tested only. Cloud credentials and live Ollama service are not available in this check.
- Starting commit: `f7107c36ab7bcb4a3fabbb05fe0ad5e4b05a2b00` on `integration/aura-voice-consolidation`; `main` remains unchanged. Exclude the pre-existing untracked `device-backups/` directory.
- Next: commit source/test and project records together, push the integration branch, then after the Honor reconnects and the owner approves installation of this exact build, verify normal questions do not receive a spoken action acknowledgement and explicit action commands still enter the guarded planner. Continue live backend and voice checks separately; do not mark them verified based on these unit tests.

## Conversation routing implementation commit — 2026-11-06

- Source, regression tests, and initial records committed on `integration/aura-voice-consolidation` as `a487a29` (`Keep ordinary questions out of action planner`). `main` unchanged. The existing untracked `device-backups/` directory was excluded.
- Branch publication is the remaining repository step; record the remote result below after pushing. APK remains uninstalled and phone/live-service checks remain pending.

## Conversation routing publication — 2026-11-06

- GitHub accepted the implementation commit `a487a29` and handover commit `035ee0f`; remote `origin/integration/aura-voice-consolidation` now resolves to `035ee0f`. Production `main` remains unchanged. APK remains uninstalled; device and live-provider checks are pending.

## No-backend response reliability — 2026-11-07

- Removed the silent chat gate when no model is configured. After saved history loads, the composer and hands-free control remain available; the existing orchestrator now emits its explicit no-model failure, which is rendered and persisted through the normal assistant turn path and can be spoken when voice replies are enabled. The failure text now directs the user to select Ollama or complete cloud setup. This does not run an action or claim that a model answered.
- Changed: `app/src/main/java/com/aura/ai/ui/chat/ChatBackendAvailabilityPolicy.kt`, `ChatScreen.kt`, `ChatViewModel.kt`, `app/src/main/java/com/aura/ai/data/remote/ollama/OllamaResult.kt`, and `app/src/test/java/com/aura/ai/ui/chat/ChatBackendAvailabilityPolicyTest.kt`; project records updated. No permission, database, package, or stored setting changed.
- Verification: `cmd.exe /d /c ".\gradlew.bat --offline --no-daemon --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :app:assembleDebug"` completed `BUILD SUCCESSFUL` in 9 minutes (70 tasks; 14 executed, 56 up-to-date). App + voice-library JUnit XML: 91 tests / 31 suites, 0 failures, 0 errors, 0 skipped. The new composer-availability test ran. `git diff --check` passed with CRLF normalization warnings. The default wrapper-cache access was denied initially; retry with existing Gradle cache access passed. No cache was cleared.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,877,183 bytes; package `com.aura.ai`, version 1.0.0/code 1; SHA-256 `7C860B980741E322B134F3C9D1662A78F701DD39CA0978FFF192F329CA4E527C`. `apksigner verify --print-certs` passed; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- Device/live status: Honor disconnected; no ADB, mic, transcript, TTS, wake, interruption, live Ollama/cloud, or WhatsApp test. This is JVM/build verification only. Cloud credentials are not configured for live checks and Ollama was not contacted.
- Starting commit: `6ee487ae47146c2b47fe1c6860e1cbc37c37bc33`, branch `integration/aura-voice-consolidation`. `main` unchanged. Exclude pre-existing `device-backups/`.
- Next: commit and push this fix with the project records. Then keep working on provider/voice reliability in code while disconnected; when the Honor reconnects, install only after approval for this exact APK and test the no-backend spoken status, selected voice, wake/multi-turn flow, interruption, Ollama/cloud fallback, and WhatsApp draft.

## No-backend response implementation commit — 2026-11-07

- The code, regression test, and project records are committed as `5c93f2a` (`Explain missing model instead of dropping chat`) on `integration/aura-voice-consolidation`. `main` unchanged; `device-backups/` excluded. Push and remote status follow.

## No-backend response publication — 2026-11-07

- GitHub accepted implementation commit `5c93f2a` and handover commit `6dc324a`; `origin/integration/aura-voice-consolidation` resolves to `6dc324aec2d6616da3e57ba9dfebfaae5ee56da5`. `main` remains unchanged. APK not installed; phone/live-provider checks remain pending.

## Android speech-recognition stall recovery — 2026-11-07

- Added a 30-second no-progress watchdog to the existing Android in-app `SpeechRecognizer` turn. Speech-begin and partial-result callbacks restart the watchdog. If the recognizer stops responding, Aura now emits a recoverable speech error, releases that request through the existing completion path, and the chat's existing retry logic can reopen listening. Final results, errors, stop, close, and replacement turns cancel the watchdog. This is a safety bound for a stalled recognizer, not a claim that Android ASR is always fast or accurate.
- Changed: `lib/aura-voice/src/main/kotlin/com/aura/voice/android/AndroidSpeechRecognizerProvider.kt`; added `lib/aura-voice/src/main/kotlin/com/aura/voice/android/RecognitionTimeoutPolicy.kt` and `lib/aura-voice/src/test/kotlin/com/aura/voice/RecognitionTimeoutPolicyTest.kt`; updated the project records. No permission, package identity, app data, voice ID, or database changed.
- Verification command: `cmd.exe /d /c ".\gradlew.bat --offline --no-daemon --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug"` completed `BUILD SUCCESSFUL` in 9m01s (75 tasks; 15 executed, 60 up-to-date). App + voice-library XML: 94 tests / 32 suites, 0 failures, 0 errors, 0 skipped. Three timeout-policy tests passed. `git diff --check` passed with Git line-ending normalization warning.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,877,183 bytes; package `com.aura.ai`, version 1.0.0/code 1; SHA-256 `D7BADFCC1402F85E786D00131087E6BF3CFC4DA88218F0A0259D2700EEBEF293`. `apksigner verify --print-certs` passed; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- Device/live status: `adb devices -l` listed no device. No phone microphone, wake phrase, transcript timing, TTS, interruption, Ollama/cloud, or WhatsApp test was performed. In this check, GitHub was also unreachable (`git ls-remote` could not connect to github.com:443 through the configured local proxy); record local commit and retry publication when connectivity returns.
- Starting commit: `091c7a400e0969856fdd92a0cb055505598f7523` on `integration/aura-voice-consolidation`. Do not merge to `main`; exclude pre-existing `device-backups/`.
- Next: commit the source and records locally. Retry pushing the authorized integration branch when GitHub is reachable. After the Honor reconnects and this exact APK is approved, verify no-callback recovery, natural pauses, partial transcript progress, and successive turns on the actual device. Keep the 30-second bound under review if device tests show unwanted cutoffs.

## Speech-recognition watchdog commit and publication — 2026-11-07

- Implementation/tests/initial records committed as `6518392` (`Recover stalled Android speech recognition`) on `integration/aura-voice-consolidation`. GitHub accepted the push (`091c7a4..6518392`). Remote branch verification and final documentation commit follow. Production `main` unchanged; APK is not installed.

## Speech-recognition watchdog final publication — 2026-11-07

- Final handover commit `a1a3447` is also pushed. `git ls-remote` confirms `origin/integration/aura-voice-consolidation` at `a1a34472b12d738ac54d16a84187ef6c77e1c8ef`. Production `main` remains unchanged. `device-backups/` remains untracked/excluded.

## Low-confidence spoken-action confirmation — 2026-11-07

- Added an ASR-confidence guard for agent actions. When Android supplies a valid confidence score below 0.45 and the utterance is classified as an action request, Aura preserves the recognized text in the transcript and asks the user to repeat it before sending anything to the agent. The clarification is persisted and optionally spoken through the currently selected voice; hands-free listening then resumes. Ordinary chat, confidence scores that are absent/invalid, and action requests at or above the threshold keep the existing flow.
- Changed: `app/src/main/java/com/aura/ai/ui/chat/ChatViewModel.kt`; added `app/src/main/java/com/aura/ai/ui/chat/SpeechCommandConfidencePolicy.kt` and `app/src/test/java/com/aura/ai/ui/chat/SpeechCommandConfidencePolicyTest.kt`. No package, permissions, databases, stored settings, selected voice ID, or existing action implementations changed.
- Automated verification: `cmd.exe /d /c ".\gradlew.bat --offline --no-daemon --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug"` completed `BUILD SUCCESSFUL` in 9m58s. App and voice-library reports total 97 tests across 33 suites, 0 failures, 0 errors, 0 skipped; three new policy tests passed. `git diff --check` passed.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,964,676 bytes, SHA-256 `DDC6A445B95E760D118F0D77E0D463DA0E84F93B3232E079F3FD57296530B4B0`; package `com.aura.ai`, version 1.0.0/code 1. `apksigner verify --print-certs` passed; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. APK was not installed.
- Branch: `integration/aura-voice-consolidation`; starting commit `2f6c76dfdf6376a43d66376698c88ee4814defb3`; implementation and records committed as `8efb987ea687283edfbd71aff6d11bc86fa70294` (`Guard low-confidence spoken actions`) and pushed. Remote branch verification confirms the same commit. `main` is unchanged; pre-existing `device-backups/` remains excluded. The phone is disconnected; no live ASR calibration, conversational-action, TTS, wake-word, interruption, Ollama/cloud, or WhatsApp test was performed. The 0.45 threshold is an uncalibrated conservative starting point and may ask for repeats unnecessarily on the Honor. This change does not improve transcription accuracy; it prevents low-confidence action execution.
- Next: validate confidence values and repeat behavior on the Honor before tuning the threshold; then continue local Ollama/cloud routing and WhatsApp action work. Keep the original app/data intact, do not install without exact-build approval, and do not merge to `main`.


## Provider cancellation regression check — 2026-11-07

- Reviewed `ProviderChatRouter`: `TimeoutCancellationException` is handled as a cloud timeout before the general cancellation branch, which may incorrectly start local fallback when the caller itself timed out. Added a regression test to the local worktree at `app/src/test/java/com/aura/ai/data/remote/inference/ProviderChatRouterTest.kt`; this test change is uncommitted and not present on the branch.
- Test command attempted: `cmd.exe /d /c ".\\gradlew.bat --offline --no-daemon --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest --tests com.aura.ai.data.remote.inference.ProviderChatRouterTest.callerTimeoutDoesNotStartLocalFallbackAfterCancellation"`. It did not reach test execution: Gradle failed during `:app:compileDebugKotlin` while writing compiler output with `There is not enough space on the disk` (47m22s). The C: drive reported 0 bytes free afterward. No production-code fix or new APK was produced.
- During the run, additional uncommitted voice/settings changes appeared in the worktree. They were not modified or staged. Preserve and review them before any commit. Existing APK and `device-backups/` were left intact.
- Next: restore disk space, inspect the in-progress voice/settings changes, then implement and run the cancellation regression test plus full app/voice tests. Keep the Honor acceptance pending; do not install or merge to `main`.


## Cloud cancellation guard — 2026-11-07

- Fixed the cloud-first router so a timeout caused by caller cancellation is rethrown before local fallback. Provider-owned cloud timeouts still fall back to Ollama when the parent request remains active.
- Added regression coverage for caller timeout cancellation. Changes are on `integration/aura-voice-consolidation`: router commit `904b6bbd011ee9a139981bcc15e3d9e045443218`; test commit `9666bba3575c6a3d8a66706259702026b6a515b4`. These are source commits only; no merge to `main`.
- Verification is pending. Gradle previously exhausted C: while compiling, and the latest check found only 967 MB free. The focused test did not execute; no new APK was built or installed. Honor remains disconnected, so no real-device, Ollama, cloud, voice, wake, interruption, or WhatsApp acceptance is claimed.
- The shared local checkout has other uncommitted voice/settings changes and personal `device-backups/`; those were preserved and excluded from the two source commits. Local Git metadata is in a protected worktree directory, and fetch was denied, so local HEAD still trails the remote branch. GitHub API publication confirms these commits on the integration branch.
- Next: free several GB of disk, sync the local worktree to the integration branch without discarding its in-progress voice edits, run the focused router test and full app/voice test plus debug APK build, verify package/signature/hash, then update test evidence. On phone reconnect, perform previously pending microphone, speech, interruption, Ollama/cloud recovery, and WhatsApp draft checks. Keep APK installation and main merge off until separately authorized.


## Verification follow-up — 2026-11-07

- The focused cancellation test passed. The combined command `.\gradlew.bat --offline --no-daemon --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` completed `BUILD SUCCESSFUL` in 11m16s. XML reports: 101 tests, 0 failures, 0 errors, 0 skipped across 34 report files. This includes app and voice-library tests.
- Fresh local debug APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,703,885 bytes, SHA-256 `D755ADDBD94D23A707016DF23421CE9BA7B579CBF64F2CB4A9A86BD3BBF6B716`. Package `com.aura.ai`, version 1.0.0/code 1. `apksigner verify --print-certs` passed; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- Important provenance: this build came from the local dirty worktree at HEAD `a3e5c1cc79f78dd916c584833704f812efd84bcb`, containing additional uncommitted voice/style/settings changes. The router and test commits are on the remote integration branch, but local Git metadata access is blocked, so this APK is not certified as an exact clean remote-tip build. Do not install until local/remote state is reconciled and the exact source artifact is verified.
- Honor remains disconnected. No real-device voice, wake, interruption, Ollama, cloud, or WhatsApp test/installation was performed. Main unchanged.


## Exact acknowledgement build verification — 2026-11-07

- Confirmed the existing action acknowledgement is exactly `I'll be back`; ordinary questions retain their direct-response route.
- Rebuilt and reran `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug`: `BUILD SUCCESSFUL` in 9m19s. Latest reports contain 101 tests, 0 failures, errors, or skips (voice-library tests were up-to-date from the prior full suite).
- APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,815,582 bytes, SHA-256 `29BC3FA4F5C1E4255CED20C7685BF8C8FD68156757CC63A38C8FB4075D81FCF9`; package `com.aura.ai`, version 1.0.0/code 1. Signature passed; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- This artifact was built from the synchronized local branch tip plus uncommitted ElevenLabs style-control edits. It is not an exact clean remote-tip artifact. No install; Honor remains disconnected. Austrian accent/voice playback, live Ollama/cloud routing, wake, barge-in, WhatsApp and real-device conversation remain unverified.
- The cloud cancellation guard and regression test are committed on the integration branch. Local style-control changes remain uncommitted and were not staged. Do not merge to `main`.


## Honor Ollama endpoint and recognition retest — 2026-11-07

- On the Honor, the original `com.aura.ai` app had a loopback Ollama URL that cannot reach the Windows PC. Updated settings normalization to migrate legacy `127.0.0.1` alongside known emulator/loopback URLs, while preserving user-entered LAN URLs. Settings copy now reflects the configured Base URL instead of claiming a fixed address. Added three endpoint-policy tests.
- Device configuration was saved as `http://192.168.8.18:11434`; Aura Settings returned ONLINE and found `qwen3:1.7b` and `qwen3:4b-q4_K_M`. This verifies `/api/tags` discovery only, not chat generation.
- App and voice test reports contained 104 tests with zero failures/errors/skips. Debug APK assembly succeeded after an initial D8 disk-space failure; only failed generated dex intermediates were removed.
- APK installed in place after backup, package/signature verification: `com.aura.ai` v1.0.0/code 1; SHA-256 `AA123E89EFA64194EC66D439B54F2888DCA5E86D541157612A16E5417DAA7759`; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Pre-update app-data archive SHA-256 `811FCD9129E26C14797A50C4EDDD8CA9BC4A7926233DC54670515501ED72A64C`; existing app was updated with `adb install -r`, not uninstalled or cleared.
- Owner then reported an incorrect transcript. ADB confirmed the original app was foreground and the separate debug package absent. The exact transcript/confidence was not captured before the phone returned to the lock screen. Treat recognition as failed and unresolved; no ASR tuning based on guesswork. No post-update voice pass is claimed.
- Local checkout branch `integration/aura-voice-consolidation`, local HEAD `d5d0fe92ca890c964685403aee0fcafd3fe30fd6`; no local Git metadata mutation. Keep private `device-backups/` out of Git. Main remains unchanged.

Next: capture the exact diagnostic transcript/confidence on the unlocked original app, then fix the measured recognition issue; verify Qwen generation through Aura, spoken answer, second turn, wake, interruption, and voice consistency.

## Publication metadata — 2026-11-07

- Source and test changes, plus the Honor verification record, were published to this integration branch in commit `74848b6029db3368b01f27cb8dda997d12695740`. Files: `SettingsRepository.kt`, `SettingsScreen.kt` (Ollama endpoint text), `OllamaEndpointPolicyTest.kt`, and all four continuity documents. Main was not changed; device backups were excluded.
- The commit records 104 passing unit tests and a verified in-place APK install. The owner's latest recognition result is still failed and exact transcript capture is pending; no device voice pass is claimed.
- Workspace branch remains `integration/aura-voice-consolidation`, local HEAD `d5d0fe92ca890c964685403aee0fcafd3fe30fd6`; local Git metadata is protected, so no local branch mutation was performed.


## Wake transcript diagnostic build/install — 2026-11-07

- Wake-triggered utterances now retain the full recognition transcript in the Agent diagnostic line before the command tail is sent to the model. Files: `app/src/main/java/com/aura/ai/ui/chat/ChatViewModel.kt`, new `WakeTranscriptDiagnostic.kt`, and new `app/src/test/java/com/aura/ai/ui/chat/WakeTranscriptDiagnosticTest.kt`. This improves diagnosis visibility; it is not a claim that acoustic recognition accuracy itself is fixed.
- Automated verification on BEATK: offline app + aura-voice unit tests and debug assembly completed BUILD SUCCESSFUL in 7m17s; 106 tests, 0 failures/errors/skips. Existing Moshi Kapt deprecation warning only.
- APK `app/build/outputs/apk/debug/app-debug.apk`: 20,824,770 bytes; SHA-256 `1D2DDE7F3F4B90A5732D06E4C027C42A5C32393DE2A92ACCE681225C152A80C8`; package `com.aura.ai`, version 1.0.0/code 1; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3` verified.
- Before update, private app-data backup `device-backups/com.aura.ai-pre-wake-diagnostic-20261107.tar` (822,784 bytes; SHA-256 `4E8A290A49EE69F23D78F6DA23A592A6CA7B2C6AB19E9B23D0816F1B71D6865B`) was created. Installed package/signature matched; `adb install -r` returned Success with no uninstall/data clear. Pulled installed base APK hash exactly matched the built APK. Backups are excluded from Git.
- Device remained at lock screen during the check; no post-update transcript, Qwen chat-generation, TTS, wake, interruption, or multi-turn pass is claimed. User was asked to unlock and capture the new `Wake heard:` line and spoken response.
- Local workspace branch `integration/aura-voice-consolidation`, HEAD `d5d0fe92ca890c964685403aee0fcafd3fe30fd6`; protected local Git metadata was not mutated. No merge to main.


## Source commit reference — 2026-11-07

- Wake transcript visibility implementation, its unit tests, and build/install verification records are in commit `04e5cc1f3092de6b824cef5b7c9dccd91b7e03b8` on `integration/aura-voice-consolidation`.
- The debug APK is installed in place on the Honor after the verified app-data backup. Automated tests/build passed; actual spoken transcript and response are still awaiting an unlocked-phone retest. `main` is unchanged.


## Current Ollama availability — 2026-11-07

- A direct non-streaming `qwen3:1.7b` request from BEATK to `http://192.168.8.18:11434/api/generate` returned TCP connection refused. Local Ollama is unavailable at this check; earlier Settings `/api/tags` discovery is historical and does not prove current generation.
- Ollama was not started because the owner previously stopped it to reduce PC load. No cloud key or app setting was changed. Router fallback has unit coverage, but live cloud fallback/device inference remain unverified.
- Wake-transcript APK installation and test details are in source commit `04e5cc1f3092de6b824cef5b7c9dccd91b7e03b8`. Honor remains locked; wake transcript acceptance is pending. Main unchanged; private backups excluded.
- Next: unlock Honor and capture `Wake heard:`; when local processing is wanted, start Ollama deliberately and verify both `/api/tags` and one generated response through Aura. Separately verify cloud response while local is offline.


## Cloud-first preference and CI test gate — 2026-11-07

- Changed `T1000Settings` so an unset Cloud preference defaults on, matching the owner's online-first goal. An explicitly saved Cloud-off preference remains respected by DataStore. Cloud calls still require a configured URL, model, and encrypted key; existing daily request cap and Ollama fallback are unchanged.
- Added a unit test asserting the default and updated GitHub Actions Android workflow to run app tests, voice-module tests, and debug APK assembly on branch updates.
- Local verification attempt for this change: `:app:testDebugUnitTest :aura-voice:testDebugUnitTest` failed before tests completed after 7m12s because C: ran out of disk. Gradle failed at `:app:bundleDebugClassesToRuntimeJar`; Windows reported 0 bytes free. The previous 112-test/ APK success predates this preference change and does not verify it. No APK for this change was produced.
- Latest reported Honor test remains incorrect transcription; exact recognized words/confidence remain unavailable. Honor is not connected, so there is no phone verification.
- The remote integration branch was read at `33829ebc09ad9995bddcfe951747ff9fd2bc29dc`. This commit only changes the settings default/test/CI gate and this record; main remains unchanged. Local working tree still has earlier uncommitted voice/UI changes and is four commits behind that remote revision at the last local status check. Private backups remain excluded.
- Next: wait for GitHub Actions to run the expanded verification on this commit; do not claim it passed until the workflow reports success. To build locally, the owner must free several GB on C:; no files/caches/data were deleted. Then reconnect the Honor, request approval for the exact fresh APK, back up and verify package/signature before in-place install, and capture exact transcript/confidence plus spoken answer.


## Verification correction — 2026-11-07

- Current integration branch head is `42c6b8851620f8007ab5e6a1f86906226c0c2278` (commit: `Prefer cloud reasoning by default and verify Android changes in CI`). Production `main` was not modified.
- GitHub Actions run `42344494193` was retried once. Both attempts ended in failure before any job steps ran; job `128319519371` has `steps: null`, and the log endpoint returned 404. This does not establish a source/test failure; CI did not provide test evidence.
- The local test command reached Android/Kotlin compile tasks, then failed at `:app:bundleDebugClassesToRuntimeJar` because C: had 0 bytes free. Unit tests did not complete. No APK was produced for the cloud-default change. Earlier 112-test/APK results predate this change and do not verify it.
- Latest owner-reported Honor check: wake phrase produced an incorrect transcript. The exact recognized text/confidence was not captured. Phone/ADB is disconnected, so no new device verification or installation occurred.
- Source review found Android `SpeechRecognizer` used for repeated wake listening; no Vosk dependency, engine implementation, or model was found. Dedicated local wake-word detection is still outstanding.
- Changed code in commit `42c6b8851620f8007ab5e6a1f86906226c0c2278`: `app/src/main/java/com/aura/ai/settings/T1000Settings.kt`, `app/src/main/java/com/aura/ai/data/SettingsRepository.kt`, `app/src/test/java/com/aura/ai/agent/ChatBackendAvailabilityPolicyTest.kt`, and `.github/workflows/android-build.yml`. That commit also updated the project records. The cloud-default behavior is not yet test-verified.
- Follow-up documentation commit: records CI retry result, latest incorrect transcript, storage blocker, and wake-engine status. Main remains unchanged; no phone install.
- Next: free several GB on C: without deleting user data, rerun unit tests and APK assembly; reconnect Honor, back up data and verify package/signature before installing an approved exact APK; capture the actual ASR transcript/confidence and spoken output; then address the recognized-word failure using that diagnostic evidence. Implement/verify a dedicated wake-word engine separately. Configure cloud credentials and Ollama deliberately before live fallback tests.


## Published transcript diagnostics and voice controls — 2026-11-07

- Updated `integration/aura-voice-consolidation` with commit `db11eedcbea3ac0affd606a5a7fd994f90fa0b1a`, parent `184319cc60df1064e0985879e56416a48ac47c21`. `main` was not changed. The reviewed source update brings the current local voice work onto the GitHub branch while preserving its newer wake-transcript plumbing.
- Changes include: pass wake confidence and alternate hypotheses from Android recognition into the chat diagnostics; retain and display the full wake transcript; request multiple candidates and conditionally bias the recognizer toward “Yo, yo, yo” on Android 13+; ask to repeat low-confidence actionable commands; expose copy controls for completed chat messages; and include the associated recognizer, transcript, voice-settings, acknowledgement, and router changes/tests. The code is still based on Android `SpeechRecognizer`; this does not add Vosk or prove acoustic accuracy.
- Published source/test paths: `app/src/main/java/com/aura/ai/MainActivity.kt`, `app/src/main/java/com/aura/ai/agent/AgentOrchestrator.kt`, `app/src/main/java/com/aura/ai/agent/AgentPrompts.kt`, `app/src/main/java/com/aura/ai/agent/ResponseAcknowledgementSanitizer.kt`, `app/src/main/java/com/aura/ai/data/remote/inference/ProviderChatRouter.kt`, `app/src/main/java/com/aura/ai/ui/chat/ChatScreen.kt`, `app/src/main/java/com/aura/ai/ui/chat/ChatViewModel.kt`, `app/src/main/java/com/aura/ai/ui/chat/SpeechCommandConfidencePolicy.kt`, `app/src/main/java/com/aura/ai/ui/chat/WakeTranscriptDiagnostic.kt`, `app/src/main/java/com/aura/ai/ui/settings/SettingsScreen.kt`, `app/src/main/java/com/aura/ai/ui/settings/SettingsViewModel.kt`, `app/src/main/java/com/aura/ai/voice/WakeWordEvents.kt`, `app/src/main/java/com/aura/ai/voice/WakeWordService.kt`, `app/src/main/java/com/aura/ai/voice/tts/ElevenLabsSpeechTransport.kt`, `app/src/main/java/com/aura/ai/voice/wake/WakePhraseMatcher.kt`, `app/src/test/java/com/aura/ai/ResponseAcknowledgementSanitizerTest.kt`, `app/src/test/java/com/aura/ai/WakePhraseMatcherTest.kt`, `app/src/test/java/com/aura/ai/ui/chat/SpeechCommandConfidencePolicyTest.kt`, `app/src/test/java/com/aura/ai/ui/chat/WakeTranscriptDiagnosticTest.kt`, `app/src/test/java/com/aura/ai/voice/tts/ElevenLabsSpeechTransportTest.kt`, `lib/aura-voice/src/main/kotlin/com/aura/voice/VoiceModels.kt`, `lib/aura-voice/src/main/kotlin/com/aura/voice/android/AndroidSpeechRecognizerProvider.kt`, `lib/aura-voice/src/main/kotlin/com/aura/voice/android/SharedPreferencesVoiceSettingsStore.kt`, `lib/aura-voice/src/test/kotlin/com/aura/voice/VoiceStyleSettingsTest.kt`.
- Before publication, the local test reports were read directly: app suite 95 tests and voice-library suite 17 tests, each with zero failures/errors/skips. The existing debug artifact was 20,891,446 bytes, SHA-256 `9F7A8D184147C017A21AEB43FEB2C8C2577A1070E7CF6FDAF5661DB5C116C71D`; apksigner passed, signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`; package `com.aura.ai`, version `1.0.0`, code 1. These reports/artifact came from the pre-publication local tree. They are not evidence that the merged remote commit `db11eed` has been rebuilt.
- Current-turn build attempt used the installed Gradle 8.11.1 distribution directly because the wrapper lock under the protected user Gradle home denied access. With the workspace cache in offline mode, `:app:checkDebugAarMetadata` failed before tests with Gradle's `ModuleVersionResolveException.getMessage` null-path NPE. No new build or tests resulted. A first invocation also failed at PowerShell argument parsing before Gradle tasks started. Do not count either as test results for `db11eed`.
- ADB listed no devices. The owner's latest report is “incorrect transcript”; the actual recognized text and confidence were not captured. No install or phone test was made. Do not claim this code fixed transcription.
- Next: check CI for `db11eed`; obtain a successful build of this exact branch revision; then, after the Honor reconnects and explicit approval for that exact build, back up the existing app data and verify package/signature before in-place installation. Capture the visible `Wake heard:` and `Recognized` diagnostics, then test repeated conversation and interruptions. Keep main unchanged.


## CI result for published voice update — 2026-11-07

- GitHub Actions run `42346749653` for source commit `db11eedcbea3ac0affd606a5a7fd994f90fa0b1a` completed with failure. Job `128326059219` reported `steps: null` and no log URL; the connector could not provide step output. The follow-up documentation commit run `42346867212` failed the same way (job `128326410760`, no steps/logs). Cause is unknown; neither run provides evidence that compilation or tests began.
- This confirms the current branch still lacks a completed CI verification. Keep the source commit, but do not treat either failed run as a code test failure or pass.
- Current refs: integration branch is being updated with this record; production `main` remains `4b48185e9f110764938f10548f80a29acda674f7`. No merge/deploy/install occurred.


## 2026-11-12 validation update

- **Workspace / branch:** Local checkout `C:\Users\shawn\Desktop\T1000-main\aura-ai-integration`, branch `integration/aura-voice-consolidation`, local HEAD `d5d0fe92ca890c964685403aee0fcafd3fe30fd6`. The checkout is four commits behind its configured origin and has uncommitted pre-existing work; Git metadata is not writable here. No source-code commit was created from this checkout.
- **Focused code change:** `app/src/main/java/com/aura/ai/ui/chat/ChatScreen.kt` keeps the latest speech diagnostic visible while hands-free listening resumes, so a captured or incorrect transcript can remain inspectable. This is diagnostic visibility only; it does not improve the Android speech model's recognition accuracy.
- **Build and automated tests:** Android Gradle build and unit-test tasks passed. XML test reports: `app` 96 tests / 0 failures / 0 errors / 0 skipped; `aura-voice` 17 tests / 0 failures / 0 errors / 0 skipped; total 113 / 0 failures / 0 errors / 0 skipped. Initial Gradle attempt failed on a locked native-platform DLL; rerun succeeded with the configured Gradle user home.
- **APK artifact:** `app/build/outputs/apk/debug/app-debug.apk`, package `com.aura.ai`, versionName `1.0.0`, versionCode `1`, SHA-256 `900D12B917B93A095DA959146CDC926D97C53E64C384770251029CC8F44B0CA2`. Signature verification passed; certificate SHA-256 matches the previously documented original Aura signing certificate. APK was **not installed**.
- **Device result / unresolved:** The owner's latest report was “incorrect transcript.” ADB currently reports no connected device, so wake capture, transcript accuracy, response audio, interruptions, Ollama connectivity and cloud failover could not be re-tested. Automated unit tests do not establish real-device speech accuracy. No claim of voice completion is made.
- **Other app identity context:** The original Aura package is `com.aura.ai`. The archived T1000 APK identified during read-only investigation uses `com.aura.ai.debug`, so Android treats it as a distinct app with a separate data sandbox. Both can display version 1.0.0 without being the same app.
- **Next steps:** Reconnect the Honor over Wireless debugging; confirm the installed `com.aura.ai` version and signer before updating; obtain the visible transcript and confidence/alternate hypotheses from the new diagnostic UI; then investigate the recognition provider/language configuration based on that device evidence. Install only after the owner authorizes this exact verified APK. Do not uninstall either app, clear app data, or merge to `main`.


## Local barge-in regression validation — 2026-11-12

- Android local code change: BargeInTranscriptPolicy now treats a one-word transcript as insufficient evidence of TTS echo; the existing two-stable-partial requirement remains. Added a regression test for the word “it” overlapping Aura's “I'm on it.” utterance.
- Android tests/build: focused :aura-voice:testDebugUnitTest passed; full :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug passed in 11m58s. Reports show 96 app + 18 voice tests = 114, zero failures/errors/skips.
- Fresh local APK (not installed): com.aura.ai v1.0.0/code 1, 20,824,625 bytes, SHA-256 A569AFF2DE044B74520351EB4D9D485156346BF36C301FFDD8D6909EF812D022. apksigner v2 verification passed; signer matches the documented original signer (1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3).
- Device: adb reports no connected Honor. User's incorrect-transcript report remains unresolved; no wake, ASR, TTS, interruption, Ollama/cloud, or voice identity device test was run.
- PC web read-only check: pnpm typecheck passed; 42 existing tests, 41 passed and 1 failed. The failing test expects one-word overlap not to be classified as echo: isLikelySpeechEcho("I'm on it.", "it") returned true. Root PC source was not edited because that folder has no valid project Git checkout/remote; existing instructions require preserving it until repository setup is restored.
- Branch/source publication: Android local checkout is integration/aura-voice-consolidation at d5d0fe92ca890c964685403aee0fcafd3fe30fd6, dirty and four commits behind local tracking ref; Git metadata is protected, so this source change remains local/uncommitted. This GitHub entry records validation only; it does not claim the code was pushed. No main change; backups untouched.
- Next: reconnect Honor and obtain approval for this exact APK, back up and verify the installed original package/signer, then verify transcript, spoken answer, follow-up and one-word interruption. Restore the PC project's correct checkout before modifying its failing policy.


## Owner approval and wireless reconnect pending — 2026-11-12

- The owner explicitly approved installing the exact local APK SHA-256 A569AFF2DE044B74520351EB4D9D485156346BF36C301FFDD8D6909EF812D022 (package com.aura.ai).
- At the approval check, adb devices -l and adb mdns services showed no connected/discoverable Honor. adb start-server did not change discovery.
- No backup/install/device test has occurred. The install approval remains valid for this exact APK; proceed only once the Honor is visible, after backing up the existing com.aura.ai data and verifying the installed package and signer. Do not uninstall or clear data.
- The owner selected Wireless debugging. Requested the Honor's one-time pairing IP:port and pairing code, or confirmation that both devices use the same Wi-Fi.


## Honor reconnect and approved APK install — 2026-11-12

- Owner replied “try again”; ADB discovery succeeded without requiring the one-time pairing code. Connected Honor serial: `adb-AYAV6R3707021141-9Vd6ZA._adb-tls-connect._tcp` (product CRT-NX1, device HNCRT-M2).
- Before update, backed up existing `com.aura.ai` private data to `device-backups/com.aura.ai-preupdate-20261112-A569AFF2.tar` (SHA-256 `5C34F6A38FB9EA78844A9E8078269D59FA6264D465BAE83BDC72C0DBC02581A2`). Archive includes databases, DataStore preferences and voice/settings preferences. No app data was cleared.
- Pre-install package and signing certificate were verified compatible. Installed the explicitly approved APK in place with `adb install -r`; result: `Success`. No uninstall, reset, or action on `com.aura.ai.debug`.
- Post-install package remains `com.aura.ai`, version `1.0.0` / code `1`; installed base APK SHA-256 is `A569AFF2DE044B74520351EB4D9D485156346BF36C301FFDD8D6909EF812D022`, matching the approved build. Signer remains the original `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- Existing databases and preferences remain present; Aura launched successfully. Android reports microphone permission allowed while foreground and the app process is running. This confirms launch/mic availability only; it does not prove the wake phrase or speech pipeline works.
- Awaiting owner-reported real-device test for “Yo, yo, yo, what is five plus five?”; transcript, spoken answer, multi-turn flow, interruption, voice identity, and Ollama/cloud routing are not yet verified in this session.
- Repo state: `integration/aura-voice-consolidation`, local HEAD `d5d0fe92ca890c964685403aee0fcafd3fe30fd6`, dirty and four commits behind tracking ref. Git metadata is protected; no local commit/push attempted. `main` unchanged. Existing source edits and backup files preserved.
- Next: obtain the exact transcript and spoken result from the live test; if wrong or silent, capture logs/transcript diagnostics before changing code. Keep further device changes on the same app/package and preserve the backup.


## Follow-up connectivity check — 2026-11-12

- Honor remains visible over Wireless debugging and `com.aura.ai/.MainActivity` is the foreground activity.
- From the PC, a TCP probe to `192.168.8.18:11434` briefly succeeded; the subsequent Ollama `/api/tags` HTTP request was refused (`WinError 10061`). Treat Ollama as unavailable/intermittent at this check. No model generation request was sent and no service/firewall settings were changed.
- The owner’s live “Yo, yo, yo, what is five plus five?” result is still pending. This check does not verify phone-to-PC reachability, Qwen inference, or voice response.


## Owner voice retest and backend recheck — 2026-11-12

- Owner reported saying “Yo, yo, yo, what is five plus five?” and hearing “I'm on it,” followed by “I'm not sure what you're asking.” This is a failed conversational result; do not mark wake-to-answer as passing.
- Package attribution is uncertain: immediately after the report, ADB showed `com.aura.ai.debug/.MainActivity` in the foreground. Android audio and `WakeWordService` log lines also name `com.aura.ai.debug`. Therefore this spoken result cannot be attributed to the newly installed original `com.aura.ai` APK. The separate debug app was not modified or reinstalled.
- Latest PC Ollama check recovered: `http://127.0.0.1:11434/api/tags` and `http://192.168.8.18:11434/api/tags` both returned `qwen3:1.7b` and `qwen3:4b-q4_K_M`. `ollama list` also succeeded. No inference/generation request was sent; this proves PC API/model discovery only, not Honor-to-PC inference or Aura fallback.
- ADB confirms the Honor remains connected. No code changed and no automated tests were run in this follow-up. The prior APK install/backup and 114-test/build verification remain documented separately.
- Branch: `integration/aura-voice-consolidation`, local HEAD `d5d0fe92ca890c964685403aee0fcafd3fe30fd6`; source tree dirty, four commits behind its tracking ref, local Git metadata protected. No source commit or main merge. Existing app data backup remains preserved.
- Next: test from the original `com.aura.ai` app specifically (not `com.aura.ai.debug`), capture its visible wake/recognized transcript and answer, then verify one follow-up. If that app also says “I'm on it” then fails, use its package-filtered logs and captured transcript to trace the model request and acknowledgement path. Separately test Honor-to-PC reachability without starting a model generation until requested.


## Isolated original-app retest setup and Honor-to-Ollama network check — 2026-11-12

- Owner reported the transcript showed only “what is.” Before isolation, ADB showed `com.aura.ai.debug` foreground and both `com.aura.ai.debug/.voice.WakeWordService` and original `com.aura.ai/.voice.WakeWordService` active. The speech result while both were active is not a valid single-app acceptance test.
- Temporarily force-stopped only the debug app process to release its competing microphone listener; no uninstall or data clear. Then launched the original app. Verified `com.aura.ai/.MainActivity` is foreground, original `WakeWordService` remains active, and no debug app process or debug wake service remains.
- Verified Honor-to-PC Ollama HTTP reachability from the device itself: Android `/system/bin/curl` fetched `http://192.168.8.18:11434/api/tags` successfully. It returned `qwen3:1.7b` and `qwen3:4b-q4_K_M`. ICMP ping got no reply, but HTTP works. This proves API/model discovery from Honor; no inference was generated and the app’s request/model selection is not yet verified.
- The isolated original-app wake/answer retest is pending owner response. No code changed and no automated tests ran during this check.
- Next: collect the exact transcript and spoken answer from the original app while it is the only listener; if the transcript remains clipped, capture its diagnostic line and package-filtered recognizer/network logs before adjusting recognition behavior. Test one Qwen generation through Aura only after the owner reports this voice check.


## Partial wake transcript continuation and verified build — 2026-11-12

- Implemented a guard for incomplete wake tails such as “I” and “what is.” Aura keeps listening, combines the next recognized segment with the tail, and sends one request. Complete requests and later conversation turns are unchanged.
- Source and tests in this commit: `app/src/main/java/com/aura/ai/MainActivity.kt`, `app/src/main/java/com/aura/ai/agent/AgentOrchestrator.kt`, `app/src/main/java/com/aura/ai/agent/AgentPrompts.kt`, `app/src/main/java/com/aura/ai/agent/ResponseAcknowledgementSanitizer.kt`, `app/src/main/java/com/aura/ai/data/remote/inference/ProviderChatRouter.kt`, `app/src/main/java/com/aura/ai/data/settings/SettingsRepository.kt`, `app/src/main/java/com/aura/ai/ui/chat/ChatScreen.kt`, `app/src/main/java/com/aura/ai/ui/chat/ChatViewModel.kt`, `app/src/main/java/com/aura/ai/ui/chat/SpeechCommandConfidencePolicy.kt`, `app/src/main/java/com/aura/ai/ui/chat/WakeCommandContinuationPolicy.kt`, `app/src/main/java/com/aura/ai/ui/chat/WakeTranscriptDiagnostic.kt`, `app/src/main/java/com/aura/ai/ui/settings/SettingsScreen.kt`, `app/src/main/java/com/aura/ai/ui/settings/SettingsViewModel.kt`, `app/src/main/java/com/aura/ai/voice/WakeWordEvents.kt`, `app/src/main/java/com/aura/ai/voice/WakeWordService.kt`, `app/src/main/java/com/aura/ai/voice/tts/ElevenLabsSpeechTransport.kt`, `app/src/main/java/com/aura/ai/voice/wake/WakePhraseMatcher.kt`, `app/src/test/java/com/aura/ai/ResponseAcknowledgementSanitizerTest.kt`, `app/src/test/java/com/aura/ai/WakePhraseMatcherTest.kt`, `app/src/test/java/com/aura/ai/data/settings/OllamaEndpointPolicyTest.kt`, `app/src/test/java/com/aura/ai/ui/chat/ChatBackendAvailabilityPolicyTest.kt`, `app/src/test/java/com/aura/ai/ui/chat/SpeechCommandConfidencePolicyTest.kt`, `app/src/test/java/com/aura/ai/ui/chat/WakeCommandContinuationPolicyTest.kt`, `app/src/test/java/com/aura/ai/ui/chat/WakeTranscriptDiagnosticTest.kt`, `app/src/test/java/com/aura/ai/voice/tts/ElevenLabsSpeechTransportTest.kt`, `lib/aura-voice/src/main/kotlin/com/aura/voice/BargeInTranscriptPolicy.kt`, `lib/aura-voice/src/main/kotlin/com/aura/voice/VoiceModels.kt`, `lib/aura-voice/src/main/kotlin/com/aura/voice/android/AndroidSpeechRecognizerProvider.kt`, `lib/aura-voice/src/main/kotlin/com/aura/voice/android/SharedPreferencesVoiceSettingsStore.kt`, `lib/aura-voice/src/test/kotlin/com/aura/voice/BargeInTranscriptPolicyTest.kt`, `lib/aura-voice/src/test/kotlin/com/aura/voice/VoiceStyleSettingsTest.kt`.
- Verification on BEATK: focused `:app:testDebugUnitTest --tests com.aura.ai.ui.chat.WakeCommandContinuationPolicyTest` passed (3 tests). Full `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` passed in 2m41s; reports: app 99 tests, shared voice 18 tests, 0 failures/errors/skips (voice test task was up-to-date from its prior passing run). `git diff --check` passed; only line-ending normalization warnings.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, `com.aura.ai` v1.0.0/code 1, 20,943,047 bytes, SHA-256 `BA1F1E4F17640D83DE43E8208A5A2B94EED4886EB21EEE4E71BA951E5EFA7471`; signature passed with signer `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed; no data cleared.
- Honor fetched Ollama `/api/tags` from `192.168.8.18:11434`; it listed `qwen3:1.7b` and `qwen3:4b-q4_K_M`. No generation request was sent.
- Device voice remains failing/unresolved: during the isolated original-app test the owner reported transcript “I”; no final answer was reported. A later check found the debug app in front, so no voice acceptance pass is claimed.
- Local checkout: `integration/aura-voice-consolidation`, HEAD `d5d0fe92ca890c964685403aee0fcafd3fe30fd6`, dirty and four commits behind; local Git metadata is protected. This tested source/records commit is for the integration branch only; `main` is unchanged.
- Next: obtain approval for this exact APK before installing; test original Aura with debug stopped, capture full transcript and spoken answer, then verify follow-up, interruption and an actual Qwen response through Aura.


## Branch publication and test attribution — 2026-11-12

- Published the verified source, tests, and status records to `integration/aura-voice-consolidation` in commit `915b3f9e37525653bd5063a2e76eba04e797cbb2`, parent `c338ec22b325a1befd54daee68e7290ba8dd723d`. The commit includes 36 changed paths. The main branch was not changed.
- The commit's only reported CI status is a successful Vercel check; no Android build/test CI result is available. Android validation is the local Gradle/JUnit/APK result documented above.
- After the owner's reply that the visible transcript was only “i,” a fresh ADB/logcat check showed `com.aura.ai.debug` using audio input and emitting wake-recognition logs. Thus the user's “i” result cannot be attributed to the original `com.aura.ai` app. The original-app voice test remains unverified and must be repeated with the debug app stopped and original Aura visibly foreground.
- Fresh APK SHA-256 `BA1F1E4F17640D83DE43E8208A5A2B94EED4886EB21EEE4E71BA951E5EFA7471` is signed compatibly for `com.aura.ai`, but is not installed. The earlier exact-build approval was for A569…; obtain approval for BA1F… before installation. Preserve both app data sets.
- Next: isolate original Aura, obtain owner approval for the exact fresh APK, back up existing original-app data, verify package/signature, then install in place and test transcript, final spoken answer, second turn and interruption. Separately test actual Ollama inference through Aura; `/api/tags` reachability alone is not inference verification.


## Approved APK update completed — 2026-11-12

- Owner approved APK SHA-256 `BA1F1E4F17640D83DE43E8208A5A2B94EED4886EB21EEE4E71BA951E5EFA7471`. Before install, backed up original Aura private data to `device-backups/com.aura.ai-preupdate-20261112-BA1F.tar` (1,036,288 bytes, SHA-256 `2A30FFC8E80FB427916F849C131E3BFAE38F1926E2FF358CEB172B8EDAC29808`) and preserved the previous installed APK (SHA-256 A569AFF2…). Both APKs are `com.aura.ai`, v1.0.0/code 1, and have the same signing certificate.
- `adb install -r` returned Success. The installed base APK was pulled from Honor and its SHA-256 exactly matches approved BA1F…; package/signature reverified. Database and shared-preference directories remain. No uninstall or data clear.
- Stopped `com.aura.ai.debug` without removing it, launched original `com.aura.ai/.MainActivity`, and confirmed its `WakeWordService` is active. The earlier owner transcript “i” is ambiguous: device logs at the time showed the debug package holding audio input. The new build's clean original-app speech test has been requested and is pending; no pass is claimed for wake recognition, ASR accuracy, final voice response, follow-up, barge-in, or Qwen inference.
- Source changes are in integration commit `915b3f9e37525653bd5063a2e76eba04e797cbb2`; remote handover/status updates are `ed934073126bc0c326dc1e206fc22c359bd540d9` and `fc9957287a6b80d7f41986c898f96f76ce464a43`. Main remains unchanged. Local automated results: focused 3/3; full app 99 + shared voice 18, zero failures/errors/skips; APK assemble passed. Android CI result is unavailable.
- Next: capture actual test transcript and what Aura says, verify a second turn and interruption, then run a real Qwen request through Aura; test cloud fallback only with configured credentials. Update status with measured results.


## Locale fallback source and verification — 2026-11-12

- Device-side logs from original Aura showed Android SpeechRecognizer error 12 while it requested `en-ZA`. Android's API documentation defines error 12 as the requested language not being supported by the selected recognizer. The prior wake fallback switched recognizer engines but kept requesting `en-ZA`, explaining the repeated recognition failure.
- Implemented fallback candidates `en-ZA` → `en-GB` → `en-US` for Android speech recognition. The conversation recognizer retries the request with the next locale after Android language errors 12/13, records the locale in the transcript, and permits the configured system network engine on fallback. Wake listening similarly switches to the system engine and retries with the next locale. This is ASR-only; TTS voice settings are unchanged.
- Published code commits on `integration/aura-voice-consolidation`: wake service `10414666cc2473eaafdc7df8118be4becc1cb865`; Android speech provider `21e69a85b7f980d19c6d30d0929598a724a7ef64`; fallback policy `3365106a11f87449307806f4fcf8473f97910ffb`; 3 unit tests `49e5e272fdefee701591bf11f765594d84271ab1`. The source commit `915b3f9e37525653bd5063a2e76eba04e797cbb2` carries the prior continuation/transcript changes.
- Local verification: focused fallback policy suite 3/3 passed; full `:aura-voice:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug` passed in 12m38s. XML reports: app 99 tests, voice library 21 tests, zero failures/errors/skips. APK package `com.aura.ai`, version 1.0.0/code 1, size 20,943,047 bytes, SHA-256 `8402932206749EAC09C0EDA6F92268E4004F633C35D4189EF104F315D7FAC3BE`; apksigner verified the existing original signer `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. No Android CI pass is available.
- Owner asked to keep only the original Aura active. Identified `com.aura.ai.debug` as the separate test package and disabled it for user 0 (reversible); did not uninstall or clear its data. Original `com.aura.ai` remains installed, updated to BA1F… (the previously approved build), and its wake service is active. Data backup and prior installed APK are preserved as recorded above.
- The fresh 840293… APK has not been installed. It requires explicit approval for this exact hash; previous approval named BA1F…. Real-device voice acceptance remains unresolved. Earlier “i” cannot be attributed because debug app held audio input. After debug was disabled, original logs exposed error 12; the locale-fallback code now needs real-device verification.
- Local checkout remains branch `integration/aura-voice-consolidation`, HEAD `d5d0fe92ca890c964685403aee0fcafd3fe30fd6`, dirty; local Git metadata is protected. Remote integration branch received the source commits above; main was not changed. Local data backups are excluded from Git.
- Next: get exact-hash approval, back up data again, verify package/signer and install 840293… in place; then capture the visible transcript and spoken answer from original Aura, test another turn and interruption, and verify an actual Qwen generation through Aura. Cloud fallback remains unverified without a routed failure/recovery test.


## GitHub handover references — 2026-11-12

- ASR fix source commits: `10414666cc2473eaafdc7df8118be4becc1cb865`, `21e69a85b7f980d19c6d30d0929598a724a7ef64`, `3365106a11f87449307806f4fcf8473f97910ffb`, `49e5e272fdefee701591bf11f765594d84271ab1`.
- Companion handover commits: `314d65dc320426afdfaecc17104762f3661899a3`, `9ec46bd7c427815cb7abb13c71975db7b7918ebc`, `a24073f0a0bce31c1c34a67fd8aa55d3312e0998`, `0034f35032230554191b3a166b9cd1698a92e13a`. All were written to `integration/aura-voice-consolidation`; no commit targeted `main`.
- The exact APK hash `8402932206749EAC09C0EDA6F92268E4004F633C35D4189EF104F315D7FAC3BE` awaits owner's install approval. The latest installed package is still the prior approved BA1F… build. Duplicate debug package remains disabled, not removed.


## CRITICAL APP IDENTITY CORRECTION — 2026-11-12

This section supersedes any earlier handover statements claiming `com.aura.ai` is the original Aura or that `com.aura.ai.debug` is the duplicate.

- On the Honor, package metadata verified: `com.aura.ai.debug` is labeled “Aura AI” and matches the original Aura app; `com.aura.ai` is labeled “T1000.” The installed Aura APK was pulled for identity inspection, not modified. User context identifies the Aura AI icon as the original Terminator-skull app.
- Root workspace `T1000-main/android/app` has namespace `com.aura.ai`, debug applicationId suffix `.debug`, and app label “Aura AI”; this matches the installed original Aura package.
- The GitHub integration branch `ferreiraeshawn8209-app/aura-ai:integration/aura-voice-consolidation` has no `android/app` tree. Its `app/build.gradle.kts` builds `com.aura.ai`, and its app label/strings say “T1000.” Therefore this branch is T1000, not the original Aura target. The recently published locale fallback commits apply only to T1000; do not claim or install them as an original Aura fix.
- Owner asked to retain one app. Phone state was corrected reversibly: `com.aura.ai` (T1000) is disabled for user 0; `com.aura.ai.debug` (Aura AI) is enabled and was launched. Neither was uninstalled; no app data was cleared. Original Aura private data was backed up to `device-backups/com.aura.ai.debug-original-aura-before-isolation-20261112.tar` (14,136,320 bytes, SHA-256 `5DA4C808F9AD33CC627461980161E7EB6ADF867D5B5B0136617B3171A1C7FF04`). T1000 app data and its previous APK backups remain preserved too.
- The T1000 APK `8402932206749EAC09C0EDA6F92268E4004F633C35D4189EF104F315D7FAC3BE` was installed only into `com.aura.ai` after exact-hash approval, then that T1000 package was disabled per the owner's instruction. It did not update original `com.aura.ai.debug`; no new original Aura APK has been built or installed.
- Root Git state is not a valid project checkout: `git` resolves to a profile-level empty `master` with no remote. Root `AGENTS.md` requires leaving source intact when history/remote is missing or ambiguous. The original Aura source at `T1000-main/android/app` has not been modified. To continue coding the original safely, first restore/identify its correct Git checkout/remote; do not assume this T1000 integration branch is its source.
- Main remains unchanged. No real-device voice acceptance was completed for the original Aura, and the published T1000 ASR fix must not be described as fixing Aura.


## Local Aura fragment guard follow-up — 2026-11-13

- Owner reported the latest five-plus-five voice transcript or answer was wrong. Inspection of the saved Aura exchange found garbled ASR fragments and clarification replies; the intended math request was not recognized. Raw fragments are intentionally omitted.
- Local workspace change: `C:\\Users\\shawn\\Desktop\\T1000-main\\android\\app\\src\\main\\java\\com\\aura\\ai\\voice\\HandsFreeTranscriptMerger.kt` now keeps apostrophe contractions together in the transcript word counter and rejects an incomplete one-word contraction fragment. Regression tests were added in `android/app/src/test/java/com/aura/ai/voice/HandsFreeTranscriptMergerTest.kt`.
- Focused test: `:app:testDebugUnitTest --tests com.aura.ai.voice.HandsFreeTranscriptMergerTest` passed, 23 tests; 0 failures/errors/skips. Full local Android app suite and assembly passed: 784 JVM tests, 0 failures/errors/skips across 176 suites; debug APK assembled. `git diff --check` passed (line-ending normalization warnings only).
- New local APK: `com.aura.ai.debug`, label Aura AI, version 1.0.0/code 1, SHA-256 `1324E4BBE21FEC7CFE8AE21F41700DA9F8C0AAFED12EB9B7B21BFA42CDCDE5F6`; signature verification passed, signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`. This exact build has not been installed or approved; Honor remains on the previously approved 5A17 build, with data unchanged.
- The guard only prevents the specific apostrophe-fragment counting bug. Longer recognition errors and 27–57 second model reply latency remain unresolved; no voice fix is claimed.
- Source workspace Git is `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, with no remote configured and pre-existing user changes. This local source patch is not committed or present on this GitHub branch. The GitHub branch handover is updated as a communication record only; no source commit, merge, or main-branch change is claimed.
- Next: request approval for this exact APK hash, then back up data and install in place only after approval. Retest the actual transcript and spoken answer twice; separately diagnose ASR and slow inference. Austrian TTS identity, wake, barge-in, and fallback remain unverified.


## Approved local Aura APK 1324 installed on Honor — 2026-11-13

- Owner approved exact APK hash `1324E4BBE21FEC7CFE8AE21F41700DA9F8C0AAFED12EB9B7B21BFA42CDCDE5F6`. The connected Honor's existing package was `com.aura.ai.debug`, label Aura AI, v1.0.0/code 1; previous APK hash 5A17 and signer matched.
- Created and validated fresh private data backup before update: `.gradle-aura-cache/device-backups/com.aura.ai.debug-pre-1324-20261113-072851/private-data.tar`, 14,205,440 bytes, 127 entries, SHA-256 `44EE51B1DF093779D7C2F6D0D98FD1F6354B44A9CC74550EB4B8D428A83DCAD4`. Previous installed APK was retained.
- `adb install -r` succeeded. Pulled installed APK has the exact approved hash; signature passed with signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`. Package/version and first-install timestamp were preserved; data directories remain. No uninstall or data clear.
- Launched `com.aura.ai.debug/.MainActivity`; it is top-resumed. Owner's voice/transcript test for this build is pending. Install and launch are verified; wake, recognition, spoken answer, multi-turn, interruption, and latency are not.
- Automated verification for this APK: 23 focused transcript-merger tests passed; full local Android suite passed 784/784 with no failures; debug assembly and signature verification passed.
- Important source provenance: the code change and APK came from local `C:\\Users\\shawn\\Desktop\\T1000-main`, local branch `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, with no configured remote. That checkout contains pre-existing user changes. No source code was pushed to this GitHub branch; this update only records the result in the handover. Do not treat the GitHub branch as containing this local APK source.
- Next: collect the exact displayed transcript and audible answer on this updated Aura. If wrong, inspect engine/locale and transcript logs; diagnose ASR separately from provider delay. Keep the verified backup, preserve package/data, and do not install another APK without its exact-hash approval.


## 1324 live voice failure and local speech-hint build blocker — 2026-11-13

- Owner tested approved original Aura build 1324 on Honor. Speech recognition returned an unrelated phrase; Aura spoke a refusal. The owner interrupted. Saved app messages corroborate garbled recognition and a refusal. This is a failed device test, not a successful math response. The interrupted reply was still in progress at about 33.7 seconds.
- The prior contraction-fragment guard did not prevent this longer erroneous transcript. A local follow-up added `SpeechRecognitionBiasPolicy.kt`, changed local `SpeechToText.kt` to provide Android recognition ranking hints for the wake phrase, arithmetic/numbers, and common commands, and added `SpeechRecognitionBiasPolicyTest.kt`. Android documents bias strings as optional hints, not guaranteed recognition correction.
- Two focused Gradle attempts on the local Windows checkout stalled at KSP/compile with no output for approximately 10 minutes each while the machine became highly unresponsive. Both were cancelled to release resources. No test pass or APK exists for the bias change. The installed Honor app remains the previously approved 1324 build; its validated pre-install data backup is retained. No additional APK was installed.
- Source provenance: local changes are under `C:\\Users\\shawn\\Desktop\\T1000-main`, branch `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, without a configured remote. The source change is not committed or present on this GitHub branch. This is handover information only; no main merge or source push.
- Next: once Android Studio/build work is idle, inspect KSP/Gradle daemon logs and system load, retry the focused tests once, then full tests/assemble only after focused success. Verify exact package/signature/hash and request approval before any new install. Preserve the existing 1324 install and data backup.


## Build timing correction — speech-bias verification — 2026-11-13

This entry corrects the elapsed-time estimates in the preceding speech-bias blocker notes. Gradle daemon logs show the first focused run started at 07:37 and the next run started at 08:04; the exact cancellation timestamps are not established. The second attempt was cancelled shortly after starting and was not a 10-minute run. Both runs produced no focused test result while KSP/compile output was quiet. A later `gradlew --status` check reported no daemons running. Do not cite the earlier approximate per-run durations. The bias-policy change remains unverified and no new APK was produced.


## Wake phrase homophone recognition candidate — 2026-11-14

- Implemented on the source-of-truth branch `integration/aura-voice-consolidation`: `app/src/main/java/com/aura/ai/voice/wake/WakePhraseMatcher.kt` now recognizes three consecutive wake-like tokens at the start, including narrow common ASR variants (yo/yoh/yoo/you/yah/ya/yeh/yeah) plus the merged `yoyoyo` form. The intended spoken wake phrase remains “Yo, yo, yo”; alternatives exist only to tolerate recognition output. The command text after the phrase is still returned by the same prefix parser.
- Added `app/src/test/java/com/aura/ai/voice/wake/WakePhraseMatcherTest.kt` with five cases for exact phrase + command, homophones/merged form, candidate selection/confidence, incomplete/non-leading rejection, and empty command.
- GitHub source commits: `4f159677f3b0fa2b33746b1c694624d7b2b9ad8c` (matcher) and `18d8b1db7c9c64cceaa54bb07b410282323eed64` (tests), both on the integration branch. No merge to `main` and no install.
- Automated verification is **blocked, not passed**: Android workflow run #254 for commit `18d8b1db7c9c64cceaa54bb07b410282323eed64` failed in 3 seconds before assigning a runner (`runner_id=0`, no job steps). Therefore these new unit tests and the Android build have not executed on GitHub. The workflow failure reason is not exposed by the available run details; no assertion of code failure or success is made.
- The writable local checkout remains a separate dirty tree at `codex/aura-original`; its Android code is under `android/app/src/main`, while this integration branch uses `app/src/main`. A separate local diagnostic APK from that checkout is not aligned with this branch and remains uninstalled. Honor still has the previously approved APK SHA-256 `44636F2B74C4B60784088F1FA973E2331C3E3F4716040ADD53E3BC72E1EE1E4C` (read-only pulled hash). No new source has been installed on-device.
- Latest bounded Honor logs on the existing install showed two finalized standby recognizer results with `wakePhraseDetected=false` at 07:26:32 and 07:26:39; transcript text was not logged, Aura was backgrounded, and the owner’s exact two-turn result is still unknown. This is not a device test of the branch change.
- Next: restore an actionable Android CI runner or build the integration branch in a verified local checkout; execute tests/build; then request exact-hash approval before backing up/installing and testing wake, follow-up, transcript, and speech. Do not use the unaligned local candidate.


## Wake matcher verification retry — 2026-11-14

- Retried GitHub Actions run #254 (run ID `43561921766`) once. Attempt 2 also failed in 3 seconds before execution; the GitHub jobs response has `logs_url=null` and `steps=null`, consistent with no runner/test execution. This remains an infrastructure blocker, not a test assertion result.
- A standalone Java `Pattern` smoke check using the exact regex logic passed 6 cases: exact phrase, two homophone variants, merged form, incomplete phrase rejection, and non-leading phrase rejection. This is only a regex-level smoke check; the Android Kotlin/JUnit test and app build remain unexecuted on the integration source.
- Branch code is unchanged after commits `4f159677f3b0fa2b33746b1c694624d7b2b9ad8c` (matcher) and `18d8b1db7c9c64cceaa54bb07b410282323eed64` (tests). Honor has not received this code.
- Next: resolve GitHub Actions runner availability or otherwise build this exact integration branch in a verified environment; then run the focused test and app build before requesting exact-hash device install approval.
