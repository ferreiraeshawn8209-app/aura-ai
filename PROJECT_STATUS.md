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


## Commit record — 2026-11-05

- Code and test commit: `b93d67d (rebased commit; previous local hash ec7a312 was replaced during safe integration)` (`Gate spoken actions on acknowledgement completion`) on `integration/aura-voice-consolidation`. Production `main` unchanged. `device-backups/` remains untracked and was excluded.


## Remote development update — 2026-11-06

- Cloud-first inference and automatic Ollama fallback are implemented behind an OpenAI-compatible chat endpoint. Failure opens a 30-second cooldown; future turns retry cloud. Chat trace reports the selected/fallback backend. Cloud mode is disabled until endpoint/model/key are configured.
- The cloud key entry is masked and saved through encrypted Android storage. This build has no user key configured and no live cloud test.
- Added a WhatsApp draft action that opens the standard app with a prefilled message; sending remains a manual confirmation in WhatsApp.
- Build/tests: Gradle app tests, voice-library tests, and debug APK assemble passed (14m26s). 32 unit tests passed, 0 failed/errors/skipped, 11 suites. APK identity/signature verified: com.aura.ai v1.0.0/code 1, SHA-256 0A85F8C5811A5F3F332CB93A5985AD0C775D67AFD4FE56A3CF62283C5FBDA64A, signer SHA-256 1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3. Not installed.
- Real-device tests remain pending: Honor disconnected. Ollama is stopped; cloud credentials are not configured. No current phone voice, wake, interruption, WhatsApp launch/send, local-model, or cloud-live result.
- Existing voice selection was preserved; ar-language is not the requested Austrian TTS. Acoustic barge-in and a dedicated wake detector remain incomplete. Daily cloud budget controls are also still outstanding.
- No database/package replacement or merge occurred. Personal device-backups remains untracked and excluded.
- Next: test on Honor after reconnect and authorization for this APK; configure/test an actual provider; add a daily cloud budget; continue verified voice and wake repairs.


## Commit record — 2026-11-06

- Cloud/local routing, WhatsApp draft tool, tests, and updated project docs committed as 45eaf56dc6caf629927c6ba0e23319e2f2351d11 (Add cloud fallback and WhatsApp draft actions) on integration/aura-voice-consolidation.
- Prior spoken-action gate is b93d67d after rebasing on the latest remote audit commits. Branch has not been merged to main. The debug APK is not installed; Honor tests remain pending.


## Branch publication — 2026-11-06

GitHub accepted the integration branch push: origin/integration/aura-voice-consolidation now includes the spoken-action gate and cloud/WhatsApp work through commit b7ba19425b8c0cf6ba2d40cba26f4375049d94bd. Production main remains unchanged. Phone acceptance and live provider tests remain pending.
## Daily cloud cap and current build — 2026-11-06

- Settings now includes a daily cloud inference request limit (default 60; 1–1,000). The UTC-day usage count is stored in existing DataStore and atomically reserved before cloud inference. At the limit, the request routes to local Ollama with a fallback status. This caps request count, not monetary spend; multi-step tasks may use multiple requests.
- Automated result: Gradle app and voice-library tests plus debug assembly passed in 12m20s (75 tasks; 15 executed, 60 up-to-date). 35 tests passed, 0 failures/errors/skips across 12 suites. git diff --check passed.
- APK is debug build, package com.aura.ai v1.0.0/code 1, 20,761,174 bytes, SHA-256 F2B2FC2D00E6028B9DD708385319422411EE668622E26CB2CD9BC3998ECD63E5; signature verified with existing debug signer SHA-256 1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3. Not installed; no phone connected.
- Cloud adapter still requires user configuration and live credentials. Mocks cover router and HTTP behavior; no live cloud request or active Ollama fallback/recovery was run. Voice/wake/WhatsApp remain untested on the device. Saved Android voice ar-language is not Austrian. The original app/database/package were preserved; device-backups/ was not staged.
## Implementation commit — 2026-11-06

- Daily cloud request limit and test/documentation updates committed as f4a2e0a9d5cb50c2ee97f084f9e6209e3e2593b6 on integration/aura-voice-consolidation. Working tree contains no tracked changes. device-backups/ remains untracked and excluded. Branch publication is pending; main unchanged.
## Branch publication — 2026-11-06

The quota code and test documentation are published on GitHub. Integration branch and origin both resolve to 8c332e49254fa4d40a372794202bf8ca61c1e2e1. main was not changed. The pre-existing device-backups/ folder remains untracked and excluded.
## Voice recognizer lifecycle repair — 2026-11-06

- Repeated speech turns now invalidate previous recognizer callbacks and destroy the old Android SpeechRecognizer before creating a fresh session. Stop/close also release the recognizer. Asynchronous microphone/recognizer setup errors are returned to Aura as explicit failures.
- New JVM coverage exercises stale callback invalidation on restart/stop. Full Gradle app+voice tests and APK assemble passed in 10m23s: 39 tests, 0 failures/errors/skips, 14 suites; 75 tasks, 16 executed, 59 up-to-date. git diff --check passed.
- Rebuilt APK: com.aura.ai v1.0.0/code 1, same existing debug signer, SHA-256 C241E0899CB45F23AE74E736B674349DF5DFCF14A826562CB9F7734BC6B41E2C. Not installed.
- No physical microphone, wake, transcription, TTS interruption, or multi-turn behavior is claimed. Honor is disconnected by request. Cloud credentials and live Ollama are unavailable (Ollama stopped); Austrian voice remains unconfigured. Original app data/database/identity preserved.
## Voice repair commit — 2026-11-06

