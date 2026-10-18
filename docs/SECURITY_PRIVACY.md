# Security, child safety and privacy

DeX Kids has no advertising/analytics SDK or child-tracking backend. GPS is an
opt-in foreground source; microphone answers use the existing on-device path.
Optional parent-configured AI and HTTPS content sources have their own settings;
cloud sharing of child text remains separately controlled. This change adds no
Android permission or new location/audio upload path.

Parent dashboard and in-game controls share the current DeX display. There is
no PIN, kiosk/device-owner lock or session time limit. Parent F10/Back/menu and
End adventure remain available. Signature-protected IPC cannot bypass a pending
YouTube return acknowledgement. Do not obscure navigation controls or alter
Maps/VietMap, Accessibility or the separate DeX-Assistant applications.

## Optional official YouTube handoff

Personal-use YouTube links are local and manually reviewed. Feature and automatic
launch default OFF; show a parent disclosure before enabling. Only a canonical
video URL is handed to installed official YouTube on the current active external
display. No browser/phone fallback, embedded player, API key, Google login/token/
cookie access or child data in the URL. YouTube owns its account, advertising,
recommendations and history; these surfaces are not restricted to the local list.
Premium benefits are determined there, not certified by this app.

Before video, stop local audio/listening and pause DeX Kids journey/GPS/POI with
the owner's explicit authorization. Preserve manual GPX pause and resume only
on parent confirmation that YouTube has stopped. A durable hold prevents local
audio from resuming after Activity recreation/process loss. The app cannot stop
official YouTube or detect its playback end. It does not disable system GPS or
change navigation application settings. Actual vehicle navigation-audio coexistence
is still NOT VERIFIED.

Delete-library disables local YouTube handoff and deletes its catalog. It does
not delete Google history or sign out of Google. Details:
[T-071 official YouTube design](T071_OFFICIAL_YOUTUBE.md).
