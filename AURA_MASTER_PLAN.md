# Aura master integration plan
Updated: 2026-10-31
Status: Audit in progress. No code integration or successful APK build is claimed by this document.

## Goal
Keep aura-ai as the main hub. Compare existing implementation against all candidate repositories, choose the best proven components, integrate in a separate branch, test, and merge only after verification. Do not rebuild from scratch or overwrite working code.

## Candidate repositories (verify exact ownership, branches, and contents)
- ferreiraeshawn8209-app/aura-ai: Android main hub; prior amalgamation PR #10, repair PRs #15 and #16 need verification.
- ferreiraeshawn8209-app/aura-core: agent backend, Ollama, Windows runner.
- ferreiraeshawn8209-app/Aura-Voice-Subsystem: speech controller and interruption handling.
- keypilot-ai: candidate credential-management tools.
- code-companion-pro: candidate GitHub and coding tools.
- spokdot: candidate coding and deployment tools; check duplication.
- tony-my-ai-friend: candidate conversation and voice features.
- vocalocal: candidate local voice backend.
These are audit candidates, not verified superior implementations.

## Audit checklist for each repository
1. Confirm owner, default branch, latest commit, languages, build and test commands.
2. Inventory modules, APIs, dependencies, secrets handling, and relevant branches/PRs.
3. Compare feature-by-feature against aura-ai; record file paths, evidence, compatibility, test status.
4. Decide KEEP HUB / ADOPT / ADAPT / IGNORE with rationale. No wholesale copying.
5. Identify licensing, platform and security constraints.

## Execution sequence
1. Complete repository comparison matrix.
2. Inspect PR #10 and repair PRs #15/#16; run baseline Android CI/tests.
3. Integrate chosen voice controller on isolated branch; verify listening, speech, barge-in, and restart handling.
4. Verify Android-to-Core APIs, Ollama, memory persistence and Windows runner with permissions.
5. Add selected coding and credential tools only if better than existing implementations.
6. Run regression, integration and security tests; build test APK; record results.
7. Request explicit approval before merging or deployment. Preserve original repositories.

## Acceptance criteria
Reproducible Android build; voice interaction and interruptions tested; memory continuity verified; authenticated Core/Windows actions with explicit approvals; no secrets exposed; passing checks; documented handover and rollback.
