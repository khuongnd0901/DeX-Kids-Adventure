
# GPS design

Future Android LocationProvider tries FusedLocationProviderClient when available, with LocationManager fallback.
Use timestamps, accuracies, heading and speed validation; reject teleport jumps and stale fixes; interpolate animation instead of moving sprites on GPS callback.
Distinguish nearby from actually passing a POI; direction/road matching may be necessary.
Replay: timestamped GPX fixtures, pause/loss/jump cases and independent ground-truth checks.
No real GPS integration currently implemented.
