# T-031/T-032 audio evidence — Main, 2026-10-11

The parent confirmed personally adjusting DeX volume during the nine-beat BOTH test.
The original failure is retained in regressions/audience_audio-both-volume-failed.log
and audience-audio-volume-failed.json. Its end-to-end volume comparison was
invalidated by external user input; it is not relabeled PASS.

The test completed nine beats: four SAU, four ONG, one shared; voiceUnavailable=0.
Earlier solo SAU/ONG blocks completed three beats each. Production ducking,
F10 pause/continue and disposal assertions preceded the final volume assertion.
Separate audio_volume smoke PASS (13238ms), with stream music volume 10→10.
Native offline Vietnamese narrator/tone playback PASS (12845ms).
No human listening or concurrent real navigation/road acceptance is claimed.
The user's selected volume is preserved.
