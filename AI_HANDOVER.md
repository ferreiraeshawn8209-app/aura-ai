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
