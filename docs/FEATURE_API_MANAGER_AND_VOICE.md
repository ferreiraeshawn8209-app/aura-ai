# Aura/T1000: API management and voice implementation

Status: specification only; no application functionality implemented or verified yet.

## API manager
- Accept natural language tasks for discovering, creating, restricting, rotating and testing API credentials across providers.
- Use official provider APIs and OAuth where supported, otherwise an authenticated user-controlled browser session.
- Never collect passwords in model prompts, bypass MFA/CAPTCHA, or log/print/commit secrets. Store tokens in Android Keystore-backed encrypted storage or a dedicated secret vault; prefer server-side secrets for server integrations.
- Require explicit user approval for billing activation, charges, privileged scopes, credential deletion/rotation and irreversible actions. Show requested scopes and destination before authorizing.
- Keep an auditable per-task state machine (discover, authenticate, configure, approve, test, report) with retry/resume and truthful failure messages.
- First integration: Google Maps Platform existing key discovery, Places/Routes service setup, restrictions, and test calls. Never silently enable paid APIs.

## Voice
- Keep existing Ollama Qwen setup intact. Offer selectable TTS engine and voice profiles, with Android TTS fallback and offline Piper-compatible neural voice where device resources permit.
- Settings: voice model, language/accent, pitch, speed, volume, and preview. Never claim accent availability without installed model.
- Silence repeated microphone activation chirps when possible while retaining legally/system-required microphone indicators. Avoid repeated SpeechRecognizer start/stop loops and maintain clear listening state.
- Voice controls for interrupt, pause, mute and hands-free responses.

## Acceptance tests
- Existing Aura chat, Ollama connection and permissions remain functional.
- No API keys in logs, repository, chat transcripts or plaintext preferences.
- MFA requires user interaction; paid action cannot proceed without approval.
- Voice preview and settings persist; switching voice works offline with installed models.
- Android build and tests pass before merging; no merge to main without review.
