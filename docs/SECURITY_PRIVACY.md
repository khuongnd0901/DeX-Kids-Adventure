
# Security, child safety and privacy

No adverts or child tracking. No default cloud GPS, voice capture or audio uploads.
History remains local with explicit retention and delete controls once implemented.
Parent settings must be separate from child display, including emergency stop and session limit.
IPC is never an unprotected exported implicit broadcast. Prefer signature-level permission; with differing certificates require verified caller identity.
Do not obscure navigation warnings or driver controls; audio focus must yield to navigation.
Current demo requests no location, microphone, internet or special permission.

Parent dashboard and in-game Parent Controls use a single DeX display. They have
**no PIN or local authentication gate**, by product decision; do not represent
them as kiosk/device-owner controls. Previously saved PIN hash/counter fields are
removed on upgrade. Android OS location/microphone consent, local session limits,
and signature-protected IPC remain separate safeguards.
