# Next steps

1. Finish the current unit test and APK build; fix any remaining errors, then record the APK hash.
2. Inspect the latest supplied eight-repository audit when it arrives and reconcile it with the existing handover inventory.
3. Implement a real provider router in the agent path: configured cloud-first reasoning, local Ollama fallback and recovery, bounded timeouts, diagnostics, idempotent generation-only failover, and secure key storage. Add deterministic tests for cloud success, cloud failure to local, recovery, and offline failure. Do not ship a fake endpoint or embedded secret.
4. Keep the user-selected voice ID stable. The original Austrian English voice requires an installed/authorized TTS voice provider; this build cannot synthesize the specific accent by choosing a label or changing pitch.
5. Replace platform standby recognition with a maintained on-device wake-word/VAD engine and test battery, echo suppression, false activations, interruption, and five-minute conversation on the Honor.
6. Review compatible functionality from the other repositories one capability at a time. Do not copy web-only systems or singing voice conversion into the Android app without an explicit integration boundary.
7. Update the GitHub handover on `integration/aura-voice-consolidation` with verified results and commit identifiers. Keep the main branch untouched; request deployment approval before installing over the owner's app.
