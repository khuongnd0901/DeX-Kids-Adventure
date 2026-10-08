# DeX Kids Adventure - current verified status

Updated: 2026-10-08
Current milestone: M0 bootstrap; T-001 IN_PROGRESS until runtime/privacy acceptance.
Related M1 T-002: IN_PROGRESS; code-drawn world demo is not final game art.
Development branch: feat/T-001-bootstrap-libgdx
Verified source SHA: 554cb5f002921687607da5d079c6e7efc4d05b3f

## VERIFIED
- Connected GitHub user has admin and push permissions for repository.
- Project source + docs + Gradle wrapper are pushed, PR #1 open as DRAFT.
- GitHub Actions run #37795183419: SUCCESS for Gradle :core:test, :desktop:classes, :android:assembleDebug.
- CI APK artifact dex-kids-debug-apk id 11557614153 (3,432,409 bytes) uploaded.
- Archive digest sha256:28695b115e8e632beba7237551539f38246469b8ec7df4c3f895331369a74aba.
- Exact evidence: .agent/evidence/T-001-m0-ci-2026-10-08.md.

## NOT VERIFIED / BLOCKED
- Desktop OpenGL runtime or screenshot; emulator app launch and physical Samsung Fold3/DeX.
- Actual GPS, OSM POI, Vietnamese narration, parent controls, Android IPC, animation asset packs, audio focus, long-running frame rate.
- Real-world 30 FPS and 60-min soak metrics (not measured).
- Environment of this chat has JDK21 but no local Android SDK/Gradle; CI verified build only.
- Repository visibility PUBLIC whereas baseline asks for PRIVATE. Owner must change visibility in GitHub repository settings.

## Next actions
1. Reproduce on Ubuntu laptop: ./gradlew :core:test :desktop:run and ./gradlew :android:assembleDebug.
2. Install debug APK on Fold3, capture launch/runtime evidence; do not modify unrelated DeX Assistant.
3. Add FPS/P95 frame timing, screenshot validation, and test chunk continuity for T-002.
4. Keep T-001/M0 IN_PROGRESS until runtime smoke and privacy decision. Start T-003 after T-002 gate.
5. Keep PR #1 draft until its acceptance tasks are met.
