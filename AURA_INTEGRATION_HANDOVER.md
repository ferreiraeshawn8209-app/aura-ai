## Workspace/GitHub reconciliation and local Qwen runtime check — 2026-11-14

- Read the connected GitHub repository read-only. The canonical integration branch is present at `97dfa28541edd2f44f025ae888b3d69238a10850` (2026-11-13 06:25 UTC). GitHub comparison reports `integration/aura-voice-consolidation` is 182 commits ahead and 5 behind `main`; no merge or source sync was attempted.
- The local writable workspace is a separate checkout, `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, with no configured remote and pre-existing dirty user work. It is not the GitHub integration checkout. No source files were overwritten or copied between these checkouts. The GitHub handover says an earlier 5A17 build was installed on 2026-11-13; current ADB sees no Honor, so that historical phone state cannot be freshly verified here.
- From this local workspace, Ollama 0.40.16 at `127.0.0.1:11434` listed `qwen3:1.7b` and `qwen3:4b-q4_K_M`. A direct Qwen request without disabling thinking used a tiny response budget and yielded blank visible content; with Ollama's documented `think:false` parameter it returned a visible answer. The existing Aura PC route `POST /api/ollama/chat` then streamed `2 + 3 = 5` from `qwen3:1.7b`; the one-shot request unloaded the model. The active Ollama API behavior is documented at https://github.com/ollama/ollama/blob/main/docs/api.md.
- Android `OllamaRepository` already adds `think=false` for Qwen3 local and hosted routes; added a regression assertion to `OllamaChatRequestSerializationTest`. Focused command `:app:testDebugUnitTest --tests com.aura.ai.data.remote.ollama.OllamaChatRequestSerializationTest` passed: 2 tests, 0 failures/errors/skips. The first run hit access denied on the shared user Gradle wrapper lock; rerun with `--gradle-user-home .gradle-aura-cache` succeeded in 1m33s using the in-process compiler fallback after a Kotlin daemon marker access denial.
- Prior Windows website status correction is in the separate local checkout: a successful Explorer dispatch is reported as `launchRequested`, not a verified browser page. Local typecheck passed and focused Windows-control/status tests passed 16/16. A visible Aura preview window was not identified, so page-load behavior is not claimed verified.
- Current device/network state: `adb devices -l` reports no Honor. Remote Desktop Commander reports BEATK offline. From this workspace, `192.168.8.18:11434` and local port 11435 were unreachable; only local loopback Ollama is verified. No fresh phone backup, install, phone voice test, firewall change, or public service exposure occurred.
- Approved local APK remains `android/app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `F010A4E144451B587AB8D5D93F595A6A95EE64F24ADE761AB594F2B5AE3ED5B8`, package `com.aura.ai.debug` v1.0.0/code 1. Signature verification passed on the APK; installed-phone signature comparison and data backup remain pending because ADB has no device. No APK was installed in this check.
- No production `main` changes and no merge. Next: reconnect the Honor; compare installed package/signature and make/verify a fresh data backup before installing only the approved hash. Test wake, full transcript, spoken response, multi-turn interruption, selected voice, and actual app-to-PC Ollama. Separately restore PC LAN reachability before testing phone-local inference.

# Aura AI Integration Handover

Updated: 2026-11-05 Africa/Johannesburg
Repository: `ferreiraeshawn8209-app/aura-ai`
Working branch: `integration/aura-voice-consolidation`
Production `main` has not been changed or merged.

## Latest implementation and device update — 2026-11-05

Work below was performed from the existing `integration/aura-voice-consolidation` checkout; the old desktop APK was not used as source.

- Restored `gradle/wrapper/gradle-wrapper.jar` from the installed wrapper and set wrapper distribution to the cached Gradle 8.11.1 distribution after the 8.7 download returned 504.
- Corrected physical-device Ollama defaults from emulator-only `10.0.2.2` / `gemma3:1b-cloud` to the user-confirmed PC endpoint `http://192.168.8.18:11434` and installed model `qwen3:1.7b`. Known legacy emulator URL and `gemma3:1b-cloud` preferences normalize to those defaults; other user-entered settings remain intact.
- Changed speech recognition from `en-US` to `en-ZA`; added visible partial transcript, final transcript/confidence and error diagnostics. Simple spoken arithmetic is answered deterministically. The ordinary chat path now withholds partial model tokens until a complete reply arrives so a failed stream cannot be treated as a finished “I'm on it” answer.
- The chat mic button starts the hands-free session immediately. Added the requested disciplined, concise command-assistant wording; simple questions are instructed to receive direct answers without filler acknowledgements.
- The app still uses the single selected Android TTS provider and does not auto-switch. Before installation, existing app data showed selected TTS ID `ar-language` (Arabic), so an Austrian T-800 voice is **not** configured in this installation. No compatible cloud TTS service/voice ID is wired in this checkout; do not claim the accent requirement is met.

### Build, test and install evidence

- Build/test command: `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug`.
- Result: `BUILD SUCCESSFUL`; 16 app tests and 2 voice-library tests passed, 0 failures. New `SimpleMathIntentTest` covers the wake phrase plus “what's five plus five?” → `10`.
- Final APK: `app/build/outputs/apk/debug/app-debug.apk`, package `com.aura.ai`, version `1.0.0` / code `1`, size 20,662,135 bytes.
- Final APK SHA-256: `49CA00A7945EE1F1B0C82B58385C3F325E7292E9B5092DF630C25FFAF56A1EFD`.
- Signer SHA-256: `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`; this matched the existing installed `com.aura.ai` certificate. Package identity/version were verified before updating.
- Existing app data was backed up to `device-backups/aura-phone-com.aura.ai-before-integration-update-20261105.tar.dpapi` and integrity round-trip verified. `adb install -r` succeeded for the preceding build; no uninstall, clear-data, or package replacement was used.
- The preceding app launch was observed with no fatal exception. The Honor went offline from wireless ADB before final APK installation and live speech tests. Wake, mic, transcript, TTS, interruption and phone-to-PC model calls remain **unverified**.
- Direct PC request to `http://192.168.8.18:11434/api/tags` returned HTTP 200 and listed `qwen3:1.7b` and `qwen3:4b-q4_K_M`. A non-streaming chat request to `qwen3:1.7b` returned the exact sentinel `LOCAL_QWEN_OK`; cold load was 41.8 seconds. This confirms PC-local Qwen works, not the phone-to-PC route.

### Remaining critical gaps

- Cloud-first reasoning/failover/recovery is still absent: `AgentOrchestrator` uses `OllamaRepository` only. No cloud chat endpoint, secure cloud key configuration, or usage limit is implemented. The app cannot automatically fall back to cloud at this point.
- Austrian voice is not met: current selection is the persisted Android Arabic voice ID above. This build must preserve the selected voice; an authorized Austrian TTS provider and voice ID need to be configured before changing it.
- Wake standby uses Android `SpeechRecognizer`, not a dedicated on-device wake engine. Acoustic VAD/barge-in is absent; button-controlled interruption remains.
- App-generated mic beeps were not found in the inspected voice code. Android speech service/device tones are outside app control and were not tested on the phone.
- The eight-repository inventory/audit details remain as documented below; no main branch changes or merge.

## Spoken action acknowledgement update — 2026-11-05

- Spoken requests classified as actions now emit one `I'll be back` TTS event and suspend task execution until the selected TTS provider reports speech completion. If it cannot speak the acknowledgement, the task is not executed. The event is emitted only for spoken action requests, not typed requests or ordinary spoken questions.
- Final direct and task responses remove opening “I'm on it” / “I'll be back” duplicates before they are displayed or spoken. The completion prompt also forbids repeating the pre-action line.
- No voice settings were changed; the flow uses the persisted TTS provider/voice selection. Current stored voice remains `ar-language`, so it is not verified as the Austrian T-800 voice.
- Tests: 19 app unit tests and 2 voice-library tests passed, 0 failures. They cover policy (spoken action only) and response sanitizing; they do not prove speech ordering on a physical device.
- Rebuilt APK: `app/build/outputs/apk/debug/app-debug.apk`, package `com.aura.ai`, version `1.0.0` / code `1`, size 20,662,137 bytes, SHA-256 `A884A778B3587D347CBF85240C21CA9E830AFC5E4EEC42ECA6E8D68679BE8464`. Signer SHA-256 remains `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- The Honor is not connected to ADB. This latest APK has not been installed or device-tested; do not report the acknowledgement requirement complete until verified on the phone.
- Cloud fallback is absent in this branch, so cross-provider behavior cannot be verified. The acknowledgement is placed in the orchestration layer before tool execution and is not generated by the local model, but no cloud chat transport exists here.

## Delivery status

This branch now builds and produces a debug APK. The Android voice library and app changes are locally integrated. This is **not yet the complete requested system**: cloud-first inference/failover and real-device conversation acceptance remain unimplemented/unverified. Do not describe the app as finished.

## Source review

The existing handover and the eight named repositories were reviewed. Repository inventory:

1. `aura-ai` — canonical Android Kotlin/Compose app.
2. `Aura-Voice-Subsystem` — compatible reusable Kotlin voice library; integrated at `lib/aura-voice`.
3. `aura-core` — separate Next.js/TypeScript backend and voice contracts, not a drop-in Android chat service.
4. `code-companion-pro` — separate web/server coding agent.
5. `tony-my-ai-friend` — web application with voice hooks/settings.
6. `keypilot-ai` — credential orchestration product, not a mobile voice subsystem.
7. `spokdot` — separate web AI provider implementation.
8. `vocalocal` — singing/voice-conversion backend, not conversational Android TTS.

Only the voice subsystem was directly compatible with this native app. No other repository was copied blindly. The promised separate eight-repository audit attachment was not present in this session; this inventory comes from the accessible GitHub repositories and handover.

Pull request status checked through GitHub:

- [PR #10](https://github.com/ferreiraeshawn8209-app/aura-ai/pull/10): merged.
- [PR #15](https://github.com/ferreiraeshawn8209-app/aura-ai/pull/15): open draft, not merged.
- [PR #16](https://github.com/ferreiraeshawn8209-app/aura-ai/pull/16): open draft, not merged. Its permission-aware recognizer backoff and fail-closed verification behavior have been incorporated into this working branch.

## Changes made

- Added `:aura-voice` from `Aura-Voice-Subsystem`; wired Android speech recognition, Android TTS, shared voice settings, preview, and persistent selected voice into the existing app.
- Added a continuous in-app conversation loop: start hands-free once, re-listen after a response, stop session, and tap to interrupt speech. This is button/session interruption; acoustic barge-in is not implemented.
- Added installed voice selection with a presentation label and preview. The selected voice ID is persisted and is not silently replaced by another TTS provider.
- Changed the wake phrase to “Yo, yo, yo” and connected wake events to the in-app voice session. The wake service now stops on missing microphone permission and uses bounded exponential restart backoff.
- Changed agent verification to fail closed when its verification response is missing or malformed.
- Fixed existing compile/runtime blockers in Room migration/DAO wiring, Hilt dependency wiring, malformed Kotlin/JSON strings, model declarations, Compose state collection, and resources.
- Replaced `ActionParser`'s Android `org.json` dependency with kotlinx.serialization JSON parsing so parser unit tests run correctly on the JVM.
- Changed network logging in both paths to avoid body logging and redact `Authorization` headers.
- Added `PROJECT_STATUS.md`, `VOICE_TESTS.md`, and `NEXT_STEPS.md`.

## Prior baseline build evidence (superseded by the latest results above)

Command run with installed Gradle 8.11.1:

```text
:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug
BUILD SUCCESSFUL in 1m 34s
```

- App tests: 10 passed (ActionParser 7, logging redaction 1, Ollama model parsing 2).
- Voice subsystem tests: 2 passed.
- `git diff --check`: passed before documentation update; repeat before commit.
- APK: `app/build/outputs/apk/debug/app-debug.apk`
- APK size: 20,658,213 bytes
- APK SHA-256: `A448021B004F51C5BCFAD1A3B946E6BB3473DA8E7FB8643973DF7EB509D1B427`

This is a debug APK suitable for sideloading/testing. It has **not** been installed over the phone's app in this task. ADB detected an Honor device, but no device-level run has been performed.

## Remaining blockers and truth about capability

- **Cloud-first / local fallback:** not implemented. `AgentOrchestrator` still calls `OllamaRepository` directly; there is no configured cloud provider in that path, no health router, automatic failover/recovery, usage limits, or cloud-key UI/secure storage flow. No cloud endpoint/credential was available for verifying a real request. Do not claim local/cloud failover works.
- **Voice identity:** this build persists an Android system TTS voice selection. The Austrian-accented deep masculine voice is not included or synthesized here. Android pitch/rate controls cannot create the requested accent. An installed authorized TTS voice/provider with the selected identity is required. No ElevenLabs voice or “voice 3” connection is configured.
- **Conversation reliability:** user reports of “I’m on it” followed by no final spoken response remain unresolved at device level. Automated JVM tests do not exercise microphone permission, speech engine behavior, Android background limits, speaker echo, or wake listening.
- **Wake word:** current standby recognizer is Android `SpeechRecognizer`, not a dedicated on-device keyword detector. Availability, privacy, battery, lock-screen behavior, and false activation need real-device testing.
- **Barge-in:** only UI-controlled stop/interrupt is implemented. VAD-based acoustic interruption and echo cancellation have not been implemented or verified.
- **Other repositories:** compatibility was reviewed; none of the remaining web products is an Android library that can be safely merged as-is. Their server APIs would need scoped contracts and authentication before selective integration.
- **Audit attachment:** not received. No missing GitHub repository permissions were observed for the named repositories.
- **Deployment at this baseline checkpoint:** no merge or main-branch change occurred. Phone install was pending then; the latest update above records the approved in-place installation.

## Required follow-up

1. Finish the separate audit review if/when its attachment is provided; update the inventory if it contains additional repositories or findings.
2. Add a configurable cloud provider with encrypted credential storage and a tested cloud-first/local fallback router; include failure/recovery/offline tests.
3. Run hands-free acceptance tests on the Honor: wake phrase, transcript, final response spoken once, multi-turn, interruption, permission denial/recovery, and app restart. Record actual results.
4. Confirm which installed/licensed TTS provider supplies the Austrian English voice; test persistence and previews on the phone.
5. Complete the final APK installation and real-device voice tests after the Honor reconnects. Do not merge to `main` without approval.

## Commit record

Implementation commit: `1ecfc0d` (`Integrate Aura voice subsystem and repair Android build`).
Latest implementation and test/handover update: `46f84a0` (`Fix Aura voice flow and phone Ollama defaults`), pushed to GitHub branch `integration/aura-voice-consolidation`; `main` is untouched.
Spoken acknowledgement change: `09cf455` (`Speak one pre-action acknowledgment for voice commands`). APK build/test results are recorded above; physical-device acceptance remains pending.

## GitHub audit checkpoint — 2026-11-05 (read-only verification)

- Re-read this handover directly from the integration branch.
- GitHub compare `main...integration/aura-voice-consolidation` reports branch **ahead by 8 commits, behind by 0**. Base `main` commit: `4b48185e9f110764938f10548f80a29acda674f7`. No merge was performed in this audit.
- Changed-file inventory includes the Android voice library, orchestrator, wake service, chat view model, voice settings, unit tests, and documentation. This confirms files are present on the branch, not that device behavior passes.
- Keep outstanding gates open: cloud-first/local fallback, authorized Austrian TTS identity, physical Honor install and speech acceptance, acoustic barge-in, and complete eight-repository integration decisions.
- This checkpoint involved GitHub metadata review only. No APK build, PC connectivity check, physical-device test, or new feature integration was performed. Do not count earlier test results as newly rerun.
- Next implementation should occur on the integration branch, preserve user data and voice selections, test failure/recovery cases, and append exact commits, test output, and device results before seeking approval to merge.

## Spoken-action gate continuation — 2026-11-05

- Added `SpokenActionGate` around the existing executor so spoken actions wait for the selected TTS voice to finish the one required “I'll be back” acknowledgement. The gate aborts the task if the voice reports failure; ordinary questions bypass acknowledgement. Files: `AgentOrchestrator.kt`, new `SpokenActionGate.kt`, new `SpokenActionGateTest.kt`.
- Automated verification on BEATK: `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` succeeded, tasks up-to-date; XML reports contain 22 tests, 0 failures/errors/skips across 8 suites. APK metadata confirms `com.aura.ai` v1.0.0/code 1; signature certificate SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`; APK SHA-256 `5FAF6738A7DA194EF3274813552D6E557D7F723D8EB51AD8CCC48ECEB3123B89`. It was not installed.
- Current branch pre-task HEAD was `448b96002d32e7474e43bf57002f6009e562d3a1`. Existing untracked `device-backups/` was preserved and not staged. Phone tests remain pending; cloud fallback and requested Austrian voice are not implemented.


