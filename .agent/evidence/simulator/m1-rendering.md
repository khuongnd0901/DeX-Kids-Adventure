# M1 real Android rendering/lifecycle

AVD ZFold3_API35 / emulator-5580 / Android15 API35. Source at initial UI run923f297.
Actual images manually inspected: Capybara, yellow bus, wheels, buildings/clouds/road visible;
no black/splash-only window, missing atlas regions or visibly stretched bus at720p/1080p.

Evidence: build/simulator-artifacts/functional-20261009T014805Z/:
render-1280x720-160.png, render-1920x1080-160.png, render-1920x1080-240.png,
resume.png and reload-0..4.png; results.json contains actual UI steps/assertions.
All UI assertions except inconclusive shell broadcast passed. Shell broadcast is NOT_VERIFIED,
separate unprivileged APK probe is the actual IPC evidence.

HOME/background and launcher-task foreground resumed child; five actual close/reopen cycles
loaded visible textures. Original1768×2208 foldable uses FitViewport letterboxing; this is not
Samsung DeX display routing. Test wm/density overrides were reset afterward.

A narrow795px HUD backdrop allowed metric text to overflow onto sky. Source widened backdrop;
actual desktop software-GL image verifies layout; final Android post-fix image pending.
Real Activity.recreate/deadline/feed checks prepared as Android framework instrumentation,
pending until baseline soak ends. Do not infer resource disposal PASS from five visible reloads:
a separate ownership regression found Game.dispose omission and fixed it.

Stable30FPS gate remains open: preliminary whole-run P95 histogram upper bound43ms.
No physical DeX/Fold3 acceptance; M1 IN_PROGRESS.


## Actual post-fix Android recreation
Source9c25ae3. Baseline runner `activity-recreation-20261009T025631Z-19410`
actually FAILED with deadline_before=deadline_after=0 and same_feed=false.
Same runner after installing compiled fixes `activity-recreation-20261009T025703Z-19479`
PASS: distinct Activity, unchanged deadline8765104, same_feed=true, distance20.190→49.191m.
Before/after real1080p PNGs manually inspected: visible bus/Capybara/buildings/clouds/wheels;
no black frame or missing atlas, widened HUD contains metric text. Captured early25–35FPS
snapshots are not sustained-performance acceptance. Two earlier instrumentation attempts
failed QA window-root/stale-monitor assumptions; retained, corrected using actual window focus
and a fresh ActivityMonitor before recreation. These harness failures are not product bug claims.

Final source bad299a follow-up completed: actual permission/offline/recreation PASS `privacy-20261009T032118Z/`; final post-fix smoke capture126seconds and GL disposal log PASS. See final-test-summary.md for current gates and restoration.
