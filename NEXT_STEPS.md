# Aura: remote-first development queue (2026-11-05)

## Current queue update — 2026-11-06

- Added agent tools for opening an installed app or web page and preparing a call/SMS draft. Automated tests and APK assembly pass; APK remains uninstalled while the Honor is disconnected.
- Added contact-name lookup and preparation of a call/SMS draft for a uniquely matched contact. Existing `READ_CONTACTS` permission is required. The legacy action flow now also opens the native confirmation screens instead of directly calling or sending.
- Added a Settings folder picker and bounded filename search/open tools scoped to the selected read-only SAF folder. Unit/build checks pass; picker grants and file opening still need Honor validation.
- Continue device-independent code work on the existing integration branch. Revisit voice-turn reliability and backend routing, then test after reconnect: file-folder grant/search/open/revoke, Contacts permission and ambiguous matches, app/browser/dialer/SMS/WhatsApp intents, wake and repeated conversation, interruption, phone-to-PC Ollama, and live cloud fallback/recovery.
- Do not install without the previously required package/signature check, app-data backup, and explicit build approval. Do not merge `main`.

**Owner unavailable for phone/PC testing. Do not block software development on device access.** Work on `integration/aura-voice-consolidation`; do not merge `main` without approval. Preserve app data, recordings, models, voice choice, and checkpoints.

## Mandatory execution order: AUDIT AND INTEGRATION FIRST

1. Audit all eight named repositories and the current Aura integration branch, documenting reusable components, dependencies, duplicates, incompatibilities, and missing features.
2. Integrate suitable code into the protected integration branch, retaining the existing architecture and avoiding wholesale merges of unrelated applications.
3. Build and run automated tests; fix regressions and document evidence.
4. Only after the audit and integration, proceed with additional cloud routing, TTS, and voice refinements below.
5. Defer physical-device tests until the owner is available. Do not block other work on device access.

## Work that can proceed without the phone

1. Implement a provider-neutral inference router with cloud-first routing, local Ollama fallback, timeouts, circuit breaker/retry limits, health checks, and recovery to cloud. No invented credentials; provider adapters should be configurable and fail safely if unconfigured.
2. Add unit/integration tests with mocked transports for cloud success, cloud timeout, 401/429, local fallback, recovery, both providers unavailable, cancellation, and no duplicate TTS responses. Run Gradle tests and APK build in an available build environment; report exactly what ran.
3. Audit the spoken action state machine: say exactly one `I'll be back` only before spoken actions, wait for TTS completion before executing, and ensure normal questions respond directly. Test interruption and state transitions without a phone.
4. Audit selected TTS persistence and prevent silent switching. Do not claim Austrian T-800 voice until a licensed voice provider and matching voice ID are configured and verified.
5. Audit eight repository integrations for reusable code and API contracts; do not merge unrelated web projects wholesale. Prioritize compatible Android voice functionality and well-scoped backend adapters.
6. Improve transcript logging and error observability without recording private speech or secrets by default. Maintain a readable history and handover with exact commit SHAs, tests, and unresolved blockers.

## Deferred until the Honor or PC is available

- Install latest signed APK in place; verify package/certificate and preserve app data.
- Test microphone permissions, wake detection, live transcript, audio playback, selected Austrian voice, barge-in, Android mic tones, and multi-turn sessions.
- Verify actual phone-to-PC Ollama connection and cloud credentials, if supplied.

**Acceptance rule:** A successful compilation or mocked test is not proof of a working physical-device experience. Do not claim completion until both automated and device acceptance pass.

## Execution handoff

Continue the queue above in a code-capable workspace without waiting for the owner to return to the computer. Record progress and blockers in `AURA_INTEGRATION_HANDOVER.md` after each meaningful change. If a provider key or license is missing, build/test the interface with mocks and flag the integration gate rather than stopping all other tasks.

1. Reconnect/unlock the Honor and install the latest APK in place. Test a spoken action: hear exactly one “I'll be back” before execution, then confirm the action and final response; also test that “what is five plus five?” gets an immediate answer without the acknowledgement.
2. On the phone, verify selected voice is unchanged and the acknowledgement uses it. Do not claim Austrian T-800 voice until the actual installed voice matches; current persisted Android ID is `ar-language`.
3. On the phone's Settings screen, test the saved PC endpoint `http://192.168.8.18:11434`, discover both Qwen models, and confirm one responds. The PC endpoint and local generation passed from Windows.
4. Configure and implement a real cloud provider only after an endpoint and credential are available to this app. Add encrypted key storage, usage limits, failover/recovery/offline tests; current code has no cloud chat transport.
5. Replace platform standby recognition with a maintained on-device wake-word/VAD engine and test interruption, echo suppression, false activations, and a five-minute conversation on the Honor.
6. Publish the verified changes and handover on `integration/aura-voice-consolidation`; keep `main` untouched.


