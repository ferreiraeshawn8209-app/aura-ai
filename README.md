# AURA AI

**AURA AI** is a mobile-first AI personal assistant for Android, powered by OpenAI GPT.  
It understands natural language and performs real actions on your phone.

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
- **AI**: OpenAI GPT-4o-mini via REST API
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
├── AuraApplication.kt          # Hilt application entry point
├── MainActivity.kt
├── data/
│   ├── database/               # Room DB (conversations + memory)
│   ├── model/                  # ChatMessage, AuraAction
│   ├── remote/                 # OpenAI Retrofit service + DTOs
│   └── repository/             # ConversationRepository, MemoryRepository
├── di/
│   └── AppModule.kt            # Hilt dependency graph
├── domain/
│   ├── action/
│   │   └── ActionParser.kt     # Parses AI responses into actions
│   └── usecase/                # MakeCall, SendSms, SetReminder, LaunchApp, …
├── ui/
│   ├── components/             # MessageBubble, InputBar, TypingIndicator
│   ├── screen/                 # MainScreen, ChatScreen
│   ├── theme/                  # Color, Type, Theme
│   └── viewmodel/
│       └── AuraViewModel.kt
└── util/
    ├── ReminderReceiver.kt     # BroadcastReceiver for alarm notifications
    └── VoiceHelper.kt          # SpeechRecognizer Flow wrapper
```

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/ferreiraeshawn8209-app/aura-ai.git
cd aura-ai
```

### 2. Add your OpenAI API key

Create (or edit) `local.properties` in the project root:

```properties
OPENAI_API_KEY=sk-...your-key-here...
```

> ⚠️ **Never commit `local.properties`** — it is already in `.gitignore`.

### 3. Build and run

Open the project in Android Studio (Hedgehog or newer) and run on a device or emulator with API 26+.

```bash
./gradlew assembleDebug
```

---

## How AURA Performs Actions

AURA uses a structured JSON convention to signal device actions.  
When the AI decides an action is needed it returns a response like:

```json
{
  "action": "make_call",
  "contact": "Mom",
  "reply": "Calling Mom now!"
}
```

Supported actions:

| `action` | Required fields | Description |
|---|---|---|
| `make_call` | `contact` | Dial a contact |
| `send_sms` | `contact`, `message` | Send a text message |
| `set_reminder` | `title`, `datetime` (yyyy-MM-dd HH:mm) | Schedule a notification |
| `launch_app` | `app_name` | Open an installed app |

---

## Permissions

AURA requests the following permissions at runtime:

| Permission | Purpose |
|---|---|
| `CALL_PHONE` | Make phone calls |
| `SEND_SMS` | Send text messages |
| `READ_CONTACTS` | Resolve contact names to numbers |
| `READ/WRITE_CALENDAR` | Create calendar events |
| `RECORD_AUDIO` | Voice input |
| `POST_NOTIFICATIONS` | Reminder notifications |
| `SCHEDULE_EXACT_ALARM` | Precise reminder timing |

All permissions are optional — AURA degrades gracefully when denied.

---

## License

MIT — see [LICENSE](LICENSE).