## Fresh test rerun — 2026-11-05

- `:app:testDebugUnitTest :aura-voice:testDebugUnitTest --rerun-tasks` was forced and completed `BUILD SUCCESSFUL` in 10m02s, 50 actionable tasks executed. JUnit reports: 22 passed, 0 failures/errors/skips across 8 suites. Existing debug APK assembly task had succeeded as up-to-date; APK was not installed.


## Commit record — 2026-11-05

- Code and test commit: `b93d67d (rebased commit; previous local hash ec7a312 was replaced during safe integration)` (`Gate spoken actions on acknowledgement completion`) on `integration/aura-voice-consolidation`. Production `main` unchanged. `device-backups/` remains untracked and was excluded.


## Cloud/local and WhatsApp integration — 2026-11-06

- Existing Aura now routes inference through a new provider router. With cloud enabled and complete settings, an OpenAI-compatible chat-completions endpoint is tried first. Failed requests/timeouts fall back to the existing selected Ollama model; failures trigger a 30-second cooldown before cloud is retried on a future request.
- Settings expose cloud endpoint/model/API key; API key is stored through the existing encrypted Android storage and not logged. Cloud remains opt-in. No actual cloud key or live provider test was available.
- Chat trace now identifies whether the response came from cloud or Ollama and explains fallback.
- Added a WhatsApp draft action using the official wa.me link format. It opens standard WhatsApp with prefilled text and requires the user to review/tap Send. No automatic send.
- Final Gradle command passed in 14m26s: 32 tests, no failures/errors/skips in 11 suites; debug APK assemble passed. APK package/version: com.aura.ai 1.0.0/code 1; SHA-256 0A85F8C5811A5F3F332CB93A5985AD0C775D67AFD4FE56A3CF62283C5FBDA64A; signer SHA-256 matches the prior debug signer, 1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3.
- APK was not installed; Honor is disconnected. No real cloud, phone-to-PC Ollama, WhatsApp launch/send, microphone, wake, voice, or interruption acceptance test occurred. Ollama is stopped.
- Still missing: user-configurable daily spend/request budget; actual provider credentials; Austrian TTS voice (current saved Android voice ID ar-language); dedicated wake-word engine/acoustic barge-in. The handset acceptance gate remains open.
- No database change, app identity change, replacement APK install, or main merge.


## Commit record — 2026-11-06

- Cloud/local routing, WhatsApp draft tool, tests, and updated project docs committed as 45eaf56dc6caf629927c6ba0e23319e2f2351d11 (Add cloud fallback and WhatsApp draft actions) on integration/aura-voice-consolidation.
- Prior spoken-action gate is b93d67d after rebasing on the latest remote audit commits. Branch has not been merged to main. The debug APK is not installed; Honor tests remain pending.


## Branch publication — 2026-11-06

GitHub accepted the integration branch push: origin/integration/aura-voice-consolidation now includes the spoken-action gate and cloud/WhatsApp work through commit b7ba19425b8c0cf6ba2d40cba26f4375049d94bd. Production main remains unchanged. Phone acceptance and live provider tests remain pending.
## Cloud request cap continuation — 2026-11-06

- Settings now provides a persisted daily cloud request cap, default 60 and configurable from 1 to 1,000. Reservation is atomic and based on UTC day. When exhausted, cloud is skipped and the existing Ollama route is attempted with a trace reason. This is a request-count limit, not a monetary spend limit; an agent task with multiple inference steps can consume multiple units.
- Latest build/test result: Gradle app + aura-voice unit tests + debug APK assemble passed in 12m20s; 75 tasks (15 executed, 60 up-to-date), 35 tests, 0 failures/errors/skips, 12 suites. git diff --check passed. Exact APK SHA-256 and signer are in AI_HANDOVER.md.
- No physical phone or live model/provider test occurred. Honor is disconnected; Ollama is stopped; cloud credentials are not configured. No APK installed. Existing app package/database remain unchanged.
- Next after this docs update: commit only the quota implementation and handover records on integration/aura-voice-consolidation, push branch and confirm remote head. When the phone reconnects, obtain authorization for this exact build before in-place installation, then test voice, wake, interruptions, local Ollama, WhatsApp draft opening, and cloud success/fallback/recovery. Continue Austrian TTS integration only with an authorized provider/voice identity. Keep main unchanged.
## Implementation commit — 2026-11-06
- Daily cloud request cap and verification record committed as f4a2e0a9d5cb50c2ee97f084f9e6209e3e2593b6 on integration/aura-voice-consolidation. The commit preserves existing app identity/data and excludes device-backups/. Main remains unchanged. Branch publication is pending.
## GitHub publication — 2026-11-06

The latest quota implementation and test record are pushed to origin/integration/aura-voice-consolidation, verified at 8c332e49254fa4d40a372794202bf8ca61c1e2e1. No main merge. The existing device-backups/ directory was not staged.
## Voice capture lifecycle improvement — 2026-11-06

- Code inspection found each new Android listening turn created another SpeechRecognizer without destroying the previous one. The provider now invalidates callbacks from old turns and releases the old recognizer on restart, stop, close, final result and recognition error. Permission/runtime setup failures become explicit voice errors.
- Added request-gate JVM tests. Build/test command passed in 10m23s: 39 tests, zero failures/errors/skips in 14 suites; APK assembled and signature verified. See AI_HANDOVER.md for exact APK metadata and SHA-256.
- Device acceptance is still blocked only by the phone being disconnected (per user instruction). Do not claim microphone, wake word, transcript, acoustic interruption, or voice response works from this code/build evidence. The platform recognizer still needs a real-device test and is not evidence of a dedicated hotword engine.
- Provider routing and WhatsApp draft work from the previous commit remain. Live cloud needs user endpoint/model/key; Ollama is currently stopped. The Austrian TTS voice is still unavailable. Daily cloud quota is a request-count limit, not spend in currency.
## Speech lifecycle commit — 2026-11-06

- Provider cleanup and unit regression checks are committed as e3d86b3215ca67a88f595a77e01dc8946255a1f3 on integration/aura-voice-consolidation. Push and verify branch; do not merge to main.
## Publication update — 2026-11-06

The speech lifecycle code commit e3d86b3215ca67a88f595a77e01dc8946255a1f3 and handover commit 12af8d08565847d406672fa4741b77c4123f3 were pushed successfully. main remains untouched. The Honor remains disconnected, so physical tests are pending.

## Saved transcript integration — 2026-11-06

- Reused the existing Room conversation repository in active Agent chat to load/persist user and assistant messages; added transcript search and copy via text selection. No database/schema changes.
- Added 3 JVM tests. App and `aura-voice` tests: 42 passed, 0 failures/errors/skips, 15 suites. Debug APK build passed; `git diff --check` passed. Full run: 21m12s.
- APK package/version/signer verified; SHA-256 `1614E230F6663E00D86D975B5CFE21F9BF84E7F998EADB24C98F3E356F8CB3F1`. Not installed. The Honor remains disconnected by request.
- No live device voice, Ollama/cloud routing, or WhatsApp run is claimed. Provider credentials are not configured; Austrian voice remains unavailable and the existing `ar-language` preference is preserved.

Implementation commit: `d0e8e9c` on `integration/aura-voice-consolidation`; branch push was verified in the following entry; main unchanged.

## Publication verification — 2026-11-06

- Immediately after publishing the code and first handover commits, GitHub `origin/integration/aura-voice-consolidation` resolved to `1ae7c1c99562b4c00ca767e2f57ade323202bf68`; `main` remains `4b48185e9f110764938f10548f80a29acda674f7`. No merge. The publication step in the prior entry is complete.

