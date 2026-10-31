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
