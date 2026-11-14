## Full regression rerun and pending candidate install — 2026-11-14

- After the active-session ASR recovery changes, full Android JVM regression passed: **801 tests across 177 suites, 0 failures/errors/skips**. Focused recovery tests passed 9/9. `:app:assembleDebug --offline --max-workers=1` succeeded.
- Candidate remains `com.aura.ai.debug` v1.0.0, SHA-256 `E2ADEC373945565A797E98851C14CDC04748FDC0A0DF047572BA4B214EFDE7FE`, signer verified. The previous approval does not cover this new hash; owner approval requested. No install performed.
- ADB now lists no Honor, so device retest awaits exact-hash approval and reconnect. The failure on the currently installed build remains: wake/math first turn passed, next no-wake utterance returned Android no-match.
- Local handover/status/test/next-step docs were updated. Local source is still a separate dirty worktree; no code sync/merge and `main` unchanged.

## Honor follow-up no-match recovery candidate — 2026-11-14

- Owner reported that after a successful wake/math turn, Aura failed on the next utterance. Honor logs show platform ASR detected speech and end-of-speech, then returned `ERROR_NO_MATCH` (code 7). Source trace found that ended-session handling discarded the partial and returned to Vosk instead of retrying full command capture.
- Local candidate preserves a partial after final no-match and retries the platform command recognizer once inside an active conversation without repeating the wake phrase; a subsequent failure returns to the wake listener. Files: `WakeWordService.kt`, `SpeechToText.kt`, `OfflineSpeechFallbackPolicy.kt`, `VoiceCommandCapturePolicy.kt`; tests `OfflineSpeechFallbackPolicyTest.kt`, `VoiceCommandCapturePolicyTest.kt`.
- Focused Android tests: 9 passed, 0 failed/errors/skips. `:app:assembleDebug --offline --max-workers=1` passed; `git diff --check` passed aside from line-ending notices. The Kotlin daemon was denied a temp marker and Gradle completed with its in-process fallback.
- Candidate APK SHA-256 `E2ADEC373945565A797E98851C14CDC04748FDC0A0DF047572BA4B214EFDE7FE`, package `com.aura.ai.debug` v1.0.0/code 1, verified signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`. Not installed; prior approval was for another hash. Exact-hash approval required.
- Local checkout remains a separate dirty `codex/aura-original` worktree at `471ef9d4c34bc62758908b9efe1f5dff2af54941` with no configured remote. This remote write records findings only; no source sync, merge, or change to `main`.
- Next: obtain exact APK approval, back up and verify app data/package/signer, update in place, then test a no-wake follow-up, repeated turn, and interruption. The candidate fix is not device-verified.

## Approved Honor installation and one-turn voice check — 2026-11-14

- Owner approved exact APK SHA-256 `BBCF723A51AE1BFCB50672A9C39DF0B41AD942F7723FBA4B3E0D51C148CF0EF1`, package `com.aura.ai.debug` v1.0.0/code 1. Honor ADB device `AYAV6R3707021141` reconnected.
- Before installation, backed up Aura private data to `backups/aura-com.aura.ai.debug-20261114-050014.tar.gz` (365,653 bytes, 128 readable entries, includes Room database and shared preferences) and saved the pre-install APK. Verified installed package/version and signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b` matched the approved APK.
- Ran `adb install -r`; Android returned `Success`. No uninstall or data clear. After update, `t1000_database`, WAL/SHM, and saved preferences were present. Pulled installed APK SHA-256 exactly matches the approved artifact; signer verification passed.
- Owner completed one physical-device test in the original Aura: wake phrase/question produced a correct transcript and spoken answer “10.” This verifies one wake-to-transcript-to-answer turn only; repeated turns, interruption, five-minute conversation, and sustained hands-free listening remain unverified.
- Android created Aura `MainActivity`; Chrome was top-resumed shortly afterward, so stable foreground rendering is not confirmed by ADB. The owner did report the voice test from Aura, so the single turn passed at the user level.
- Automated evidence for this APK: Android JVM suite 799 tests across 177 suites passed (0 failures/errors/skips); debug assembly passed. Physical-device speech acceptance is reported separately above.
- Local source checkout is `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no configured remote and pre-existing user work. This GitHub handover update is documentation-only; no source sync or merge, and `main` remains unchanged.
- Next: test at least one follow-up turn and interruption; verify stable foreground display and selected voice. If either fails, capture relevant logs and diagnose before another APK. Preserve current data and backup.

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


## Military wording follow-up — 2026-11-14

- Reviewed the active local Aura source. The action acknowledgement is deterministically “I'll be back”; the system prompt already permits concise situational military wording (“Affirmative”, “Copy that”, “Roger”, “Negative”, “Standing by”), reserves Alpha/Bravo/Charlie/Delta/Echo for spelling, and allows “Hasta la vista, baby” occasionally only after a successful task. No code change was needed.
- Focused automated tests passed: AgentPromptsTest 5/5 and VoiceReplyAcknowledgementPolicyTest 2/2. These verify configured instructions, not synthesized speech or actual use by the model.
- The Honor is absent from ADB. The exact APK approval F010A4E144451B587AB8D5D93F595A6A95EE64F24ADE761AB594F2B5AE3ED5B8 remains pending backup, installed-signer checks, and physical-device speech validation; no install or device test occurred.
- Local checkout remains separate at codex/aura-original / 471ef9d4c34bc62758908b9efe1f5dff2af54941, no remote, with pre-existing dirty user changes preserved. No source sync, local commit, or main-branch change.
- Next: reconnect Honor, perform backup/package/signer checks, install only the approved exact hash if compatible, then record exact transcript and speech for one action and one direct question. If model output still violates the wording prompt, evaluate a narrowly scoped response policy based on actual observed output.


## Wake acknowledgement implementation and local APK verification — 2026-11-14

- In the separate local checkout, changed the wake reply to one central “Standing by. Go ahead.” string; `WakeWordService` uses the shared policy constant. The pre-action acknowledgement remains “I'll be back.” This code is not present on this GitHub branch because local and remote source histories remain separate.
- Local Android suite passed: 792 tests / 0 failures, errors, or skips. Debug APK built and signature-verified: package com.aura.ai.debug v1.0.0/code 1, SHA-256 4856A5FA0D6062669B57BC6F6FA0EC40513D742266AD7FDBB84F45BEBCDFAF60, signer SHA-256 59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b.
- No Honor in ADB; no backup, install, or physical TTS/voice test occurred. Prior install approval was for a different APK hash, so this artifact remains uninstalled and requires its own approval after device/package/signer checks.
- Local branch codex/aura-original remains at 471ef9d4c34bc62758908b9efe1f5dff2af54941, no remote and pre-existing dirty changes preserved. This handover update is docs-only on integration/aura-voice-consolidation; no code sync or main-branch change.
- Next: reconnect Honor and verify backups/signature, obtain approval for the exact new APK if needed, then test wake reply, full transcript, two-way turns, barge-in, Austrian voice and local/cloud routing. The PC Settings page previously reported the saved ElevenLabs key was rejected.


## Windows desktop agent live smoke check — 2026-11-14

- In the separate local checkout, Aura's PC preview at http://127.0.0.1:3008/ returned HTTP 200. The existing local Windows-control GET returned 175 Start-menu apps, 8 visible windows, and phonePaired=false. ADB lists no Honor.
- Via the existing Windows-control route, opened Calculator; the API returned opened=Calculator and a fresh window snapshot found one Calculator window. Read-only UI Automation inspection returned 37 controls and visible text. A history-flyout invocation returned verified=false, so that state change is not claimed verified.
- Closed the test Calculator window; a fresh snapshot found zero Calculator windows. This confirms only basic PC app open/close and read-only inspect in the current desktop session; phone pairing, browser workflows, file changes, speech-triggered actions, and other app operations remain untested.
- No source code changed and no automated tests ran in this runtime check. Local handover/status/next-steps updated. Local branch codex/aura-original at 471ef9d4c34bc62758908b9efe1f5dff2af54941 has no configured remote and retains user changes; no code synchronization, local commit, install, or main-branch change.
- Next: enable private pairing from Aura on PC and original Aura Android, reconnect ADB, back up/verify the app before any newly approved APK install, then test an actual phone-originated PC action and the remaining real-device voice functions.


## Private PC-to-Android pairing session — 2026-11-14

- In the separate local Aura runtime, created a session-only Windows listener on private LAN address 192.168.8.18:3002 through the app's existing pair-phone route. No firewall rule or public Ollama exposure was added.
- An unauthenticated request received HTTP 403; a request carrying the generated bearer received HTTP 200 and the read-only desktop list (175 apps, 10 windows). This verifies the listener and gate, but not Android-side pairing: the Honor is absent from ADB and has not entered the code.
- The one-time bearer was shared directly with the owner and is intentionally omitted from this handover. It is valid only while the PC Aura runtime remains active; recreate/revoke via PC Settings after restart/as needed.
- A first HTTP probe used the environment's configured local proxy and failed; proxy-bypassed LAN probes produced the 403/200 results above.
- No Android settings, app code, APK, or user data changed; no voice/device test. Local docs updated; local branch codex/aura-original at 471ef9d4c34bc62758908b9efe1f5dff2af54941 has no remote and retains user changes. Docs-only update on this integration branch; no code sync or main change.
- Next: enter PC address 192.168.8.18, port 3002, and the code already supplied to the owner in original Aura > Settings > Windows desktop agent, then run a read-only connection check from the Honor.


## Refreshed Android-to-PC pairing session — 2026-11-14

- Rechecked live state: the local PC preview at http://127.0.0.1:3008/ returned HTTP 200, while the previous Android agent listener was no longer active. ADB lists no connected Honor.
- Created a fresh private pairing session on the existing PC Aura service at 192.168.8.18:3002. The bearer code was provided directly to the owner and is intentionally omitted here.
- Verified the new LAN listener: authenticated read-only request returned 175 apps, 11 windows, and 2 saved Ollama model entries; unauthenticated request returned HTTP 403. This verifies the PC listener and gate, not Android-side pairing.
- No app source, phone data, firewall rules, public endpoint, APK, or production main branch changed. Android pairing and real-device voice tests remain pending.
- Local checkout remains codex/aura-original at 471ef9d4c34bc62758908b9efe1f5dff2af54941 with no remote; existing dirty user changes were preserved. No local commit or source sync.
- Next: enter the fresh address/port/code in original Aura Android Settings > Windows desktop agent, tap Save and test, and verify a phone-originated read-only listing. Recreate the session if PC Aura restarts.


## Paired-PC actions exposed to the Android agent — 2026-11-14

- Local original Aura source now extends Android's paired-Windows tool to call the existing guarded PC actions for opening HTTP(S) websites, searching/opening files only within named user folders, reading visible non-editable text, and filling an exact named non-password field without submission. The PC side validates URLs and paths, withholds password/editable fields from reads, and reports browser/file launches honestly; Android validates the returned confirmation before reporting success. The prompt marks returned visible text as untrusted data. Arbitrary commands, button clicks/submissions, password access, and unrestricted paths are not exposed.
- Changed local files: android/app/src/main/java/com/aura/ai/agent/tool/WindowsDesktopAgentClient.kt; android/app/src/main/java/com/aura/ai/agent/AgentPrompts.kt; android/app/src/test/java/com/aura/ai/agent/tool/WindowsDesktopAgentClientTest.kt; android/app/src/test/java/com/aura/ai/agent/AgentPromptsTest.kt. Two prompt files had earlier user changes; these were preserved.
- Focused tests passed 25/25 (19 Windows client, 6 prompt), 0 failures/errors/skips. Command: :app:testDebugUnitTest --offline --max-workers=1 --tests com.aura.ai.agent.tool.WindowsDesktopAgentClientTest --tests com.aura.ai.agent.AgentPromptsTest. Wrapper first hit access denied in shared Gradle home; local .gradle-aura-cache rerun succeeded using Gradle's in-process Kotlin fallback because daemon marker writes are denied.
- :app:assembleDebug --offline --max-workers=1 passed. Output APK: android/app/build/outputs/apk/debug/app-debug.apk; package com.aura.ai.debug v1.0.0/code 1; SHA-256 0BC02E5F95C208AC36035235E01E04ECE1AEE078AC3B0E6C3FD8D6682F7CC508. APK signature verification passed; signer digest 59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b.
- ADB still lists no Honor. No app-data backup, install, Android-side action, or real voice test occurred. Existing exact APK approval names a different hash and does not authorize this one. Local source is not synced to this branch; no merge to main.
- Local checkout: codex/aura-original at 471ef9d4c34bc62758908b9efe1f5dff2af54941, no remote, dirty user work preserved. No local commit/push.
- Next: reconnect Honor, back up and verify package/signer, obtain approval for the exact APK hash before in-place installation, then test the new Android-to-PC actions from the phone and separately verify hands-free voice/TTS.


## Pairing and APK approval state rechecked — 2026-11-14

- Current private PC listener at 192.168.8.18:3002 still accepts the generated bearer: authenticated read-only inventory returned 175 apps, 12 windows, and 2 saved model entries; no-auth request returned HTTP 403. The code was supplied directly to the owner and remains live while PC Aura stays running.
- ADB still lists no Honor. Exact-hash approval for the newly built Android connector APK is pending; the previous approval covered a different APK. No device backup/install occurred.
- No code or user data changed in this recheck. The latest handover includes the Android connector extension, focused test results (25/25), successful APK build/signature verification, local checkout state, and remaining device tests. No source sync or merge to main.
- Next: phone-side pair and connect; after exact-build approval and ADB reconnection, back up existing app data, verify package/signer, install in place, and test paired website/file actions and hands-free voice on the actual Honor.


## Full Android regression after paired-PC connector extension — 2026-11-14

- Ran :app:testDebugUnitTest --offline --max-workers=1 from the local source checkout using repository-local Gradle home. Result: BUILD SUCCESSFUL; 177 test suites, 798 tests, 0 failures, 0 errors, 0 skipped. The test task was the only executed task; Kotlin main/test compilation was up to date.
- git diff --check passed for the Android connector/prompt source and test files (Git reports only existing LF-to-CRLF notices). No source code changed in this verification.
- ADB still lists no Honor. The suite does not establish wake/microphone/TTS/interrupt behavior, actual Android-to-PC calls, or hardware acceptance. Exact-hash install approval remains pending for the new APK SHA-256 0BC02E5F95C208AC36035235E01E04ECE1AEE078AC3B0E6C3FD8D6682F7CC508.
- Local checkout stays codex/aura-original at 471ef9d4c34bc62758908b9efe1f5dff2af54941 with no remote; dirty user changes preserved. No local commit, source sync, or merge to main.
- Next: reconnect the Honor; back up data and verify package/signer before the exact-approved in-place APK install; test paired website/file actions, then actual wake/transcript/reply/interrupt/voice behavior.


## Military response wording and PC command safety checks — 2026-11-14

- The current Aura prompt specifies concise, situational military wording: “Affirmative”, “Copy that”, “Roger”, “Negative”, and “Standing by”. Alpha/Bravo/Charlie/Delta/Echo are reserved for spelling or clarification, rather than inserted as decoration. The fixed acknowledgement for an actionable task remains “I'll be back”; ordinary questions should be answered directly. No source change was needed for this request.
- Android wording verification previously passed: `:app:testDebugUnitTest --tests com.aura.ai.agent.AgentPromptsTest --tests com.aura.ai.voice.VoiceReplyAcknowledgementPolicyTest --offline --max-workers=1` — 7 tests, 0 failures/errors/skips. This verifies prompt/policy expectations only, not spoken device output.
- PC checks completed in the local workspace: `node app/api/windows-control/desktop-control-policy.test.mjs` — 4/4 passed; `node tools/website-url.test.mjs` — 3/3 passed; `node tools/user-files.test.mjs` — 8/8 passed. Total 15 passed, 0 failed. Running the files directly succeeded; an initial combined node test-runner attempt failed due an incorrect path and worker-process EPERM.
- No source changed in this continuation. Local checkout remains branch `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, with no configured remote and extensive pre-existing dirty work; no local commit or push. The GitHub repository is a separate docs-only handover channel; no code was copied or merged and `main` was not changed.
- Android regression suite remains 798/798 from the latest verified run. No Honor is visible in ADB. The approved APK hash is `F010A4E144451B587AB8D5D93F595A6A95EE64F24ADE761AB594F2B5AE3ED5B8`; it has not been installed here, and no backup or physical-device voice test occurred.
- Next: reconnect the Honor; back up Aura data and verify its signer before installing only the exact approved APK. Then test wake capture, full transcript, spoken response, successive turns, interruption and actual voice output. Do not infer device success from unit tests.