## Ollama reachability — PC only — 2026-11-06

A direct request from BEATK to the configured PC LAN host `http://192.168.8.18:11434/api/tags` succeeded and returned `qwen3:1.7b` and `qwen3:4b-q4_K_M`. The proxy route failed at local proxy address `127.0.0.1:9`; the no-proxy request worked. No configuration was changed. This does not verify phone-to-PC connectivity or model inference on Honor.

## Optional ElevenLabs voice provider — 2026-11-06

The existing voice subsystem now has an optional ElevenLabs cloud TTS provider and private-cache audio player, registered alongside Android TTS. Aura settings support a masked key saved through existing encrypted storage, provider voice discovery with gender/accent labels, exact voice selection and preview. The saved voice was not changed. No API key, live provider call, audio preview or Austrian voice confirmation was available in this code-only session.

Regression history: initial test run exposed three Android JSONObject stub failures; changed request and response JSON handling to the project's kotlinx.serialization implementation. Corrected a missing typed put import after a compile failure. A subsequent KSP run hit low C: disk space; cleared only verified repo-local generated intermediates/cache/temp directories while preserving outputs, source, backups and user data. Focused tests then passed. Final app+voice tests and debug assembly passed in 5m21s: 41 tests, zero failures/errors/skips across 14 suites. APK verification and hash are recorded in AI_HANDOVER.md.

Not installed; Honor disconnected by instruction. No real-device audio, wake, interruption, transcript, Ollama/cloud, or WhatsApp acceptance is claimed. Continue on integration/aura-voice-consolidation; do not merge main. device-backups/ excluded.


## Commit record — 2026-11-06

Implementation commit: 3eb89d5cdb1b8c634797c971dcc43527188b317e (Add optional ElevenLabs voice provider) on integration/aura-voice-consolidation. It includes provider code, tests, and project records. Main unchanged; device-backups/ was excluded.

## Branch publication — 2026-11-06

GitHub origin/integration/aura-voice-consolidation was pushed and verified at 50bb1ed4d404a42454f57355931779b12e413bd0. This includes implementation commit 3eb89d5cdb1b8c634797c971dcc43527188b317e and documentation commit 50bb1ed4d404a42454f57355931779b12e413bd0. Main was not updated. The existing device-backups/ folder remains untracked and excluded.

## Wake-listener restart lifecycle — 2026-11-06

- `ChatViewModel.stopHandsFreeSession()` now restarts standby wake listening when the saved wake-word setting is enabled and microphone permission is granted. Start failures surface in the chat banner. Added a pure policy helper and a unit test spanning the enabled/disabled and granted/denied conditions.
- Files changed: `app/src/main/java/com/aura/ai/ui/chat/ChatViewModel.kt`; added `app/src/main/java/com/aura/ai/voice/wake/WakeWordResumePolicy.kt`, `app/src/test/java/com/aura/ai/voice/wake/WakeWordResumePolicyTest.kt`.
- BEATK verification: `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` succeeded in 9m08s. App XML: 42 passed, 0 failed/error/skipped, 15 suites. No aura-voice test XML was found. APK signature verified; package `com.aura.ai` v1.0.0/code 1, signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`, APK SHA-256 `C3659671B0E42BB488E8CFC3E87075CB74D1106C4B1ABD1E5A84C4EB6F317DA4`.
- Honor disconnected; APK not installed; all hardware/audio and phone-backend tests remain pending. Branch `integration/aura-voice-consolidation`; starting HEAD `cfdf931`. Main untouched; `device-backups/` excluded.
- Next: verify the fresh APK and backed-up app data before installing once the phone reconnects, then run the physical voice/Ollama/cloud/WhatsApp checks.
- Code and records were committed as `04fd7a4ad8e3927b2f6d39271fe83b4afc57d61c` and pushed to the integration branch. Remote head matched; `main` remains unchanged.

## Delayed wake restart race guard — 2026-11-06

- Wake listener restart now rechecks that no hands-free session became active while persisted setting and microphone permission were being read. Added the active-session policy case to the JVM test.
- Verification on BEATK: `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` passed in 8m27s; app reports 42/42 tests, 0 failed/errors/skipped, 15 suites; voice-library test task up-to-date. `git diff --check` passed before handover edits.
- Latest APK: `com.aura.ai` v1.0.0/code 1, 20,862,522 bytes, SHA-256 `3C5BA73F4582281AC2A27BAEB17B3BD008E68F068566438E876CBDB12CAAD511`; signature verified with existing Android Debug signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- Honor disconnected, APK not installed, phone voice/Ollama/cloud/WhatsApp checks pending. Main unchanged.
- Final code/test/handover commit `b33b06dd8832893d8f6429b074933f7b0fce2e70` is pushed and verified on `integration/aura-voice-consolidation`; `main` remains unchanged.


## 2026-11-12 continuation guard and network check

Aura now continues listening when wake recognition returns an explicit incomplete fragment, then merges the next recognition segment before inference. The 3-case focused policy test passed; full reports show app 99 + shared voice 18, no failures/errors/skips; APK assembly passed. Fresh APK is `BA1F1E4F17640D83DE43E8208A5A2B94EED4886EB21EEE4E71BA951E5EFA7471`, not installed. Honor successfully fetched Ollama /api/tags and found both Qwen models, but no inference was run. Voice test remains unresolved: owner reported “I” and no spoken answer. Do not claim continuous conversation works until retested.

## Local desktop continuation status — 2026-11-12

This entry records evidence gathered in the local Windows checkout at C:\Users\shawn\Desktop\T1000-main. That checkout is on branch codex/aura-original, HEAD 471ef9d, and has no configured Git remote. Its source changes are not included in this handover-only commit and are not represented as code changes on integration/aura-voice-consolidation.

- Android App Builder timeout source now follows the existing configured 10–600 second setting instead of a hard 150–180 second cap. Full Android JVM regression: 754 passed, 0 failures/errors/skips. Debug APK assembled and package/signature verified.
- Current local debug APK candidate: SHA-256 FD5990AE32E8B89DB41283261AD776C9D4F03594AF69E5639997215A059C3FDD; package com.aura.ai.debug, version 1.0.0. It is not installed. ADB lists no connected Honor. Earlier owner approvals named other APK hashes; do not install this candidate without exact-hash approval and a fresh verified app-data backup.
- Direct local Ollama smoke check found qwen3:1.7b and qwen3:4b-q4_K_M. A tiny HTML generation prompt completed through direct local /api/chat in 58.3 seconds on Qwen 1.7B and 127 seconds on Qwen 4B. Direct GET to http://192.168.8.18:11434/api/tags returned HTTP 200. This does not verify Android Builder file creation, preview/export, or phone network access.
- PC web voice output was changed to preserve the exact selected voice and report unavailability without silently substituting another provider. Web checks: node --test --test-isolation=none 46/46 passed, pnpm.cmd typecheck passed, and Next.js 16.3.3 Webpack production build passed. Browser inspection of the rebuilt local preview showed the no-substitution message. No mic permission/audio, recognition, wake, or interruption test was performed.
- Rebuilt local preview: http://127.0.0.1:3009/. The existing :3008 page still showed old voice-settings wording; it was left running and untouched.
- Local AI_HANDOVER.md, PROJECT_STATUS.md, VOICE_TESTS.md, and NEXT_STEPS.md were updated with the full evidence and exact pending tests. This GitHub update is documentation-only; no implementation files were pushed, and main was not changed.
- Next: reconnect Honor; back up app data; verify the exact approved APK hash/package/signature before in-place install; test the Builder through Aura using a short Qwen 1.7B request; verify wake/transcript/spoken reply/interruption on device; configure and test the authorized Austrian voice on the PC before microphone/audio acceptance. Preserve saved prompts and chat history.

## Critical APK identity gate — 2026-11-12 (supersedes local-candidate install wording)

A final inspection of the local Android Gradle configuration found that the debug build applies applicationIdSuffix ".debug". The available artifact FD5990AE32E8B89DB41283261AD776C9D4F03594AF69E5639997215A059C3FDD is therefore com.aura.ai.debug, not the canonical installed Aura package com.aura.ai. Its certificate SHA-256 is 59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b. The canonical integration history records the original app as com.aura.ai with historical signer 1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3; re-verify the actual installed signer on the phone before any update.

Do not install the .debug artifact: it would create a separate app and is not update-compatible with original Aura. No install or backup was attempted; ADB lists no connected Honor. This correction supersedes prior next-step wording that suggested installing SHA FD5990AE… after approval. First produce an artifact with package exactly com.aura.ai and a certificate verified to match the installed original app. If its signing key is unavailable, stop and request the correct signing workflow. Back up app data and request exact-artifact approval only after package, signer, and hash are confirmed. No source code was pushed to this branch from the local codex/aura-original checkout; this is documentation-only.

## Release-signing availability check — 2026-11-12

The local Windows checkout's Gradle configuration sets the release application ID to com.aura.ai and adds .debug only to debug builds. The existing output android/app/build/outputs/apk/release/app-release-unsigned.apk is an older unsigned artifact; its metadata lists com.aura.ai and its SHA-256 is 73C7AFA8A792AD742ED63A48D0A01A27D9A035985C3B3CADD68C00EC3452662E. It was preserved and not overwritten.

All four Codemagic signing variables (CM_KEYSTORE_PATH, CM_KEYSTORE_PASSWORD, CM_KEY_ALIAS, CM_KEY_PASSWORD) are unset in this session, and the configured workspace-local debug keystore path is absent. No release build was run because it would produce another unsigned artifact and overwrite the existing user artifact. No installable update-compatible original Aura APK is currently available. Keep the separate com.aura.ai.debug artifact and the unsigned release artifact off the phone. The owner must restore the original signing key through a secure approved signing workflow; do not request the private key or password in chat. Verify the signer against the actual installed app when the Honor is connected.

No source code or APK was pushed or installed in this documentation-only update. The local checkout is codex/aura-original at 471ef9d with no remote; the Honor remains absent from ADB. Production main remains unchanged.


## Approved draft-save APK installation attempt — 2026-11-12

- The owner approved only APK SHA-256 `3FB0AF7496A94E53218BB3BBE7C7AE83056256579D47093445D3D38576F7B952`, conditional on backing up the installed app data and verifying package/signature.
- That exact APK is absent from the local Android build outputs and top-level Desktop/Downloads APK locations. Available build artifacts have different hashes, including debug `FD5990AE…A059C3FDD` (`com.aura.ai.debug`) and an unsigned `com.aura.ai` release APK. No artifact was substituted.
- `adb devices -l` returned no devices. No backup, installed package/signer comparison, installation, or draft persistence test was performed. This is an unavailable-artifact/device blocker, not a failed app test.
- Local code, APKs, phone data, and production main were not changed. Local checkout remains `codex/aura-original` at `471ef9d`, without a configured remote.
- Next: reconnect the Honor and restore the exact approved APK (or request approval for a newly built exact hash), verify package and signing certificate, make and verify a fresh app-data backup, then update Aura in place and test drafts across closing/reopening. Do not install the separate `.debug` app or unsigned release artifact.


## Builder draft persistence test improvement — 2026-11-12

- Local test source was strengthened at `android/app/src/test/java/com/aura/ai/data/settings/AppBuilderPromptPersistenceTest.kt` to recreate the Preferences DataStore after closing its scope, verify the saved prompt after reopening, clear it, then verify the cleared state after another recreation.
- Attempted the focused test using installed and cached Gradle 8.11.1; both failed before project configuration because Windows native services could not load `native-platform.dll`. This checkout's `android/` directory also lacks the Gradle wrapper and root `settings.gradle(.kts)`; only `app/build.gradle.kts` is present. The new test is therefore unverified and no APK was built.
- Local source is on `codex/aura-original` at `471ef9d`, no remote; this test edit is uncommitted and not pushed as code. The exact owner-approved install APK `3FB0AF7496A94E53218BB3BBE7C7AE83056256579D47093445D3D38576F7B952` remains unavailable locally, and ADB reports no Honor. No backup, install, or device test occurred; no other APK was substituted.
- Next: restore the verified full Android Gradle root/wrapper, run the focused persistence test and regression suite, then reconnect the Honor and verify APK package/signature and data backup before the approved in-place install/draft-reopen test. Any new APK hash requires fresh approval.


## Builder persistence test result correction — 2026-11-12

Correction to the preceding environment note: the repository-root Gradle wrapper and settings files do exist. An initial attempt from the `android/` subdirectory was invalid. Running the root wrapper with `GRADLE_USER_HOME` directed to the ignored `.aura-gradle-cache` workspace directory avoided the access-denied lock under the global user cache. The focused test first exposed an active-DataStore-instance issue; after the test joined the cancelled store scopes before reopening, `:app:testDebugUnitTest --tests com.aura.ai.data.settings.AppBuilderPromptPersistenceTest` passed **1/1** with `BUILD SUCCESSFUL`. This is JVM DataStore persistence evidence only; the UI's close/reopen behavior is not device-tested. No APK was assembled.

The exact approved APK hash `3FB0AF7496A94E53218BB3BBE7C7AE83056256579D47093445D3D38576F7B952` is still not present in the local build/download locations, and `adb devices -l` still lists no Honor. No data backup, install, or real-device test occurred. The test edit and local source are uncommitted on `codex/aura-original` at `471ef9d`, with no remote configured; no source implementation was pushed. Main was not changed.


## Android voice-path audit and regression run — 2026-11-12

- Reviewed the local wake capture flow from Vosk partials through Android SpeechRecognizer finals/alternatives, WakePhraseSession, arithmetic fast-answer routing, AgentOrchestrator.runVoice, and queued TTS playback. The code includes transcript merging, an arithmetic fast path, and final-speech replay. No new mic/ASR/TTS code was changed because no fresh device transcript or log evidence identifies the failing stage.
- The local draft-persistence test waits for each Preferences DataStore scope to cancel and join before recreating it. Focused persistence test passed **1/1**. Full local app suite `:app:testDebugUnitTest` passed **754 tests, 0 failures, 0 errors, 0 skipped**. Gradle wrapper ran with `GRADLE_USER_HOME` redirected to the ignored workspace cache. A combined task invocation also named `:aura-voice:testDebugUnitTest`, which is not a project in this checkout, so that invocation did not run; the valid app-only suite passed.
- `git diff --check` passed. No APK was assembled or installed. ADB lists no Honor, and exact owner-approved APK SHA `3FB0AF7496A94E53218BB3BBE7C7AE83056256579D47093445D3D38576F7B952` is unavailable locally. No data backup, package/signer comparison, install, microphone, transcript, TTS, interruption, or UI draft reopen test occurred.
- Local source/test changes remain uncommitted on `codex/aura-original`, HEAD `471ef9d`, with no remote. This documentation update does not publish implementation source. Production main remains unchanged.
- Next: reconnect Honor, restore the exact approved APK (or obtain approval for a newly built hash), verify package/signing identity, back up app data, then install in place and test draft restoration plus wake/transcript/spoken-answer/follow-up/interruption on-device. Record the actual transcript and audio outcome; JVM tests do not establish device voice reliability.


## Local Ollama Builder timeout repair — 2026-11-12

This is a handover-only update based on the local Windows checkout at `C:\Users\shawn\Desktop\T1000-main`; it does not publish source code to this branch.

- Raised the local text inference attempt cap before configured cloud fallback from 45 to 180 seconds in `ProviderChatRouter.kt`, still bounded by the configured request timeout. The shorter voice-specific limits (30 seconds with a backup, 60 seconds local-only) were not changed. Earlier direct PC timing samples were about 58 seconds for Qwen 1.7B and 127 seconds for Qwen 4B on a tiny generation.
- Added/updated router regression coverage for the configured timeout and Qwen 4B local success without a cloud call.
- Verification in the local checkout: `ProviderChatRouterTest` 9/9; `AppBuilderProviderPolicyTest` 8/8; full `:app:testDebugUnitTest` 755 passed, 0 failures/errors/skips; `git diff --check` passed. The successful focused run took 5m14s after an interrupted compile attempt; the full suite completed in 1m34s.
- Automated tests do not verify the live Aura Builder screen or generated file/preview/export. No APK was built or installed in this task.
- Honor remains absent from ADB. Owner-approved APK SHA `3FB0AF7496A94E53218BB3BBE7C7AE83056256579D47093445D3D38576F7B952` was unavailable; no different artifact was installed. No data backup, package/signer check, draft reopen, microphone, TTS or phone acceptance test occurred. Available debug APK uses `com.aura.ai.debug`, not the original Aura package.
- Local source remains uncommitted on `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, without a configured remote. No code was pushed to this integration branch and `main` is unchanged.
- Next: recover the exact approved APK or obtain approval for a new exact hash; reconnect Honor; verify package/signer and make a data backup before any in-place install. Then test draft persistence and a short local Builder generation through Aura, including file, preview/export and elapsed time. Keep device voice and backend failover acceptance open.