- Speech recognizer lifecycle repair and test records committed as e3d86b3215ca67a88f595a77e01dc8946255a1f3 on integration/aura-voice-consolidation. Build passed; Honor tests pending. Main unchanged.
## Speech repair publication — 2026-11-06

The recognizer lifecycle repair is pushed. At verification, origin/integration/aura-voice-consolidation matched 12af8d08565847d406672fa4741b77c4123f3. No tracked local changes; device-backups/ remains excluded; main unchanged.

## Transcript persistence and verification — 2026-11-06

- Existing Agent chat now loads and persists turns through the existing Room-backed conversation repository. Search filters saved/current transcript text case-insensitively; Compose text selection supports copy. No database migration or package change.
- New unit coverage: 3 transcript conversion/search tests. Full app + voice-library tests and debug APK build passed: 42 total tests, 0 failures/errors/skips, 15 suites; Gradle 21m12s, 75 tasks (46 executed, 29 up-to-date). `git diff --check` passed.
- Debug APK: `com.aura.ai` v1.0.0/code 1, 20,621,965 bytes, SHA-256 `1614E230F6663E00D86D975B5CFE21F9BF84E7F998EADB24C98F3E356F8CB3F1`; signature verified, signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed; Honor disconnected.
- Initial build was blocked by a full C: drive; only generated Gradle intermediates/cache/temp folders inside the repo were cleared, preserving source, backups, APK outputs and personal data. The rerun completed successfully.
- Remaining: device acceptance pending (mic, wake, transcript, voice, interruption, Ollama/cloud fallback and WhatsApp draft review); no live provider credentials used. Austrian TTS is not configured; `ar-language` remains saved. Continue on integration branch, do not merge to main.

## Commit state — 2026-11-06

- Transcript feature and its verification records are committed as `d0e8e9c` on `integration/aura-voice-consolidation`; branch publication was verified in the following entry. `main` unchanged; personal `device-backups/` remains untracked and excluded.

## Publication verification — 2026-11-06

- Immediately after publishing the code and first handover commits, GitHub remote `origin/integration/aura-voice-consolidation` resolved to `1ae7c1c99562b4c00ca767e2f57ade323202bf68`. `main` remains `4b48185e9f110764938f10548f80a29acda674f7`; no merge. This supersedes the earlier pending-publication note.

## Local backend check — 2026-11-06

- Direct no-proxy GET to `http://192.168.8.18:11434/api/tags` from the PC succeeded; Ollama advertised `qwen3:1.7b` and `qwen3:4b-q4_K_M`. The app default endpoint is `http://192.168.8.18:11434`.
- The default proxy route was refused at `127.0.0.1:9`; bypassing it for this read-only check succeeded. No system proxy/firewall/server settings were changed and no model generation was run.
- Phone-to-PC access remains unverified until Honor reconnects. Cloud fallback remains unit/mock verified only without configured user credentials.

## Optional cloud TTS provider — 2026-11-06

- The existing Aura voice subsystem now registers ElevenLabs as an optional cloud TTS provider alongside Android TTS. Voice settings can save the key in existing encrypted storage, list provider voices with gender/accent labels, preserve the selected provider voice ID, and preview it through the established voice path.
- The integration does not set a default or automatically change the saved voice. No ElevenLabs key was configured, no provider request was made, and no Austrian accent or audio quality has been verified. Existing saved Android voice remains selected.
- Verification: focused transport tests passed; full app and voice-library unit suites plus debug APK assemble passed. Result: 41 tests, 0 failures/errors/skips, 14 suites; BUILD SUCCESSFUL in 5m21s. APK SHA-256 95EC076E0F424A8C76CB30BAB3BC067F630C6217524F32624366573390B70DE2; package com.aura.ai v1.0.0/code 1; signer unchanged. Details in AI_HANDOVER.md.
- No install or real-device test. The Honor remains disconnected by request. Existing chat routing/WhatsApp draft behavior was not changed or newly live-tested.
- Main remains unchanged. Continue on integration/aura-voice-consolidation; device-backups/ stays untracked and excluded.


## Commit record — 2026-11-06

Implementation commit: 3eb89d5cdb1b8c634797c971dcc43527188b317e (Add optional ElevenLabs voice provider) on integration/aura-voice-consolidation. It includes provider code, tests, and project records. Main unchanged; device-backups/ was excluded.

## Branch publication — 2026-11-06

GitHub origin/integration/aura-voice-consolidation was pushed and verified at 50bb1ed4d404a42454f57355931779b12e413bd0. This includes implementation commit 3eb89d5cdb1b8c634797c971dcc43527188b317e and documentation commit 50bb1ed4d404a42454f57355931779b12e413bd0. Main was not updated. The existing device-backups/ folder remains untracked and excluded.