## Spoken-action gate follow-up — 2026-11-05

- Record the code commit SHA here and in AI_HANDOVER.md/PROJECT_STATUS.md after commit; keep `device-backups/` unstaged.
- When the Honor reconnects, test the exact spoken action sequence and confirm acknowledgement completes before tool execution; separately test an ordinary question.
- Highest outstanding implementation: add configured cloud-first model routing with tested Ollama fallback/recovery, then wire secure settings and verify without logging credentials.
- The current `ar-language` TTS ID is not the requested Austrian male voice. Preserve it until an authorized compatible voice and its identity are configured; then test preview, persistence, and spoken turns on-device.


## Commit record — 2026-11-05

- Code and test commit: `b93d67d (rebased commit; previous local hash ec7a312 was replaced during safe integration)` (`Gate spoken actions on acknowledgement completion`) on `integration/aura-voice-consolidation`. Production `main` unchanged. `device-backups/` remains untracked and was excluded.


## Updated implementation checkpoint — 2026-11-06

Completed in code and JVM verification:

- Cloud-first OpenAI-compatible chat routing with Ollama fallback, timeout handling, 30-second failure cooldown, recovery retry, encrypted key input, and chat-trace backend status.
- WhatsApp draft preparation through the installed standard WhatsApp app; user must review and send manually.
- 32 app/voice unit tests pass, 0 failures/errors/skips, 11 suites. Debug APK assembled and signature/package verified; details are in AI_HANDOVER.md.

Next implementation and acceptance:

1. Add a user-configurable daily cloud request/spend limit; current per-request cloud output is capped at 768 tokens, but no daily budget exists.
2. Configure the user's provider endpoint/model/key in Aura Settings (key stays on-device). Run actual cloud success, failure-to-Ollama, cooldown, and recovery checks; do not infer them from mocks.
3. When Honor reconnects, request authorization for this exact APK before installing. Preserve/verify existing package, signer, and app data; test voice, wake, interruption, phone-to-PC Ollama, and WhatsApp draft opening. Sending must remain a user-confirmed tap.
4. Verify the one selected TTS voice. Current saved ID is ar-language, not Austrian. Do not label the Austrian voice complete until the licensed engine and voice ID are available and tested.
5. Keep main unchanged; update handover with the actual phone/provider results, unresolved items, and commit IDs.


## Commit record — 2026-11-06

- Cloud/local routing, WhatsApp draft tool, tests, and updated project docs committed as 45eaf56dc6caf629927c6ba0e23319e2f2351d11 (Add cloud fallback and WhatsApp draft actions) on integration/aura-voice-consolidation.
- Prior spoken-action gate is b93d67d after rebasing on the latest remote audit commits. Branch has not been merged to main. The debug APK is not installed; Honor tests remain pending.
## Implementation update — 2026-11-06

- Implemented the configurable daily cloud inference request cap (60/day default; 1–1,000 setting; atomic persisted UTC-day accounting; local Ollama fallback after the cap). This is a request-count cap, not a cost/spend cap. Added quota and fallback tests.
- Latest Gradle verification passed: :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug, 12m20s, 35 tests/0 failures/0 errors/0 skipped across 12 suites. APK assembled and identity/signature verified; details in AI_HANDOVER.md.
- Next: finish code/doc commit and branch push, confirm the remote branch head; then, only after phone reconnects and approval for this exact build, test actual voice/wake/interruption, phone-to-PC Ollama, WhatsApp draft opening, and configured cloud operation. Do not install while disconnected. Austrian voice remains unavailable/unverified. Do not merge into main.
## Quota implementation commit — 2026-11-06

- Code and the current handover records committed as f4a2e0a9d5cb50c2ee97f084f9e6209e3e2593b6 on integration/aura-voice-consolidation. Verify branch publication and continue with live provider and phone acceptance only when resources reconnect.
## Branch publication confirmation — 2026-11-06

The implementation and handover are published on integration/aura-voice-consolidation at remote commit 8c332e49254fa4d40a372794202bf8ca61c1e2e1. Main stays unchanged. Device testing remains pending until the phone reconnects.
## Offline voice work — 2026-11-06

