# Voice verification

Updated: 2026-11-05

## Android action tools build check — 2026-11-06

- Voice-related regression suites were included in the full offline app/voice run: 64 tests total, 0 failures/errors/skips across 21 suites. APK assembly passed; package/signature/hash are recorded in `AI_HANDOVER.md`.
- No Honor/ADB test was run. Wake phrase, mic lifecycle, transcript accuracy, TTS, continuous conversation and interruption remain device-acceptance items; this build result does not verify them.
- The added phone/SMS actions only prepare Android UI for user confirmation. Their real device intent targets have not yet been checked on the Honor.

## Automated checks (2026-11-05)

| Check | Result |
|---|---|
| App unit tests | 19 passed, including math, acknowledgement policy and response sanitization. |
| `DefaultAuraVoiceSubsystemTest` | 2 passed. Confirms selected-provider behavior and no silent provider switch. |
| Debug APK build | `:app:assembleDebug` passed; APK SHA-256 is recorded in `AURA_INTEGRATION_HANDOVER.md`. |

Combined: 19 app tests and 2 voice-library tests passed, 0 failures. The tests do not exercise Android microphone behavior, background wake, actual spoken acknowledgement, acoustic interruptions, or live cloud fallback.

## Manual device checks

Verified on the Honor 90 Lite:

- An earlier build from this same branch installed in place after backing up app data. Package, version and signer matched; pulled installed APK hash matched that earlier build. Launch was observed without a fatal exception.
- The latest APK also includes the spoken pre-action acknowledgement change and is not installed because ADB is disconnected.

Still pending on the physical phone (ADB is currently disconnected):

- Wake phrase recognition and opening Aura from the background.
- Speech capture/transcript in the active app.
- A complete spoken answer after an “I’m on it” acknowledgment.
- Multi-turn conversation and recovery after long pauses.
- Interrupting TTS and resuming recognition.
- Same voice after app restart, and Austrian-accent voice availability on the device.
- Local Ollama connectivity from the phone (PC endpoint itself responds and lists both Qwen models).
- Exact single “I'll be back” spoken before an actionable voice command, followed by the task executing once.

Do not mark pending checks as passing until tested on the actual phone. The user previously reported “I'm on it” followed by a misunderstanding. The direct chat path no longer exposes partial model output as a completed answer, but the real-phone voice flow still needs acceptance testing.


## 2026-11-05 — Spoken-action gate (automated only)

- Added tests for acknowledgement-before-execution, fail-closed execution if speech acknowledgement fails, and bypass for ordinary questions. Combined app and voice-library JVM reports: 22 passed, 0 failed/errors/skipped across 8 suites.
- No physical-phone speech test or APK install occurred. The exact selected voice remains unverified on the disconnected Honor; this test checks orchestration ordering only.


## 2026-11-05 — Fresh rerun

- Forced the app and `aura-voice` JVM unit suites (`--rerun-tasks`): 22 passed, 0 failed/errors/skips, 8 suites; Gradle completed in 10m02s with 50 tasks executed. This verifies JVM policy/order tests only, not live recognition, barge-in, or Austrian voice playback.


## 2026-11-06 — Provider and WhatsApp regression checks

- Added provider router tests for cloud-first behavior, local fallback after cloud failure, cooldown suppression, recovery to cloud on a later turn, incomplete cloud settings, and caller cancellation. Added MockWebServer checks for OpenAI-compatible request/auth parsing, HTTPS enforcement, and error-body redaction.
- Added WhatsApp draft URL/phone validation tests. The tool only opens a prefilled draft; it does not send.
- Combined app and voice-library JVM result: 32 tests passed, 0 failures/errors/skips across 11 suites. Full debug APK assemble also passed.
- No live provider call, phone-originated Ollama call, WhatsApp app launch/send, or speech/wake/interruption/TTS test was performed. Honor is disconnected and Ollama is stopped. No APK installation occurred. Do not treat the automated checks as device acceptance.


## Commit record — 2026-11-06

- Cloud/local routing, WhatsApp draft tool, tests, and updated project docs committed as 45eaf56dc6caf629927c6ba0e23319e2f2351d11 (Add cloud fallback and WhatsApp draft actions) on integration/aura-voice-consolidation.
- Prior spoken-action gate is b93d67d after rebasing on the latest remote audit commits. Branch has not been merged to main. The debug APK is not installed; Honor tests remain pending.
## 2026-11-06 — Daily cloud quota regression checks

