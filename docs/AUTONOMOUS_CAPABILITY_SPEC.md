# Autonomous Capability Specification

Aura/T1000 exposes one conversational agent. Capability selection is internal to the agent.

## Intent examples

- "Call John and tell him I'll send the quote tonight." → contacts + telephony + conversational voice workflow.
- "Build the BeatKulture quote feature, test it and fix the errors." → coding + build + test + iterative repair.
- "Prepare my wedding music for tonight and keep the energy around 120 BPM." → DJ/music planning + local media capabilities.
- "Give me directions to the venue." → location + navigation.

## Agent requirements

1. Maintain conversational context and durable memory.
2. Select tools based on intent instead of asking the user to choose a feature menu.
3. Plan multi-step work and track pending tasks.
4. Execute only authorized actions.
5. Verify results and retry recoverable failures.
6. Learn reusable procedures from successful work.
7. Report progress and final outcomes naturally.
8. Keep an auditable action history.
9. Respect Android lifecycle, permission and background-execution rules.
10. Never bypass security or access controls.

## Wake phrase

The intended interaction is system-wide `Hey Aura` activation where Android device capabilities permit it. The implementation must account for microphone permission, foreground-service requirements, battery optimization and manufacturer-specific restrictions.
