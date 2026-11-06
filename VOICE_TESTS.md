# Voice verification

Updated: 2026-11-05

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