- Added tests for daily UTC quota increment, cap enforcement, date rollover and bounds, plus router fallback to local when the quota is reached.
- Full Gradle command (:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug) passed in 12m20s. 35 tests passed, 0 failures/errors/skips, 12 suites. APK assemble passed. git diff --check passed.
- This is automated JVM/build verification only. No phone test, live cloud provider call, Ollama call, or WhatsApp launch/send was run. No install was performed.
- APK package/version/signature and SHA-256 are recorded in AI_HANDOVER.md; the handset remains disconnected. Do not treat this as device acceptance.
## Commit — 2026-11-06

- Daily quota logic, local-fallback reason, Settings control and tests committed as f4a2e0a9d5cb50c2ee97f084f9e6209e3e2593b6 on integration/aura-voice-consolidation. 35 JVM tests and APK build passed; no phone install/device check occurred.
## Publication — 2026-11-06

The quota tests and handover are on origin/integration/aura-voice-consolidation at 8c332e49254fa4d40a372794202bf8ca61c1e2e1. main unchanged; the debug APK is not installed.
## 2026-11-06 — Speech recognizer lifecycle regression tests

- Fixed Android speech provider resource handling: a new recognition turn destroys the prior recognizer, stop/close invalidate old callbacks and release the recognizer, and asynchronous setup failures are reported to the caller.
- Added JVM tests for stale-request invalidation. Gradle app and voice tests plus debug assemble passed; 39 tests, 0 failures/errors/skips across 14 suites. APK package/signature/hash are recorded in AI_HANDOVER.md.
- This checks the request-generation guard and compilation, not Android AudioRecord exclusivity or physical microphone behavior. No device was connected, installed, or tested. Wake/transcript/TTS/barge-in acceptance remains pending on the Honor.
## Commit — 2026-11-06

- Android speech recognizer lifecycle fix and stale-callback tests committed as e3d86b3215ca67a88f595a77e01dc8946255a1f3. This is automated-only verification; no phone install or real-mic test.
## Speech repair branch publication — 2026-11-06

The speech provider lifecycle fix and test record were published to integration/aura-voice-consolidation. At verification remote HEAD was 12af8d08565847d406672fa4741b77c4123f3. No install was performed.

## Transcript unit/build checks — 2026-11-06

- Added `TranscriptSearchTest`: persisted-message mapping, blank search preserving order, case-insensitive filtering. Result: 3 added tests pass.
- Combined app + `aura-voice` JUnit reports: 42 tests, 0 failed, 0 errors, 0 skipped, 15 suites.
- Debug APK assemble passed in the same Gradle invocation. APK is uninstalled. Build evidence does not verify Android microphone/audio behavior; phone remains disconnected.
- `git diff --check` passed.

## Commit — 2026-11-06

Transcript feature and the 42-test/build record are committed as `d0e8e9c` and published with the handover commit `1ae7c1c`. No device installation or physical voice test was performed.

## Publication verification — 2026-11-06

The transcript feature commit `d0e8e9c` and its handover commit `1ae7c1c` are published on `integration/aura-voice-consolidation`; the post-push remote head was verified as `1ae7c1c99562b4c00ca767e2f57ade323202bf68`. No phone installation or physical voice test was performed.

## PC backend check — 2026-11-06

A PC-side direct (no proxy) GET to `http://192.168.8.18:11434/api/tags` returned `qwen3:1.7b` and `qwen3:4b-q4_K_M`. This is not an Android phone test and did not run inference. Honor remains disconnected.

## ElevenLabs transport regression checks — 2026-11-06

- Focused JVM suite for the ElevenLabs transport passed: voice names/gender/accent mapping, preserved provider voice ID, speech request path/body/auth header, absent-key no-request behavior, and provider error redaction. No real API key or provider request was used.
- Full app and voice-library unit test suites and debug APK assemble passed: 41 tests, 0 failures, 0 errors, 0 skipped across 14 suites. APK signature verification passed. Details and APK hash are in AI_HANDOVER.md.
- No audio playback, voice preview, Austrian accent, microphone, wake, conversation, barge-in, local Ollama, or cloud inference test occurred on a phone. The Honor is disconnected and no APK was installed.


