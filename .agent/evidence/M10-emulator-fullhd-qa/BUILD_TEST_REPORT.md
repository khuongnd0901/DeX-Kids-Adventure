# M1 build/test — PASS with scope limits

Base main: `1b9f962`; T-044 source modifications tested locally. JDK17, Gradle8.11.1, compile/targetSdk35, Linux SDK build-tools35; Windows emulator37.1.11 API35 x86_64.

Executed in pinned art virtualenv: `python3 tools/build_sprites.py`, `python3 tools/test_sprite_atlas.py`, `python3 tools/test_m10_art.py`, `python3 tools/test_unlimited_session.py`, `bash scripts/test-local.sh`, Android task discovery, `./gradlew --no-daemon :core:test :android:testDebugUnitTest :android:assembleDebug :android:assembleDebugAndroidTest`, `python3 tools/render_m10_gallery.py`.

167 core JUnit tests, zero failures/errors/skips. Android unit task is **NO-SOURCE**, not Android unit-test PASS. Android debug/test APK build and installation PASS. Final logs: `local-post-content-guard.log`, `gradle-post-content-guard.log`; prior failures retained.

Initial missing CairoSVG/Pillow resolved in `/tmp/T044-art-venv` using pinned requirements, without CI changes. Safe debug dispatch logging required updating the existing privacy guard to allow only three exact log expressions; arbitrary gateway logging remains rejected.

Art:30 legacy regions,92/172 sourced M10 SVGs,3 full atlas pages,48 prop regions on1 resident prop page. Repeated generated M10 outputs byte-identical (`art-reproducibility.json`). Actual Android GL TextureAtlas load/dispose PASS92 regions/3 pages and48 props/1 page (`final-m10-atlas.log`). Loading is not gameplay visibility or human art approval.

Final APK hashes/core counts recorded in `final-build-fingerprint.json`; soak APK has a separate fingerprint. Original unrelated local changes preserved; no CI or production credentials modified.

Final APK offline cached smoke30s:accepted existing reviewed5/4 pack,one spoken resolution,voiceUnavailable0,three display checks. Harness FAIL only FPS37.16/P95upper33ms (`final-apk-cached-smoke.log`); not a repeat60min benchmark. Final credential cleanup restored original AI settings,zero encrypted key entries/no secret fixture; source/evidence/decompressed APK credential scan PASS.

Raw .log files, screenshots and per-sample dumps remain local under this evidence directory (Git ignore policy retained). Versioned `execution-results.json` preserves instrumentation result fields; normalized performance and APK fingerprints are versioned separately.
