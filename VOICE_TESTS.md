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

Transcript feature and the 42-test/build record are committed locally as `d0e8e9c`. No device installation or physical voice test was performed.
