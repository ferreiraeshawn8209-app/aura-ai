# Aura project status

Updated: 2026-11-06
Branch: `integration/aura-voice-consolidation`

## Offline build check — 2026-11-06

- Offline Gradle build/test command completed successfully in 1m13s. App + `lib/aura-voice` reports: 71 tests / 23 suites, 0 failures/errors/skips. All 75 Gradle tasks were up-to-date, so this invocation reused prior outputs rather than executing test bodies again.
- Debug APK artifact verified: package `com.aura.ai`, version 1.0.0/code 1, SHA-256 `FFC0E51F2AE16C02004E142BF325310C2BDFF681098E8ED5701AE51613C5309F`; signing verified with existing debug signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- No phone/ADB or live provider test. Cloud-first/Ollama fallback, WhatsApp draft, voice/wake and action policy checks are not equivalent to live-device acceptance. The original app, data, package identity and main branch were untouched; `device-backups/` remains excluded.

## Wake recognizer update — 2026-11-06

- Standby wake now prefers Android's dedicated on-device speech recognizer on API 31+ when available, with the existing Android system recognizer as fallback; the intent also requests offline recognition. The OS/provider may ignore the offline hint, and Honor support/language resources have not been checked.
- Added two policy tests. Full offline app + voice test/build completed successfully: 73 reports / 24 suites, 0 failures/errors/skips; 14 of 75 Gradle tasks executed, other tasks up-to-date. Debug APK SHA-256 `A88BF09A91C596752669D3D274065B81F66E2D32511D19F10C96685CD45D4363`, package `com.aura.ai` v1.0.0/code 1; signature verified with the established debug signer. Not installed.
- Phone and live-provider acceptance remain pending. No ADB/install was used. Code and record are on the integration branch; `main` and `device-backups/` remain untouched.

## Wake language recovery — 2026-11-06

- On-device wake recognition now falls back to the Android system recognizer if the local provider reports the requested language is unsupported/unavailable, avoiding endless retries of an unusable local language pack. This fallback policy is unit-tested; actual Honor `en-ZA` support is unknown.
- Offline Gradle app/voice tests and debug APK assembly succeeded: 74 tests / 24 suites, 0 failures/errors/skips. APK SHA-256 `4930072AF2184041BD9482DFC67E8B9012347D5B92538DB46F0631C31BB8E3B6`, package `com.aura.ai` v1.0.0/code 1, signer verified. Not installed; no phone test.
- Source/tests/docs are being committed to the existing integration branch; main is unchanged and personal backup data is excluded.

## Memory and conversation context — 2026-11-06

- Long-term memory recall now uses bounded meaningful-word searches plus ranking and includes high-priority preference/setting/instruction/project memories. Older transcript retrieval selects up to three matching exchanges (1,800 characters), alongside recent history; task understanding now receives history too. No schema/data change.
- Regression tests cover memory word matching/ranking/deduplication, old conversation selection/bounds, and task prompt context. Full offline Android build passed: 81 tests / 27 suites, zero failures/errors/skips. Debug APK package/signature verified, SHA-256 `9D46DEB9542FE2F200BDA0DDE98B7875B0F1106834D565FBE8808D0F699F56E5`; not installed.
- This is lexical policy/build verification only. No Honor, voice, live cloud/Ollama, or action test. Device acceptance remains pending; `main` and personal backups untouched.

## Current verified status — 2026-11-06

- The existing Aura app now exposes `open_app`, HTTP(S)-only `open_website`, user-reviewed `prepare_phone_call`, and user-reviewed `prepare_sms` tools to its active agent registry. Existing WhatsApp draft action is retained. Calls and messages are not placed/sent automatically.
- Tests/build: focused action-validation tests passed; full app + voice-library suites and debug APK assembly passed offline: 64 tests, 0 failures/errors/skips across 21 suites. `git diff --check` passed. Online Gradle dependency resolution initially failed due Windows disk space; offline build from cached dependencies passed without deleting files.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, package `com.aura.ai`, version 1.0.0/code 1, SHA-256 `9cbb5714d088c8e134e8c7044ff48b99194971adbafae1aa6f7b1a959d5a1a2e`; v2 signature valid with existing debug signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. APK is not installed.
- Honor disconnected as requested. Real device UI/action and voice acceptance are pending; do not infer success from unit/build tests. Cloud/Ollama live failover/recovery still needs live configured providers and device/network checks. Austrian voice remains unconfigured. No database, permissions, package identity, personal data, or main branch changes.
- Personal `device-backups/` remains untracked and excluded.
- Code and initial verification records are committed as `f5655fa` on `integration/aura-voice-consolidation`; the final handover record and branch publication status follow in `AI_HANDOVER.md`.

## Contact actions and call/SMS confirmation — 2026-11-06

