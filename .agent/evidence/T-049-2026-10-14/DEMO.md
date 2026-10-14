# T-049 — 30-second Sâu/Ong demo clip

Owner Main; dependency T-048 merged into main. User requested 30seconds with sound
and questions calling both Sâu and Ong. Local recording acceptance met.

## Delivered artifact

`D:\DeX-Kids-Adventure\build\demo-recordings\Sau-Ong-demo-30s.mp4`

30.00s 1920×1080 H264 video, AAC stereo 48kHz; 15,319,624 bytes. SHA256:
`78d4be1d34e4c1753c92255a2c957ac2f2f59e5295548493d49e2ad79b4482bb`.
MP4 video export uses 30fps; this is an encoding setting, not a physical FPS result.
Actual Windows API35 emulator capture, not physical Samsung DeX.

- First segment: “Sâu ơi, con chim có thể bay bằng gì nhỉ?” (existing sau-bird).
- Second segment: “Ong ơi, bánh xe tròn tròn! Con làm vòng tròn bằng hai tay được không?” (existing ong-round).
- Both children and new composite bus are visible; native question/answer overlays
  and source pose animations remain. Representative source frames 12/54 seconds
  inspected for the two prompts. Original catalog/body/story source unchanged.

## Capture and editing

Natural BOTH DEMO, opening offset 7, existing offline speech/ambient/effects enabled
for capture, child mic OFF. Temporary parent settings backed up locally and restored
after capture; ParentActivity reopened. No child microphone, provider or GPS capture.
Actual TTS engine logs showed embedded vi-vn-x-vic-lstm-embedded dispatch for
Vietnamese speech. No new synthesized voice track was added during editing.

Official scrcpy 5.0.1 Windows archive SHA256 verified against release asset digest:
`b12a2c4ee8be317422451fc7dcf8ee20a71b5ea7ef9ad73ddd825a25316227a5`.
Capture output/audio source and MP4 options follow [official recording docs](https://github.com/Genymobile/scrcpy/blob/master/doc/recording.md)
and [official audio docs](https://github.com/Genymobile/scrcpy/blob/master/doc/audio.md).
Audio source output records Android playback, not microphone. Recording command:

```text
scrcpy -s emulator-5580 --no-playback --no-window --no-control --audio-source=output --audio-codec=aac --video-bit-rate=8M --max-fps=30 --time-limit=78 --record=source-both.mp4
```

Recorded 78.01seconds of natural playback, then selected source intervals 8–23 and
50–65 seconds. Two 15-second segments were concatenated; no production timeline
change. FFmpeg 7.0.2 (imageio-ffmpeg 0.6.0) exports H264 CRF 18/veryfast and AAC 160k,
with faststart. Recorded speech was quiet at the emulator's existing volume;
postprocessing loudnorm target -16LUFS/-1.5dBTP raises the file audio while copying
video unchanged. Actual loudnorm output -16.29LUFS/-4.35dBTP, with a 0.3s fade-out.
System media volume unchanged. No fake speech or re-created animation.

Media validation: final container 30.00s, decoded PCM audio 29.9733s (AAC framing),
non-silent RMS 2742.85/peak 19594 in 16-bit mono decode. Full final video/audio decode
completed; blackdetect found no black interval >=0.05s at its configured threshold.
This is scoped media verification, not continuous hardware/render performance or
human speech quality signoff. Source/working files and private prefs backup remain
in ignored build/demo-recordings/T-049/. Final video stays local, not uploaded.

Source publication completed separately: PR #55 merged 8260fe1; merge receipt pushed
at 2fec808. Main local checkout fast-forwarded, all five original T-045 documentation
additions restored without committing them; backup stash retained. No new app code
or automated software test for this recording request.
