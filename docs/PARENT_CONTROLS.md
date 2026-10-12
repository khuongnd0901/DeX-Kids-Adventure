# Parent controls on one display

The dashboard and child adventure use the same display. Mouse hold on Parents,
F10, Menu or Back pauses the adventure and opens parent controls. The session
wall-clock deadline continues during a pause and cannot be replenished by
Activity recreation.

## Optional PIN

Choose **Set parent PIN**, enter matching 4–8 digit values, and keep the PIN with
the parent. When enabled, dashboard entry and the in-game parent menu require
it. Cancel returns to the journey; three wrong attempts require a 30-second wait.
The dashboard locks on leaving it and after recreation. A parent can change the
PIN from an unlocked dashboard; removal asks for the existing PIN again.

Only a random salt and PBKDF2-HMAC-SHA256 digest (120,000 iterations) are stored
in app-private preferences; Android backup is disabled. Hashing runs off the UI
thread. This is an in-app parental gate, not an Android kiosk or device-owner
restriction. There is no network recovery or default bypass PIN. Clearing app
data through Android settings resets all local preferences if the PIN is lost.

## Audio-only

Enable **Audio-only** on the dashboard before starting. Moving scenery is hidden;
the foreground journey, reviewed/sample caption selection, controls and session
deadline continue. It does not enable speech automatically: the parent must also
allow offline speech, switch quiet mode OFF, and have an offline Vietnamese voice
installed. Without a suitable voice, captions remain and no online voice is used.
Backgrounding or opening parent controls stops speech as before. Audio-only is
not a background playback service or a measured battery-saving guarantee.