- Added active agent tools to search contacts and prepare a call or SMS draft from a uniquely matched contact. Contact names/counts may be summarized to the model; phone numbers remain in the local Android tool and are not returned in the agent transcript. Ambiguous matches stop for clarification. Existing READ_CONTACTS permission is checked; this work added/granted no permission.
- Changed the legacy action path from direct calls/SMS to Android ACTION_DIAL/ACTION_SENDTO review screens. Static scan shows no direct call or SMS send API remaining in app Kotlin source.
- Verification: full app and voice suites plus debug build passed: 68 tests, 0 failures/errors/skips, 22 suites. APK v2 signature verified, SHA-256 `4cdfdbc615394e201c0395f7d774b32a1b16ddd7b3c3bed5b8b28a912e7399c7`; package remains `com.aura.ai` v1.0.0/code 1. Not installed.
- Honor disconnected; live permission, contact, call/SMS/WhatsApp, voice and backend tests are pending. Main unchanged; personal backup folder excluded.
- Contact implementation/tests and this status are committed as `055d535a0028713fb3c7ce8d6355b80472b929c1` on the integration branch; publication will be recorded in the handover.

## User-selected file access — 2026-11-06

- Settings now lets the user grant one folder with Android's system folder picker. Aura persists only the selected tree URI and the OS read grant. Agent tools search filename/relative path and open a unique match through an installed app. No file contents are read; no modification/deletion, broad storage permission, or Room migration was added.
- Automated checks: 71 tests passed, 0 failures/errors/skips across 23 suites; debug APK assembled; signature verified. APK SHA-256 `ffc0e51f2ae16c02004e142bf325310c2bdff681098e8ed5701ae51613c5309f`, package `com.aura.ai` v1.0.0/code 1. Not installed.
- Folder picker, persistent permission, revocation and opening files are not device-verified; Honor remains disconnected. Main unchanged; personal backup folder excluded.
- File access code and test results are committed as `5883d84284f42c3849378010eb4da637ec207bec` on the integration branch; see `AI_HANDOVER.md` for publication state.

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

## Wake-listener lifecycle repair — 2026-11-06

- Code now attempts to resume standby wake listening after a hands-free session stops, gated by the saved wake setting and Android microphone permission, and cancels that restart if a new hands-free session is already active. Policy unit tests cover these conditions.
- Verification: full app/voice task invocation and debug APK build succeeded on BEATK; app JUnit XML: 42 passed, 0 failures/errors/skips, 15 suites. No test XML existed under `aura-voice/build/test-results`. APK package/signature verified; SHA-256 `C3659671B0E42BB488E8CFC3E87075CB74D1106C4B1ABD1E5A84C4EB6F317DA4`.
- Honor disconnected; not installed. No real-device voice, wake, interruption, Ollama/cloud or WhatsApp claim. No production merge.
- Branch `integration/aura-voice-consolidation`, starting HEAD `cfdf931`; implementation commit `04fd7a4ad8e3927b2f6d39271fe83b4afc57d61c` is published and verified. `main` remains unchanged. Honor remains disconnected; tests pending on device. Exclude `device-backups/`.
- Final race-guard verification: app tests 42/42 passed, no failures/errors/skips; Gradle build successful. Latest APK SHA-256 `3C5BA73F4582281AC2A27BAEB17B3BD008E68F068566438E876CBDB12CAAD511`, package `com.aura.ai` v1.0.0/code 1; signature verified. This APK is not installed. Commit `b33b06dd8832893d8f6429b074933f7b0fce2e70` is published on the integration branch; main unchanged.

## Cloud-only readiness fix — 2026-11-06

- Android chat now enables Send/Talk when either a local model is selected or cloud is enabled with URL, model, and saved encrypted API key. The header distinguishes cloud-first with Ollama fallback, cloud-only, and local-only. The provider router remains responsible for routing and fallback.
- Focused policy tests passed; full app and voice-library test/build passed: 50 tests, 0 failures/errors/skips across 18 XML suites. APK signature passed. SHA-256: `C07912A9B18D9D8827C9ABE427DB23A46B8F3A5F49AED8B69BD55F221647581B`; package `com.aura.ai` v1.0.0/code 1; signer digest unchanged.
- No phone install or device acceptance. Live cloud/Ollama inference and WhatsApp action remain unverified. See `AI_HANDOVER.md` for exact pending tests.

Commit `a0a4606` records the cloud-only chat-readiness fix and automated verification on `integration/aura-voice-consolidation`. Main unchanged; `device-backups/` excluded.

## Honor update and startup — 2026-11-06

