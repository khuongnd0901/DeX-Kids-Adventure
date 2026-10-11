# T-026 current build and route-test preparation — 2026-10-10

Owner Main. Source HEAD 6248d864013e1ee14bf28e34896dff85713b96f3.
Current debug + AndroidTest APK build SUCCESS; local contracts PASS;
100 core JUnit tests, zero failures/errors/skips. Android unit NO-SOURCE.
JDK17 at /home/kunrg/.local/opt/jdk-17.0.20.1+1; Android SDK35.
Initial attempts failed due unset JAVA_HOME and missing CairoSVG; resolved
with JDK17 and temporary isolated Python dependencies (CairoSVG2.8.2/Pillow12.3.0).
No application source change. Build logs and local test log retained here.

Debug APK SHA256: b4307d0a79d44cb41894eb8514dd2e0d7d9013e41be271edf7095ba0e95b3797
AndroidTest SHA256: a12905770d5f8b2ff81f006809b7e8460644a22a6ba52629df3383ef2312460f

26 requested-region points and 44 full-catalog IDs prepared with exact existing
coordinates/source IDs; unique counts and schedules within1800s checked.
Runbook docs/T026_30_MIN_ROUTE_TEST.md; manifests test-data/gps/T-026/.
ADB sees SM-F926B 192.168.1.3:34741. No install/session/GPS injection performed.
30-minute route runtime NOT_RUN. Requires test-only per-point fixture harness:
normal GPX reads empty reviewed catalogue; LIVE/HCMC catalogues are separate;
teleport guards must remain intact. Full44 dialogue cannot fit 30minutes.
