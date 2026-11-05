# Next steps

1. Reconnect/unlock the Honor and complete the spoken acceptance test: tap mic, transcript, speak “Yo, yo, yo, what's five plus five?”, hear `10`, continue another turn, interrupt TTS, and verify wake activation.
2. On the phone's Settings screen, test the saved PC endpoint `http://192.168.8.18:11434`, discover the two Qwen models, and confirm one responds. The PC endpoint itself returned HTTP 200 during this build.
3. Configure and implement a real cloud provider only after an endpoint and credential are available to this app. Add encrypted key storage, usage limits, failover/recovery/offline tests; current code has no cloud chat transport.
4. Resolve the voice mismatch: preserved settings currently select Android voice ID `ar-language`; the selected Austrian T-800 voice/provider is not configured in this checkout. Do not silently switch voices or label a different voice as Austrian.
5. Replace platform standby recognition with a maintained on-device wake-word/VAD engine and test battery, echo suppression, false activations, interruption, and five-minute conversation on the Honor.
6. Publish verified changes and handover to `integration/aura-voice-consolidation`; keep `main` untouched.