## Current-worktree build validation — 2026-11-14

- Revalidated the local PC companion: `pnpm.cmd typecheck` passed. Default Turbopack production build failed reading the pre-existing inaccessible directory `tmp7sn2c9j5`; normal Webpack build hit sandbox worker-process EPERM. Using the repository's existing build-sandbox flags (`AURA_NEXT_INLINE_BUILD=1`, `AURA_SKIP_NEXT_TYPECHECK=1`) and approved elevated local execution, `next build --webpack` completed successfully. Next's internal typecheck was skipped for that build; separate TypeScript check passed. Existing preview remained HTTP 200.
- Current Android Gradle test/build tasks completed as `UP-TO-DATE`; this invocation did not rerun tests. Latest full suite recorded here is 798/798 JVM tests. Current debug APK is `com.aura.ai.debug` v1.0.0/code 1, SHA-256 `0BC02E5F95C208AC36035235E01E04ECE1AEE078AC3B0E6C3FD8D6682F7CC508`; signature verification passed (signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`).
- Workspace APK search found no artifact matching the owner's approved exact SHA-256 `F010A4E144451B587AB8D5D93F595A6A95EE64F24ADE761AB594F2B5AE3ED5B8`. The current artifact is a different build and is not approved. ADB lists no Honor; no backup, installation, or device test occurred.
- This local source checkout remains separate, dirty, and without a configured remote: branch `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`. No code was copied or merged to this GitHub branch; `main` unchanged.
- PC safety tests: desktop control policy 4/4, website URL validation 3/3, scoped user file handling 8/8 (15 passed, 0 failed). Military prompt/ack policy test 7/7; device audio remains unverified.
- Next: reconnect the Honor. Locate the exact approved artifact only if its provenance is clear, back up app data and verify package/signer before in-place installation. Otherwise obtain approval for the new exact APK hash. Then validate wake capture, full transcript, spoken answer, multiple turns, interruption, actual selected voice and phone-to-PC actions.


## Live local Ollama inference verification — 2026-11-14

- Aura PC `GET /api/ollama/models` returned `serverOnline=True` and models `qwen3:1.7b`, `qwen3:4b-q4_K_M`. Local Ollama `/api/tags` agreed.
- Sent a simple arithmetic prompt through the live Aura PC route `POST http://127.0.0.1:3008/api/ollama/chat` with `qwen3:1.7b`. HTTP 200 streamed “10”. Ollama reported 7.01 seconds total, 5.46 seconds model load, 1.32 seconds prompt evaluation, and 0.23 seconds generation. The request asked to unload after response; subsequent `ollama ps` showed no loaded model. This confirms PC-local inference only, not cloud fallback or Android.
- No source or app settings changed. Local checkout remains separate branch `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no configured remote and dirty user edits preserved. No source copied or merged to GitHub; `main` unchanged.
- Honor remains absent in ADB; no backup/install/device voice test. Exact previously approved APK `F010A4E144451B587AB8D5D93F595A6A95EE64F24ADE761AB594F2B5AE3ED5B8` was not found among workspace APKs; current artifact differs and is not approved.
- Next: reconnect Honor and provide the exact approved APK artifact or approve the exact current hash; back up data and verify installed signer before updating in place. Then verify on-device wake, transcript, spoken answer, turn-taking, interruption, and local route. Cloud failover/recovery remains pending authorized credentials and tests.


## Android speech phrase-bias improvement — 2026-11-14

- Added full arithmetic request variants to `SpeechRecognitionBiasPolicy` (“what is five plus five?”, “what's five plus five?”, plus numeric forms) so Android SpeechRecognizer receives the complete utterance as contextual bias hints. This may improve ranking; it is not proof or a guaranteed fix for spoken recognition.
- Changed local source/test files: `android/app/src/main/java/com/aura/ai/voice/SpeechRecognitionBiasPolicy.kt`, `android/app/src/test/java/com/aura/ai/voice/SpeechRecognitionBiasPolicyTest.kt`. Local docs also updated (`AI_HANDOVER.md`, `PROJECT_STATUS.md`, `VOICE_TESTS.md`, `NEXT_STEPS.md`).
- Focused test: 2/2 passed. Full `:app:testDebugUnitTest --offline --max-workers=1`: 798 tests / 177 suites passed, 0 failures/errors. APK build succeeded and signature verified: `com.aura.ai.debug` v1.0.0/code 1, SHA-256 `C92B15DC41071F910D39CB434A3DC5074234C109F905D6CC5EEB942FF8A7ABD2`, signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- ADB lists no Honor. The previously approved artifact SHA `F010A4E144451B587AB8D5D93F595A6A95EE64F24ADE761AB594F2B5AE3ED5B8` was not found. This new APK is a different hash and was not installed; no device transcript/audio result.
- Local checkout remains separate branch `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no configured remote, with extensive pre-existing user edits; no source sync, merge or push. `main` unchanged.
- Next: reconnect Honor; get the exact approved APK or approve the new exact hash. Back up and verify signer/package before in-place installation, then test the phrase, visible transcript, spoken result, multi-turn conversation, interruption and actual voice. Keep this improvement marked unverified until then.


## Verified LAN Ollama endpoint and Android default update — 2026-11-14

- Direct live LAN checks: `http://192.168.8.18:11434/api/tags` returned HTTP 200 with `qwen3:1.7b` and `qwen3:4b-q4_K_M`; `http://192.168.8.18:11435/api/tags` refused connections. The local Android settings default and stale shipped-default recovery now prefer `http://192.168.8.18:11434`, retaining alternate-port fallback. Explicit custom user-saved URLs remain unchanged.
- Added common full arithmetic question variants to Android SpeechRecognizer bias hints; these can affect ranking but cannot guarantee recognition.
- Source/test changes: `android/app/src/main/java/com/aura/ai/data/remote/ollama/OllamaEndpointPolicy.kt`, `android/app/src/main/java/com/aura/ai/data/settings/SettingsRepository.kt`, `android/app/src/main/java/com/aura/ai/voice/SpeechRecognitionBiasPolicy.kt`, `android/app/src/test/java/com/aura/ai/data/settings/T1000SettingsDefaultsTest.kt`, `android/app/src/test/java/com/aura/ai/voice/SpeechRecognitionBiasPolicyTest.kt`; local docs updated: `AI_HANDOVER.md`, `PROJECT_STATUS.md`, `VOICE_TESTS.md`, `NEXT_STEPS.md`.
- Focused settings/recovery tests 16/16, phrase-bias tests 2/2, full Android JVM suite 798/798 across 177 suites, 0 failures/errors. Debug APK build and signature verification passed: `com.aura.ai.debug` v1.0.0/code 1, SHA-256 `CB1FC0A2C75BF3EC979BF53FD2119815A2B1C253DD966202789673EA859A433D`, signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- The exact previously approved APK `F010A4E144451B587AB8D5D93F595A6A95EE64F24ADE761AB594F2B5AE3ED5B8` was not found in the workspace. Current APK has a different hash and is not authorized for installation. ADB sees no Honor; no backup/install or phone speech/model test occurred.
- Local source checkout is separate, dirty, and has no remote: `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`. No local commit or code sync/merge; production `main` unchanged.
- Next: reconnect Honor; obtain the exact approved artifact or approval for the current hash; back up data and verify the installed signer/package before updating in place. Then confirm model list and run wake, transcript, answer, turn-taking, interruption, actual voice and cloud-fallback checks on the phone.


## Approved APK install attempt — 2026-11-14

- Owner approved exact APK SHA-256 `CB1FC0A2C75BF3EC979BF53FD2119815A2B1C253DD966202789673EA859A433D`, package `com.aura.ai.debug` v1.0.0/code 1, signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`. Approval is conditional on backing up existing Aura app data and verifying package/signature, then updating in place; it does not authorize uninstall or data clearing.
- Rechecked `adb devices -l` after the owner said they would reconnect the Honor. ADB listed no devices. No backup, install, app launch, or real-device test occurred. The exact approved APK remains available at `android/app/build/outputs/apk/debug/app-debug.apk`; its hash, package, and signer were reverified in the local workspace.
- Automated evidence remains the latest full Android JVM run: 798 tests across 177 suites, 0 failures/errors/skips; APK assembly/signature verification passed. These results do not verify install compatibility or device voice behavior.
- Local source checkout remains separate at `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, with no configured remote and pre-existing user edits preserved. No source code was changed, copied, or merged here; production `main` is unchanged.
- Next: reconnect the Honor, identify the installed Aura package and signer, create and integrity-check a fresh private-data backup, and update only with the exact approved APK. Then verify data retention, model discovery, wake, full transcript, spoken answer, follow-up turn, and interruption on the phone. Do not substitute another APK hash.


## Android speech timeout recovery — 2026-11-14

- The speech timeout path previously started a new recognizer session after the original platform recognizer stalled. That session could not replay the audio already captured, risking another long wait and losing the best partial transcript.
- Updated Android speech capture to retain the latest platform or Vosk partial hypothesis. At the 18-second no-new-partial timeout, it now finalizes that transcript instead of opening another microphone session. Existing transcript validation still rejects incomplete phrases. With no partial, it surfaces a timeout error. The final-result grace stays at 5 seconds.
- Changed source/tests: `android/app/src/main/java/com/aura/ai/voice/SpeechToText.kt`, `android/app/src/main/java/com/aura/ai/voice/SpeechRecognitionTimeoutPolicy.kt`, `android/app/src/test/java/com/aura/ai/voice/SpeechRecognitionTimeoutPolicyTest.kt`.
- Tests: timeout policy 6/6; transcript merger 27/27; full Android JVM suite 798 tests across 177 suites, 0 failures/errors/skips. Focused compilation/tests passed after Gradle used the in-process Kotlin fallback because the user-profile Kotlin daemon temp marker was access denied. Full cached suite passed. `:app:assembleDebug --offline --max-workers=1` passed.
- New APK is `android/app/build/outputs/apk/debug/app-debug.apk`, package `com.aura.ai.debug` v1.0.0/code 1, SHA-256 `4DCD1CB1E76F86B7E360D563B8630485C107A09B8FBF4C8B270E1CB478746C39`; apksigner verification passed, signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- Owner's exact install approval covered the previous APK hash `CB1FC0A2C75BF3EC979BF53FD2119815A2B1C253DD966202789673EA859A433D`, not this rebuild. Do not install without approval for this exact hash. ADB still lists no Honor; no backup, install, voice, wake, interruption, or phone-side Ollama test occurred. The improvement remains device-unverified.
- Local source checkout is separate, branch `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote, pre-existing dirty work preserved. No source was synchronized or merged to this branch; `main` remains untouched.
- Next: reconnect the Honor, obtain exact-hash approval, back up private app data, verify installed package/signer, update in place, and test full speech transcript, response latency/audio, multiple turns, wake, interruption, and Ollama model discovery.


## Voice local Qwen 4B fallback latency — 2026-11-14

- Found a voice routing exception: Qwen 4B received a 180-second attempt timeout even when an authenticated cloud backup route was configured. This bypassed the existing 30-second fallback budget and could leave the spoken request waiting for two minutes.
- Updated `ProviderChatRouter.timeoutForAttempt`: for voice mode, Qwen 4B is capped at 30 seconds if another provider route is configured; with no backup it retains the measured 180-second cold-start allowance. Text-chat timeout behavior remains unchanged. Provider output remains buffered and partial output is discarded before fallback.
- Changed files: `android/app/src/main/java/com/aura/ai/data/remote/ProviderChatRouter.kt`, `android/app/src/test/java/com/aura/ai/data/remote/ProviderChatRouterTest.kt`. Prior speech-timeout work in this build also changed `SpeechToText.kt`, `SpeechRecognitionTimeoutPolicy.kt`, and `SpeechRecognitionTimeoutPolicyTest.kt`.
- Tests: focused `ProviderChatRouterTest` 15/15; full JVM suite 799 tests across 177 suites, 0 failures/errors/skips. Android debug assembly succeeded. Initial compilation caught a test-only Mockito matcher arity error (six arguments for a five-argument cloud API); it was corrected, then focused and full suites passed.
- Current APK: `android/app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `BBCF723A51AE1BFCB50672A9C39DF0B41AD942F7723FBA4B3E0D51C148CF0EF1`, package `com.aura.ai.debug` v1.0.0/code 1; apksigner verified, signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- Earlier approval was for different hashes and does not authorize this APK. ADB still lists no Honor. No app-data backup, install, live cloud request, or phone voice test occurred. The fallback test uses mock providers; actual cloud fallback remains unverified.
- Local source is separate branch `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, with no remote and existing dirty user changes preserved. No source sync or merge; production `main` unchanged.
- Next: reconnect the Honor and approve this exact APK hash. Back up private data, verify installed package/signer, update in place without uninstall/data clear, then test voice transcript and latency on-device; exercise live cloud fallback only with the authorized credential configured.


## Follow-up still fails on installed build — 2026-11-14

- Owner reports the first wake-and-answer turn on the installed Aura build succeeded, but Aura stopped listening or did not respond to the next question without another wake phrase. This confirms the installed build does not yet meet continuous multi-turn conversation acceptance.
- The no-match recovery candidate preserves an interim transcript after Android `ERROR_NO_MATCH`, retries full platform command capture once during an active conversation, then returns to the wake listener. It passed focused tests (9/9) and the full Android regression (801 tests across 177 suites, 0 failures/errors/skips); `:app:assembleDebug --offline --max-workers=1` succeeded.
- Candidate APK `com.aura.ai.debug` v1.0.0 SHA-256 `E2ADEC373945565A797E98851C14CDC04748FDC0A0DF047572BA4B214EFDE7FE` remains uninstalled. Prior approval was for another hash; do not install this candidate until the owner explicitly approves this exact hash. ADB currently lists no connected Honor.
- No new phone test was possible in this update. The retry fix is not device-verified. The previous installed build's first-turn transcript and spoken answer (“10”) passed by owner report; its next-turn continuity failed by owner report.
- Local checkout: `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`; no configured remote and pre-existing user edits preserved. No local commit/push or source sync. This GitHub update is documentation-only; production `main` remains unchanged.
- Next: after exact-hash approval and Honor reconnection, make a fresh private-data backup, verify installed package/signature, update in place without uninstall/data clear, then test two or more consecutive turns without repeating the wake phrase and capture bounded ASR logs if a turn fails.


## Candidate verification rerun — 2026-11-14

- Re-ran the focused voice recovery suites with `--rerun-tasks` (not a test-cache hit): 9 tests passed, 0 failures/errors/skips. Gradle took 6m58s because Windows denied Kotlin daemon temporary marker files and Gradle compiled using its in-process fallback.
- Rechecked the candidate artifact: SHA-256 remains `E2ADEC373945565A797E98851C14CDC04748FDC0A0DF047572BA4B214EFDE7FE`; package is `com.aura.ai.debug` v1.0.0/code 1; `apksigner verify --print-certs` passed with signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- This is automated/artifact verification only. ADB lists no Honor; this exact candidate remains uninstalled pending exact-hash approval. The installed build passed one owner-reported first turn, then failed its no-wake follow-up; recovery is not device-verified.
- Local handover, status, voice tests, and next steps were updated. Local checkout is still separate/dirty at `codex/aura-original` HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote. No source sync, merge, or change to production `main`.
- Next: get approval for this exact APK hash, reconnect the Honor, make a fresh private-data backup and verify package/signer, then update in place without clearing data and retest consecutive turns and interruption.


## Quiet retry after detected follow-up speech — 2026-11-14

- The owner reported that the installed original Aura completed one wake/math turn correctly, then stopped listening or did not answer the next turn. The installed build still fails multi-turn acceptance.
- Implemented a quiet single retry if speech was detected but Android ASR returns no usable transcript; Aura delays speaking “didn't catch that” until the retry has failed. A no-speech timeout continues to report immediately. The retry preserves active conversation state and does not require another wake phrase.
- Changed files: `android/app/src/main/java/com/aura/ai/voice/WakeWordService.kt`, `VoiceCommandCapturePolicy.kt`, test `VoiceCommandCapturePolicyTest.kt`; prior recovery changes remain in `SpeechToText.kt` and `OfflineSpeechFallbackPolicy.kt`.
- Tests: focused voice-recovery suites executed, 10 passed/0 failed/errors/skips; full Android JVM suite 802 tests across 177 suites passed/0 failed/errors/skips. `:app:assembleDebug --offline --max-workers=1 --project-prop=kotlin.compiler.execution.strategy=in-process` passed. `git diff --check` passed for changed source/docs.
- Candidate APK: `com.aura.ai.debug` v1.0.0/code 1; SHA-256 `B1AB27C66BAB518A1C1D0A57D11FA0CADE096246A7CDE0708DBC5272499BF0B2`; `apksigner verify --print-certs` passed with signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`. This exact APK is not installed; earlier approvals were for other hashes. ADB lists no Honor.
- No device retest was possible. The fix is not physically verified, and the previous build's follow-up failure remains outstanding. No app data changed, no APK installed, no source synced/merged, and production `main` remains unchanged.
- Local checkout remains codex/aura-original at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote and pre-existing user edits preserved. Local handover/status/voice tests/next steps updated. Next: owner approves this exact APK hash, reconnect Honor, back up app data, verify package/signer, update in place without uninstall/data clear, then test multiple no-wake follow-ups and barge-in.


## ElevenLabs saved voice ID mismatch — 2026-11-14

- Read-only connected ElevenLabs workspace query found two saved voices: “Aura Austrian Male — Option 3” (`TqK0pPhqb2Kaoa1VWUMI`) and “Aura Austrian Male · Cinematic Deep 2” (`JqNKQLBjDVraAK8Y8G9e`). Workspace descriptions identify masculine English with Austrian German influence.
- Previously read phone DataStore stored `speech_voice_id=elevenlabs:2IgbPrbutSMSQWwJ9beb`, which matches neither workspace voice. This does not prove what was audibly played. No phone setting was changed; Aura's key access to either voice and real playback are untested.
- Keep the Austrian voice marked unverified. After the multi-turn candidate is installed/tested, verify the existing user's selected Option 3 can be loaded and persisted in Aura settings, then play a device preview. Do not silently replace preferences or claim voice consistency before that check.
- Local `AI_HANDOVER.md`, `PROJECT_STATUS.md`, `VOICE_TESTS.md`, and `NEXT_STEPS.md` record the same result. No source sync/merge or `main` change.


## PC/web voice-path automated verification — 2026-11-14

- Reviewed existing `app/use-hands-free.ts` and `app/hands-free-policy.ts`. The browser flow keeps conversation mode through reply playback, starts recognition for barge-in, requires two stable non-echo partials before interrupting, buffers final command segments, and restarts recognition on browser `onend` while enabled. This is code review, not runtime proof.
- Existing suites executed directly in-process because Node's `--test` worker harness failed before test execution with Windows `spawn EPERM`: `tools/hands-free-policy.test.mjs` 5/5; `app/simple-arithmetic.test.mjs` 24/24; `app/website-open-status.test.mjs` 3/3. Total 32/32 passed. `pnpm run typecheck` passed.
- No web source changed. No live PC microphone, TTS audio, browser permission, or end-to-end voice conversation test. Android candidate remains pending exact-hash approval and Honor reconnect.
- Local `AI_HANDOVER.md`, `PROJECT_STATUS.md`, `VOICE_TESTS.md`, and `NEXT_STEPS.md` updated. Local checkout remains dirty `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941` with no remote. No source sync/merge; production `main` unchanged.
- Next: run live PC voice acceptance in Chrome/Edge when the Aura web app and microphone are available; separately back up/install/test the exact approved Android candidate only after approval and device reconnect.


## PC preview availability and cross-device boundary — 2026-11-14

- `http://127.0.0.1:3008/` returned HTTP 200 and title “Aura AI | Your personal assistant”; the existing Aura page was opened and rendered with the workspace, saved conversation, and hands-free toggle. This confirms preview availability at the time checked only.
- The page explicitly says calls, GPS, voice, and phone media run in Android and that the web companion is not connected to the phone. Cross-device control parity is incomplete.
- No live PC mic, browser permission, or TTS test was run. Automated browser suites passed 32/32 in-process (`tools/hands-free-policy.test.mjs` 5, `app/simple-arithmetic.test.mjs` 24, `app/website-open-status.test.mjs` 3); `pnpm run typecheck` passed. Node's `--test` harness failed with `spawn EPERM` before execution, so the same test modules were executed directly.
- Preview process ownership/lifecycle was not verified. A start attempt failed due to argument parsing; direct Next startup hit `spawn EPERM`, and elevated startup reported port 3008 already occupied. A direct GET/browser render succeeded. No code or user data changed.
- Local handover/status/voice-tests/next-steps updated. Local checkout remains `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, with no remote and existing user changes. No source sync/merge; `main` unchanged.
- Next: verify a stable PC preview process, run browser mic/TTS/wake/follow-up/interruption acceptance with browser permission, and keep Android install/device tests gated on exact-hash APK approval plus Honor reconnection.


## Owner-reported follow-up failure — 2026-11-14

- Owner reports the first wake-to-answer turn passed, but the next question without repeating “Yo, yo, yo” stopped listening or produced no response. Continuous conversation remains failed on the installed build.
- The existing code is intended to hand off to a fresh platform speech capture after every completed answer while the wake session remains active. Unit tests cover follow-up command routing but do not exercise the Android microphone/service handoff between turns.
- ADB currently reports no connected Honor, so no logcat for this event was available. The local quiet-retry candidate SHA-256 is `B1AB27C66BAB518A1C1D0A57D11FA0CADE096246A7CDE0708DBC5272499BF0B2`; it remains uninstalled and lacks approval for this exact hash. No install attempted.
- This is a handover-only update; no Android source, settings, app data, APK, or `main` branch was changed. Local source checkout remains dirty `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, without a configured remote.
- Next: reconnect the Honor and capture bounded `WakeWordService`/`AuraSpeechToText` logs across wake, answer playback, and the next no-wake utterance. Identify whether command capture starts, Android ASR becomes ready, speech is detected, and which final/error/timeout/service event occurs. Fix the observed transition, then back up and install only after exact-hash approval; retest multiple consecutive turns.


## Honor wireless ADB reconnection attempt — 2026-11-14

- BEATK is online through the authorized Remote Desktop Commander. Read-only `adb devices -l` showed no connected Honor. `adb mdns services` discovered device serial `AYAV6R3707021141` advertising `_adb-tls-connect._tcp` at `192.168.8.6:43283`.
- `adb connect 192.168.8.6:43283` returned Windows connection refused (10061). Restarted the ADB server on BEATK while no device was attached, rediscovered the same endpoint, and retried; connection remained refused. No phone settings/app data changed; no logs or voice retest were obtained.
- Current local candidate remains `com.aura.ai.debug` v1.0.0 SHA-256 `B1AB27C66BAB518A1C1D0A57D11FA0CADE096246A7CDE0708DBC5272499BF0B2`; it is not installed and lacks approval for this exact hash. No source change, install, or production merge.
- Next: refresh Wireless debugging on the Honor (off/on, same Wi-Fi as BEATK, keep the settings page open), connect to its newly advertised endpoint, then collect bounded service/ASR logs across wake, response, and one follow-up. Use that trace to fix multi-turn capture and retest before claiming success.


## Fresh ADB endpoint retry — 2026-11-14

- BEATK remains online. ADB mDNS now resolves Honor serial `AYAV6R3707021141` at `192.168.8.17:43283` (the phone IP changed from `.6` to `.17`). `adb devices -l` lists no connected phone.
- `adb connect 192.168.8.17:43283` and a Python TCP `connect_ex` probe both returned Windows connection refused (10061). No phone settings/app data were touched; no logcat or voice retest was possible.
- The first wake turn passed by owner report; the subsequent no-wake turn failed. Current candidate `com.aura.ai.debug` v1.0.0 SHA-256 `B1AB27C66BAB518A1C1D0A57D11FA0CADE096246A7CDE0708DBC5272499BF0B2` remains uninstalled and lacks exact-hash approval.
- No source change or test run in this check. Next: refresh Wireless debugging on the phone (off/on, same Wi-Fi as BEATK, keep the screen awake) so the advertised endpoint accepts a trusted ADB connection; then collect bounded wake/answer/follow-up logs.


## Queued next-turn speech capture — 2026-11-14

- Code inspection found a silent commandRecognitionJob?.isActive == true guard in WakeWordService.startCommandRecognition that discarded a new capture request while a previous recognizer job was still active. Added a single queued follow-up capture for a pending wake/conversation session; it waits for prior capture completion, checks that the service/session is still valid and no command is running, suppresses duplicate queue jobs, and cancels queued work on service destruction. Blocked start reasons are now logged.
- Changed code/test files: android/app/src/main/java/com/aura/ai/voice/WakeWordService.kt, VoiceCommandCapturePolicy.kt, VoiceCommandCapturePolicyTest.kt.
- Verification: focused policy suite 4/4; full Android JVM suite 803/803; debug APK assembly succeeded; git diff --check passed. The first build attempt hit Access denied on the global Gradle wrapper cache; rerun with workspace-local --gradle-user-home .gradle-aura-cache passed. Focused cold compilation/test took 10m02; incremental full suite and assembly took 1m20.
- Candidate APK android/app/build/outputs/apk/debug/app-debug.apk: SHA-256 44636F2B74C4B60784088F1FA973E2331C3E3F4716040ADD53E3BC72E1EE1E4C, package com.aura.ai.debug v1.0.0/code 1. apksigner verification passed; signer SHA-256 59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b matches previous debug builds.
- This exact APK is not installed and has no exact-hash approval. BEATK ADB mDNS sees the Honor at 192.168.8.17:43283, but connection attempts are refused (10061). No device logs or voice retest occurred. The old installed build still has the owner-reported failed follow-up; this change is not device-verified and is not claimed fixed.
- Local source checkout is dirty codex/aura-original at 471ef9d4c34bc62758908b9efe1f5dff2af54941 with no configured remote. No local source commit/push or sync; this update is documentation-only on the integration branch, production main unchanged.
- Next: reconnect the Honor, obtain exact-hash approval, make/verify a fresh app-data backup and package/signer checks, install in place without uninstall/data clear, then test at least two consecutive no-wake turns and interruption. Capture service/ASR logs; do not claim voice continuity until actual phone tests pass.


## Final Honor ADB state for queued-capture candidate — 2026-11-14

After the build, BEATK reported no ADB devices and no mDNS services. The Honor is currently unreachable for logs or installation. Candidate SHA-256 44636F2B74C4B60784088F1FA973E2331C3E3F4716040ADD53E3BC72E1EE1E4C remains uninstalled and not approved for this exact hash. No additional device changes or tests occurred.


## Owner reports follow-up stopped after successful first turn — 2026-11-14

- Owner reports the first wake-to-answer turn succeeded, but Aura then stopped listening or did not respond to a follow-up without another wake phrase. Multi-turn hands-free is still failing on the installed build.
- The queued recognizer-capture repair is present in the local candidate and has already passed focused policy tests (4/4), the full Android JVM suite (803/803), debug APK assembly, and `git diff --check`. These are automated checks only; they do not prove Android's microphone/ASR handoff works on the Honor.
- Rechecked the authorized BEATK computer through Remote Desktop Commander: it is online, but `adb devices -l` is empty and `adb mdns services` reports no devices. No phone logs, backup, install, or live retest were possible in this turn.
- Candidate remains `android/app/build/outputs/apk/debug/app-debug.apk`, `com.aura.ai.debug` v1.0.0/code 1, SHA-256 `44636F2B74C4B60784088F1FA973E2331C3E3F4716040ADD53E3BC72E1EE1E4C`; signer verification passed earlier. This exact hash is not approved or installed. Earlier approvals refer to different APK hashes.
- No code changed and no tests were rerun in this turn. Local branch/HEAD remain `codex/aura-original` / `471ef9d4c34bc62758908b9efe1f5dff2af54941`; no configured remote and pre-existing user work preserved. No source sync, commit, install, or merge; production `main` remains untouched.
- Next: refresh Wireless debugging on the Honor and leave its page open on the same Wi-Fi as BEATK. After the Honor is discoverable, request approval for this exact APK hash, make a fresh app-data backup, verify package/signature, and update in place without uninstall/data clear. Test two consecutive no-wake follow-ups and collect bounded `WakeWordService`/ASR logs if either fails.


## PC Ollama inventory and Honor ADB retry — 2026-11-14

- Read-only checks on BEATK: Ollama 0.40.16; `ollama list` shows `qwen3:1.7b` and `qwen3:4b-q4_K_M`; `ollama ps` shows no loaded model. GET `http://127.0.0.1:11434/api/tags` returned HTTP 200 and both models. No inference request was made to avoid loading a model without owner direction.
- ADB lists no connected Honor. mDNS advertises `AYAV6R3707021141` at `192.168.8.6:43283`; connecting to that endpoint is refused (10061). No phone logs, backup, install, or settings change occurred.
- Candidate `com.aura.ai.debug` SHA-256 `44636F2B74C4B60784088F1FA973E2331C3E3F4716040ADD53E3BC72E1EE1E4C` remains uninstalled and without approval for this exact hash. Previous automated evidence is 803/803 Android JVM tests plus successful APK assembly; continuous multi-turn voice remains unverified on device and failed on the previously installed build.
- No source changed and no tests reran. Local source remains dirty `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, with no remote; no local source commit/sync or production merge. Next: restore ADB connectivity, obtain exact-hash approval, back up app data, verify package/signer, update in place, then test two consecutive no-wake follow-ups with bounded service/ASR logs.


## Voice output consistency check and approved APK connection attempt — 2026-11-14

- Corrected a stale comment in `WakeWordService.kt`: the selected ElevenLabs voice reports a failure and does not fall back to Android TTS. No runtime behavior changed.
- Focused `SpeechOutputFallbackPolicyTest`: 2 passed, 0 failures/errors/skips. Gradle test completed in 6m48 using compiler fallback after a Kotlin daemon temp-marker access denial; `git diff --check` passed. No APK rebuild was needed for a comment-only source edit.
- Owner explicitly approved candidate `com.aura.ai.debug` SHA-256 `44636F2B74C4B60784088F1FA973E2331C3E3F4716040ADD53E3BC72E1EE1E4C` and refreshed Wireless debugging. BEATK mDNS advertises `192.168.8.6:43283`, but `adb connect` returns Windows error 10061 (connection refused). No backup, install, or phone retest yet.
- No runtime code changed; local documentation and the stale comment changed. Local checkout remains dirty `codex/aura-original` at `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no remote; no source sync/merge and `main` remains unchanged.
- Next: pair ADB using the Honor's Wireless debugging “Pair device with pairing code” endpoint/code or connect by USB. Then back up Aura data, verify package/signature, install the approved APK in place, and test consecutive no-wake follow-ups plus logs.


## PC/web saved speech locale repair and Honor pairing status — 2026-11-14

- Local checkout review found that activating Hands-free reset the browser speech-recognition locale to the system language, overriding the saved Aura setting. Added `resolveActiveSpeechRecognitionLanguage` and wired startup to preserve the selected locale, use the Windows locale only for the “system” option, and prefer the installed offline locale when offline recognition is enabled.
- Changed local files: `app/speech-recognition-language.ts`, `app/use-hands-free.ts`, `app/simple-arithmetic.test.mjs`, plus local status/voice-test/next-step/handover files. `pnpm.cmd typecheck` passed; direct `node app/simple-arithmetic.test.mjs` passed 25/25. Node's `--test` worker could not spawn (Windows EPERM), while direct execution passed. No live PC microphone test. `git diff --check` passed.
- This code is in the separate local dirty checkout `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, which has no configured remote. It has not been synced or committed to this GitHub branch; no source files were copied, and `main` remains unchanged.
- Exact approved Android candidate is unchanged: `com.aura.ai.debug` v1.0.0/code 1, SHA-256 `44636F2B74C4B60784088F1FA973E2331C3E3F4716040ADD53E3BC72E1EE1E4C` (package/signer verified previously). Owner approved this exact APK. BEATK ADB `devices -l` and `mdns services` are empty after the requested Wireless debugging refresh, so no fresh data backup, installed signer comparison, installation, log capture, or phone retest could occur.
- Owner says they will send the temporary Wireless debugging pairing details. Next: pair or connect USB, verify installed package/signer, make and verify a fresh app-data backup, then install only this exact approved APK in place if signatures match. Test multiple no-wake turns and interruption; capture bounded voice service logs on failure. Keep Android device acceptance unverified until then.


## Approved APK installed and preserved-data verification — 2026-11-14

- The Honor was reachable through the workspace's local ADB connection; the separate Remote Desktop Commander ADB session showed no phone. Installed package is the original `com.aura.ai.debug` v1.0.0/code 1. No app was uninstalled or cleared.
- Approved candidate verified before install: SHA-256 `44636F2B74C4B60784088F1FA973E2331C3E3F4716040ADD53E3BC72E1EE1E4C`, package `com.aura.ai.debug`, version `1.0.0`/code `1`. Candidate signature verified. Pulled pre-install APK hash was `BBCF723A51AE1BFCB50672A9C39DF0B41AD942F7723FBA4B3E0D51C148CF0EF1`; its signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b` matched the candidate.
- Before update, archived private app data at `.gradle-aura-cache/device-backups/com.aura.ai.debug-pre-44636F2B-20261114-070131/private-data.tar`: 18,457,088 bytes; SHA-256 `06E79DA0B56310222C23B020A2B2DE93D40D58B1CB3B5A6B3F6761EFF18CC1B8`. Tar integrity check passed with 130 entries, including the Room database/WAL/SHM, settings/preferences and three existing Builder HTML files. The pre-install APK is also retained beside the archive.
- `adb install -r` returned Success. No uninstall or data-clear operation. First-install timestamp remained `2026-10-30 01:25:43`. Pulled installed APK SHA-256 exactly matches the approved hash and signer validation passed. Database/settings/Builder files remain present.
- Started `com.aura.ai.debug/com.aura.ai.MainActivity`; process remained alive and a bounded logcat sample showed no fatal exception. Another app was in front at the later check, so no live microphone, wake, transcript, speech, follow-up, or interruption test is claimed yet. Owner was asked to test “Yo, yo, yo, what is five plus five?” then a follow-up without repeating the wake phrase.
- Separately, the local PC/web saved-recognition-locale bug was fixed: start now honors Aura's saved locale, uses Windows locale for the “system” option, and uses the offline pack when selected. `pnpm.cmd typecheck` passed; direct `node app/simple-arithmetic.test.mjs` passed 25/25. `node --test` could not spawn worker (Windows EPERM); direct execution passed. No live browser mic test. Local source is separate dirty checkout `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no configured remote; these web code changes were not synced or committed to this branch.
- No production `main` merge. Next: collect the owner's exact transcript and spoken answer for the first and no-wake follow-up turns. If follow-up fails, inspect bounded service/ASR logs now that ADB is connected; diagnose before another build. Any new APK requires exact-hash approval and a fresh data backup before in-place update.


## Honor post-install speech-recognition observation — 2026-11-14

- ADB confirms original Aura `com.aura.ai.debug` remains connected and its `WakeWordService` is running as a foreground service with RESUME action. MainActivity window exists and the Android process is alive.
- Recent bounded logcat samples contained multiple finalized offline wake-recognizer speech results (1–5 alternatives each). Every logged result reports `wakePhraseDetected=false`; no live wake-match or command-handoff event appeared in the sampled window. Log output omits recognized transcript words, so the words the recognizer heard remain unknown.
- This is evidence of active service/audio recognition results, not proof of the owner's exact phrase or a successful wake/command. Two-turn live speech acceptance remains pending the owner's exact transcript and spoken outcome.
- No Android source changed after installation. Next: capture the user's test result; if the transcript is not visible, implement local-only user-visible diagnostic transcript capture with clear retention/erase behavior, test it, rebuild, verify, obtain approval for the new exact APK, back up app data again, and then update in place. No automatic cloud transcript logging or claim of wake success.


## Failed-wake transcript preview visibility — 2026-11-14

- Increased the visible-only, transient failed-wake transcript preview from 5 seconds to 30 seconds. This is diagnostic visibility only; it does not change wake detection or speech-recognition accuracy. The preview remains bounded to 160 characters, shown only while Aura is foreground, and is not uploaded.
- Local files changed for this task: `android/app/src/main/java/com/aura/ai/voice/wake/WakeTranscriptPreviewPolicy.kt`, `android/app/src/main/java/com/aura/ai/voice/WakeWordService.kt`, `android/app/src/test/java/com/aura/ai/voice/wake/WakeTranscriptPreviewPolicyTest.kt`, `AI_HANDOVER.md`, `PROJECT_STATUS.md`, `VOICE_TESTS.md`, and `NEXT_STEPS.md`.
- Verification: focused test `:app:testDebugUnitTest --tests com.aura.ai.voice.wake.WakeTranscriptPreviewPolicyTest` passed (4 tests, 0 failures); `:app:assembleDebug` passed. Combined offline Gradle run completed successfully in 7m50s. Kotlin daemon could not create its temp marker under `%LOCALAPPDATA%`; Gradle fallback compilation succeeded. `git diff --check` passed (line-ending notices only).
- Candidate: `android/app/build/outputs/apk/debug/app-debug.apk`, package `com.aura.ai.debug`, version `1.0.0`/code `1`, size 105,700,759 bytes, SHA-256 `A6A2EADFE1746E909EBB1E13C3AF43B90A0C058DA6C58C0A520647F009A1D733`. APK signature verification passed; signer SHA-256 `59df53c4026967ee86f6313a0ae8aa1ce4f4fbd9c025cd808e41d3eaf7da3a3b`.
- Not installed. Pulled the currently installed `com.aura.ai.debug` APK read-only from the Honor; its SHA-256 is `44636F2B74C4B60784088F1FA973E2331C3E3F4716040ADD53E3BC72E1EE1E4C`, confirming the phone remains on the previously approved build. New exact-hash approval is required before installing any candidate, and this candidate additionally requires source-branch reconciliation first.
- The candidate APK was built from available local dirty checkout `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`; this source has not been reconciled with GitHub's source-of-truth branch, so do not install the candidate until source alignment is verified. The local checkout has no configured remote; no local commit or main merge. This GitHub write is handover-only on `integration/aura-voice-consolidation`.
- Device status: prior approved install/data/signature checks passed. Earlier wake-service logs showed ASR finals without a wake match and omitted recognized words. Owner's requested two-turn report remains pending; wake detection, transcript accuracy and continuous follow-up are not claimed fixed.
- Next: record the exact two-turn transcript/answers; request approval for this candidate hash; back up data and verify package/signature before any in-place install; then retest one-wake plus follow-up. If it still fails, capture visible transcript and bounded voice-state logs.


## Follow-up ADB observation — 2026-11-14

- ADB still sees the Honor. A read-only pull of installed `com.aura.ai.debug` base APK matches SHA-256 `44636F2B74C4B60784088F1FA973E2331C3E3F4716040ADD53E3BC72E1EE1E4C`; the new diagnostic candidate is not installed.
- Latest resumed activity was the Honor launcher, not Aura. A bounded logcat sample showed two finalized `WakeWordService` speech results (`alternatives=1`, `wakePhraseDetected=false`) at 07:26:32 and 07:26:39. No wake-to-command handoff appeared. Logs omit recognized text, so these events cannot establish the exact transcript or whether they correspond to the owner's requested utterance.
- This adds evidence that the background listener received speech but did not match the wake phrase in those two events. It does not verify the owner's first/second spoken answer, exact transcript, or cause. Because Aura was not foreground, its local visible `Heard:` preview would not show.
- GitHub integration source layout was checked read-only: Android code is under `app/src/main/...`, while the writable local checkout used for the candidate has code under `android/app/src/main/...`. Do not install the candidate until the correct integration source is reconciled and rebuilt.
- Next: capture the owner's exact two-turn result. For visible transcript diagnostics, bring original Aura foreground and repeat the test, then trace bounded events. Do not install the unaligned candidate.


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


## Honor transcript and local Ollama runtime check — 2026-11-14

- Read-only review of the Honor's Room database found a saved voice turn at 07:06:14 local: USER text `with which life`; the next assistant row at 07:06:46 contains Ollama HTTP 402 with “Add usage credits or subscribe.” This confirms the saved transcript was wrong and the request did not produce an answer. No unrelated conversation rows or secret values were included in this handover.
- The current Honor DataStore settings (keys only; no credentials read) show `ai_provider=OLLAMA`, automatic fallback enabled, Ollama base URL `http://192.168.8.18:11434`, and selected model `qwen3:4b-q4_K_M`.
- On BEATK, Ollama CLI is `0.40.16`; `ollama list` shows `qwen3:1.7b` and `qwen3:4b-q4_K_M`; `ollama ps` was empty before the generation check. A direct no-proxy GET to `http://192.168.8.18:11434/api/tags` returned HTTP 200 with both local models. One direct local `/api/chat` request to `qwen3:4b-q4_K_M` returned `4.`; total 19.5 seconds including 17.3-second load. The request used `keep_alive:0`; a subsequent `ollama ps` was empty. This verifies PC/LAN-local Ollama inference, not Aura's Android request path.
- The HTTP 402 indicates a credit-gated Ollama endpoint was reached. Given saved local preference/fallback and the timing, cloud fallback after a failed or timed-out local attempt is plausible but not proven: the available device logs do not record provider-route transitions. The local checkout's 30-second voice fallback policy is from a separate, unaligned source tree and cannot be treated as the integration-branch implementation.
- The Honor remains on installed APK SHA-256 `44636F2B74C4B60784088F1FA973E2331C3E3F4716040ADD53E3BC72E1EE1E4C`; no new APK installed. The most recent ADB check had the Honor launcher foreground and two standby finals logged with `wakePhraseDetected=false`; exact text is not recorded there. The new integration-branch matcher has not been built or device-tested because GitHub Actions has no runner/job steps.
- Next: resolve integration-branch build/test execution, then test its cloud/local routing and voice flow on the Honor with explicit route diagnostics that do not expose keys or store unapproved transcripts. Reconcile which app build is actually installed before any install approval; do not use the unaligned local candidate.


## Two-turn voice lifecycle source review — 2026-11-14

- Read the integration branch's `ChatViewModel.kt`, `WakeWordService.kt`, `AgentOrchestrator.kt`, `Speaker.kt`, and provider router. No source code changed in this review. Branch remains `integration/aura-voice-consolidation`; the latest previously recorded code commits are `4f159677f3b0fa2b33746b1c694624d7b2b9ad8c` and `18d8b1db7c9c64cceaa54bb07b410282323eed64`; latest handover commit before this entry is `4ad130445a0ce725c23f25acfb212392bd848887`.
- Code path: a wake event enters a hands-free session; each recognized final transcript is sent to the orchestrator; after TTS starts, ChatViewModel starts an echo-monitoring recognizer, and it normally schedules the next turn after TTS completion or recognizer completion/error. A possible stall exists if echo-monitoring speech recognition remains active but never produces a final/error callback: `monitoringSpeechOutput` remains true, so the TTS completion callback skips the fallback re-arm. This is a code-level hypothesis only; it requires the owner's current second-turn result and, if reproduced, state-transition logs to confirm.
- The owner is testing now. The exact transcript and audible response for the first wake turn and the no-repeat-wake follow-up are still pending. No app was opened, no APK installed, and no device test was run by this review.
- Verification: source inspection only; no automated tests or build run. The prior blocker remains: Android CI run `43561921766` started no runner or job steps, so the integration branch matcher test and APK build remain unverified.
- Next: record the owner's exact first- and second-turn transcripts/responses. If the second turn stalls, instrument the controller's listening/TTS callbacks with bounded, privacy-safe state logs, then build and test the exact integration branch before requesting installation approval. Keep all unverified candidate APKs off-device.


## Honor background wake status during owner test — 2026-11-14

- Read-only ADB at 07:54 local found the Honor home launcher resumed and the installed `com.aura.ai.debug/com.aura.ai.voice.WakeWordService` active with its RESUME intent. The installed application is the previously approved build; this check installed nothing.
- WakeWordService logged two new finalized standby speech results at 07:54:33.196 and 07:54:43.560. Both reported `alternatives=1, wakePhraseDetected=false`. Recognized transcript text is not logged, so these events cannot show what the recognizer heard or prove they were the owner's exact requested sentence. A bounded live logcat observation found no further events during the following ~45 seconds.
- This is evidence of wake mismatch for those background recognizer events, not evidence about the in-app two-turn conversation (Aura was not foreground). The owner's exact manual test transcript/answer remains pending. A read-only Room snapshot attempt could not be completed because the Windows execution environment denied writing its temporary local database copy; no phone database or settings were modified.
- Verification: ADB device connected; launcher and service state read; two wake results captured. No automated test, app build, or APK install performed. The source-level echo-monitor stall remains a hypothesis. GitHub Android workflow still has no runner steps.
- Next: obtain the owner's exact two-turn transcript/result. For a live test, keep original Aura foreground and use its visible transcript; only then correlate recognition, model route, and speech completion. Build the exact integration branch and run tests before any exact-hash install approval.


## Android CI retry — 2026-11-14

- Retried failed GitHub Actions run `43561921766` for the integration branch after discovering a configured default remote environment. The retry queued job `132353984081` and then completed as failure. Job steps returned an empty list; log download returned GitHub `BlobNotFound`. Therefore no Gradle test or APK build executed in this attempt. This reproduces the runner/steps infrastructure failure; it is not a code-test failure.
- The registered default environment reports `canRun=true` but has no checked-out working directory (`cwd=null`). No remote coding task was started. Local checkout remains dirty on `codex/aura-original` without a remote, so it is not suitable as a verified build of the source-of-truth branch.
- Next: regain actionable Android Actions runner/build access or a directly accessible checked-out executor, then test/build the exact branch. Do not install any unverified candidate.


## Original Aura vs integration branch identity audit — 2026-11-14

- Read-only Honor Package Manager evidence: installed app is `com.aura.ai.debug`, version `1.0.0` / code `1`, launcher activity `com.aura.ai.MainActivity`, last update 2026-11-14 07:03:36. The device package list showed no `com.aura.ai` package in this check.
- Local workspace project `C:\\Users\\shawn\\Desktop\\T1000-main\\android\\app` declares base ID `com.aura.ai` plus debug suffix `.debug`, app label `Aura AI`, wake resource `yo, yo, yo`, and Vosk-based wake-service code. This matches the installed package and observed Vosk log format.
- This GitHub branch uses a different Android app layout: `app/build.gradle.kts` declares `com.aura.ai` without a debug suffix; `app/src/main/res/values/strings.xml` labels it `T1000`; its wake service uses a platform SpeechRecognizer restart loop. The integration branch has not been shown to be an update-compatible source for the installed original Aura.
- The local source branch `codex/aura-original` is dirty at `471ef9d4c34bc62758908b9efe1f5dff2af54941` and has no configured Git remote. Current AGENTS.md says not to switch or overwrite source when the correct remote/source is missing or ambiguous. Therefore no app source was changed; no branch build/install or data change was made.
- This identity conflict supersedes the prior assumption that the integration branch can safely generate the installed Aura update. Before further code changes, confirm a verified remote/branch for the installed original Aura or explicitly decide whether the T1000-labelled `com.aura.ai` branch is intended to become the primary app. Do not install it beside/over `com.aura.ai.debug` before package, signing, resources, and database compatibility are verified.
- Tests/build: none in this identity audit. Latest Android Actions retry failed before steps (run `43561921766`, job `132353984081`).
- Next: resolve the source mapping, then compare signer, app ID, database, icon/label, wake engine, and settings before building a verified in-place update. No production merge.


## Original Aura remote provenance search — 2026-11-14

- The local installed-app source under `T1000-main/android/app` has local-only Git history and no remote; its baseline commit was created as `Codex Local Project`. The timestamped source backup contains no `.git/config`. This GitHub repo's integration branch is explicitly identified in its own source as T1000 and is not the original Aura's verified update source.
- Read-only GitHub search also found `spokdot777-dot/aura-ai`, an older `AURA AI` prototype with package base `com.aura.ai`. Its README describes hotword support as a future phase, and no history/source evidence ties it to the installed Vosk-based `com.aura.ai.debug` application. Do not treat it as verified original source.
- No app source was changed, built, or installed. Next: owner or repository records must identify the existing remote/branch that corresponds to the installed original Aura; then reconcile source history, package/signing, UI/resources, and databases before an update build. Keep this integration branch and `main` unchanged for app code until that mapping is known.


## Remote branch fingerprint search — 2026-11-14

- Read-only branch checks show `copilot/aura-audit-foundation` and `feature/aura-api-manager-voice-20261012` are T1000-labelled; `v0/aura-core-android-integration` labels its app AURA AI but uses base ID `com.aura.ai` and an earlier OpenAI prototype, with no Vosk `WakeWordService` at the expected path. The separate `spokdot777-dot/aura-ai` repo also describes hotword as a future phase. None is verified to match installed `com.aura.ai.debug` and its local Vosk source.
- Indexed GitHub code searches for `WakePhraseSession` / `WakeWordService Vosk` across accessible `aura-ai`, `aura-core`, and `Aura-Voice-Subsystem` default-branch indexes returned no results. Search-index limits mean this does not rule out an unindexed/private branch.
- No app changes/build/install. Next: identify the correct original Aura remote/branch or an authorized source-control setup, then compare source history to the local snapshot before any code edit. Preserve data and do not install T1000 as Aura.


## Local Android regression and connected Honor observation — 2026-11-14

- Rechecked the active local workspace before testing. It is branch `codex/aura-original`, HEAD `471ef9d4c34bc62758908b9efe1f5dff2af54941`, no configured remote, and has pre-existing modified/untracked files. The installed Honor package remains `com.aura.ai.debug` v1.0.0/code 1 (Package Manager last update 2026-11-14 07:03:36). This does not prove the local snapshot's upstream provenance.
- Ran the repository-root Gradle wrapper offline with in-repository cache: `GRADLE_USER_HOME=.gradle-aura-cache .\\gradlew.bat :app:testDebugUnitTest --no-daemon --offline --max-workers=1`. Result: BUILD SUCCESSFUL; 804 tests across 177 XML reports, 0 failures, 0 errors, 0 skipped. The initial call from `android/` could not find a wrapper; the root wrapper first hit access denial in the shared Gradle cache, then passed using the isolated cache. This is a local-workspace unit test result, not evidence that the GitHub integration branch or installed APK was rebuilt.
- ADB is connected. Read-only window state showed the Honor on the launcher/notification shade, not Aura. In the 08:10:59–08:17:06 local-time window, the background WakeWordService emitted 86 speech-final events, all with `wakePhraseDetected=false`. Transcripts are intentionally absent from logs; these events cannot identify what was said. The requested in-app two-turn wake/math/follow-up test is not verified.
- No app source, APK, app data, device setting, or production branch was changed. No installation or merge occurred. Local original-source mapping remains unresolved; do not treat `integration/aura-voice-consolidation` as the installed original app source solely because of package similarity.
- Next: complete the two-turn test with original Aura foreground and capture the visible transcript/answer for both turns. After authoritative original source mapping is established, diagnose the standby mismatch and session recovery, rerun tests against that source, build and verify the candidate, then seek exact-hash install approval with data backup.


## Honor app launch staged for owner voice test — 2026-11-14

- After the previous read-only check, ADB launched the already-installed original package `com.aura.ai.debug`. Android reports Aura's `MainActivity` resumed, but `NotificationShade` still has current focus and the Honor launcher is the focused app. The phone remains behind the lock/notification shade; no unlock was attempted. App data/install was not changed.
- The two-turn voice test is not yet underway. Owner needs to unlock and bring original Aura visibly to the foreground, then test the wake/math prompt and no-wake follow-up and report exact visible transcripts and spoken answers.
- Local test status remains 804 Android JVM tests passing; no device voice pass is claimed. No source/APK changes or install/merge occurred. Local original-source provenance remains unresolved.


## Windows/web automated checks — 2026-11-14

- `pnpm.cmd typecheck` passed in the local workspace.
- All 13 existing Node test files passed: **70 tests, 0 failures**. Coverage includes wake/interrupt policies, memory/search helpers, URL and scoped-file policies, and Windows control policies. No live browser mic session or fresh real desktop-control task was exercised.
- Android local JUnit suite remains 804 passed, 0 failures/errors/skips. The Honor remains connected, but Aura's main activity is behind the notification shade; real-device conversation/follow-up/interrupt tests are not verified.
- No app source, APK, device data, or permissions changed. Original Aura source provenance remains unresolved. Next: obtain visible Honor test results after unlock, then establish authoritative source mapping before voice-path edits.


## Live PC companion smoke test — 2026-11-14

- The existing Aura web preview responded HTTP 200 at `http://127.0.0.1:3008/` and identified itself as Aura AI.
- The local Windows agent's read-only `list_windows` action returned 12 visible windows. No app was opened, focused, typed into, or changed in this test. This confirms one live PC-agent read path only; it is not a complete desktop-control test.
- TypeScript typecheck passed and existing web Node tests passed 70/70. Android local JUnit remains 804/804. Honor Aura is still behind the notification shade; two-turn voice test remains pending. No source/APK/device-data change or merge.


## Verified Windows Calculator launch — 2026-11-14

- Used the live Aura local Windows agent to open Calculator. The response identified the opened app as Calculator; a subsequent read-only `list_windows` call found exactly one Calculator window. This confirms one supported low-risk app-launch path. No voice-driven routing or arbitrary desktop control is claimed. Calculator remains open.
- No Android app source, APK, installation, or phone data changed. The Honor still needs to be unlocked with Aura visibly foregrounded for the pending two-turn hands-free test.
