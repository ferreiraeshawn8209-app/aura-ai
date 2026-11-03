# Aura AI / T1000 — Handover

Last reviewed: 2026-11-03
Working branch: `copilot/aura-audit-foundation`

## Repository/runtime

This is an existing Kotlin Android app (API 26+, Compose, Room, Hilt, Retrofit/OkHttp, coroutines). The launcher is `MainActivity` and `ui/T1000App.kt`. The active assistant path is `ChatViewModel` → `AgentOrchestrator` → `OllamaRepository`/registered `Tool`s. Durable agent memory and learned procedures are in `T1000Database` (`MemoryDao`, `SkillDao`); the older `AuraDatabase` has a separate legacy key/value memory and transcript store. This repository does not currently have a Windows desktop app or companion service.

## This session's changes

- Fixed the broken `BuiltInTools.kt` memory wiring: it previously had an invalid import (`T1000T1000MemoryRepository`) and depended on an undeclared/wrong `MemoryRepository` type. `RecallMemoryTool` and `RememberTool` now use the actual `T1000MemoryRepository` API (`recall` returns `MemoryEntity` records; `remember` takes named fields).
- Tightened calculator parsing: bounded expressions, strict trailing input, unary plus/minus, division-by-zero and non-finite result rejection. Added unit coverage.
- Added transcript search to `ConversationDao` / `ConversationRepository`; ChatViewModel hydrates saved messages and saves each new user/assistant turn.
- Made mic activation interrupt active generation and TTS, enabling a spoken turn to interrupt Aura. Existing recognition and TTS providers remain platform Android implementations.
- Removed automatic “Completed task” memory writes. The memory tool and planner prompt are explicitly limited to facts the user asks Aura to remember; this avoids silently retaining every request.

## Verification / current status

- No local Android SDK/Gradle command runner is available in this GitHub editing environment, so `./gradlew testDebugUnitTest assembleDebug` has **not** been run here.
- The latest GitHub Actions run available during this session (`41715559135`, on the pre-existing branch head) failed, but the Actions log endpoint did not return a job log. A fresh CI run on this commit is required to validate compilation and tests; fix any compile failures reported there before claiming success.
- The app's existing build and tests are not verified by this handover. This is intentionally not a success claim.

## Known gaps and next steps

1. Run `./gradlew testDebugUnitTest assembleDebug` on Java 17 with Android SDK installed; resolve all KSP/Hilt/Room/Kotlin errors. Do not rely on static inspection alone.
2. Verify interrupted agent requests do not leave partial chat rows or stale `busy` state; add ViewModel/repository tests using test dispatchers.
3. Add a visible transcript search/export screen; the repository exposes search but the Compose UI does not yet expose it.
4. Review `AgentOrchestrator.parseVerdict`: currently it treats missing/malformed verifier JSON as verified. Change to fail closed, and ensure the UI surfaces verifier/model failure without false success.
5. The current tool registry has no phone/SMS/WhatsApp control tools wired into `AgentOrchestrator`; do not advertise direct-send behavior. Device actions need explicit per-action confirmation and runtime permission handling before registration.
6. Wake-word service is a prototype using Android `SpeechRecognizer` restart loops, not a dedicated always-on local hotword engine. Validate permission, Android foreground-service restrictions, battery usage, and lifecycle on devices. It is not started from settings currently.
7. Cloud AI providers are not integrated in the active Ollama agent path. Ollama configuration is local/LAN by default; implement any cloud provider only with explicit endpoint/key configuration and secure secret storage.
8. Windows is absent. First decide supported Windows scope (desktop UI, local Ollama host, or companion bridge) and communication/auth model. Keep Android as-is; do not claim Windows support until a separately built/tested component exists.
9. Inspect all `Mock*` and placeholder integrations (coding, browser, finance, trading) before connecting APIs; any coding/self-modification must be proposal/diff based, sandbox-tested, and explicitly approved before applying.
10. Review legacy `AuraDatabase` migration coverage and the separate `T1000Database` `fallbackToDestructiveMigration()` behavior. Never change migration behavior in a way that silently destroys personal data.

## Safe continuation instructions

- Keep working on `copilot/aura-audit-foundation`; do not merge to `main` without user approval.
- Never run a data-clearing migration against user data. Never make external calls or send messages automatically.
- After every meaningful change, update this file with exact tests run/results, known failures, and next step.