- Device reconnected. Backed up accessible private app files to ignored `device-backups/com.aura.ai-preinstall-cloud-only-fix-20261106.tar` (997,376 bytes; SHA-256 `B7604B79A17CDFA52937361F7C9DD0E081AC7B6A350C70887C4A458A388F043A`). Verified old/new package IDs and matching Android Debug signer, then updated `com.aura.ai` in place with `adb install -r`; no uninstall/data clear. Separate `com.aura.ai.debug` was untouched.
- New APK SHA-256 `C07912A9B18D9D8827C9ABE427DB23A46B8F3A5F49AED8B69BD55F221647581B`. MainActivity started and was top resumed; recent log scan contained no fatal/AndroidRuntime exception matches.
- This is launch verification only. Live voice response is pending the user's test; provider calls, interruption, Ollama from phone, and WhatsApp remain unverified. Backup APK/data files remain untracked and excluded.

## Continuous voice implementation and build — 2026-11-06

- Added simultaneous listening/speaking state and opt-in mic monitoring during hands-free TTS. Likely TTS echo is filtered, and two stable non-filler ASR partials are required to barge in. Errors resume listening when recoverable; denied mic permission stops hands-free. Unit-tested code only; no device result.
- Focused voice tests passed. Full app + voice library tests and APK assembly passed: 56 tests, 0 failures/errors/skips across 19 XML suites; Gradle `BUILD SUCCESSFUL` in 19m13s. `git diff --check` passed before docs were appended.
- Fresh APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,862,524 bytes; `com.aura.ai` v1.0.0/code 1; SHA-256 `D9607C66F0835DFD73E1F77FA813BA1463A8E5405C46603A8411426E704048FE`; APK v2 signature verified; signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- Honor disconnected by instruction; no ADB used. Live speech, interruption, wake, backend network calls, and WhatsApp remain pending. Ollama was not started; no cloud credentials were used.
- Integration branch remains `integration/aura-voice-consolidation`; code/tests/records committed as `6e2a221`; main unchanged. Exclude `device-backups/`. Publication status to follow.

The Honor install/startup record is committed as `1a40245` and was pushed successfully to `origin/integration/aura-voice-consolidation`; `main` remains unchanged.

The voice-monitor implementation is `6e2a221`; final test-results handover is `ac8c9bc`. Both were pushed to the integration branch; `main` remains unchanged. Honor remains disconnected and the APK remains uninstalled.

## Wake listener lifecycle repair — 2026-11-06

- Stale recognizer callbacks are now ignored after a wake listener restarts or stops; recognizer construction/start failures use capped retry, while mic permission denial stops the service.
- Full app/voice tests and APK assemble passed: 58 tests, zero failures/errors/skips, 20 suites. APK `com.aura.ai` v1.0.0/code 1; SHA-256 `A242340DC1003BE359FC52F66638FC0884A284DF265A4F09E363054589BABFA4`; v2 signature verified. Not installed.
- No ADB or live device/provider test was performed. Device wake/audio, cloud/Ollama requests and WhatsApp remain pending. Branch `integration/aura-voice-consolidation`; main unchanged. Exclude `device-backups/`.
- Implementation `1eb7027` and handover `86e2f72` are pushed to `integration/aura-voice-consolidation`; `main` is unchanged.

## Wake-triggered microphone handoff — 2026-11-06

- Activity startup now skips a second standby wake service start when it was launched by the wake listener, removing a microphone ownership race with hands-free chat. Ordinary launches remain opt-in and permission-gated.
- Full Android unit/build verification: 60 tests, 0 failures/errors/skips, 20 suites; APK assembled and v2 signature verified. Fresh APK SHA-256 `9CF7CD5B965D36E964D124877165362C11514B180A8DE957E607E586454B0CB6`, package `com.aura.ai` v1.0.0/code 1. Not installed.
- No ADB/live voice/provider/WhatsApp test was run. Honor is disconnected. Main unchanged; exclude `device-backups/`.
- Wake-to-chat handoff source/tests `cb04a22` and records `25e9af6` are pushed on the integration branch; `main` unchanged.
- The publication confirmation `d95ec2a` is pushed. The newly built APK remains uninstalled; real-device/provider checks remain pending.

## Memory recall publication — 2026-11-06

- Source and tests are committed as `13b64ed0d788be2881e8375f798fc999b715e45b` and published to `origin/integration/aura-voice-consolidation`; `main` is unchanged.
- Build/test: offline app + voice unit tests and debug APK assemble passed; 81 tests, no failures/errors/skips. APK SHA-256 `9D46DEB9542FE2F200BDA0DDE98B7875B0F1106834D565FBE8808D0F699F56E5`; not installed.
- Honor remains disconnected; no real-device acceptance. See `AI_HANDOVER.md` and `NEXT_STEPS.md` for exact remaining checks.

## Offline quick-answer path — 2026-11-06