## Builder-specific timeout setting — 2026-11-12

This is a documentation-only record based on local checkout `C:\Users\shawn\Desktop\T1000-main`; its source changes are not pushed to this branch.

- A further review found the previous 180-second text-router cap did not help the local App Builder when its local-only policy disabled provider fallback: with no next provider, the router uses the configured request timeout, which defaults to 120 seconds. This remained below the measured ~127-second Qwen 4B cold-generation sample.
- Added a dedicated persisted App Builder timeout setting, default 180 seconds and adjustable 30–600 seconds from the Builder screen. Build requests now use this value; the general chat timeout and the safeguard disabling paid cloud fallback for local builds are unchanged. The existing 180-second router cap remains for requests that do have a fallback provider. Hands-free limits remain unchanged.
- Changed local files: `SettingsRepository.kt`, `AppBuilderRepository.kt`, `AppBuilderViewModel.kt`, `AppBuilderScreen.kt`, `AppBuilderProviderPolicyTest.kt`, and `T1000SettingsDefaultsTest.kt`; continuity files were also updated locally. No database migration or existing setting was replaced; the new DataStore key defaults when absent.
- Focused Builder/default tests passed. Full `:app:testDebugUnitTest` passed **755 tests, 0 failures/errors/skips**; `git diff --check` passed. These automated tests compile the changed Android UI/logic but do not prove live Builder generation, on-device preference persistence, or HTML export.
- No APK was assembled or installed. The exact owner-approved APK hash `3FB0AF7496A94E53218BB3BBE7C7AE83056256579D47093445D3D38576F7B952` remains unavailable, and the Honor is absent from ADB. No backup, signing check, install, phone UI test, or real-device voice acceptance occurred. Do not substitute `com.aura.ai.debug` or the unsigned release APK.
- Local Git remains `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no configured remote. No source code was committed or pushed, and `main` is unchanged.
- Next: verify the new Builder setting through a real original Aura install (exact approved APK or fresh approval for a new signed build), then run a short Qwen 4B build with at least 180 seconds, confirm saved timeout after relaunch, generated HTML, preview and export. Keep those device checks unverified until performed.


## Builder timeout guidance correction — 2026-11-12

- Updated local `AppBuilderFailureMessage` so timeout errors direct users to the Builder’s new “Build time limit” control instead of the unrelated global chat request-timeout setting. Updated the existing focused assertion.
- Re-ran the focused Builder policy test and full `:app:testDebugUnitTest` suite after the change. Full suite passed **755 tests, 0 failures/errors/skips**. No APK assembly or device test was run.
- Local code remains uncommitted on `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`; no local remote. This handover commit contains no implementation source and does not change `main`.


## Local Aura continuation — wake acknowledgement policy — 2026-11-13

This update reflects work in the local Windows checkout at C:\\Users\\shawn\\Desktop\\T1000-main. The local checkout is on codex/aura-original at 471ef9d4c34bc62758908b9efe1f5dff2af54941 and has no configured Git remote. Its source changes are not included in this GitHub branch; this entry records evidence only.

- Changed the local original Aura wake acknowledgement path to use a bounded timeout based on the currently selected voice engine (native 3 seconds, network voice 5 seconds). If playback fails, Aura reports the selected-voice failure in its state/notification and then continues command recognition; no replacement voice is selected by this path.
- Standalone Kotlin check: compiled the exact WakeAcknowledgementPolicy.kt source using cached Kotlin 2.2.10 and executed five assertions. Result: PASS 5/5 (native/network timeout, successful completion, trimmed provider failure, and default failure text).
- Android integration verification remains incomplete: the root Gradle focused test reached :app:compileDebugKotlin but remained active for roughly 30 minutes; it was stopped before test execution to limit PC load. No Android JUnit assertion or APK build ran. Existing 761-test Android suite, PC typecheck and 22/22 Node tests predate this change.
- Honor is reachable by Wireless ADB. The original app resolves as com.aura.ai.debug/com.aura.ai.MainActivity. A direct launch was issued, but the latest top-resumed activity check showed Chrome rather than Aura, so no fresh phone transcript/voice answer was verified. No test utterance, message, setting change, backup, or install occurred.
- Owner-approved APK SHA 3FB0AF7496A94E53218BB3BBE7C7AE83056256579D47093445D3D38576F7B952 is not present in the local checkout. No other APK was substituted or installed.
- Local files changed: Speaker.kt, WakeWordService.kt, new WakeAcknowledgementPolicy.kt, new WakeAcknowledgementPolicyTest.kt, plus AI_HANDOVER.md, PROJECT_STATUS.md, VOICE_TESTS.md and NEXT_STEPS.md. git diff --check passed (exit 0; line-ending warnings only). The app integration test and actual device voice behavior remain unverified.
- No source commit or push was made from the local checkout because it has no configured remote. This handover update is documentation-only on integration/aura-voice-consolidation; production main is unchanged.

Next: complete the focused Android compile/JUnit test without a prolonged uncontrolled build, have the owner foreground original Aura and provide the live phrase result, then build/hash/signature-check an APK and request approval for that exact new hash before any backup or in-place install.

## Honor foreground recheck — 2026-11-13

- A fresh ADB check now shows the Honor connected and original Aura top-resumed as com.aura.ai.debug/com.aura.ai.MainActivity. This supersedes the preceding observation that Chrome was foreground.
- No owner-run test phrase/result has arrived yet. Wake recognition, transcript, local-model answer, selected-voice playback, continuous listening and interruption therefore remain unverified on the phone. No test prompt or action was initiated by the agent; app data/settings were not changed and no APK was installed.
- Local docs were updated. The Android focused Gradle test remains incomplete; only the standalone 5-assertion policy smoke passed. No production merge.


## Honor ASR failure and candidate route change — 2026-11-13

Local Windows checkout: C:\\Users\\shawn\\Desktop\\T1000-main, branch codex/aura-original, HEAD 471ef9d4c34bc62758908b9efe1f5dff2af54941, no configured remote. The source changes below have not been pushed to this GitHub branch; this entry records test evidence and current installation gate.

- Owner reported the Honor captured only the first word, then showed a “usage error.” Logcat during the test showed Android Speech Services requested en-US and logged “Failed to get language pack of required locale: error 13.” The device system locale is en-ZA. Aura logged 4–5 alternatives but wakePhraseDetected=false; no AI provider call or 402/429 is evidenced. The failure occurred before AI inference; the exact UI wording was not found in logs.
- Local code changes now prioritize the regular Android system recognizer when available, keep on-device recognition as fallback, preserve bundled Vosk fallback, set EXTRA_PREFER_OFFLINE=false, and log backend/language/error code/result word count without transcript text. Saved language setting was not modified.
- Verification: standalone routing harness passed 5/5; focused Gradle policy test passed; full :app:testDebugUnitTest passed with 766 tests, 0 failures/errors/skips across 175 XML report files. Debug APK assembly succeeded.
- Candidate android/app/build/outputs/apk/debug/app-debug.apk SHA-256 0AB97E6B77FA4E01DC3E529C65731885AFE88EC347220CD69BF1E6C708EDFA99, package com.aura.ai.debug v1.0.0/code 1. Signature verified; signer SHA-256 59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b matches installed Aura. Installed base APK hash is D7F8322BDA43C98A2FF7099EB749ED87F3807A2301451EAF0585AA20330CFCD6. Candidate is update-compatible but not installed; prior approval applied to a different hash. No backup/install occurred. Latest foreground was Lovable, not Aura; post-fix device voice is unverified.
- No production main change. Next: request approval for exact candidate hash, make/verify app-data backup, install in place, test phrase/transcript/spoken result/provider route, and inspect new diagnostic logs if it still truncates.

## Honor voice failure evidence and updated local-Qwen candidate — 2026-11-13

- Owner approved and installed APK 0AB97E6B77FA4E01DC3E529C65731885AFE88EC347220CD69BF1E6C708EDFA99 in place. Before updating, installed base package com.aura.ai.debug SHA-256 was D7F8322BDA43C98A2FF7099EB749ED87F3807A2301451EAF0585AA20330CFCD6; signer matched the candidate signer SHA-256 59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b. adb install -r succeeded; no uninstall/data clear. First install time was preserved, installed APK pulled back from the Honor matched the approved SHA, original Aura MainActivity launched.
- Fresh app-data archive: aura-ai-integration/device-backups/com.aura.ai.debug-pre-0AB97E6B-20261113-021242.tar, 14,193,152 bytes, SHA-256 A36001F4DC1AA27A14B3F80C6D9143E83DDC9A0FE55BC01C6075752E10593B4F, 127 entries. Verified Room DB/WAL/SHM, DataStore settings, secure preferences and all 3 saved Builder outputs. All remain present after install.
- After update the wake service was stopped. Aura's Resume control restarted the foreground service, but this is not proof of speech recognition. The UI showed an Ollama HTTP 402 usage-credit message and Android speech logs showed error 13 for en-ZA (language pack unavailable).
- Saved non-secret route configuration is OLLAMA, http://192.168.8.18:11434, qwen3:4b-q4_K_M, automatic fallback enabled. PC /api/version and /api/tags returned successfully with Qwen 1.7B and 4B models. A local, non-cloud Qwen 4B test returned LOCAL_QWEN_OK in 128.71s. Evidence explains the 402: the app's local voice attempt was capped at 30s, then automatic fallback called paid Ollama Cloud before the selected local Qwen cold run could finish.
- Focused code fix now routes English language-pack-unavailable errors directly to bundled English Vosk before trying another Android locale. The selected Qwen 4B voice route now receives 180s before cloud fallback. A regression case verifies Qwen 4B can complete without a cloud request.
- Source files changed: android/app/src/main/java/com/aura/ai/voice/SpeechToText.kt, android/app/src/main/java/com/aura/ai/data/remote/ProviderChatRouter.kt, and android/app/src/test/java/com/aura/ai/data/remote/ProviderChatRouterTest.kt. These source changes remain local only.
- Automated results: focused route/ASR policy suite passed; full app suite 767 tests, 0 failures/errors/skips; :app:assembleDebug succeeded; git diff --check passed. Gradle used .gradle-aura-cache after the default cache was denied; the Kotlin daemon could not create its temp marker, so fallback compilation was used.
- New candidate SHA-256 41E164EF25434FB200E2547E7CB857002880DE715709BCCA9C1F5DE218CF13D9; package com.aura.ai.debug, version 1.0.0/code 1, verified signer matches the installed Aura app. This candidate has not been installed; the prior approval was for a different APK. A fresh exact-hash approval and data backup are required.
- Device voice acceptance remains open: no full transcript/answer or new-candidate microphone result is verified. Next: obtain approval for this exact hash, back up, update in place, then test wake phrase, full sentence, spoken local Qwen answer, provider route, a second turn and interruption. Main was not changed.
- Local checkout: codex/aura-original, HEAD 471ef9d4c34bc62758908b9efe1f5dff2af54941, no configured remote; no local source commit. This GitHub write updates only this handover on integration/aura-voice-consolidation.


## Honor test-state recheck — 2026-11-13

- ADB detects the Honor CRT-NX1, but ActivityManager reports ChatGPT foreground rather than Aura. The sampled last 2,500 device log lines contain no new Aura speech-recognition/provider events, and the owner has not supplied a result from the latest attempted phrase. Understanding is therefore still unverified.
- Installed original Aura remains `com.aura.ai.debug` v1.0.0, last updated 2026-11-13 02:14:24, matching the previously approved `0AB97E6B…EDFA99` build. Candidate SHA-256 `41E164EF25434FB200E2547E7CB857002880DE715709BCCA9C1F5DE218CF13D9` was rechecked, but it is not installed; the earlier approval does not cover this artifact.
- No source, app data, settings, permissions, or installed APK changed during this check. No tests were run. The candidate's prior automated verification (767 Android unit tests and debug assembly) is recorded above; real-device voice acceptance is still pending.
- Local checkout remains `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, with no Git remote and pre-existing user changes preserved. This update is documentation-only on the integration branch; production `main` was not changed.
- Next: after exact-hash approval, make and verify a fresh private-data backup, update Aura in place, then capture the complete transcript, spoken answer, model route and latency. Do not treat the wake acknowledgement as task completion.


