# T-053 evidence — PARTIAL
Owner Main; base origin/main 8ee864d; branch feat/T-053-windows-child-outfits-qa.
M0 synchronization verified; unrelated historical untracked evidence preserved.
M1 existing Windows emulator-5554: DeX_Stability_API35_20261005, Android API35, physical1920×1080/density160. Windows tools D:\Android\Sdk. No new AVD/server installed.
M2–M9 pending. No visual/runtime/build PASS claim at this checkpoint.

M2 baseline: all seven requested command groups exit0 in existing CairoSVG/Pillow venv; initial system Python PIL missing resolved by reusing /tmp/t051-art-venv. Logs and timings baseline-results.json. Core tests 193 PASS. Debug APK and instrumentation APK build/install PASS. Instrumentation captured72 actual FullHD Android screens before correction; visual acceptance FAIL (face/headband and fixed-body overlays).

M3/M4 corrected:96pose-specific story/passenger overlays, fabric shading retained, eyes/mouth excluded, skirt geometry follows crouch, crowns anchored to original hat. Passenger overlay uses EXACT same alpha>=128 crop and base aspect. No original child source PNG/VehicleLayout/rotation timer modified. Six source palettes plus distinct sun/circuit/web/bolt/shield/moon and flower/wave/sky/leaf/star/lavender motifs. No new cape/headband across face. Atlas1page2048×2048 RGBA16MiB (previous8MiB).
M5/M6 Android fixture PASS: six unique samples perchild across simulated240s boundaries; same atlas object; play clock pauses/resumes; fresh/swapped/correct/custom/restart preference cases and per-focus ages5/4/min PASS. Actual72post-fix FullHD captures in screenshots/after. Agent reviewed all48story variants and24pairedbus cases using crops from these actual Android captures, not desktop-render proof. Further fallback/recreation/normal-gameplay performance and native single-audience HUD checks pending.
Baseline cumulative post-fix sample20.20FPS/P95upper62ms/19drawcalls/10managedtextures/PSS64260KiB over69.22renderseconds includes frozen fixture captures; not controlled gameplay-only comparison or physicalDeX result.
Retained failures: missing systemPIL(reused existingvenv); generated Python indentation error corrected; first face-alpha assertion failed then renderer masks strengthened. No assertion removed.
