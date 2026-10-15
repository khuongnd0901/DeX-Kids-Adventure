# Retained failed/aborted attempts

- `baseline-build.log`: lowercase workspace alias caused D8 outside-root path;
  initial Kotlin enum `name()` calls failed. Actual-case cwd and `.name` fixed.
- `integrated-build.log`: concurrent agent Gradle output/cache races and
  source-enumeration during addition of new classes; not acceptance.
- `final-build.log`: Android API35 java.nio.Files lacks readString/writeString;
  converted instrumentation to readAllBytes/write.
- `t056-library.log`: process terminated when a second instrumentation run was
  mistakenly started. Invalid run; not a library failure result.
- `t056-library-retry.log`: old uncached JSON implementation consumed >3m CPU
  during55-story/100-selection test. Explicitly aborted for optimization, no PASS.
  Validator safety regexes precompiled; immutable decoded episodes bounded LRU64.
- `t058-images.log`: actual emulator cache/consent/bounds/corruption/replay tests
  PASS; optional real image provider/renderer integration explicitly unverified.
- `t057-manager.log`: AGP overrides the test manifest instrumentation name;
  converted UI validation to a helper under the existing runner.
- `t057-manager-runner.log`: Android renders Button labels in uppercase;
  exact case-sensitive test lookup missed the visible control. Test now matches
  labels case-insensitively; production UI unchanged.
- First1800s LIVE soak (`t059-live-soak.log`) **FAIL**: journey distance did not
  increase. Windows AVD config had `hw.gps=no`; emulator console reported OK for
  injected fixes but Android GPS never delivered them. Main missed this AVD flag
  at startup. No GPS code/quality threshold relaxed. Original config backed up;
  GPS temporarily enabled for real emulator delivery and final rerun. Initial
  AVD process also disappeared after the test; final emulator stdout/stderr now
  captured. Previous result is not accepted as a LIVE GPS trip.
