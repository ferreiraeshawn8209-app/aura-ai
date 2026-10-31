# Aura repository comparison matrix
Updated: 2026-10-31. Status: first-pass structural verification; NOT a complete file-level functional comparison.

| Repository | Verified structure | Preliminary disposition | Verification remaining |
|---|---|---|---|
| aura-ai | Native Android Gradle project, settings includes only :app; Gradle wrapper and Codemagic configuration | KEEP as Android main hub | inspect app code, compile and test |
| aura-core | Next.js backend plus runner directory and API/runner docs | KEEP as separate backend/Windows process; connect via API | verify runner operations, authentication and end-to-end connectivity |
| Aura-Voice-Subsystem | pnpm TypeScript workspace with android, lib, artifacts and scripts directories | EVALUATE voice components, not wholesale merge | compare controller files, dependencies and interruption tests |
| code-companion-pro | Vite/React/Capacitor, Monaco editor, Supabase, Android folder | EVALUATE coding/GitHub operations | inspect exact implementations and test coverage |
| keypilot-ai | Android/Capacitor and src/supabase directories | EVALUATE security/credentials | inspect actual secret handling before adoption |
| spokdot | Vite/React/Capacitor with same package scripts and dependency families as code-companion-pro | POSSIBLE DUPLICATE, do not copy blindly | file-by-file diff and functional comparison |
| tony-my-ai-friend | Vite/React/Supabase project | EVALUATE conversation features | inspect modules and tests |
| vocalocal | Vite/React project plus local-backend directory | EVALUATE voice backend separately | inspect local backend runtime and compatibility |

## Confirmed architecture constraint
The main Android hub is a native Gradle project; several candidates are TypeScript web/Capacitor projects. Their source files cannot simply be pasted into the Android app as native Kotlin modules. Integrate via stable APIs, portable libraries or deliberate ports only after tests.

## Immediate next audit tasks
- Enumerate candidate source modules and exact code paths.
- Compare voice state machine and interruption handling against aura-ai implementation.
- Diff code-companion-pro and spokdot to avoid duplicated work.
- Inspect Core runner and hub API client contracts.
- Check open PRs #15 and #16 and run baseline CI.

No files deleted, no app code moved, no build passed as part of this audit.


## Second-pass evidence-based decisions (2026-10-31)
All eight `main` branch Git trees were retrieved (not truncated); targeted implementation files were fetched. This completes **component selection at source-inspection level**, NOT exhaustive file-by-file security review, compilation, or runtime verification.

| Repo | Evidence (source path) | Decision | Caveat |
|---|---|---|---|
| aura-ai | `app/src/main/java/com/aura/ai/voice/WakeWordService.kt`, `data/repository/MemoryRepository.kt`, `data/remote/OllamaConfiguration.kt` | **KEEP** native Android app, existing Room memory, Ollama configuration; improve voice controller in place | Wake service loops Android SpeechRecognizer, not a dedicated offline hotword engine; memory is key-value facts, not complete conversation history |
| Aura-Voice-Subsystem | `lib/aura-voice/src/main/kotlin/com/aura/voice/DefaultAuraVoiceSubsystem.kt`, `ANDROID_INTEGRATION.md` | **ADAPT / highest-priority integration** native Kotlin library for speech/tts state and interruption, not web frontend | Android API 26+, host Gradle compatibility, permission lifecycle and real-device barge-in need tests |
| aura-core | `runner/src/operations.ts`, `runner/src/security.ts`, `lib/brain/provider/ollama.ts`, `app/api/runner/*` | **KEEP SEPARATE**, connect Android via authenticated API; consider runner as PC execution service | Realpath checks, command allowlist and output redaction exist; not proof of full security. Must test pairing, network and approvals |
| keypilot-ai | `src/routes/_authenticated/credentials.tsx`, `src/lib/keypilot.functions.ts` (referenced) | **REFERENCE ONLY**, do not import web UI or secret store into Android | UI has audited reveal/export workflow; backend encryption and access policy still require security review |
| code-companion-pro | `src/lib/github.functions.ts`, `src/lib/agent-ops.server.ts` | **REFERENCE / ADAPT API concepts** only | GitHub implementation depends on Lovable gateway and Supabase auth; not a standalone native Android integration |
| spokdot | `src/lib/github.functions.ts`, `src/lib/agent-ops.server.ts` | **IGNORE DUPLICATE GITHUB MODULE**, compare other unique features only as needed | GitHub module exact same blob SHA `46f1f925140cde6d9e84765c456d0d384be7a42f` as code-companion-pro; not proof whole repos identical |
| tony-my-ai-friend | `src/hooks/use-voice.ts` | **REFERENCE ONLY** browser voice UX patterns | Browser Web Speech and SpeechSynthesis APIs cannot be transplanted into native Android |
| vocalocal | `local-backend/voices.py`, `local-backend/train_voice.py` | **DEFER** optional PC voice-conversion experiment | Python RVC voice-bank/training code, not a replacement for STT or core TTS; model files and runtime performance unverified |

### PR baseline verified
- PR #10 `Amalgamate T1000 into Aura AI`: **merged**.
- PR #15 `Repair merged Aura/T1000 build and local Ollama connection`: **open**, 22 files; evaluate/test on isolated branch before merge.
- PR #16 `Fix agent verification and throttle wake-word restarts`: **open**, 2 files; fixes a permissive default verification verdict, plus wake restart throttling. Evaluate/test before merge.

### Integration ordering
1. Run baseline Gradle test/build and inspect CI; establish whether PR #15 is necessary.
2. Test PR #16 agent-verdict correctness and wake restart behavior.
3. Adapt native voice subsystem as a library on isolated branch; avoid duplicate recognizers.
4. Verify Ollama connectivity, Core/Windows runner authentication and action approvals.
5. Add transcript history and cross-session memory; then task scheduler for income and longevity missions.
6. Build APK, device-test, record reproducible results, request approval before merge/deploy.

### Remaining verification
This is an actionable source-level disposition of all eight repos. **Not yet proven**: clean build, voice reliability, end-to-end integration, automated tests, full security audit, licensing, or feature-completeness. No production app code changed.