- Common current-local-time questions and existing arithmetic questions now bypass the model router; the Android UI permits only these deterministic answers when no model is ready. Other requests still require configured cloud/Ollama. No database, permission, or package identity changes.
- Full app/voice test reports show 86 tests (76 app, 10 voice library), zero failures/errors/skips. Fresh debug APK built and signature verified; SHA-256 `D9F08E1338CE993E75F6A7014F5DFA0F1D978DF88595985CEC9A4A320FAF0713`; not installed. Device and live backend tests remain pending.
- Source commit and remote publication state will be recorded in `AI_HANDOVER.md` after commit/push. `main` remains untouched.

## Offline quick-answer publication — 2026-11-06

- Commit `bfe0f2ef81da6205f706e487684f9fd2a38730b2` is published on `origin/integration/aura-voice-consolidation`; `main` unchanged.
- The 86 automated reports pass and a fresh signed APK was built, but it is not installed. Honor and live provider acceptance remains pending.

## Persistent voice tuning — 2026-11-06

- Settings now offers saved, bounded pitch and speaking-rate controls for Android voices; cloud voices remain provider-controlled. Voice ID is not changed. This tuning cannot create an Austrian accent.
- 88 app/voice tests passed (30 suites, zero failures/errors/skips), and a fresh signed `com.aura.ai` APK was built (SHA-256 in `AI_HANDOVER.md`). Not installed. Phone playback, persistence after restart, Austrian accent, hands-free, and live backends remain unverified.
- Source and handover commit details follow after publication; `main` is unchanged.

## Voice tuning publication — 2026-11-06

- `af603cf83fe379e275681b4676b8c298505b39e5` is published on `origin/integration/aura-voice-consolidation`; `main` unchanged.
- 88 app/voice tests passed; signed APK built and left uninstalled. Device validation remains pending.

## Wake retry recovery — 2026-11-06

- The wake listener now resumes after normal Android speech timeout/no-match in 800 ms, rather than treating silence as a repeated provider failure and eventually leaving 16-second listening gaps. Actual recognizer failures keep capped exponential backoff.
- 90 app/voice tests passed (31 suites, no failures/errors/skips); fresh signed debug APK built. SHA-256 is in `AI_HANDOVER.md`; not installed. No Honor acoustic/wake test was possible.
- Implementation and publication commit details follow. `main` remains unchanged.

## Wake recovery publication — 2026-11-06

- Commit `abf83a815d7311d153fbf3eb4e347e7394f0585c` was published on `origin/integration/aura-voice-consolidation`; `main` unchanged.
- 90 app/voice test reports passed, APK built and signature verified but not installed. Real-device wake and microphone checks remain pending.

## Conversation intent routing repair — 2026-11-06

- Ordinary questions mentioning words such as WhatsApp, message, queue, code, calendar, or file now stay on the direct conversation path. Explicit command-at-start requests, common polite command forms, wake-prefixed commands, calls, and reminders continue to use the action planner. Three regression tests cover both sides.
- App unit tests passed: 78 tests / 27 suites, zero failures/errors/skips. The unchanged voice module's existing reports show 12 tests / 4 suites passing; a redundant rerun was blocked by access denied on the Gradle wrapper lock before task execution.
- Single-worker offline debug APK assembly passed after the first combined run hit a disk-space error at dex merge. APK: `com.aura.ai` v1.0.0/code 1, 20,877,242 bytes, SHA-256 `1348A0BC74D1B83C6BF1D013C2500AC105443BB00793946319CAA473A07ED012`; signature verified with the established debug signer. Not installed.
- Honor is disconnected: no device voice/wake/interruption/Ollama/cloud/WhatsApp acceptance. Cloud/live Ollama not exercised. The APK build and JVM tests are not device proof.
- Changed files: `app/src/main/java/com/aura/ai/agent/ConversationIntent.kt`, `app/src/test/java/com/aura/ai/ConversationIntentTest.kt`, `AI_HANDOVER.md`, `PROJECT_STATUS.md`, and `NEXT_STEPS.md`. Starting commit `f7107c36ab7bcb4a3fabbb05fe0ad5e4b05a2b00`; implementation/docs commit and publication confirmation follow. `integration/aura-voice-consolidation` only; main and user backups untouched.
- Next: publish the current code/docs on the integration branch; after reconnection and exact-build approval, confirm question/action routing on the Honor, then verify live voice and configured cloud/Ollama behavior. Keep all device checks pending until run.

## Conversation routing commit — 2026-11-06

- Code/tests and baseline handover committed as `a487a29` (`Keep ordinary questions out of action planner`) on `integration/aura-voice-consolidation`. `main` unchanged. Publication confirmation follows; APK remains uninstalled.

## Conversation routing publication — 2026-11-06

- Implementation `a487a29` and handover record `035ee0f` are published to `origin/integration/aura-voice-consolidation`; production `main` is unchanged. No phone install or real-device test was performed.

