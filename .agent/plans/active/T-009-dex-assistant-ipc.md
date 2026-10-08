# T-009 M8 DeX + Assistant IPC ExecPlan

Source: secondary-display discovery with DisplayManager, ActivityOptions.launchDisplayId, no hardcoded IDs, no implicit phone fallback; stop-only explicit receiver protected by signature permission.
Existing DeX-Assistant and Dex-Assistant-UI sources must remain unchanged before dedicated cross-repo audit.
Critical blockers:
- Samsung DeX Activity launch policies only verifiable on physical Fold3.
- Signing identity of DeX-Assistant may not match DeX Kids; signature permission can refuse all calls safely.
- START/PAUSE/RESUME/SET_VOLUME/GET_STATUS cross-APK controls, command router, phone-vs-TV session acceptance not yet wired or tested.
- Audio focus coexistence with Google Maps + Vietmap LIVE unverified.
Do not claim completed IPC integration or change existing voice/accessibility/device-owner setup.
