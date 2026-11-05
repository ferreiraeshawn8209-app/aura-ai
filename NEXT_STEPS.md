# Next steps

1. Reconnect/unlock the Honor and install the latest APK in place. Test a spoken action: hear exactly one “I'll be back” before execution, then confirm the action and final response; also test that “what is five plus five?” gets an immediate answer without the acknowledgement.
2. On the phone, verify selected voice is unchanged and the acknowledgement uses it. Do not claim Austrian T-800 voice until the actual installed voice matches; current persisted Android ID is `ar-language`.
3. On the phone's Settings screen, test the saved PC endpoint `http://192.168.8.18:11434`, discover both Qwen models, and confirm one responds. The PC endpoint and local generation passed from Windows.
4. Configure and implement a real cloud provider only after an endpoint and credential are available to this app. Add encrypted key storage, usage limits, failover/recovery/offline tests; current code has no cloud chat transport.
5. Replace platform standby recognition with a maintained on-device wake-word/VAD engine and test interruption, echo suppression, false activations, and a five-minute conversation on the Honor.
6. Publish the verified changes and handover on `integration/aura-voice-consolidation`; keep `main` untouched.