## No-backend response reliability — 2026-11-07

- Chat no longer drops turns or disables the composer solely because no model is configured. After history loads, the user can submit or speak; the orchestrator reports that Aura needs a local Ollama model or a configured cloud provider. The status follows the normal transcript persistence and optional TTS path; no task is executed without a model.
- Verification: Android app and voice reports show 91 tests across 31 suites, 0 failures/errors/skips. Offline single-worker `:app:assembleDebug` succeeded. The debug APK is `com.aura.ai` v1.0.0/code 1, 20,877,183 bytes, SHA-256 `7C860B980741E322B134F3C9D1662A78F701DD39CA0978FFF192F329CA4E527C`; signature verified. Not installed.
- Honor disconnected: no real-device or live provider test. No microphone/voice/wake/interruption, Ollama/cloud, or WhatsApp acceptance claimed. Initial wrapper invocation hit access denied; retry with the existing Gradle cache completed successfully. No cache was cleared.
- Files changed: `ChatBackendAvailabilityPolicy.kt`, `ChatScreen.kt`, `ChatViewModel.kt`, `OllamaResult.kt`, `ChatBackendAvailabilityPolicyTest.kt`, `AI_HANDOVER.md`, `PROJECT_STATUS.md`, `VOICE_TESTS.md`, and `NEXT_STEPS.md`. Starting commit `6ee487ae47146c2b47fe1c6860e1cbc37c37bc33`; code/docs commit and remote publication follow. Integration branch only; `main` and backups untouched.
- Next: publish, then continue code-only reliability work. On phone reconnect and owner approval, verify a no-backend spoken request gets the clear status response, then verify full voice and backend/action paths on the actual Honor.

## No-backend response implementation commit — 2026-11-07

- Code, test, and initial records are committed as `5c93f2a` (`Explain missing model instead of dropping chat`) on `integration/aura-voice-consolidation`; `main` unchanged. `device-backups/` was excluded. Push result to be recorded after remote verification.

## No-backend response publication — 2026-11-07

- Implementation `5c93f2a` and handover record `6dc324a` are published; remote integration head is `6dc324aec2d6616da3e57ba9dfebfaae5ee56da5`. No main merge or APK installation.

## Speech-recognition stall watchdog — 2026-11-07

- The Android in-app recognizer now reports a recoverable failure if 30 seconds pass without speech progress. Speech beginning and partial-result callbacks reset the timer; final results, errors, stop, close, or request replacement cancel it. Chat's existing recoverable-error path can then restart listening rather than waiting indefinitely.
- Automated verification: 94 app/voice tests across 32 suites, zero failures/errors/skips; the new three timeout-policy tests passed. Offline app build/APK assembly succeeded in 9m01s. APK package/signature verified, SHA-256 `D7BADFCC1402F85E786D00131087E6BF3CFC4DA88218F0A0259D2700EEBEF293`, `com.aura.ai` v1.0.0/code 1. Not installed.
- No Honor/ADB or live ASR test; `adb devices -l` was empty. GitHub push is pending because this check could not connect to github.com:443 via the configured proxy. Local branch is `integration/aura-voice-consolidation`, starting commit `091c7a400e0969856fdd92a0cb055505598f7523`; local code/docs commit follows. `main` and backup data untouched.
- Next: commit locally, retry branch publication when GitHub is reachable, then test the watchdog and pauses on the Honor after exact-build approval. Continue separate cloud/Ollama, voice identity, interruption, and action checks; automated results do not establish them.

## Speech-recognition watchdog publication — 2026-11-07

- Watchdog code/tests and initial records are committed as `6518392` and the push to `origin/integration/aura-voice-consolidation` succeeded (`091c7a4..6518392`). `main` unchanged. Final remote head and handover confirmation follow; APK not installed.

## Speech-recognition watchdog final publication — 2026-11-07

- Final handover commit `a1a3447` is pushed; `git ls-remote` verified remote branch head `a1a34472b12d738ac54d16a84187ef6c77e1c8ef`. `main` unchanged; APK is still uninstalled and phone checks remain pending.

## Spoken action confidence guard — 2026-11-07

- Added a tested guard: valid ASR confidence below 0.45 on agent-action requests triggers a persisted transcript plus repeat request and prevents the unclear utterance from reaching the action agent. Other requests preserve their current routing.
- Automated checks: 97 tests across 33 app/voice suites passed (0 failures/errors/skips); offline debug APK build and signature/package verification passed. APK SHA-256: `DDC6A445B95E760D118F0D77E0D463DA0E84F93B3232E079F3FD57296530B4B0`; not installed.
- Honor disconnected: device ASR confidence distribution and end-to-end action prevention are unverified. 0.45 is not calibrated and needs phone evaluation. No new WhatsApp, voice, Ollama/cloud fallback, wake, or interruption capability is claimed.
- Current branch `integration/aura-voice-consolidation`; source and records committed/pushed as `8efb987ea687283edfbd71aff6d11bc86fa70294`. Remote head was verified. `main` and user data remain untouched; `device-backups/` was excluded.


