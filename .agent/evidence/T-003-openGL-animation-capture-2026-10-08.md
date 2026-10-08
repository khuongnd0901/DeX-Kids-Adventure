# T-003 animation evidence capture

Desktop smoke mode now saves ten sequential actual LibGDX OpenGL frames per scenario, plus final full-resolution screenshot. CI assembles the 1920x1080 time samples into a 960x540 GIF preview using Pillow; source screenshots are retained.
The Capybara begins with a one-time *nonverbal* greeting wave. Wheel angle is derived from journey distance; the sprite batch renders parallax clouds/hills, foreground props and day/night lamps. Talking sprite is explicitly gated by narration signal, not simulated narration.
GIF sample cadence is an illustration only, **not** evidence of sustained 30 FPS on Android/DeX.
CI result: PENDING. T-003 IN_PROGRESS; final artwork review, narrator sync and physical testing remain open.
