# M6 — dynamic illustrative scenery from offline POI context

## Source and behavior

- `PoiSceneDirector` selects an **illustrative** backdrop from a *source-backed* `OfflinePoiEngine.Notice`: PARK → trees/flowers; RIVER → waterside props; BRIDGE → stylized bridge; MUSEUM/LANDMARK → buildings/lamps; NATURE → countryside; all other existing fictional neighborhoods remain seeded and deterministic.
- Only `NEARBY` / `APPROACHING` candidates with POI confidence >=0.55 and within 200m can trigger. `PASSING_CANDIDATE` NEVER triggers: a close centroid is NOT proof of driving over a real bridge.
- On acceptance, the visual transition is anchored to **world chunk index ahead of the bus** (`firstChunk(distance)+2`). It blends themed artwork on top of the deterministic base over 2.5 seconds. New types **queue** until the current themed art fades out; no abrupt texture swap. No new textures or network lookup at render time.
- Theme fades out after 26 seconds or >=800m of traveled distance. GPX backward seek/reset clears it. Opening the parent menu sends zero simulated render delta and freezes the transition. Only alpha and theme metadata are retained in memory, never raw GPS history.
- **Preview**: the HCMC source-cross-checked, *NOT manually approved*, four-POI pack is enabled only after selecting **Start HCMC sample journey (preview)** on the same Samsung DeX display. GPX simulated events and the renderer label are always tagged `GPX SAMPLE`. Human-approved LIVE POI pack is empty by default; without POIs, the game continues its unchanged deterministic fictional world.
- Includes one new project-authored water-ripple SVG (`river_water`) in the existing single-page 2048px atlas. Uses only project-authored sprite atlas assets, retains 6 bounded loaded world chunks and max 4 themed chunks. POI lookup is pre-indexed/throttled by M4.

## Reproduce in desktop software OpenGL (no phone required)

```bash
gradle --no-daemon :core:test :desktop:classes :android:assembleDebug :android:assembleDebugAndroidTest
python3 tools/test_hcm_sample.py
# Software-GL Xvfb CI executes:
xvfb-run -a -s "-screen 0 1920x1080x24" gradle --no-daemon :desktop:run --args="--gpx test-data/gps/synthetic-hcm-poi-loop.gpx --sample-preview true --start-seconds 170 --smoke-frames 150 --width 1920 --height 1080 --screenshot build/visual-evidence/m6-hcm-preview-1920x1080.png"
```

The real CI screenshot is an Xvfb/Mesa software OpenGL image, **not physical Z Fold3 performance**. `--start-seconds` is an explicit *desktop smoke-only* preseek to bring the synthetic track near Tao Đàn within the 150-frame visual test; it does not change any driving/Android behavior.

## Automated acceptance gates

| Gate | Test / evidence | Status |
|---|---|---|
| POI type → biome mapping (all six types) | `PoiSceneDirectorTest` | Authored |
| Never apply events with poor accuracy/low confidence/far distance/PASSING_CANDIDATE | Pure JUnit tests | Authored |
| Theme enter/exit soft fades, no snap on switch | Pure JUnit tests | Authored |
| Same seed/GPX produces same theme event sequence | `HcmSceneJourneyIntegrationTest` | Authored |
| Parent menu freezes fades, GPX reset clears active scene | Pure JUnit tests | Authored |
| Android and desktop compile + Xvfb 1280/1920 render | GitHub Actions | Pending |
| Source truth: no real route/map matching or field-verified point crossings | Human review | NOT VERIFIED |
| Actual Samsung Fold3/DeX hardware FPS and input/audio | Physical QA | DEFERRED BY OWNER |

## Out of scope / follow-up
- Verified road/segment map matching, object-accurate rivers, time-to-intersection, official landmark placement and real terrain synthesis.
- Spoken claims about actually crossing a particular bridge. Only originally sourced narrative cues from M5 may speak, subject to consent.
- Asset-level animation/skeletal physics and benchmark 30 FPS on Z Fold3; those remain separate M2/M9 gates.
- Production content review: `assets/poi/reviewed.tsv` still has **zero human-approved locations**.

Task T-007 stays **IN_PROGRESS** until visual review and actual runtime acceptance.



## Verified M6 automated checks — 2026-10-09
- [CI #37907139530](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37907139530): new `PoiSceneDirector` JUnit PASS.
- [CI #37907599024](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37907599024): deterministic synthetic GPX → source-checked HCMC POI → M6 theme integration test PASS.
- [CI #37907989363](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37907989363): **SUCCESS** including M6 water SVG, 30-region atlas alpha checks, Android debug/AndroidTest builds, desktop 1280x720+1920x1080 OpenGL and **third 1920x1080 HCMC theme preview screenshot**.
- [CI #37908099168](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37908099168): **SUCCESS** after asset license documentation.
- M6 screenshot artifact ID **11605695877** (`m6-hcm-preview-1920x1080.png` and 10 frames); atlas **11605511141**; debug APK **11605336770**.
- Actual 1920x1080 screenshot reviewed: PARK theme reaches **100%**, bus/Capybara render visibly; the preview banner clearly identifies synthetic GPX and makes no factual road claim. Source screenshot also showed new illustrative label slightly below the HUD background; corrected by `96b0ff14` (CI pending at this checkpoint).
- No physical Samsung Fold3/DeX benchmark or approved named-POI field validation claimed.


### Final HUD-fixed source acceptance — 2026-10-09
- Commit `96b0ff14` extends the HUD background to include the clearly readable **ILLUSTRATIVE SCENERY: PARK (100%) [GPX SAMPLE]** label.
- [GitHub Actions #37908584642](https://github.com/khuongnd0901/DeX-Kids-Adventure/actions/runs/37908584642) **SUCCESS** after that HUD fix, with core JUnit (including POI-to-scene deterministic replay), Android debug/AndroidTest builds, atlas alpha check, three real desktop Xvfb/Mesa render captures.
- Screenshot artifact **11606126138**, APK artifact **11605727094**, sprites artifact **11605796955**. Actual 1920×1080 HCMC screenshot visibly confirms the PARK 100% themed scene with the corrected HUD label inside the background; sampled FPS and P95 in that image reflect **software GL**, not Z Fold3.
- Dynamic theme code reaches its **software CI acceptance gate**; the T-007 **milestone remains IN_PROGRESS** pending full Android runtime/visual review, verified LIVE geographic content and postponed Fold3/DeX physical tests.
