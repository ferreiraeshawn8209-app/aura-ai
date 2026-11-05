# AURA AI Integration Handover
Updated: 2026-11-05
Branch: integration/aura-voice-consolidation
Main branch is not to be overwritten during integration.

## Verified repository inventory
- aura-ai: main native Android Kotlin application (canonical destination)
- Aura-Voice-Subsystem: reusable native Android voice library at lib/aura-voice and separate demo app
- aura-core: Next.js/TypeScript backend and voice contracts/API; NOT a drop-in Android library
- code-companion-pro: web application; review integrations selectively
- tony-my-ai-friend: web application with voice hooks/settings; review selectively
- keypilot-ai: web application; review selectively
- spokdot: web application with TTS API; review selectively
- vocalocal: separate voice training backend; do not blindly merge with Aura Android
This is an inventory, not proof that all features are safe or useful to integrate.

## Confirmed integration gap
aura-ai/app/src/main/java/com/aura/ai/di/VoiceModule.kt still provides the older SpeechRecognitionManager, TextToSpeechManager, WakeWordDetector and VoiceStateManager. It does not construct DefaultAuraVoiceSubsystem from Aura-Voice-Subsystem.
aura-ai/app/src/main/java/com/aura/ai/voice/tts/TextToSpeechManager.kt initializes Android TTS with Locale.getDefault() and has no persistent fixed voice selection in the inspected implementation.
Aura-Voice-Subsystem/lib/aura-voice/ANDROID_INTEGRATION.md describes exact Gradle module inclusion, speech and TTS providers, persistent SharedPreferencesVoiceSettingsStore, interrupt/stop semantics and response handoff. Its README says the demo is NOT the main Aura app.
This explains an implementation gap, but does not establish who caused the delay.

## Next code changes (not yet implemented or tested)
1. Vendor or include lib/aura-voice into aura-ai on this isolated branch, preserving package namespace and licenses.
2. Update settings.gradle.kts and app/build.gradle.kts for :aura-voice.
3. Wire one application-scoped DefaultAuraVoiceSubsystem with AndroidSpeechRecognizerProvider, AndroidTextToSpeechProvider and SharedPreferencesVoiceSettingsStore. Keep wake-word feature separate: voice library explicitly does not implement wake-word detection.
4. Connect final STT transcript to existing Aura reasoning pipeline and reasoning response to subsystem.speak.
5. Enforce one user-selected Austrian T-800 voice ID persistently. If unavailable, show a clear error instead of silently switching to another voice. Do not hardcode a voice ID until the installed provider is checked.
6. Preserve cloud fallback through authenticated backend; do not embed cloud API secrets in APK.
7. Compile and run unit tests, then test physical Android device including interruptions, denied microphone permissions, offline behaviour, voice consistency, and actual conversation loop.
8. Record command outputs, exact build SHA, APK location, and outstanding failures here before any deployment.

## Evidence and status
2026-11-05: Inspected GitHub repository trees for eight repos; inspected VoiceModule.kt, TextToSpeechManager.kt, VoiceStateManager.kt, native subsystem implementation and integration guide. Connected BEATK PC via Desktop Commander. No successful Android build or on-device test has yet been performed in this session.
No source code has been integrated in this commit. Do not claim otherwise.