## Provider cancellation regression check — 2026-11-07

- Added a local-only regression test for a caller timeout during cloud inference. It did not reach test execution because Gradle failed during `:app:compileDebugKotlin` with `There is not enough space on the disk`; the C: drive then reported 0 bytes free. No production fix or new APK was produced.
- Preserve the in-progress, uncommitted voice/settings edits in the local worktree. The cancellation test change is also uncommitted. Free disk space before another build, then inspect and test the existing edits before committing anything.


## Current continuation checkpoint — 2026-11-07

- Cloud router now distinguishes caller cancellation from a provider timeout before falling back to Ollama. Regression test added. Integration branch commits: `904b6bbd011ee9a139981bcc15e3d9e045443218` (router) and `9666bba3575c6a3d8a66706259702026b6a515b4` (test).
- Test/build status: NOT RUN/PENDING. C: has 967 MB free after the prior Gradle compile failed for insufficient disk. No fresh APK. Do not treat this patch as verified.
- Device status: Honor disconnected by user request. No install or real-device test. Existing cloud/local routing and WhatsApp draft capability still need live acceptance; this patch does not add new WhatsApp behavior.
- Next: recover disk space, sync the local checkout carefully (uncommitted voice/style work and backups remain), run targeted and full tests/build, then do phone tests only after reconnection and approval. Main is unchanged.


## Verification follow-up — 2026-11-07

- Focused cloud-caller cancellation test passed. Full app + aura-voice unit tests and debug assembly passed: 101 tests, 0 failures/errors/skips across 34 XML reports; Gradle `BUILD SUCCESSFUL` in 11m16s.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, 20,703,885 bytes, SHA-256 `D755ADDBD94D23A707016DF23421CE9BA7B579CBF64F2CB4A9A86BD3BBF6B716`; `com.aura.ai` v1.0.0/code 1; signature passed with established debug signer `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- APK source was a dirty local checkout at `a3e5c1cc79f78dd916c584833704f812efd84bcb`, including uncommitted voice/settings changes. Git metadata is protected outside the workspace, so exact clean remote-tip provenance is pending reconciliation. APK is not installed.
- Honor disconnected; all real-device checks remain pending. Do not claim live voice/Ollama/cloud/WhatsApp operation. No main merge.


## Exact acknowledgement build verification — 2026-11-07

- Action acknowledgement is exactly `I'll be back` in the tested local build. Full app/voice tests: 101 passed, 0 failures/errors/skips; APK assembly succeeded in 9m19s.
- APK SHA-256 `29BC3FA4F5C1E4255CED20C7685BF8C8FD68156757CC63A38C8FB4075D81FCF9`; package `com.aura.ai` v1.0.0/code 1 and signature verified. Artifact includes local uncommitted ElevenLabs style-control changes and is not a clean remote-tip build. Not installed.
- Honor disconnected. Device voice/wake/interruption/Ollama/cloud/WhatsApp checks remain pending. Main unchanged.


## Honor verification — 2026-11-07

- Fixed loopback Ollama URL migration and made Settings copy show the saved Base URL. On Honor, `http://192.168.8.18:11434` tested ONLINE and exposed both installed Qwen models; generation remains unverified.
- Automated app + voice suites: 104 passed, 0 failures/errors/skips. Debug APK package, signer and hash were checked; it was backed up and installed in place without data clear.
- Latest spoken test is a reported incorrect transcript on original `com.aura.ai`; exact transcript/confidence could not be captured before device lock. Voice acceptance is not passed. Main unchanged.

## Publication metadata — 2026-11-07

- Source and test changes, plus the Honor verification record, were published to this integration branch in commit `74848b6029db3368b01f27cb8dda997d12695740`. Files: `SettingsRepository.kt`, `SettingsScreen.kt` (Ollama endpoint text), `OllamaEndpointPolicyTest.kt`, and all four continuity documents. Main was not changed; device backups were excluded.
- The commit records 104 passing unit tests and a verified in-place APK install. The owner's latest recognition result is still failed and exact transcript capture is pending; no device voice pass is claimed.
- Workspace branch remains `integration/aura-voice-consolidation`, local HEAD `d5d0fe92ca890c964685403aee0fcafd3fe30fd6`; local Git metadata is protected, so no local branch mutation was performed.


## Wake transcript diagnostic — 2026-11-07

