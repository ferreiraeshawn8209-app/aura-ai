# AURA AI

**AURA AI** is a mobile-first AI personal assistant for Android, powered by Ollama + Gemma.
It understands natural language and performs real actions on your phone.

---

## Phases & Roadmap

This project will be developed and rolled out across clear phases to ensure a stable, secure, and user-friendly product. Below is the proposed set of phases and what each phase includes.

- Phase 0 — Proof of Concept (Completed)
  - Basic chat UI and integration with the Ollama API
  - Parse simple JSON actions (make_call, send_sms, set_reminder, launch_app)
  - Local storage for conversation history (Room)
  - Runtime permission handling for core features

- Phase 1 — Private Beta
  - Improve action parsing and error handling
  - Contact resolution and fallback flows when contacts not found
  - Add tests for repository and action parsing logic
  - Basic onboarding and privacy notice for users
  - Limit beta to invited testers and gather telemetry (opt-in)

- Phase 2 — Public Beta
  - Robust permission UX (granular explanations, graceful degradation)
  - Improved voice interaction (hotword, better noise handling)
  - Memory system improvements (context windows, summaries)
  - Analytics and crash reporting (GDPR/consent compliant)
  - Support for multiple assistant personalities (optional)

- Phase 3 — Production Release
  - Performance and battery optimizations
  - Security review and data handling audit
  - Accessibility improvements (TalkBack, large fonts)
  - App store preparation (screenshots, privacy policy)
  - Launch to public and staged rollouts

- Phase 4 — Post-launch Growth & Features
  - Multi-lingual support and localized models/prompts
  - Integrations (calendar providers, messaging apps)
  - Plugin/skill system for third-party actions
  - Offline-first capabilities for basic actions
  - Continuous model prompt tuning and monitoring

Rollout plan
- Start with internal testing (Phase 0 -> Phase 1 alpha testers).
- Collect feedback and crash reports; prioritize P1 issues.
- Expand to public beta with staged rollouts (Phase 2).
- Finalize security and store requirements; perform production release (Phase 3).

Success metrics
- Task completion rate for actions (calls, SMS, reminders)
- False-positive/incorrect-action rate
- User retention and DAU/MAU for beta testers
- Crash-free sessions and permission grant rates

---

## Features

| Feature | Status |
|---|---|
| 💬 Voice & text conversation with AI | ✅ |
| 📞 Make phone calls | ✅ |
| 💬 Send SMS messages | ✅ |
| ⏰ Set reminders and calendar events | ✅ |
| 📱 Open and control apps | ✅ |
| 🧠 Context-aware memory system | ✅ |

---

## Tech Stack

- **Platform**: Android (API 26+)
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **AI**: Ollama Gemma 3 1B Cloud via REST API
- **Architecture**: MVVM + Clean Architecture (Use Cases)
- **DI**: Hilt
- **Database**: Room (conversation history + memory)
- **Networking**: Retrofit + OkHttp + Moshi
- **Async**: Kotlin Coroutines + Flow
- **Voice**: Android SpeechRecognizer

---

## Project Structure

```
app/src/main/java/com/aura/ai/
├── AuraApplication.kt
├── MainActivity.kt
├── agent/                 # T1000 understand/plan/execute/verify loop and tools
├── data/                  # Room persistence, Ollama networking, repositories/settings
├── di/                    # Hilt modules
├── domain/                # Android actions, models and use cases
├── ui/                    # Compose chat, memory, skills, settings and trading screens
├── util/                  # Reminder and voice utilities
└── voice/                 # Android recognition, TTS, and wake-word service
```

---

## Setup

1. Clone the repository and open it in Android Studio (Java 17).
2. Run an Ollama-compatible server reachable from the device. Configure `OLLAMA_BASE_URL` and `OLLAMA_MODEL` in ignored `local.properties` or Gradle properties. The emulator default URL is `http://10.0.2.2:11434`; a physical device needs a reachable host address.
3. Build and install on an Android API 26+ device/emulator:

```bash
./gradlew testDebugUnitTest assembleDebug
```

Never commit `local.properties` or API credentials.

---

## Current limitations

- The active T1000 agent talks to Ollama; cloud provider fallback is not implemented.
- Wake-word service and voice processing require device-level testing and permissions; continuous listening is not guaranteed by Android.
- Direct device action tools are not currently wired into the T1000 agent. The existing action use cases are separate and must not be assumed to have run.
- No Windows desktop/companion implementation exists yet.
- For verified status, current test/build results and next steps, see [AI_HANDOVER.md](AI_HANDOVER.md) and [TODO.md](TODO.md).

---

## License

MIT — see [LICENSE](LICENSE).