## PC preview and web voice regression check — 2026-11-13

- Existing Aura web preview at `http://127.0.0.1:3008/` returned HTTP 200 with a 30,456-byte response. This establishes that the page is serving, not that microphone or spoken conversation works.
- `pnpm.cmd typecheck`: passed. `node --test --test-isolation=none app/simple-arithmetic.test.mjs`: 22/22 passed, 0 failed. `git diff --check`: passed with existing LF/CRLF warnings.
- Computer Use UI inspection stopped before input because the helper could not verify the browser URL. No window input or setting change occurred; no live PC mic/voice test was performed.
- No application source, phone data/settings, permissions, or APK changed during this check. The Honor remains on the previous approved APK; candidate `41E164EF25434FB200E2547E7CB857002880DE715709BCCA9C1F5DE218CF13D9` is still uninstalled pending exact-hash approval.
- Local checkout remains `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote, existing edits preserved. This is a handover-only commit on the integration branch; production main is unchanged. Next: test the PC microphone via a supported verified browser path; install/test the Android candidate only after exact-hash approval and a fresh verified backup.


## Approved 41E APK installed on Honor — 2026-11-13

- Owner approved exact candidate SHA-256 `41E164EF25434FB200E2547E7CB857002880DE715709BCCA9C1F5DE218CF13D9`. APK package `com.aura.ai.debug`, label Aura AI, version 1.0.0/code 1, signature verified. Signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b` matches the installed base APK signer; prior installed base hash `0ab97e6b77fa4e01dc3e529c65731885afe88ec347220cd69bf1e6c708edfa99`.
- Fresh full private-data backup `aura-ai-integration/device-backups/com.aura.ai.debug-pre-41E-20261113-031512.tar`: 14,193,152 bytes; SHA-256 `45a22c49780a708d0ecf1ac7f9a2f1870c42dfe1ad1031f4f5ef7f886fba4b96`; 127 entries. Verified database/WAL/SHM, DataStore settings, secure preferences, and three Builder HTML files.
- `adb install -r` succeeded; no uninstall or clear-data. Pulled installed APK hash exactly matches approved candidate. Package/version unchanged, first install date preserved, last update 2026-11-13 03:15:45; saved Builder files and settings remain present.
- Opened original Aura MainActivity. Listener was stopped after update; tapped its existing Resume control. Screenshot now shows “Ready quietly. Say ‘Yo, yo, yo.’” and green ready status. This verifies resumed listener, not successful recognition.
- Existing HTTP 402 on screen is persisted conversation history; no fresh prompt was submitted after update. A read-only log monitor is running for the next spoken test. No end-to-end voice acceptance pass yet.
- No app source changed in this install. Local checkout remains `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote; previous edits preserved. Production main untouched. Next: capture exact transcript, spoken answer and provider latency for one phrase; then test second turn and interruption if the first full turn succeeds.


## Post-install foreground and live-test check — 2026-11-13

- The phone had returned to ChatGPT while the initial post-install monitor ran. Brought the original Aura activity `com.aura.ai.debug/com.aura.ai.MainActivity` back to foreground; screenshot confirms “Ready quietly. Say ‘Yo, yo, yo.’” and the hands-free green indicator. `WakeWordService` remains in the foreground.
- No fresh command-recognition/provider event appeared in the running filtered log monitor and no owner-reported transcript/answer was received. Therefore no spoken-phrase test has passed yet. Existing Ollama 402 is persisted history; no new model request was sent.
- No code, settings, permissions, or app data changed in this follow-up. Next: speak the test phrase while Aura is in front and report the exact transcript and spoken answer; then correlate the logs.


## Honor wake log recheck — 2026-11-13

- At the time of a read-only device log sample, ChatGPT was top-resumed. The original Aura background service finalized three Vosk segments (alternative counts 4, 5, and 2), each with `wakePhraseDetected=false`. This shows no wake activation for those audio segments, but it is not a valid test of the owner speaking directly to Aura because another app was foreground.
- Brought original Aura back to foreground; ActivityManager resolves `com.aura.ai.debug/com.aura.ai.MainActivity`. The screen remains “Ready quietly”; no final command transcript or answer has been logged. Current end-to-end voice result remains pending.
- No code, data, settings, or permissions changed. Next: end any other voice session, speak the arithmetic phrase directly to Aura, and record visible transcript and spoken result.


## Current Honor audio-source state — 2026-11-13

- Latest ActivityManager check shows ChatGPT top-resumed, while Aura's `WakeWordService` remains registered as a foreground service. The recent 3,000 log lines contain no new wake finalization, command recognition, wake-match, or microphone-silencing/restoration event.
- Earlier segments while ChatGPT was foreground had no wake match; they were not a valid test of a phrase directed to Aura. No current spoken-answer acceptance result is verified.
- No code, app data, settings, permissions, or installed APK changed in this recheck. Next: switch to original Aura with any other voice session ended, say the test phrase, and capture the transcript/answer.


## Foreground-only failed-wake transcript diagnostics — 2026-11-13

- Implemented a transient diagnostic preview for a finalized speech result that does not match the wake session. Aura displays `Heard: <recognized words>` only while its activity is foreground; the text is trimmed, capped at 160 characters, held in memory only, and cleared after five seconds. No transcript/audio is persisted or written to logs. Activity visibility is set on `MainActivity.onResume()` and cleared on `onPause()`.
- Local source changes: `android/app/src/main/java/com/aura/ai/MainActivity.kt`, `android/app/src/main/java/com/aura/ai/voice/WakeWordService.kt`, new `android/app/src/main/java/com/aura/ai/voice/state/VoiceUiVisibility.kt`, new `android/app/src/main/java/com/aura/ai/voice/wake/WakeTranscriptPreviewPolicy.kt`, and new `android/app/src/test/java/com/aura/ai/voice/wake/WakeTranscriptPreviewPolicyTest.kt`.
- Automated verification: focused `:app:testDebugUnitTest --tests com.aura.ai.voice.wake.WakeTranscriptPreviewPolicyTest` passed (3 tests, 0 failures/errors/skips); `:app:assembleDebug` passed; `git diff --check` passed with existing line-ending warnings.
- APK candidate: `android/app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `5E7332DBFD0348083634F1B25598AE0CF16D7F19BABD42821A24391BF40A5A5C`. Signature verification passed; package `com.aura.ai.debug`, label Aura AI, version 1.0.0/code 1; signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`, matching the installed original app. This candidate is not installed; prior approval was for the different `41E164…CF13D9` APK.
- Device state: Honor connected, but top-resumed app was Honor Camera. No new spoken recognition/response was captured. Existing installed 41E build remains; live hands-free remains unverified.
- Local Git checkout: `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no configured remote. Local source edits remain uncommitted; no unrelated branch or production main was changed. This GitHub update documents evidence only and does not contain source changes.
- Next: obtain approval for this exact APK hash before a fresh data backup and in-place install; then test one actual spoken phrase in original Aura and confirm the temporary on-screen transcript plus spoken final answer.

### Follow-up regression check — 2026-11-13

