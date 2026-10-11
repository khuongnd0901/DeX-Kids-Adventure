# T-027 — scenery components, trip quizzes and English vocabulary (ages 3–6)

## Original art

18 new transparent editable SVG sprites in `art/assets-source/props`:
palm, pine, banana tree, rice field, tea bush, flower bed, lily, sailboat,
rocks, market stall, traffic light, road sign, street tree, duck, bird,
butterfly, buffalo, and kite. The existing large 2048² character atlas
is unchanged. `tools/build_extras.py` builds `assets/generated/extras.png`
and `extras.atlas` (1024² RGBA, 4 MiB GPU maximum) as part of Gradle's
`generateKidsArt`. Foreground props are deterministic per 640px chunk,
chosen by biome and drawn after a named POI panorama. They do **not**
claim real-life nearby traffic/animals.

## Questions and English learning

A new UTF-8 TSV `assets/learning/offline-learning.tsv` contains **72 newly
authored questions** and **80 basic English words** with Vietnamese meaning.
This adds to, rather than replaces, the existing 66 entertainment beats.
Topics: sea, city, road, nature, weather, animal, garden, and safety.
Age filtering uses `min_age` and `max_age` (3–6). Ages 3/4 have
simpler prompts; ages 5/6 can receive more advanced cards.
Current Parent audience options are Sâu 5, Ong 4, BOTH 4–5. Shared lessons use age 4; individual lessons in BOTH use age 5 for Sâu and age 4 for Ong. The pack still supports ages 3–6.

`OfflineLearningCatalog` is a fail-closed, bounded offline TSV parser.
Every third normal activity is an authored quiz or vocabulary lesson,
alternating with the existing play. In live GPS mode, the general
5-minute question set also draws from new cards. Accepted POI context
may select a relevant theme for up to 150 seconds, derived from its
**sourced** backdrop ID. No assertion that the vehicle passed the POI.

The Vietnamese TTS voice introduces the question; for word lessons,
an **installed, non-network English TTS voice** can pronounce the word.
If the voice is unavailable, captions and Vietnamese meaning remain.
No new microphone permission, cloud API, analytics, pronunciation
assessment, or transcript collection.

## Local verification only

```bash
python3 tools/test_trip_learning.py
python3 tools/build_sprites.py
python3 tools/test_sprite_atlas.py
./scripts/test-local.sh
./gradlew --no-daemon :android:assembleDebug
```

Open app in Sâu/Ong/BOTH mode, observe new props and natural
age-appropriate activity alternation; verify EN voice installed/offline,
parent pause/audio focus, scene/topic expiration, no unrelated GPS claims.
Measure actual FPS/PSS on Z Fold3 DeX after rebuilding and check card
readability at 1920×1080. No CI configured; physical validation pending.