## Commit record — 2026-11-06

Implementation commit: 3eb89d5cdb1b8c634797c971dcc43527188b317e (Add optional ElevenLabs voice provider) on integration/aura-voice-consolidation. It includes provider code, tests, and project records. Main unchanged; device-backups/ was excluded.

## Branch publication — 2026-11-06

GitHub origin/integration/aura-voice-consolidation was pushed and verified at 50bb1ed4d404a42454f57355931779b12e413bd0. This includes implementation commit 3eb89d5cdb1b8c634797c971dcc43527188b317e and documentation commit 50bb1ed4d404a42454f57355931779b12e413bd0. Main was not updated. The existing device-backups/ folder remains untracked and excluded.

## Standby wake restart unit/build check — 2026-11-06

- Added policy tests for wake enabled/disabled, permission granted/denied, and whether a hands-free session is active: passed.
- Full app reports: 42 tests, 0 failures/errors/skips across 15 suites; debug APK build succeeded. No `aura-voice` test XML was present. APK package/signature verified, SHA-256 `C3659671B0E42BB488E8CFC3E87075CB74D1106C4B1ABD1E5A84C4EB6F317DA4`.
- Not a device test. Honor disconnected and APK not installed. Wake service restart at runtime, microphone capture, transcript, speech, interruption, local/cloud inference and WhatsApp remain pending device verification.
- Implementation and test records are committed as `04fd7a4ad8e3927b2f6d39271fe83b4afc57d61c`, pushed to and verified on `integration/aura-voice-consolidation`; `main` was not changed.
- Additional race guard blocks a delayed wake-listener restart if a hands-free session has already restarted. Rerun: app JUnit XML 42 passed, 0 failures/errors/skips across 15 suites; Gradle build successful; voice-library unit task up-to-date. Latest APK SHA-256 `3C5BA73F4582281AC2A27BAEB17B3BD008E68F068566438E876CBDB12CAAD511`; signature/package verified. Honor still disconnected; no device result.
- Final code and test records are published as `b33b06dd8832893d8f6429b074933f7b0fce2e70`; main was not changed.

## Cloud-only chat gate regression — 2026-11-06

- Added cases for cloud-only readiness without local model, cloud-first with local fallback, local-only readiness, and incomplete cloud credentials. Focused Android unit test passed; full app and voice-library suite/build passed: 50 tests, 0 failures, 0 errors, 0 skipped.
- APK package/signature verification passed; SHA-256 `C07912A9B18D9D8827C9ABE427DB23A46B8F3A5F49AED8B69BD55F221647581B`.
- Not a live provider or device test: no cloud credential was used, no Ollama inference was run, and Honor is disconnected. Voice, wake, mic, interruption, and WhatsApp acceptance remain pending.

## Honor install and launch check — 2026-11-06

- Honor reconnected. Existing `com.aura.ai` app data archive created before update (997,376 bytes; SHA-256 `B7604B79A17CDFA52937361F7C9DD0E081AC7B6A350C70887C4A458A388F043A`). Existing and new APK package/version are both `com.aura.ai` 1.0.0/code 1; signer SHA-256 matched `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`.
- Installed the new APK in place with `adb install -r` (Success); did not uninstall or clear app data. MainActivity started and resumed. No fatal AndroidRuntime error matched in recent logcat.
- Launch is verified, not voice acceptance. User's spoken test is pending; no live mic, wake, interruption, model inference, or WhatsApp action has been verified.

## Hands-free overlap and build verification — 2026-11-06

- Automated: focused voice-library tests passed. Full `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` passed: 56 tests, 19 suites, zero failures/errors/skips.
- Added tests: a stable distinct partial interrupts after confirmation; likely TTS echo is ignored; a new user phrase after echo is accepted; contractions and similar phrases are normalized; simultaneous listen/speak publishes the combined state; ordinary listen still interrupts TTS.
- APK assembled and v2 signature verified: `com.aura.ai` v1.0.0/code 1, SHA-256 `D9607C66F0835DFD73E1F77FA813BA1463A8E5405C46603A8411426E704048FE`, signer SHA-256 `1b9b1ddda2f2df8b0a36c8b479abd809e6ac312cb01b3c59fcc9f375f7a27ef3`. Not installed.
- Real device: no ADB performed per instruction. Wake capture, actual ASR transcript, full-duplex audio, TTS echo, interruption latency, local/cloud network calls, and WhatsApp launch remain untested on Honor.

