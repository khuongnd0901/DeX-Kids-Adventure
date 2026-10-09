# M3 core synthetic GPS/GPX

18 journey tests actually PASS in full56-test suite. Existing synthetic-urban-short.gpx
parsed; deterministic repeat/reset, pause/resume, terminal timeline/distance/speed, stationary
track, duplicate/out-of-order timestamps, invalid coords/delta and implausible jump exercised.
Live-feed core loss/recovery uses an explicitly synthetic injected clock. Original11-second
loss/jump regression was executed and FAILED before fix; not a GPS radio or soak measurement.

Fixed two bugs: completed GPX kept nonzero speed; rejected fixes refreshed freshness and kept
old motion after signal loss. Regressions and related core/build tests PASS.
Evidence: gpx-terminal-before.log/.xml, live-loss-before.log/.xml,
expanded-regression-build.log, recreation-continuity-build.log and independent
gpx-replay-20261009T020940Z-11878/ (all under build/simulator-artifacts).

GPX replay is desktop/core, not wired as Android parent option. Emulator actual long journey
uses DemoJourneyFeed, not GPX replay. ADB GPS injection NOT_RUN; real GPS radio accuracy,
quality under movement and Android LIVE-GPS external-display behavior NOT_VERIFIED.
Live accepted-fix distance still advances by fix increments; full geographic interpolation,
playback-rate UI/stop control and process-death continuity remain open.
M3 core simulator subset verified; milestone IN_PROGRESS, never claims real GPS accuracy.