- Wake-triggered text now appears in the Agent diagnostic before model handling. This exposes what wake ASR heard; acoustic accuracy remains unverified.
- Automated tests/build: 106 passed, 0 failures/errors/skips. Updated `com.aura.ai` in place after backup and package/signature verification; user data was not cleared.
- Honor remains locked during post-install verification. Spoken transcript, Qwen answer, TTS, wake, interruption, and multi-turn acceptance are pending.


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


## Current verification state — 2026-11-07

- Integration branch: `integration/aura-voice-consolidation`, current code commit `42c6b8851620f8007ab5e6a1f86906226c0c2278`; main unchanged.
- Cloud preference now defaults to enabled when not previously set. A saved explicit off value remains off. Cloud still requires a provider URL, model, and encrypted API key. This change has not passed tests or a fresh APK build.
- Local Gradle verification stopped because C: had 0 bytes free; failure occurred before the test tasks completed. The prior 112 passing tests belong to an earlier code revision.
- CI run `42344494193` and one retry ended before any workflow steps/logs ran. No CI test result is available.
- Latest owner report: wake phrase yields an incorrect transcript; exact transcript/confidence not captured. Phone disconnected; no fresh device test or installation.
- Current source uses Android `SpeechRecognizer` for wake listening; no Vosk engine/dependency/model found. Dedicated wake-word detection is not verified.
- Next: free disk space safely, rerun tests/build, reconnect and verify Honor package/signature before approved install, capture the transcript/confidence, then fix and retest the ASR failure. Keep `device-backups/` private and uncommitted.


## Latest integration branch update — 2026-11-07

- Source branch head after publication: `db11eedcbea3ac0affd606a5a7fd994f90fa0b1a`; production main unchanged.
- The branch now carries wake confidence/alternate recognition hypotheses into visible transcript diagnostics, Android 13+ wake bias hint, low-confidence action repeat guard, message copy controls, and related voice/router tests.
- Local test reports before publication show 95 app tests + 17 voice-library tests passed (0 failures/errors/skips). The existing local APK is `9F7A8D184147C017A21AEB43FEB2C8C2577A1070E7CF6FDAF5661DB5C116C71D`, package `com.aura.ai` v1.0.0/code 1, signer verified. These results are from the pre-publication worktree, not a build of current remote head.
- Current-head build/test verification is pending: the wrapper lock is inaccessible; direct offline Gradle failed at `:app:checkDebugAarMetadata` with a null-path error before tests. No APK was generated for current head.
- Device: ADB disconnected. Owner reports incorrect transcript; exact recognized words/confidence not captured. No new install or device acceptance.
- Next: inspect CI for current head; get a successful build of current branch, reconnect Honor, obtain approval for that build, back up app data and verify package/signature, then capture transcript/confidence and test voice follow-through. Do not merge to main or include `device-backups/`.


## CI follow-up — 2026-11-07

- Workflow for source commit `db11eedcbea3ac0affd606a5a7fd994f90fa0b1a` failed before steps: job `128326059219`, `steps: null`, no log URL. Documentation run `42346867212` likewise failed before steps. The cause is not exposed; tests/build were not verified by CI.
- Local current-branch Gradle check remains blocked before tests. Earlier 112 pass reports and the pre-publication APK are recorded, but do not verify merged commit `db11eed`.
- Phone remains disconnected; inaccurate recognition is unresolved.


## 2026-11-12 wake transcript repair

Added a continuation guard for incomplete wake tails. Focused tests (3) passed; full verification reports app 99 + shared voice 18, zero failures/errors/skips; APK assembly passed. Fresh APK SHA-256 `BA1F1E4F17640D83DE43E8208A5A2B94EED4886EB21EEE4E71BA951E5EFA7471`, package `com.aura.ai`, existing signer, not installed. Honor /api/tags works over HTTP and lists both Qwen models; inference is unverified. Owner reports original-app transcript “I”; spoken answer not reported. Voice acceptance remains unresolved.


## 2026-11-12 approved build install

- Source commit `915b3f9e37525653bd5063a2e76eba04e797cbb2` is published to `integration/aura-voice-consolidation`; main is unchanged. Android local tests/build passed (app 99, shared voice 18, zero failures/errors/skips); no Android CI result is available.
- Owner approved the exact fresh APK SHA-256 `BA1F1E4F17640D83DE43E8208A5A2B94EED4886EB21EEE4E71BA951E5EFA7471`. Existing app data was backed up immediately before update to `device-backups/com.aura.ai-preupdate-20261112-BA1F.tar` (1,036,288 bytes, SHA-256 `2A30FFC8E80FB427916F849C131E3BFAE38F1926E2FF358CEB172B8EDAC29808`), and previous base APK preserved (A569… SHA). Both old and new APKs verified as `com.aura.ai`, version 1.0.0/code 1, same signing certificate.
- Installed in place using update mode; Android returned Success. Pulled installed base APK and verified it matches approved BA1F… SHA. `com.aura.ai.debug` remains installed but was force-stopped; original Aura was launched and its WakeWordService is active. Existing databases/shared preferences directories remain present; no uninstall or data clear.
- Live voice acceptance is pending owner retest. Previous transcript “i” was not attributable because ADB/logcat showed `com.aura.ai.debug` held audio input. Do not mark wake recognition or answer flow complete until a clean original-app test reports the transcript and spoken answer.
- Next: capture that live result, inspect original-package-only logs, then test follow-up/interruption and one actual Qwen generation through Aura. If ASR still clips, use the diagnostic alternatives/confidence to change the recognition strategy; do not claim success from build/tests alone.