## Wake recognizer lifecycle/build verification — 2026-11-06

- New JVM tests confirm that starting a replacement wake request and invalidating a stopped request reject old callbacks.
- Full app/voice test and APK build passed: 58 tests / 20 suites, no failures, errors or skipped tests. APK v2 signature verified; SHA-256 `A242340DC1003BE359FC52F66638FC0884A284DF265A4F09E363054589BABFA4`.
- No ADB or physical wake/microphone test was run. This verifies callback-generation logic, not actual wake phrase detection, background-service survival, speech recognition quality or audio behavior.

## Wake-triggered Activity mic handoff — 2026-11-06

- Two unit tests cover that a normal Activity launch may start opted-in standby listening while a wake-triggered launch must not start a competing standby listener.
- Full Android + voice run passed 60 tests with zero failures/errors/skips; APK package/signature verified. No ADB used, so actual phone mic ownership and conversation behavior are pending.

## Offline project continuation — 2026-11-06

- Rechecked existing app and voice-library reports: 71 tests across 23 suites, zero failures/errors/skips. The Gradle invocation reported all tasks up-to-date, so these are existing report results, not a forced fresh test run.
- APK assemble task succeeded/up-to-date and signature/package were checked. No phone, microphone, wake phrase, TTS, interruption, Ollama request, cloud request, or WhatsApp intent was exercised.
- Device acceptance remains pending: wake listener uses Android `SpeechRecognizer`; real handset mic ownership, ASR accuracy, Austrian voice availability, continuous turns and barge-in have not been established by this offline check.

## On-device recognizer preference — 2026-11-06

- Added policy coverage for selecting the dedicated on-device recognizer only on API 31+ when available; policy tests passed as part of the 73-test app/voice suite. The wake request also asks Android to prefer offline processing.
- Build and debug APK assembly passed. This does not confirm the Honor has an on-device en-ZA model, and Android permits the generic recognizer to ignore the offline hint.
- No real-device test: Honor disconnected; no microphone capture, wake phrase, speech answer, interruption or live backend check. Keep the APK uninstalled until reconnect and explicit install authorization.

## On-device language error recovery — 2026-11-06

- Added a policy case that uses the system recognizer only when an active on-device recognizer reports an unsupported/unavailable language. The updated app/voice reports contain 74 passing tests; APK build and signature verification passed.
- No handset test. Honor's on-device service and en-ZA pack are not checked, the APK remains uninstalled, and system recognition may use a network service.

## Offline quick-answer path — 2026-11-06

- `SimpleTimeIntent` answers supported local-time questions from the device zone, and `LocalAnswerIntent` keeps arithmetic available. The chat can route those deterministic queries without a selected model; other requests are unchanged.
- Automated checks: 76 app + 10 voice-library reports, 86 tests with no failures/errors/skips; debug APK built and signed. APK SHA-256 is in `AI_HANDOVER.md`; it is not installed.
- On reconnection, test time/arithmetic in hands-free mode while backends are disconnected; then test backend recovery, voice turns, interruption, Austrian voice selection and WhatsApp draft. Do not treat unit tests as phone acceptance.

## Voice tuning verification — 2026-11-06

- Added persistent Android-only pitch and pace controls in a bounded, natural range. Cloud voice providers retain control of their own voice settings. New unit tests verify that tuning preserves the exact selected voice ID and clamps the range.
- Automated verification: full app/voice suite reports 88 tests passed, 0 failures/errors/skips; a signed debug APK was built. Its hash is in `AI_HANDOVER.md`; it is not installed.
- On Honor reconnect, test slider persistence across reopening settings and preview the selected voice. Confirm the accent audibly; the code cannot produce an Austrian accent if the installed/authorized voice does not have one.

## Wake retry policy test — 2026-11-06

- Wake-service silence and no-match outcomes now restart after 800 ms; actual recognizer failures retain capped backoff. Two tests cover normal idle and repeated failures.
- Full app/voice test reports: 90 total, zero failures/errors/skips; APK built and signature verified. Not installed; no acoustic device test. APK hash is in `AI_HANDOVER.md`.
- On Honor reconnect, verify wake phrase after several seconds of silence and confirm any platform mic-sound behavior. Also repeat wake-to-chat, long answer interruption, and backend/action checks.

