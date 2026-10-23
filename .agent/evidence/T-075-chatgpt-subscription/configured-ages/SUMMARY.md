# T-075 follow-up — configured ages in AI prompts (2026-10-23)

Owner Main. Requested slice VERIFIED with mocks/Windows simulator; overarching
subscription live/physical gates remain open and PR66 stays Draft.

## Behavior

All age-specific AI prompts use parent-configured4–7learning ages.5/4remain
unchanged defaults. BOTH/shared targets min(Sâu,Ong), including when Ong is older.

- StoryAuthor snapshots configuration per generate/regenerate/review operation.
  Prompt text, schema example and requested metadata use the same ages. Generation
  rejects a model's wrong ages; a config change during author/review invalidates
  output. Regeneration uses configured metadata in its new explicit draft, keeping
  unselected scenes unchanged. Review of an existing episode rebases only the
  request copy; it does not modify the saved episode or auto-approve it.
- Trip generation carries configured Sâu/Ong/shared ages and both min/max card ages.
  Deterministic validation and cache loading use those same ages. The public legacy
  validator overload retains default5/4behavior; production passes actual ages.
  Incompatible cached files remain on disk. If catalog has no eligible content for
  a requested focus, no model request is dispatched and no acquisition retry occurs.
  Bundled curated catalog max6is not silently treated as age7content.
- Creative generation/review explicitly state the configured session ages already
  passed by the coordinator, retaining per-lesson targetAge/focus validation.
- POI quiz/child-reply prompts state the actual requested age. Production callers
  already resolve it from settings; childReply's omitted-age overload now does too.
- Story validator accepts supported4–7ages, still rejecting3/8and preserving every
  existing text/privacy/action/prop/schema check. learningAge(BOTH) uses the younger.
  No saved-file/schema migration, new consent, auth transport or YouTube change.

## Verification

**PASS** core293tests + Android35tests,0failures/errors/skips; final app/test APK
build and full lint0errors/89warnings. **PASS** scripts/test-local.sh and targeted
age/subscription/creative contracts. Environment matches original T075evidence:
existing QA venv, JDK17, Linux SDK35 and installed NDK27c. Commands:

```bash
./gradlew --no-daemon --max-workers=2 :core:test :android:testDebugUnitTest \
  :android:assembleDebug :android:assembleDebugAndroidTest :android:lint
bash scripts/test-local.sh
```

Windows AVD `emulator-5580`, API35, external display2/1920×1080, original signing,
install-r(no uninstall/data clear). [Configured-age fixture](t075_configured_ages.txt)
**PASS** through actual StoryAuthor/KidsAiGateway/TripPackStore with synthetic
transport for7/6,4/7,6/4and defaults5/4. It inspects author/example/regenerate/review,
trip generation, creative generation+batch review and child default-reply prompts;
checks age metadata/rotation/shared minimum, model-age mismatch, mid-operation
configuration change, old immutable episode, cached-age mismatch/file preservation
and no dispatch for unavailable curated ages. Core tests additionally cover all16
age pairs across all3audiences and configured card eligibility.

Regression fixtures **PASS**: subscription/storage/consent/settings;
coordinator/last-good/no-replay;42private story inputs; author/review cancellation
and offline saved replay. Individual receipts and XML-derived counts/artifact
hashes are in this directory and [verification.json](verification.json).

Installed APK pulled and hash matches final build
`9c802b39557c0471f48bae03010ed25e2fd4e727d91110b178d047df20780261`.
Original38private records backed up for this follow-up (not reused from earlier
QA). After restoring QA-advanced source/DB metadata with app stopped,37/38original
records exact; only ART profileInstalled differs.0missing; credentials/settings/
saved story database preserved. Private backups/APKs remain outside repo.

Initial new core test failed because synthetic WORD prompts were duplicated;
fixture corrected to unique prompts and full core/Android/local suite rerun PASS.
No real account/network inference, audio, full-story run, new visual change or
physical Fold3/DeX claim. Earlier181s rendering evidence is historical T075scope,
not a new rendering campaign for this follow-up. Live sign-in remains deferred.
