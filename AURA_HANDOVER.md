# Aura handover / next agent instructions
Updated: 2026-10-31

## Objective
Continue the existing Aura project, not a rebuild. Main hub: ferreiraeshawn8209-app/aura-ai. Read AURA_MASTER_PLAN.md first.

## Confirmed in this session
- Created documentation branch docs/aura-master-handover-20261031 from main.
- Started permanent master plan and handover files on that branch.

## Prior conversation findings, requiring fresh verification
- Amalgamation reportedly merged as aura-ai PR #10 on 2026-09-25.
- Repair PRs #15 and #16 reportedly remain open.
- Eight candidate repositories were identified, but feature-by-feature superiority and build success were NOT proven.
- A separate voice integration branch was reportedly created earlier; discover its exact name before modifying it.

## Next actions
1. Enumerate all eight repositories and accessible accounts; verify names and refs.
2. Read current hub structure and PRs #10/#15/#16, record merge/check status.
3. Build a file-level comparison matrix with evidence and decisions for each component.
4. Update this file with branch names, commits, test commands/results, blockers, and next exact step after each meaningful change.
5. Implement only on an isolated development branch; never overwrite working code, merge to main, deploy, or delete without approval.

## Current verification status
No APK built. No integration performed by this documentation commit. No passing tests claimed. No credit-cost estimate established.

## Verified development progress (2026-10-31)
- Isolated development branch: `integration/voice-core-20261031`; main untouched.
- WakeWordService restart/backoff fix committed: `028ab438a5633543a046da6fcd7f17bfd73c9c3a`.
- Android CI now runs `testDebugUnitTest`, `assembleDebug`, and uploads `aura-debug-apk` on success: `b5c5ec051a3bb6b54c4c473269a016758e62d313`.
- These commits are NOT evidence of a passing build, generated APK, or completed two-way voice integration.
- Next agent action: inspect latest Actions run for integration branch, obtain failing compiler/test logs, fix failures in integration branch, then adapt `Aura-Voice-Subsystem` Kotlin voice module and test interruptions. No merge or release without approval.
