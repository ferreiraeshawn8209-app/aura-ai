# NEXT AI HANDOVER — Aura AI

Updated: 2026-11-08
Repository: `ferreiraeshawn8209-app/aura-ai`
Working branch: `copilot/voice-diagnostics-reliability`
Last reported development commit (verify current HEAD): `6842f17dc9e46c3a80de40018729030745bca5c8`

## NEXT AI: START HERE

1. Continue the EXISTING Aura AI Android application, package `com.aura.ai`. Do not create a second T1000 application or change the package name.
2. Check current branch HEAD, diffs, and all recent commits before making changes. This handover commit itself changes HEAD; the SHA above is a previously reported development commit, not necessarily the current branch tip.
3. Read `AURA_INTEGRATION_HANDOVER.md`, `AI_HANDOVER.md`, `PROJECT_STATUS.md`, `VOICE_TESTS.md`, and `NEXT_STEPS.md` on this branch. Some older documents refer to `integration/aura-voice-consolidation` and may be stale; verify against source.
4. First run `./gradlew :app:testDebugUnitTest :aura-voice:testDebugUnitTest :app:assembleDebug` in a terminal-enabled Android SDK/JDK environment. On Windows use `gradlew.bat`. Diagnose and fix any failures. Record real test counts, APK path, SHA-256, and signer only after verification.
5. Finish wiring opt-in speech diagnostics to the settings and chat UI, then validate recognition, wake phrase, confidence, alternatives, and error handling on the Honor device. No current-branch device acceptance has been established.
6. Preserve user data, recordings, backups, and selected voice. Do not uninstall/clear app data. Verify package and signing compatibility before any installation.
7. Test the single required Austrian-accented T-800 voice, spoken action acknowledgement exactly "I'll be back", cloud/local routing, Ollama connectivity, and interruption on-device. Do not claim these are working without live evidence.
8. Keep work on the existing working branch. Do not merge into `main` without owner approval. Update handovers and commit them after each meaningful development session.

## Reported changes in this working branch (inspect source; not independently build-verified)

- `lib/aura-voice/src/main/kotlin/com/aura/voice/android/RecognitionDiagnosticPolicy.kt`: safe confidence filtering, bounded alternatives, readable speech recognition errors.
- `lib/aura-voice/src/main/kotlin/com/aura/voice/android/AndroidSpeechRecognizerProvider.kt`: recognition result/error handling and lifecycle guards.
- `app/src/main/java/com/aura/ai/data/settings/SettingsRepository.kt`: `speechDiagnosticsEnabled` setting, default false.
- `app/src/main/java/com/aura/ai/ui/chat/SpeechDiagnosticPolicy.kt`: opt-in screen-only diagnostic formatting.
- `app/src/main/java/com/aura/ai/voice/WakeWordEvents.kt`: in-memory wake metadata.
- Tests reported in `RecognitionDiagnosticPolicyTest.kt`, `SpeechDiagnosticPolicyTest.kt`, and `WakeTranscriptDiagnosticTest.kt`.

These are descriptions from the prior coding-agent handover, not evidence of a passing build. Confirm that every file and test exists at the current HEAD.

## Verified from repository inspection on 2026-11-08

- `AURA_INTEGRATION_HANDOVER.md` and `PROJECT_STATUS.md` exist on this branch, but their opening status sections describe older integration work.
- The dedicated `NEXT_AI_HANDOVER.md` was absent before this file was created.
- Previous documentation describes successful builds on older checkouts; these do not establish a clean build of the current branch.
- No build, test, Android device run, or CI execution was performed during this handover-writing session.

## Open issues / acceptance gates

- Clean current-branch Gradle unit tests and APK assembly: NOT VERIFIED.
- Real device speech recognition, wake and diagnostics: NOT VERIFIED.
- Diagnostic opt-in UI and no transcript persistence/logging: REVIEW AND VERIFY.
- Single Austrian T-800 voice: REQUIRED, NOT VERIFIED. Do not silently switch to another voice or accept `ar-language` as the desired voice.
- Exact "I'll be back" acknowledgment: code reportedly implemented, device acceptance NOT VERIFIED.
- Cloud-first routing and local Ollama fallback: conflicting historical handover claims; inspect actual current code and test real endpoints/credentials.
- CI status: inspect failing runs and repair where possible; do not infer success from source-only edits.
- Dedicated acoustic wake-word engine and acoustic barge-in: not established.

## Development safeguards

- Preserve `com.aura.ai`, existing signing/data, and all working functionality.
- Keep diagnostic transcript display explicitly opt-in, off by default; do not log/store raw speech payloads.
- Never commit API keys, credentials, private transcripts, or personal backups.
- Do not claim tests passed unless executed and evidenced.
- Do not modify `main`, create another app, or merge without approval.
- At session end, update this document with the new HEAD, changed files, actual test evidence, blockers, and next exact action.

## Immediate next action

Open a terminal-capable checkout of `copilot/voice-diagnostics-reliability`, run the Gradle command above, fix any compile/test failures, then perform opt-in speech diagnostic integration checks. Continue from existing files rather than restarting the architecture.
