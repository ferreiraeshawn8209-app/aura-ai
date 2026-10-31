# Aura Emotional Intelligence and Continuity Module
Updated 2026-10-31. STATUS: APPROVED REQUIREMENT; NOT YET IMPLEMENTED.

## User decision
Integrate emotional intelligence and durable conversational continuity into Aura during the existing eight-repository integration effort. New AI sessions must read this file and AURA_MASTER_PLAN.md, AURA_HANDOVER.md, AURA_REPOSITORY_COMPARISON.md before continuing. Do not restart planning or claim implementation without a commit and tests.

## Required capabilities
1. Emotion recognition: infer possible frustration, excitement, sadness, humour and other context from user speech/text with uncertainty; never assert unverified internal feelings.
2. Emotional memory: store user-approved preferences, interaction history, key decisions, emotional context and unresolved work. User must be able to view, correct and delete saved context. Avoid storing sensitive emotional inferences by default.
3. Simulated affect state: explicitly computational values (e.g. calm, curiosity, concern) for response selection; never claim these are genuine subjective feelings or consciousness.
4. Stable personality: respectful, straightforward, consistent tone across Android/Windows and voice/text. Support interruptions without losing conversation context.
5. Durable handover: save task status, files/branches, decisions, tests, blockers and next steps to repository handover after each meaningful milestone.

## Implementation approach
- Audit existing Kotlin conversation/memory DAOs and voice controller before creating parallel systems.
- Define a shared state schema, explicit persistence boundaries and opt-in memory controls.
- Integrate with existing voice subsystem, Android app and Core backend via interfaces/APIs as appropriate.
- Add unit tests for affect-state transitions, user correction/deletion, persistence, privacy boundaries, and session continuity; then integration tests.
- Implement on isolated branch, verify builds and obtain explicit approval before merging to main or deploying.

## Immediate next steps
1. Complete eight-repository code comparison and record reusable modules.
2. Design emotion-state + durable memory API around current code; identify exact files to edit.
3. Implement and test, then record verified results here and in AURA_HANDOVER.md.

Do not treat this requirements document as proof the feature exists in the app.