- Filtered Android Gradle run for `HandsFreeTranscriptMergerTest`, `WakeWordDetectorTest`, and `WakeTranscriptPreviewPolicyTest`: **25 passed, 0 failed/errors/skipped** (19 + 3 + 3). These validate deterministic transcript/wake policies; they do not exercise a physical microphone.
- Latest Honor read-only check: device connected, Honor Camera top-resumed. Two Vosk segments logged `wakePhraseDetected=false` while Aura was not foreground; no valid Aura-directed utterance or spoken answer was observed.
- No new APK install, app setting/data change, or permission change. Candidate hash remains `5E7332DBFD0348083634F1B25598AE0CF16D7F19BABD42821A24391BF40A5A5C` and requires exact-hash install approval.

### Full Android regression confirmation — 2026-11-13

- Ran `:app:testDebugUnitTest --no-daemon --max-workers=1`: **770 tests across 176 suites passed, 0 failures/errors/skips**. This is automated JVM evidence, not physical-device speech acceptance.
- `git diff --check` passed with existing Windows LF/CRLF warnings. No APK install, app-data/setting change, or permission change occurred.
- Exact candidate remains `5E7332DBFD0348083634F1B25598AE0CF16D7F19BABD42821A24391BF40A5A5C`; it is not installed pending approval for this exact hash. The Honor's latest foreground app was Camera, so no spoken test was captured.


## Android ASR partial-fragment guard — 2026-11-13

- Local code change in the original Aura Android checkout rejects incomplete/arbitrary single-word live ASR partials after recognizer failure, while preserving intentional standalone replies and meaningful one-word answers during an active conversation. It is a defensive fix consistent with the owner's “first word only” reports; the causal link is not yet confirmed on the Honor.
- Changed local files: `android/app/src/main/java/com/aura/ai/voice/HandsFreeTranscriptMerger.kt`, `android/app/src/main/java/com/aura/ai/voice/WakeWordService.kt`, and `android/app/src/test/java/com/aura/ai/voice/HandsFreeTranscriptMergerTest.kt`.
- Verification: full Android JVM suite **772 tests passed, 0 failures/errors/skips in 176 suites**; debug assembly succeeded. Fresh candidate `B2A19831BF3CE16B8AA3F175228A9CA61CE58DD093042B1C5AB43BCDECA80F36`, package `com.aura.ai.debug`, version 1.0.0/code 1, signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- Current device state: Honor connected, ChatGPT top-resumed. Candidate has not been installed; current approved installation is the earlier 41E build. No new speech attempt was captured. Device-level recognition, conversation, TTS, interruption and provider fallback remain unverified for this change.
- Local checkout remains `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, without a remote; the source change is not pushed. This is a documentation-only update on `integration/aura-voice-consolidation`; production `main` was not changed.
- Next: get exact-hash approval for the B2A candidate, make and verify a fresh app-data backup, install in place with `adb install -r`, then test the wake phrase and capture transcript, spoken answer, provider and latency.

## Final and partial ASR transcript filter — 2026-11-13

- Follow-up closed a gap in the previous fix: Android can report an early fragment as a final result, bypassing a filter that only handled partial recovery. Aura now applies the same completeness check to both selected final transcripts and partials. Intentional standalone replies and contextual short answers in an active conversation remain supported.
- Local source changes: `android/app/src/main/java/com/aura/ai/voice/HandsFreeTranscriptMerger.kt`, `android/app/src/main/java/com/aura/ai/voice/WakeWordService.kt`, `android/app/src/test/java/com/aura/ai/voice/HandsFreeTranscriptMergerTest.kt`.
- Tests/build: full Android unit suite **773 passed; 0 failures/errors/skips across 176 suites**; `:app:assembleDebug` passed; `git diff --check` passed.
- Current unsigned-for-install gate: APK SHA-256 `E56181293D683FAC9901E9842D9892A3F9D484415D37AE8AF84303BCAA0BC6E2`; package `com.aura.ai.debug`, version 1.0.0/code 1; apksigner exit 0; signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- Honor connected, ChatGPT top-resumed. Candidate is not installed; the earlier 41E build remains installed. No on-device speech result for this fix. Recognition accuracy, spoken answer, multi-turn conversation, interruption, and local/cloud routing remain unverified.
- Local checkout is `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, without a configured remote. No source was pushed. This is a documentation-only commit on `integration/aura-voice-consolidation`; production `main` is untouched.
- Next: obtain exact-hash approval, back up and verify Aura private data, update in place without uninstall/clear-data, then run a real Honor phrase/follow-up/interruption test and capture transcript, answer, provider, and latency.

## Voice transcript persistence source trace — 2026-11-13

- Read-only code trace: once a command is accepted, `WakeWordService.handleCommand` inserts the recognized request as a user row in the existing Room message store. `ChatViewModel` observes those rows and maps them into visible turns. `ChatScreen` has saved-history search and copy-transcript controls.
- Wake/ASR preview text is transient in-memory UI state. It is not a substitute for an accepted, persisted conversation turn. No database/schema change was made.
- This proves the source path exists, not that the Honor recognized, displayed, or retained a real spoken transcript. Add transcript visibility, persistence after reopening, search, and copy to the device acceptance test after the exact E561 APK is approved and installed.
- Local docs were updated. No code or app data changed in this trace; production `main` remains untouched.

## Device and saved-history check — 2026-11-13

- ADB still detects the Honor, but the current top-resumed app is the video-editing app `com.camerasideas.trimmer/com.camerasideas.instashot.VideoEditActivity`. Aura was not brought forward; no install, launch, app-data, or settings operation was performed. Installed Aura remains `com.aura.ai.debug` v1.0.0, last updated 2026-11-13 03:15:45.
- Candidate E561 hash remains `E56181293D683FAC9901E9842D9892A3F9D484415D37AE8AF84303BCAA0BC6E2`; it is not installed and still needs exact-hash approval.
- JUnit report from the latest full 773-test run shows `ConversationHistoryPolicyTest` passed 5/5. Coverage includes case-insensitive search, blank/no-match search behavior, clipboard ordering/speaker labels, and removal of blank streaming bubbles.
- This verifies transcript presentation policy, not live speech recognition or physical-device save/search/copy. The complete Honor voice flow remains unverified.

## Windows companion regression recheck — 2026-11-13

- Existing PC preview at `http://127.0.0.1:3008/` returned HTTP 200 (30,456 bytes). This verifies the page serves, not browser audio behavior.
- `pnpm.cmd typecheck` passed. `node --test --test-isolation=none app/simple-arithmetic.test.mjs` passed **22/22**, covering transcript formatting/memory, voice selection policy, wake phrase joining, barge-in and echo filtering.
- Read-only code review confirms continuous browser recognition, segment joining after silence, continued command mode after a turn, and speech cancellation for stable non-echo interruption. No additional PC source defect was established or changed.
- Live browser microphone, audible answer, and acoustic interruption remain untested.
- Honor remains connected with the video-editing app top-resumed. Candidate E561 hash `E56181293D683FAC9901E9842D9892A3F9D484415D37AE8AF84303BCAA0BC6E2` remains uninstalled pending exact-hash approval. No phone data/settings changed.
- Local checkout `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote. No application-source change or production-main change in this check.

## Approved Honor APK install and smoke check — 2026-11-13

- The owner approved exact Aura APK SHA-256 `41E164EF25434FB200E2547E7CB857002880DE715709BCCA9C1F5DE218CF13D9`. The build output at the current workspace path is a different E561 artifact and was not installed. A read-only pull of the Honor's installed `com.aura.ai.debug` base APK matched the exact approved 41E hash, confirming this build was already installed before this task.
- Created a full private-app-sandbox tar backup at `.gradle-aura-cache/device-backups/aura-data-20261113.tar` (14,197,248 bytes; tar listing validation succeeded). It includes the app sandbox, such as Room databases, preferences, files, and cache. Attempts to enable Windows EFS or alter its ACL were denied; the backup is not encrypted and retains inherited account/SYSTEM/Administrators permissions.
- APK signature verification passed; signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`. Package is `com.aura.ai.debug`, version 1.0.0/code 1. Reinstalled the exact verified APK in place with `adb install -r` (Success); no uninstall or clear-data command was used.
- Post-install, first-install time stayed `2026-10-30 01:25:43`; `databases`, `files`, and `shared_prefs` remained present. Aura process started (PID 24098); no matching fatal-exception entry appeared in the recent 300-line logcat sample. This is install/startup smoke evidence only.
- No source files changed and no automated tests were rerun for this install task. Real wake/ASR/transcript/TTS, multi-turn, interruption, Austrian voice, Ollama, and cloud-fallback behavior remain unverified. The owner must speak the test phrase and report the exact transcript and audible response.
- Local checkout: branch `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, with pre-existing edits and no configured remote. No local source commit/push or production-main change. E561 is still a distinct unapproved/uninstalled artifact.


## Automatic online-first routing implementation — 2026-11-13

- Added persisted `AUTOMATIC` provider mode and a Settings action “Use automatic (online first).” With configured keys/models the order is OpenAI-compatible cloud, Ollama Cloud, then local Ollama. With no configured cloud key/model, local Ollama is selected. Existing explicit saved provider values are preserved; only an absent/invalid value uses Automatic. Cloud settings edits preserve Automatic when already selected.
- Chat route readiness uses the shared ordering policy. App Builder routes using Automatic when enabled; explicitly selecting local Ollama keeps the Builder local-only and disables hosted fallback to avoid surprise charges. UI labels warn that cloud usage can be billed.
- Local source files changed: `android/app/src/main/java/com/aura/ai/agent/ProviderFallbackPolicy.kt`, `data/remote/ProviderChatRouter.kt`, `data/settings/SettingsRepository.kt`, `ui/settings/SettingsViewModel.kt`, `ui/settings/SettingsScreen.kt`, `ui/chat/ChatViewModel.kt`, and `builder/AppBuilderRepository.kt`. Regression tests added/updated for provider policy/router, Builder policy, provider defaults, and saved settings policy. Local continuity docs also updated.
- Verification: focused route/settings/Builder tests **41 passed, 0 failed** across 5 suites. Full `:app:testDebugUnitTest`: **781 passed, 0 failures/errors/skips across 176 suites**. `:app:assembleDebug` succeeded. `git diff --check` passed with existing CRLF normalization warnings.
- Fresh APK: `android/app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `EFE7540F748F04AFEC5577D2F9AAFD83DB5850A22B1482177E15488871A6130F`; signature verified. Package `com.aura.ai.debug`, Aura AI, version 1.0.0/code 1; signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- This is a new APK hash; the Honor remains on approved 41E. No fresh approval was supplied for EFE, so it was not installed. No real cloud or Ollama request and no voice test were run for these changes. Automated mocks do not prove provider availability on the user's phone.
- Git local source remains `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote, with pre-existing changes; source was not committed/pushed. Only this handover is being updated on `integration/aura-voice-consolidation`; production `main` remains unchanged.
- Next: request exact-hash approval, back up app data afresh, install EFE in place, then test actual cloud success, cloud failure to local Qwen, saved Automatic selection after restart, local-only App Builder, and spoken multi-turn conversation. Record actual device results separately.


## Final automatic online-first build verification — 2026-11-13

- Final source revision rebuilt successfully: `:app:testDebugUnitTest :app:assembleDebug --no-daemon --offline --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process` (BUILD SUCCESSFUL; 32m43s). XML reports: 781 tests, 0 failures, 0 errors, 0 skipped across 176 suite files. `git diff --check` passed; local Git printed only line-ending normalization warnings.
- Final APK: `android/app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `B7EA9FDE535D5BA9D0FC18342F68D3875940B2D7FDAC64F366952D97A428C385`. Signature verification passed. Package `com.aura.ai.debug`, label Aura AI, version 1.0.0/code 1; signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- This hash differs from both previously approved APKs and has NOT been installed. Honor remains on the already installed approved build with app data preserved. Obtain exact-hash approval before backup and in-place install.
- Tests exercise provider routing with mocks only. No live cloud call, PC Ollama request, or Honor voice test was performed on this APK. Previous owner speech report described incomplete recognition/usage error; this provider-routing change does not prove transcript accuracy or actual provider connectivity.
- Local Git remains `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote, with existing user edits. No source was committed/pushed; production `main` unchanged.
- Next: after approval for B7EA9FDE535D5BA9D0FC18342F68D3875940B2D7FDAC64F366952D97A428C385, back up app data, install in place without uninstall/clear-data, and verify full transcript, spoken answer, follow-up, interruption, and actual online/local provider routing on the Honor.


## Recognition/usage-error evidence recheck — 2026-11-13
- Read-only Honor check: ADB currently sees `com.aura.ai.debug` v1.0.0. Recent filtered logcat (30,000 lines) contained no matching speech-finalization, HTTP 402, provider-fallback, or request-error messages, so it does not establish what the owner's latest spoken request produced. No install, launch, app preference, or data operation was performed.
- Source trace: `CloudChatRepository` emits `OllamaError.CloudFailure` for non-2xx cloud responses (including HTTP 402). `ProviderChatRouter` tries the next configured route only when `automaticProviderFallback` is enabled; local fallback also requires a configured local model. Existing generic cloud-failure tests do not specifically simulate HTTP 402.
- No source or test changes were made in this diagnostic pass. The previously completed 781-test suite remains the automated evidence; it is not fresh device speech/provider evidence.
- Full-sentence recognition is still unverified; the owner's prior report indicates unreliable capture. Final APK SHA-256 `B7EA9FDE535D5BA9D0FC18342F68D3875940B2D7FDAC64F366952D97A428C385` is not installed. Next: obtain approval for that exact hash, back up and update in place, then capture the transcript/error/provider/response for one live test.
- Local source checkout is still `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941` with no remote; no source push/commit, production `main` unchanged.


## HTTP 402 cloud-to-local regression test — 2026-11-13

- Added test `automaticModeFallsBackToLocalAfterCloudUsageCreditsError` in `ProviderChatRouterTest.kt`. It simulates the HTTP 402 usage-credit response with Automatic routing and fallback enabled, then verifies configured local `qwen3:1.7b` returns `10` as the final result.
- Focused command: `:app:testDebugUnitTest --tests com.aura.ai.data.remote.ProviderChatRouterTest --no-daemon --offline --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process`. Result: **14 passed, 0 failures/errors/skips**, BUILD SUCCESSFUL in 2m44s. `git diff --check` passed.
- Test-only edit; production source and built APK unchanged. Read-only ADB SHA confirms the Honor still has approved APK `41E164EF25434FB200E2547E7CB857002880DE715709BCCA9C1F5DE218CF13D9`. New APK `B7EA9FDE535D5BA9D0FC18342F68D3875940B2D7FDAC64F366952D97A428C385` remains uninstalled pending exact-hash approval.
- This simulated case only proves local fallback when enabled and a local model is configured. It does not prove those settings, live API behavior, speech capture, or spoken response on the Honor.
- Local Git remains `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941` with no remote and existing user edits. Do not commit/push local source or modify `main`.
- Next: after exact B7 hash approval, back up app data, update in place, and test one complete spoken request plus provider/error status and actual local fallback.


## Full Android suite after HTTP 402 test — 2026-11-13

- After adding the focused usage-credit fallback test, full `:app:testDebugUnitTest --no-daemon --offline --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process` completed successfully: **782 passed, 0 failures/errors/skips across 176 suites** (1m01s). `git diff --check` passed.
- This was test-only; APK SHA remains `B7EA9FDE535D5BA9D0FC18342F68D3875940B2D7FDAC64F366952D97A428C385`. The Honor still runs the earlier approved 41E APK. No new voice/provider test was performed on-device.
- Exact-hash approval for B7 is still required before backup/install; then verify transcript, speech, follow-up, interruption, and actual provider fallback on the Honor.


## PC web companion verification — 2026-11-13

- The existing local Windows companion at http://127.0.0.1:3008/ returned HTTP 200 (30,456 bytes).
- `pnpm.cmd typecheck` passed. `node --test --test-isolation=none app/simple-arithmetic.test.mjs` passed **22/22**, including transcript history/copy, persistent-memory policy, selected speech locale, wake transcript selection, and browser speech interruption/echo policies.
- No web source changed. No live browser microphone, audible response, continuous hands-free loop, or acoustic barge-in was exercised; HTTP 200 and unit tests do not prove those runtime behaviors.
- Android still has 782/782 JVM tests passing. The B7 APK awaits exact-hash approval and remains uninstalled; the Honor remains on approved 41E. Its live spoken recognition/provider behavior is still unverified on the newer source.
- Local Git remains `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote; no local source commit or `main` change.
- Next: test live browser speech when available; after approval for exact B7 APK hash, back up and update Aura in place and test full voice/provider behavior on the Honor.


