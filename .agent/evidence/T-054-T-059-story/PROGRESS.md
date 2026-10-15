# T-054–T-059 implementation evidence

Owner Main; base b5b5423; feature feat/T-054-ai-interactive-storytelling.
All task acceptance remains IN_PROGRESS. Physical DeX NOT VERIFIED.

T-054: immutable schema1 DTO, bounded strict RFC JSON parser, deterministic validator,
4 distinct scene fixtures for SAU5/ONG4/BOTH, 3 age-targeted questions, fiction labels,
finite local visual vocabulary, malformed/duplicate/unknown/private/unsafe rejection.
Independent AI review and parent approval are separate Android revision gates.
Core tests passed with JDK17; exact aggregate in core-counts.json.
Fixtures are locally authored and are NOT evidence of a live provider review.

Retained failures: initial local plan absent until fetch/ff-only; default JDK25
incompatible with Gradle; build from case alias lowercase path failed D8 root check;
concurrent Gradle agents raced shared output/cache. Builds now serialized using
actual /mnt/d/DeX-Kids-Adventure path. No CI created or executed.

Windows existing AVD started; API35 emulator-5554,1920x1080,density160,SwiftShader.
Windows adb uses port5038 to avoid Linux adb server collision. Runtime checks pending.

Verified Android checkpoint:
- `build-qa-candidate.log`: core207/0failed, Android unit NO-SOURCE,
  debug+instrumentation APK BUILD SUCCESSFUL; actual Windows install successful.
- `python-contracts.log`: all existing local Python contracts PASS.
- `t056-library-cached.log`: real55-story SQLite, unheard-first +100 seeded
  recent-avoiding selections, approval/review/undo/delete/purge/cursor/reopen
  and UI-thread storage refusal PASS. Old uncached run explicitly aborted.
- `t058-images-final.log`: cache/parent-cost gates, SHA256 corruption, disk/count
  bounds,20 replay reads with zero adapter calls and unsupported fallback PASS.
- `protected-art.json`:33 protected source hashes unchanged.
Source/fixtures are not a substitute for live-AI, UI and long-run acceptance.