## No-backend spoken status — 2026-11-07

- The chat composer and hands-free control stay enabled once conversation history is ready, even when no AI provider is configured. The orchestrator returns a clear setup status instead of silently dropping the spoken or typed request; the normal optional voice-reply path can read it aloud. No tool/action runs without an available model.
- Automated: app/voice reports show 91 tests across 31 suites, all passing; debug APK assembled and signer/package checked. APK is not installed.
- Device test pending: Honor disconnected. Verify the clear status appears in the transcript and is spoken when voice replies are enabled, then confirm adding a provider restores normal replies. Do not infer phone playback from JVM/build results.

## Recognizer no-progress recovery — 2026-11-07

- The Android speech provider now has a 30-second watchdog reset by speech-start and partial-result callbacks. A stalled request becomes a recoverable network-style voice error; completion/stop/replacement cancels the timer. Three policy boundary/progress tests passed.
- Full app/voice test reports: 94 tests / 32 suites, all passing. Debug APK assembled and signature/package verified, not installed.
- Honor is disconnected, so actual recognizer callback timing, long pauses, transcript quality, automatic retry, wake, playback, and interruption remain pending. On device, test normal phrasing with pauses shorter than 30 seconds and verify a deliberately unavailable/stalled recognizer produces a visible error and resumes listening without a long gap.

## Low-confidence action utterance — 2026-11-07

- Automated policy coverage: 3 tests passed. Valid confidence `< 0.45` blocks only agent-action utterances; confident action and ordinary chat pass through; missing/out-of-range scores do not cause guessed classification. Full app/voice run: 97 tests across 33 suites, all passed; APK assembled and signature/package checked, not installed.
- Honor test pending because no ADB device is connected. Specifically verify transcript/clarification, no action invocation on low-confidence utterance, listening resume, and no repeat prompt on a confident instruction. Then calibrate the threshold against actual Android confidence values.
- Source and verification notes are in pushed integration commit `8efb987ea687283edfbd71aff6d11bc86fa70294`; production `main` was not changed.


## Cloud cancellation regression — 2026-11-07

- Added test `callerTimeoutDoesNotStartLocalFallbackAfterCancellation` and a router guard using the parent coroutine activity state.
- Result: NOT RUN. Gradle compilation previously failed because disk space was exhausted; current free space is 967 MB. No test pass is claimed.
- Device: Honor disconnected. No voice, Ollama/cloud, interruption, wake, or WhatsApp real-device run in this checkpoint.


## Verification follow-up — 2026-11-07

- Focused test: `ProviderChatRouterTest.callerTimeoutDoesNotStartLocalFallbackAfterCancellation` — PASSED.
- Full `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` — PASSED; 101 tests, 0 failures/errors/skips, 34 XML reports. APK package/signature verification passed; SHA-256 `D755ADDBD94D23A707016DF23421CE9BA7B579CBF64F2CB4A9A86BD3BBF6B716`.
- The build was from a dirty local checkout with additional uncommitted settings/voice edits, not a verified clean remote tip. Phone disconnected; all acoustic, wake, interruption and live provider tests pending. No installation performed.


## Exact acknowledgement rebuild — 2026-11-07

- `:app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug`: passed; 101 tests, 0 failures/errors/skips; APK package and signature checks passed.
- APK SHA-256 `29BC3FA4F5C1E4255CED20C7685BF8C8FD68156757CC63A38C8FB4075D81FCF9`. Action acknowledgement text is exact `I'll be back`.
- Built from local branch plus uncommitted style-control changes; not installed. Honor disconnected. Real device voice/accent, wake, interruption, local/cloud and WhatsApp tests remain pending.


## Honor recognition retest — 2026-11-07

- **FAIL / diagnosis incomplete:** owner reports incorrect transcript after the wake phrase.
- ADB showed original package `com.aura.ai` foreground, separate debug app absent. Exact words, confidence, response latency, model answer, and spoken answer were not captured before the phone locked.
- Ollama model discovery passed for `qwen3:1.7b` and `qwen3:4b-q4_K_M` at `http://192.168.8.18:11434`; this is not chat-generation verification. App/voice unit suites passed (104, zero failures/errors/skips); APK in-place install signature/package/hash were verified after backup.
- Next test: capture the Agent screen's exact `Recognized (language, confidence): transcript` diagnostic, then assess the ASR result. No recognition settings were changed without evidence.

