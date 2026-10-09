# T-003 M2 animation model checkpoint

Added pure Java CharacterAnimationController with explicit narrator-active gate, travel/stationary/wave/sleep/surprised states, time-derived facial animation. Default playback never impersonates verified Vietnamese narration.
VehicleMotionModel uses distance-derived wheel rotation (frame-rate independent), smoothed acceleration for bounded pitch and speed-scaled bounce.
JUnit test sources cover transitions, spoken-frame gating, invalid inputs, wheel continuity, zero-speed bounce, suspension clamping.

**Status:** Implementation authored. CI and device rendering are not verified for this commit until successful Actions run. T-003 remains IN_PROGRESS.