## Windows production build verification — 2026-11-13

- The default Turbopack build failed because a pre-existing root folder `tmp7sn2c9j5` is access denied to the compiler; that folder and its ACL were left untouched. `OLLAMA_MODELS` was confirmed to point at the user's regular Ollama model directory.
- Webpack initially returned `spawn EPERM` in the restricted runner. With authorized child-process access and the repository's `AURA_NEXT_INLINE_BUILD=1` option, `pnpm.cmd exec next build --webpack` completed: compilation, typecheck, data collection, 20 static pages, route listing, and build tracing passed.
- After the build, the existing preview at `http://127.0.0.1:3008/` still returned HTTP 200. In this work turn, `pnpm.cmd typecheck` and browser voice-policy tests passed 22/22. No web source changed.
- Build/tests do not prove live microphone/TTS/continuous conversation/acoustic interruption. Android remains 782 JVM tests passing; B7 APK awaits exact hash approval, and the Honor still has 41E.
- Local Git is `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote; no source commit/push and `main` unchanged.
- Next: actual browser voice acceptance, then approved B7 backup/in-place update and live Honor voice/provider tests.


## Honor-to-PC Ollama connectivity check — 2026-11-13

- PC Ollama version: 0.40.16. `ollama list` and `/api/tags` show `qwen3:1.7b` and `qwen3:4b-q4_K_M`; `ollama ps` was empty (no active model).
- PC Wi-Fi IPv4 is `192.168.8.18`; port 11434 listens on all interfaces. From the ADB-connected Honor, read-only `curl http://192.168.8.18:11434/api/tags` succeeded and returned both models. ICMP ping timed out, which does not contradict successful HTTP connectivity.
- No inference request was sent and no model was loaded. Phone-to-PC API reachability and model discovery are verified; chat inference, Aura settings, cloud fallback, and spoken behavior are not.
- Honor remains on approved APK `41E164EF25434FB200E2547E7CB857002880DE715709BCCA9C1F5DE218CF13D9`; final B7 build is uninstalled pending exact-hash approval. No app data/settings changed.
- No source edits. Local Git remains `codex/aura-original` / `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote. Production `main` unchanged.
- Next: after approval for exact B7 SHA, back up and install in place; run one small local inference when owner is ready for model load, then test real cloud failure/local fallback and voice turn-taking.


## Honor-to-PC local inference verification — 2026-11-13

- Sent a small non-streaming request from the ADB-connected Honor to `http://192.168.8.18:11434/api/chat` using `qwen3:1.7b`, `think:false`, `num_predict:64`, `keep_alive:0`. The reply was exactly `AURA_LOCAL_OK`; total duration about 11.1s, cold load about 9.9s.
- An initial 20-token attempt returned empty `content` because the output limit was consumed by Qwen's `thinking` field. That attempt was inconclusive; the follow-up with thinking disabled passed.
- `ollama ps` immediately afterward showed no loaded model. No APK, Aura setting, or user data was changed.
- This verifies direct phone-to-PC local inference only, not inference through installed Aura, speech recognition, TTS, wake flow, or cloud fallback.
- Honor still has approved APK `41E164EF25434FB200E2547E7CB857002880DE715709BCCA9C1F5DE218CF13D9`; final B7 APK awaits its own exact-hash approval. Local Git remains `codex/aura-original` / `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote; no local source commit and production `main` unchanged.
- Next: after exact B7 approval, back up and update in place, then test the full voice/provider flow through Aura itself.


## Voice understanding status recheck — 2026-11-13

- The owner asked whether Aura understood the spoken test. The latest previously reported result was only the first word followed by a usage error; this does **not** establish successful recognition or AI processing.
- Read-only ADB check confirmed the Honor is connected but ChatGPT is the top-resumed app, not Aura. A filtered 5,000-line log sample contained no fresh Aura wake, command-capture, provider, or request-error event. No new voice test occurred in this check, and no root cause for the reported usage error can be inferred.
- The installed original Aura remains the user-approved `com.aura.ai.debug` APK with SHA-256 `41E164EF25434FB200E2547E7CB857002880DE715709BCCA9C1F5DE218CF13D9`. New B7 routing APK remains uninstalled and requires its own exact-hash approval.
- No source files changed and no automated tests were run in this read-only verification. Local Git remains `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, with existing user edits and no configured remote; no local commit or push was made.
- Next: with other voice sessions closed, bring the original Aura app to the foreground for one test. Capture the full displayed transcript and exact usage/error message before diagnosing whether failure is in speech recognition or provider inference. Test a second turn only after the first is correct.


## Local speech-error transcript diagnostic — 2026-11-13

- Local source change keeps the most recent Android SpeechEvent.Partial hypothesis in ChatViewModel state when tap-to-speak capture ends with SpeechEvent.Error or an exception. The composer displays the retained text prefixed with “Heard:” after listening stops. Starting a new capture still clears the old partial. This is diagnostic visibility only; it does not itself improve recognition accuracy or resolve a cloud usage error.
- Local task edits: android/app/src/main/java/com/aura/ai/ui/chat/ChatViewModel.kt, android/app/src/main/java/com/aura/ai/ui/chat/ChatScreen.kt, and android/app/src/test/java/com/aura/ai/ui/chat/ChatViewModelTest.kt; these files had other pre-existing local changes and were preserved. No source changes were pushed to this GitHub branch.
- Focused ChatViewModelTest: 24 passed, 0 failures/errors. Full :app:testDebugUnitTest :app:assembleDebug: 783 passed, 0 failures/errors/skips across 176 suites; APK assembly succeeded. git diff --check passed (only existing CRLF normalization warnings).
- Local debug APK SHA-256: 5A1708E0ADAAA1AB77ABEAA9AD951F6C8803E02570C15290576896101BCB7F04; signature verification passed. Package com.aura.ai.debug, Aura AI v1.0.0/code 1; signer SHA-256 59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b.
- This exact build is not installed; previous approval was for 41E and does not authorize this hash. The current phone foreground check showed ChatGPT, so this change has no live-device voice acceptance result. Back up data and obtain approval for this exact hash before in-place installation; do not uninstall or clear data.
- Local source branch is codex/aura-original at 471ef9d4c34bc62758908b9efe1f5dff2af54941; no remote is configured, pre-existing dirty changes remain, and no local source commit was made. This handover-only GitHub update does not represent the code as merged or pushed; production main is unchanged.
- Next: after exact-hash approval, update the original Aura in place, test the full transcript/answer, capture the exact usage error if it recurs, then verify multi-turn conversation and interruption. The Austrian voice and live local/cloud route remain unverified on-device.

## Approved 5A17 diagnostic build installed on original Aura — 2026-11-13

- Owner approved exact APK SHA-256 5A1708E0ADAAA1AB77ABEAA9AD951F6C8803E02570C15290576896101BCB7F04. Preflight package is com.aura.ai.debug, Aura AI v1.0.0/code 1; signer SHA-256 59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b matches the prior installed original.
- Pulled the previous installed base APK (approved 41E hash) before update. Stopped Aura and created a fresh private-data archive at .gradle-aura-cache/device-backups/com.aura.ai.debug-pre-5A1708-20261113-070621.tar, 14,205,440 bytes, SHA-256 761874F1AF3FEC46DAA6C169F07B0F8DA2E67185A663E2CA3731E9DD5338CFFD. Tar integrity passed; 127 entries include databases, files, and shared preferences.
- adb install -r reported Success. No uninstall or clear-data. The first-install timestamp remains 2026-10-30 01:25:43; package/version unchanged; last update is 2026-11-13 07:06:52. Database/WAL/SHM, built_apps, and datastore directories remain. Pulling installed base.apk back from the Honor yielded exactly the approved 5A17 SHA and signer.
- Original Aura MainActivity is top-resumed; sampled logcat has no matching fatal exception. UI hierarchy inspection returned a null root, so no UI control was actuated. Aura's listener requires a user-initiated resume after install; no speech test has occurred.
- Automated verification for the build: 24 ChatViewModel tests passed; full Android JVM suite 783 passed / 176 suites / 0 failures/errors/skips; assemble and APK signature verification passed. These are not physical-device speech acceptance.
- Local source changes were not pushed: checkout codex/aura-original, HEAD 471ef9d4c34bc62758908b9efe1f5dff2af54941, no local remote, pre-existing user edits preserved. This handover update is documentation-only on the GitHub integration branch; no merge to main.
- Next: owner must resume hands-free listening on the Honor and perform one spoken test, then report exact transcript/error/spoken response. Only after a successful full turn should we test follow-up, interruption, and live cloud/local routing.

