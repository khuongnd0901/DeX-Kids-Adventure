# Desktop visual OpenGL smoke implementation

Two deterministic desktop smoke modes will be executed in CI with Xvfb software OpenGL:
1. 1280x720 simulated journey, 120 rendered frames, PNG screenshot.
2. 1920x1080 timestamped synthetic GPX replay, 120 frames, PNG screenshot.

Both screenshots are artifacts of actual OpenGL rendering, **not Samsung DeX acceptance**.
The game exits after capture with explicit CLI flags; normal Android and desktop runs remain unrestricted.
CI result PENDING until workflow completes. Screenshot contents and visual seam/quality inspection remain separate acceptance tasks.