## Publication metadata — 2026-11-07

- Source and test changes, plus the Honor verification record, were published to this integration branch in commit `74848b6029db3368b01f27cb8dda997d12695740`. Files: `SettingsRepository.kt`, `SettingsScreen.kt` (Ollama endpoint text), `OllamaEndpointPolicyTest.kt`, and all four continuity documents. Main was not changed; device backups were excluded.
- The commit records 104 passing unit tests and a verified in-place APK install. The owner's latest recognition result is still failed and exact transcript capture is pending; no device voice pass is claimed.
- Workspace branch remains `integration/aura-voice-consolidation`, local HEAD `d5d0fe92ca890c964685403aee0fcafd3fe30fd6`; local Git metadata is protected, so no local branch mutation was performed.


## Wake diagnostic build — 2026-11-07

- Added local display of the full raw wake transcript and two unit tests. This improves transcript observability only.
- 106 app/voice tests passed; debug APK build, signer and package verification passed. Updated the original app in place after app-data backup.
- Honor remained locked; no live acceptance was claimed. Exact transcript and spoken answer await owner retest.


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


## Latest voice evidence — 2026-11-07

- Honor test: owner reports an incorrect transcript after saying “Yo, yo, yo, what is five plus five?” Exact transcript and confidence were not captured. Treat this as a failed/indeterminate recognition acceptance check, not a pass.
- Device/ADB is disconnected. No retest, install, microphone, wake, TTS, or interruption verification was performed in this session.
- Source inspection: wake listening currently uses Android `SpeechRecognizer`; no Vosk dependency, engine, or model was found. Dedicated wake-word detection is unverified.
- Automated tests for the current cloud-default change did not finish locally because the C: drive reached 0 bytes free. GitHub Actions run `42344494193` and its retry ran no steps and provided no test logs. Earlier passing test counts apply to earlier commits only.
- Next: after space is available and the Honor reconnects, rebuild, verify/back up before approved in-place installation, capture the actual displayed transcript/confidence and spoken response, then repeat multi-turn and interruption checks.


## Published transcript diagnostics — 2026-11-07

- Branch commit `db11eedcbea3ac0affd606a5a7fd994f90fa0b1a` carries confidence and alternate recognizer hypotheses into visible wake and conversation diagnostics, plus the low-confidence command guard.
- Local pre-publication JUnit report files show 95 app + 17 voice-library tests, 0 failures/errors/skips. These results predate the three-way integration commit and are not a fresh test run of that commit.
- Local artifact SHA-256 `9F7A8D184147C017A21AEB43FEB2C8C2577A1070E7CF6FDAF5661DB5C116C71D` is package `com.aura.ai` v1.0.0/code 1 and signature-valid; it is not verified as a build of current remote head and was not installed.
- Current-turn Gradle rerun failed before test execution at `:app:checkDebugAarMetadata` due a Gradle null-path exception in offline dependency resolution. ADB lists no device. Owner reports incorrect transcript; exact recognized text/confidence remain unknown.
- No wake, recognition, TTS, interruption, Ollama/cloud, or copy-button behavior was device-tested for the published commit. Continue to mark voice acceptance as incomplete.


## CI verification gap — 2026-11-07

- GitHub Actions run `42346749653` for source commit `db11eedcbea3ac0affd606a5a7fd994f90fa0b1a` failed before any job step; job returned no steps or logs. No automated voice/build result was produced for that commit.


## 2026-11-12 wake-tail continuation

- Added a tested incomplete-fragment guard; Aura continues listening and merges the next segment rather than sending “I” or “what is” as a full request.
- Focused policy test passed (3); full app/shared-voice report totals: 99 + 18, zero failures/errors/skips; APK assembly passed.
- Device result: owner reported transcript “I” and no final spoken answer. This is a fail/unresolved, not device acceptance. Fresh APK SHA-256 `BA1F1E4F17640D83DE43E8208A5A2B94EED4886EB21EEE4E71BA951E5EFA7471` remains uninstalled.
