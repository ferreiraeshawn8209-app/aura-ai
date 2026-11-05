# Instructions for coding agents

- Before editing, read `AI_HANDOVER.md` and `PROJECT_STATUS.md`, then the relevant source and tests.
- The original Aura Android app in this repository is the primary application. Preserve package identity, Terminator artwork, databases, settings, recordings, models, user data, and working features. Do not create a replacement app or reset data.
- Work on `integration/aura-voice-consolidation`. Do not merge to `main`, deploy, install an APK, change permissions, or alter production behavior without explicit owner approval for that action.
- Preserve existing uncommitted work and personal backups. Never stage or commit device backups, API keys, signing material, local properties, or other secrets.
- After every task, update `AI_HANDOVER.md` and `PROJECT_STATUS.md` with changed files, branch and commit, exact checks and results, unresolved issues, and concrete next steps. Update `VOICE_TESTS.md` for voice-related checks.
- Separate automated tests/builds from actual phone acceptance. Never claim a wake phrase, microphone, voice, interruption, Ollama, cloud, or WhatsApp feature passed unless it was tested on the actual device/provider.
- If ADB is unavailable, continue safe code review, implementation, automated tests, and APK builds. Do not install or claim real-device success until the phone reconnects and package/signature compatibility is verified.
- Keep provider keys in Android Keystore-backed secure storage; never log or commit their values. Keep local-model and cloud-provider behavior configurable and fail honestly when unavailable.