## Android locale fallback update — 2026-11-12

- Device logs showed Android SpeechRecognizer error 12 for requested locale `en-ZA`; Android defines 12 as requested language not supported by that recognizer. The old wake fallback kept requesting the same unsupported locale.
- Published a deterministic ASR fallback: `en-ZA` → `en-GB` → `en-US`; wake listening falls back from on-device to system recognition and relaxes offline-only mode on fallback. Transcript diagnostics now include the locale used. The TTS/Austrian voice configuration is unchanged.
- Code commits: `10414666cc2473eaafdc7df8118be4becc1cb865` (wake service), `21e69a85b7f980d19c6d30d0929598a724a7ef64` (Android recognizer), `3365106a11f87449307806f4fcf8473f97910ffb` (policy), `49e5e272fdefee701591bf11f765594d84271ab1` (tests).
- Verification: full Gradle test/build succeeded in 12m38s; app 99 tests + voice library 21 tests, zero failures/errors/skips; APK assembly and signature verification passed. APK `com.aura.ai` v1.0.0/code 1, SHA-256 `8402932206749EAC09C0EDA6F92268E4004F633C35D4189EF104F315D7FAC3BE`.
- Owner explicitly asked to retain the original Aura and stop the duplicate opening. The separate `com.aura.ai.debug` package is disabled for the current Android user, not uninstalled; its data is preserved. Original `com.aura.ai` is the active app. No app data was cleared.
- Current phone remains on the previously approved BA1F… APK; the 840293… APK awaits approval for its exact hash before installation. Real-device validation of the fallback fix is pending.


## CRITICAL APP IDENTITY CORRECTION — 2026-11-12

This section supersedes any earlier handover statements claiming `com.aura.ai` is the original Aura or that `com.aura.ai.debug` is the duplicate.

- On the Honor, package metadata verified: `com.aura.ai.debug` is labeled “Aura AI” and matches the original Aura app; `com.aura.ai` is labeled “T1000.” The installed Aura APK was pulled for identity inspection, not modified. User context identifies the Aura AI icon as the original Terminator-skull app.
- Root workspace `T1000-main/android/app` has namespace `com.aura.ai`, debug applicationId suffix `.debug`, and app label “Aura AI”; this matches the installed original Aura package.
- The GitHub integration branch `ferreiraeshawn8209-app/aura-ai:integration/aura-voice-consolidation` has no `android/app` tree. Its `app/build.gradle.kts` builds `com.aura.ai`, and its app label/strings say “T1000.” Therefore this branch is T1000, not the original Aura target. The recently published locale fallback commits apply only to T1000; do not claim or install them as an original Aura fix.
- Owner asked to retain one app. Phone state was corrected reversibly: `com.aura.ai` (T1000) is disabled for user 0; `com.aura.ai.debug` (Aura AI) is enabled and was launched. Neither was uninstalled; no app data was cleared. Original Aura private data was backed up to `device-backups/com.aura.ai.debug-original-aura-before-isolation-20261112.tar` (14,136,320 bytes, SHA-256 `5DA4C808F9AD33CC627461980161E7EB6ADF867D5B5B0136617B3171A1C7FF04`). T1000 app data and its previous APK backups remain preserved too.
- The T1000 APK `8402932206749EAC09C0EDA6F92268E4004F633C35D4189EF104F315D7FAC3BE` was installed only into `com.aura.ai` after exact-hash approval, then that T1000 package was disabled per the owner's instruction. It did not update original `com.aura.ai.debug`; no new original Aura APK has been built or installed.
- Root Git state is not a valid project checkout: `git` resolves to a profile-level empty `master` with no remote. Root `AGENTS.md` requires leaving source intact when history/remote is missing or ambiguous. The original Aura source at `T1000-main/android/app` has not been modified. To continue coding the original safely, first restore/identify its correct Git checkout/remote; do not assume this T1000 integration branch is its source.
- Main remains unchanged. No real-device voice acceptance was completed for the original Aura, and the published T1000 ASR fix must not be described as fixing Aura.