- Fixed a code-level recognizer leak/conflict: listening turns now replace and destroy the previous Android SpeechRecognizer, stale callbacks are invalidated, and permission/setup failures are surfaced. Added JVM tests for stale callback protection.
- Build and automated result: 39 tests passed, 0 failures/errors/skips, 14 suites; APK rebuilt and signature/package verified. APK hash recorded in AI_HANDOVER.md. Physical phone tests were intentionally not run.
- Next: commit/publish this repair, then after phone reconnect and approval, test microphone open/close across repeated turns, wake phrase, accurate transcript, TTS response and interruption. Continue cloud-provider live checks only after configuration and Ollama recovery testing when its service is available.
## Voice lifecycle commit — 2026-11-06

- Recognizer lifecycle repair and test docs committed as e3d86b3215ca67a88f595a77e01dc8946255a1f3 on integration/aura-voice-consolidation. Next verify remote publication and keep physical phone checks pending until reconnect/approval.
## Speech repair published — 2026-11-06

The speech adapter fix and test/handover commits are on the integration branch; publication verified through 12af8d08565847d406672fa4741b77c4123f3. Continue with code-side work while the phone is offline; do not install until it reconnects and the exact build is approved.

## Next — after transcript integration (2026-11-06)

1. Commit the transcript persistence/search/copy code, its three unit tests, and the current handover updates on `integration/aura-voice-consolidation`; publish that branch and verify the remote head. Keep `device-backups/` unstaged and leave `main` untouched.
2. When the Honor reconnects, verify package/version/signature and back up existing app data before asking to install this exact APK. Check saved transcript load, search, and copy.
3. On the phone, repeat wake phrase, tap-to-speak, multi-turn recognition/TTS, interruption, and recovery checks. Record actual outcomes in `VOICE_TESTS.md`.
4. With user-configured cloud endpoint/model/key, test cloud success/failure/recovery; with Ollama available, test phone-to-PC model discovery and fallback. Verify WhatsApp draft opens and requires manual review/send.
5. Keep limitations explicit: Austrian TTS is not configured; saved `ar-language` remains; a dedicated wake detector and acoustic barge-in remain unverified; no production merge/install without approval.

The transcript feature and test records are committed as `d0e8e9c`; publication to the integration branch was verified.

## Publication verification — 2026-11-06

- The integration branch was verified at `1ae7c1c99562b4c00ca767e2f57ade323202bf68` immediately after the code and first handover commits were pushed; `main` remains `4b48185e9f110764938f10548f80a29acda674f7`. The code and handover pushes are complete; continue with the listed Honor/provider checks when available.

## Local Ollama check — 2026-11-06

PC-to-Ollama GET `/api/tags` works when bypassing the configured proxy and lists both Qwen models. Do not change the user's proxy/firewall. After the phone reconnects, test model discovery from Aura on Honor; separately test cloud only after credentials are configured.

## Continue cloud TTS and phone acceptance — 2026-11-06

1. Keep the existing Android voice selected until the owner configures a key, loads voices and selects a specific ElevenLabs voice in Aura settings. Then verify the preview on the Honor; do not claim an Austrian accent before listening.
2. When the Honor reconnects, verify package/version/signature and back up current app data before requesting approval to install this exact debug APK.
3. Test Tap to Speak, Yo, yo, yo wake, transcript capture, repeated turns, interruption, voice output and recognizer recovery on the physical phone.
4. Separately configure provider credentials and validate cloud TTS/reasoning and local Ollama fallback/recovery. Review the WhatsApp draft tool on device; sending remains a user action.
5. Never install while disconnected, claim device acceptance from JVM tests, merge to main, or stage device-backups/.


## Commit record — 2026-11-06

Implementation commit: 3eb89d5cdb1b8c634797c971dcc43527188b317e (Add optional ElevenLabs voice provider) on integration/aura-voice-consolidation. It includes provider code, tests, and project records. Main unchanged; device-backups/ was excluded.

## Branch publication — 2026-11-06

GitHub origin/integration/aura-voice-consolidation was pushed and verified at 50bb1ed4d404a42454f57355931779b12e413bd0. This includes implementation commit 3eb89d5cdb1b8c634797c971dcc43527188b317e and documentation commit 50bb1ed4d404a42454f57355931779b12e413bd0. Main was not updated. The existing device-backups/ folder remains untracked and excluded.

## After wake-listener lifecycle repair — 2026-11-06

1. The wake resume policy, unit test and handover records are committed as `04fd7a4ad8e3927b2f6d39271fe83b4afc57d61c` and published on the integration branch; `main` unchanged.
2. When Honor reconnects, verify APK package, version, signer and backup requirements before installing the approved build. Test standby wake restart after session stop with wake enabled/disabled and microphone permission granted/denied.
3. Run physical tests for repeated “Yo, yo, yo” wake, tap-to-speak, transcript accuracy, multi-turn speech, barge-in, audio output, Ollama from phone, cloud fallback/recovery, and WhatsApp draft review. Record only observed results.
4. Continue code work without ADB while disconnected; do not install, merge to `main`, or claim device acceptance.