## Voice timeout diagnostics and acknowledgement audit — 2026-11-14

- Confirmed existing Aura code already implements the requested wording: actionable spoken tasks use the one exact pre-action acknowledgement “I'll be back”; response guidance allows concise “Affirmative”, “Copy that”, “Roger”, “Negative”, and “Standing by” when fitting, uses NATO words only for spelling/clarifying letters, and permits “Hasta la vista, baby” occasionally only after successful completion. No prompt/acknowledgement code change was needed. Automated tests verify configured text/instructions, not the physical TTS audio or Austrian accent.
- In the separate local workspace, added privacy-safe recognition timeout metadata to `android/app/src/main/java/com/aura/ai/voice/SpeechToText.kt`: timeout phase, selected recognizer, language tag, partial-result presence, offline-fallback state and elapsed time. The timeout log does not include audio or transcript text.
- Source inspection found quiet standby routes a same-utterance wake phrase + command through the bundled small Vosk recognizer. This is a plausible explanation for the owner's reported garbled transcript, not a verified device diagnosis. Honor is absent from ADB; no new APK was built or installed and device recognition/TTS remain unverified.
- Focused Android tests passed: `:app:testDebugUnitTest --tests com.aura.ai.voice.SpeechRecognitionTimeoutPolicyTest --tests com.aura.ai.voice.HandsFreeTranscriptMergerTest --tests com.aura.ai.agent.AgentPromptsTest --tests com.aura.ai.voice.VoiceReplyAcknowledgementPolicyTest`. Gradle completed successfully; XML reports show 40 tests passed, 0 failures/errors/skips. Kotlin daemon marker access was denied but in-process fallback succeeded in 3m22s. An initial wrong-directory invocation failed because the wrapper is at repository root; rerun from root passed.
- Local source docs were updated. Local checkout remains `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, has no configured remote and contains pre-existing dirty changes; no local commit/push. The local diagnostic source change is therefore not part of this GitHub branch. This entry records the verified local result only and does not assert source synchronization.
- Exact previously approved APK remains SHA-256 `F010A4E144451B587AB8D5D93F595A6A95EE64F24ADE761AB594F2B5AE3ED5B8`. Installation still requires a fresh backup and signer match after Honor reconnects; do not uninstall or clear app data.
- Next: collect device logs and exact transcript for same-utterance wake+command; if Vosk is inaccurate, evaluate a non-truncating handoff to Android SpeechRecognizer with tests. Separately verify actual spoken “I'll be back” and situational military wording on the Honor. No change to main or merge.


## Local Aura debug artifact build — 2026-11-14

- The separate active local checkout `codex/aura-original` built a debug APK with `:app:assembleDebug` (**BUILD SUCCESSFUL**): package `com.aura.ai.debug`, version 1.0.0/code 1, size 106,018,493 bytes, SHA-256 `8D822DB1088C4C0CF0310BA981C5F8BD893172E41A3EADA7C6D269CE5521B901`. APK signature verification passed; Android Debug signer certificate SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- This APK was built from the local checkout and has not been copied into or installed from this GitHub branch. It differs from the prior exact approved APK hash `F010A4E144451B587AB8D5D93F595A6A95EE64F24ADE761AB594F2B5AE3ED5B8`; do not install the new hash until the owner approves it. ADB still has no Honor device, so backup, installed-signature comparison, and device acceptance remain pending.
- The same local checkout's four focused test classes passed 40/40. The handover is recording evidence across checkouts; it does not imply their source histories are synchronized. Main remains untouched.


## Current acknowledgement source scan — 2026-11-14

- A scan of current Android/PC runtime source in the local workspace found no fixed spoken “I'm on it” response: `VoiceReplyAcknowledgementPolicy.MESSAGE` is exactly “I'll be back”, and the agent prompt rejects “I'm on it” as filler. It permits concise military wording only when fitting, and the optional “Hasta la vista, baby” only after successful tasks.
- Older notes about a delayed “I'm on it” line describe historical implementation and are superseded by the current source scan. A model could still disobey its prompt; audio must be tested on-device to verify spoken output.
- This is a source-level check in separate local checkout `codex/aura-original`, not evidence that these uncommitted local files are present in this GitHub branch. Latest local APK SHA is `8D822DB1088C4C0CF0310BA981C5F8BD893172E41A3EADA7C6D269CE5521B901`; not installed or approved for install. ADB still has no Honor.


## App Builder progress diagnostics and local generation — 2026-11-14

- In the separate local workspace, added visible elapsed time and streamed character count to Aura's Android App Builder. The timer covers the backend check and generation; the character count resets when a failed provider's partial output is discarded. This improves diagnostics but does not extend the timeout.
- On the local PC, Ollama 0.40.16 served a Builder-shaped one-screen checklist prompt with `qwen3:1.7b`, `think:false`, 4096 context tokens and 1024 output tokens. It finished in **83.6 seconds**, output 806 tokens / 2,818 characters of complete HTML, and returned done reason `stop`. This was a direct local API probe, not the Android Builder UI/file-save workflow. The model was explicitly stopped afterward; `/api/ps` then showed 0 loaded models.
- Focused Android Builder test class passed 12/12. `:app:assembleDebug` succeeded. Local APK is package `com.aura.ai.debug` v1.0.0, SHA-256 `5FAE3B07ADECFEA03670F37A9BAB88E21C377AAF3AFA88B0F47D8F437ACF08A4`, signed with Android Debug certificate SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- This local artifact differs from previously approved APK hashes and has not been approved or installed. ADB reports no Honor. The app's persisted Builder timeout cannot be inspected; source default is 600 seconds but a lower saved value may override it. No claim that the Android build workflow is fixed until an actual file is generated on the Honor.
- Local source is branch `codex/aura-original`, head `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote and existing dirty work. No code is synchronized to this GitHub integration branch; this update documents evidence only. No merge/main change.
- Next: reconnect Honor; inspect persisted timeout and selected model/endpoint; backup app data and verify package signer before installing the exact newly approved APK; run a short in-app generation and confirm the resulting HTML file is saved. Capture elapsed time and character count on failure.


## Local wake-to-command handoff test/build record — 2026-11-14

- In the separate local workspace only, adjusted the same-utterance wake path so a non-empty Vosk partial after “Yo, yo, yo” hands its recognized command prefix to Android SpeechRecognizer. It does not hand off on wake-only speech, during the wake acknowledgement, or while another command capture is active.
- Focused Android tests in that local checkout: 54 passed, 0 failed/errors/skipped across WakeWordSpeechPolicyTest, WakePhraseSessionTest, and HandsFreeTranscriptMergerTest. Gradle succeeded after a Kotlin daemon temp-marker access denial and in-process fallback.
- Local debug APK assembled and verified: package com.aura.ai.debug v1.0.0/code 1, SHA-256 C2E3E2D7620FE3F64CC74674F0A75821DDCCCA4457F27B9AAAC1BCDB60EEBF40; signer SHA-256 59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b. This exact APK has not been approved or installed. ADB reports no Honor, so there is no physical-device transcript/voice acceptance.
- Existing prompt configuration already asks for concise situational military phrasing: “Affirmative”, “Copy that”, “Roger”, “Negative”, “Standing by”; Alpha/Bravo/Charlie/Delta/Echo for spelling only; occasional “Hasta la vista, baby” after successful work. One exact pre-action acknowledgment remains “I'll be back.” Tests/source confirm instructions only, not what the TTS voice says aloud.
- The local checkout remains codex/aura-original at 471ef9d4c34bc62758908b9efe1f5dff2af54941, with no remote and pre-existing user modifications. No local source was copied to this GitHub branch. This handover entry is a docs-only communication update; production main is unchanged.
- Next: reconnect the Honor, back up and verify existing app data/signature, obtain approval for this exact new APK hash before installing, then test the single-utterance wake phrase, exact transcript, spoken answer, multiple turns, and interruption behavior. Do not call recognition fixed until device verification.


## Follow-up voice wording unit checks — 2026-11-14

- After the wake-handoff record, the local checkout also ran `:app:testDebugUnitTest --tests com.aura.ai.agent.AgentPromptsTest --tests com.aura.ai.voice.VoiceReplyAcknowledgementPolicyTest`: **7 passed, 0 failed/errors/skipped**.
- These tests validate the configured “I'll be back” acknowledgement and situational military-language instructions, not physical synthesized speech, accent, or live interaction. Honor remains absent from ADB.
- This is a docs-only GitHub handover update. The local-only APK remains SHA-256 `C2E3E2D7620FE3F64CC74674F0A75821DDCCCA4457F27B9AAAC1BCDB60EEBF40`; it has not been approved or installed. Main remains unchanged.


## Local wake-handoff task file inventory

Local-only code files changed for this update: `android/app/src/main/java/com/aura/ai/voice/wake/WakeWordSpeechPolicy.kt`, `android/app/src/main/java/com/aura/ai/voice/WakeWordService.kt`, and `android/app/src/test/java/com/aura/ai/voice/wake/WakeWordSpeechPolicyTest.kt`. Local status documents updated: `AI_HANDOVER.md`, `PROJECT_STATUS.md`, `VOICE_TESTS.md`, and `NEXT_STEPS.md`. No source synchronization or production-main modification.


## PC runtime voice readiness evidence — 2026-11-14

- Read-only inspection of the current Aura PC page at `http://127.0.0.1:3008/`: HTTP 200, correct Aura title, page rendered. UI showed hands-free enabled, Ollama ready, local model `qwen3:1.7b`, and two local models found.
- Settings reported `ElevenLabs rejected the saved API key` and the selected custom voice unavailable. The Austrian voice is not verifiably available on the PC until the owner replaces the rejected key directly in Aura Settings and loads/previews the saved voice. No key was inspected or changed; no alternate voice was selected.
- `pnpm.cmd typecheck` passed. Focused web tests (`simple-arithmetic`, `website-open-status`, and `local-request-policy`) passed **32/32**. No live microphone, spoken response, barge-in, or Android phone test was performed.
- Honor remained absent from ADB; no install or data action. No local source code changed or synced. The local checkout is still `codex/aura-original` / `471ef9d4c34bc62758908b9efe1f5dff2af54941` with no remote and existing dirty user work. Main is unchanged.
- Next: owner corrects the ElevenLabs credential inside Aura Settings. Reconnect Honor, preserve and verify its data/signature, obtain exact approval for the current APK hash if testing that build, then conduct real voice acceptance.


## Full Android regression rerun — 2026-11-14

- Current local checkout ran `:app:testDebugUnitTest --offline --max-workers=1` using the repository-local Gradle cache. Result: **BUILD SUCCESSFUL; 791 tests across 177 suites; 0 failures, errors, or skips**. `git diff --check` passed.
- A first attempt had a PowerShell-split Gradle property and failed during task selection before tests started. The corrected invocation without that property passed the full suite.
- No app source changed in this continuation. Local branch `codex/aura-original` remains at `471ef9d4c34bc62758908b9efe1f5dff2af54941` without a configured remote and with pre-existing user edits.
- Physical Honor testing remains pending: ADB lists no device. No new install or data operation. The current local APK SHA remains `C2E3E2D7620FE3F64CC74674F0A75821DDCCCA4457F27B9AAAC1BCDB60EEBF40`, not approved for installation.
- PC Settings still reports the saved ElevenLabs key rejected, so the Austrian voice remains unavailable there; replace it only inside Aura Settings. No credential was read or changed. Production main untouched.