Latest code change adds a race guard for the asynchronous wake restart. It was rebuilt and verified with 42 app tests passing. Commit `b33b06dd8832893d8f6429b074933f7b0fce2e70` is published on the integration branch. The resulting APK remains uninstalled pending phone reconnection.

## Continue cloud-first chat verification — 2026-11-06

1. The cloud-only chat gate is fixed and unit-tested; with the owner's configured provider key, verify chat without a local model and run a cloud-only reply.
2. Verify cloud-first routing, local Ollama fallback, and recovery with actual provider calls; no live provider test was run here.
3. When Honor reconnects, verify package/version/signer and back up accessible app data before installing the newly built APK; then run the pending real-device voice, interruption, local/cloud, and WhatsApp draft tests.
4. Keep work on `integration/aura-voice-consolidation`; do not merge to `main` or stage `device-backups/`.

## Honor is reconnected — 2026-11-06

1. Await the user's result from one spoken arithmetic turn on the updated app. If it fails, capture the visible transcript/status and relevant logcat before changing voice or provider code.
2. Test follow-up turn, wake resume, interruption, Ollama model discovery/inference, cloud-only/fallback/recovery, and WhatsApp draft review with the user present.
3. Preserve `device-backups/`; do not stage it or merge this branch into `main`.

## Continue without phone access — 2026-11-06

1. Voice monitor/barge-in source, tests and records (`6e2a221`, handover record `ac8c9bc`) are pushed to `integration/aura-voice-consolidation`; continue only on this branch and exclude `device-backups/`.
2. Keep the fresh APK at `app/build/outputs/apk/debug/app-debug.apk` uninstalled (SHA-256 `D9607C66F0835DFD73E1F77FA813BA1463A8E5405C46603A8411426E704048FE`). Do not use ADB while the phone is disconnected.
3. Once Honor reconnects, back up accessible Aura data and confirm package/version/signer before obtaining approval to install this exact APK. Then test continuous turns and barge-in while a longer reply is speaking.
4. Verify cloud-first operation, Ollama fallback/recovery, and WhatsApp draft opening with actual configured services/device. None was live-tested during this build.

## Wake listener follow-up — 2026-11-06

1. Wake recognizer generation guard, tests, and records (`1eb7027`, publication record `86e2f72`) are pushed to `integration/aura-voice-consolidation`; continue on this branch only and never stage `device-backups/`.
2. Keep APK SHA-256 `A242340DC1003BE359FC52F66638FC0884A284DF265A4F09E363054589BABFA4` uninstalled until the Honor reconnects and package/signature/data backup checks are complete, followed by explicit approval.
3. On-device, verify stale callback recovery, repeated wake start/stop, ASR transcript, spoken answer, interruption, Ollama/cloud fallback and WhatsApp draft. The current run only proves unit/build behavior.

## Wake launch microphone handoff — 2026-11-06

1. Activity wake-launch guard and tests (`cb04a22`), records (`25e9af6`), and publication confirmation (`d95ec2a`) are pushed to `integration/aura-voice-consolidation`; continue only on this branch and do not stage `device-backups/`.
2. Fresh APK SHA-256 `9CF7CD5B965D36E964D124877165362C11514B180A8DE957E607E586454B0CB6`; keep uninstalled until phone reconnects, identity/signature and data backup are verified, and this exact install is approved.
3. On Honor, verify wake-triggered launch leaves the microphone with active chat, then test recognized text, answer speech, multiple turns, interruption, and ordinary standby restart.

## Offline verification refresh — 2026-11-06

1. The offline Gradle invocation succeeded; 71 existing app/voice tests report passed (23 suites), but all tasks were up-to-date and were not forcibly rerun. APK signature/package/hash are recorded in `AI_HANDOVER.md`.
2. Continue code-only work while the Honor is disconnected; leave the APK uninstalled and exclude `device-backups/`.
3. After reconnection and explicit build approval, back up accessible Aura data, verify package/signature, install in place only if compatible, then test wake, mic handoff, visible transcripts, two-way speech, interruption, selected voice, Ollama and confirmed cloud fallback/recovery.
4. Obtain/configure an authorized Austrian voice identity, then test voice preview/playback; do not claim it is available before an actual provider/device check.

## On-device wake recognition — 2026-11-06

1. Wake standby prefers Android's dedicated on-device recognizer when available on API 31+, requests offline mode, and keeps the existing system recognizer fallback. Unit policy tests and the APK build passed; Honor support and en-ZA model availability remain unknown.
2. On reconnection, inspect OS on-device recognition availability and language resources, then test repeated “Yo, yo, yo” plus a multi-turn exchange. Keep the APK uninstalled until its exact update has separate approval.
3. If offline recognition is unavailable or inaccurate, evaluate a dedicated wake-word engine and its model/license; do not claim local wake recognition is verified yet.

## Wake language recovery — 2026-11-06

1. The updated wake service now switches to the system recognizer after on-device `en-ZA` support errors; policy/build checks pass. The new APK is uninstalled and Honor acceptance is pending.
2. After phone reconnection, back up accessible app data and verify package/signer before getting approval for this exact build. Test on-device language availability and behavior without network, then enable network and verify system fallback; record actual device output.
3. Continue cloud/Ollama live failover, TTS identity, interruption, and WhatsApp checks only when their real provider/phone is available. Automated mocks are not acceptance evidence.

## Memory recall verification — 2026-11-06

1. Bounded lexical memory recall and older transcript exchange recall are implemented and pass 81 app/voice tests; APK SHA-256 and details are in `AI_HANDOVER.md`. No database migration; APK remains uninstalled.
2. After Honor reconnection and approval of the exact build, restart Aura and test retrieval of a saved user preference/project fact and a follow-up referring to an older exchange. Check that unrelated exchanges are not inserted.
3. Treat this as word-overlap retrieval, not semantic learning. Consider a locally evaluated synonym or embedding index only if device tests show paraphrase misses; preserve current Room data/schema.

## Offline quick-answer follow-up — 2026-11-06

1. Current-time and arithmetic quick replies now work without a selected model by design; all remaining requests still need a ready cloud or Ollama backend. Automated reports: 86 tests, 0 failures/errors/skips; APK hash in `AI_HANDOVER.md`. Not installed.
2. On Honor reconnection and separately approved exact APK, verify both wake phrase and tap-to-speak with backend unavailable: say “Yo, yo, yo, what time is it?” and “Yo, yo, yo, what is five plus five?” Confirm transcript and spoken result, then reconnect backend and continue voice/fallback/action tests.
3. Source/handover commit and push status follow in `AI_HANDOVER.md`; `main` unchanged; exclude `device-backups/`.

## Voice tuning follow-up — 2026-11-06

1. Android voice pitch and speaking speed can be adjusted and persisted in Settings. Both are limited to a natural range; cloud voice tuning is provider-controlled. No accent is promised by pitch adjustment.
2. Automated reports: 88 tests, no failures/errors/skips; fresh signed APK hash is recorded in `AI_HANDOVER.md`. Not installed.
3. After Honor reconnects and exact-build approval, verify controls persist after reopening Settings, preview the selected voice, and check voice identity across several spoken turns. Continue all other live voice/backend/action acceptance checks.
4. Source/handover publication state follows. Keep work on `integration/aura-voice-consolidation`; `main` untouched; exclude `device-backups/`.

## Wake idle recovery follow-up — 2026-11-06

1. Normal speech timeouts and no-match now retry the wake listener after 800 ms; real recognizer failures use the existing capped exponential backoff. Unit tests/build pass (90 total reports, no failures); APK is not installed. Exact hash in `AI_HANDOVER.md`.
2. After Honor reconnection and approval of this exact build, test wake activation after quiet periods, repeat several times, and record whether audio indicators/sounds come from Android's recognizer. Then check wake-to-conversation, transcript, spoken answer and interruption.
3. Source and publication commit details follow; continue only on `integration/aura-voice-consolidation`. Do not stage `device-backups/` or merge `main`.

## Conversation routing repair — 2026-11-06

- The app classifier now routes only explicit command-at-start phrases (including polite and wake-prefixed commands) to the agent planner, preventing ordinary questions that mention task vocabulary from receiving action handling. App tests: 78 pass; unchanged voice-library report: 12 pass. A separate voice-task rerun was blocked by Gradle wrapper lock access before test execution.
- Offline one-worker APK assembly passed after the parallel dex merge ran out of disk space. It is verified and remains uninstalled; exact hash/package/signature are in `AI_HANDOVER.md`.
- Next: commit and publish source/tests/docs on the integration branch. On Honor reconnection and owner approval, verify question-vs-action routing, then repeated wake/voice turns, interruption, Ollama/cloud fallback/recovery, and WhatsApp draft. No real-device or live-provider result is currently verified.
