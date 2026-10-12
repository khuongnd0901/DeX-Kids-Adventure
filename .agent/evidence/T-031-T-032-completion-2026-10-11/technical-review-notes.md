# Main review notes — actual Gemini output

Machine approval is not parent approval. The 48-row age matrix and the real
runtime refresh should be reviewed together. Content is retained verbatim.

Points for parent review:
- 6/7 item 2 translates cat as mèo con; kitten would be the precise young-cat
  term. cat is normally con mèo.
- 8/10 item 13 explains a dry leaf floating by air in veins; this is a simplified
  explanation and is not a reliable general explanation of buoyancy.
- Runtime refresh 5/4 item 14 has an imaginary whale but vocabulary fish (con cá);
  whale is cá voi, a mammal. Do not imply a whale is a fish.
- Some imaginary story prompts ask an open invented question; the spoken ending
  is one fictional possibility, not a fact a child is expected to know.

These observations do not justify overriding the independent reviewer or
claiming factual correctness. Final human content acceptance remains open.

Runtime refill issues before final prompt corrections:
- Item 4 says a chair has a table on top, inconsistent clue.
- Item 6 identifies sunflower from yellow/summer clues, not unique.

These samples are NOT accepted final content; final generation/review must be
rerun with explicit vocabulary, premise and distinctive-clue checks.

Final-candidate matrix after validator codes passed machine gates, but Main
found sunflower-always-tracks and whale-water-spout misconceptions. Preserve
that provider log; regenerate parent content after targeted factual guidance.
Sources: [UC Davis sunflower research](https://www.ucdavis.edu/news/sunflowers-move-clock),
[NOAA whale blow research](https://www.fisheries.noaa.gov/feature-story/workshop-targets-high-tech-ways-assess-endangered-whale-health).
Mature sunflower heads stop daily tracking; whale blow contains exhaled air and
condensed water vapor. These specific corrections are not factual proof for all
future model-authored content.

## Post-publication live QA

Final concrete-reason matrix FAILED after719280ms: accepted16 rows each at5/4
and6/7, but8/10 review rejected ANSWER_MISMATCH then allfive retries failed
LESSON_ANSWER_LENGTH_OR_TEXT_INVALID. Reviewed feedback demanded English words;
new guidance requires a full Vietnamese sentence naming the English word,
not a short bare word. Validation feedback now carries safe numeric lengths.
Do not claim the new generation has passed before rerun.

Latest26 HUD screenshots/14cues PASS,179355ms,11theme transitions; eight
tracked texture handles/dimensions/formats unchanged. See visual-review-latest.md.
Audience audio FAIL301066ms: SAU/ONG each3completed,voiceUnavailable0; test
required BOTH allthreefocuses within firstthree beats. nextJourney substitutes
focused learning cards and firstsharedturn is at beat9; test must observe9beats.
Audio playback PASS12845ms: native offline VN onDone,localtone,volumerestored;
nohumanlistening/navigation/drivingclaim.

## Latest 48-card answer-retry matrix

PASS184060ms:16cards each5/4,6/7,8/10, bounded structural/age/factual
retries, creative revocation true. Model and validator are actual production
code. Main review still flags wording for adult acceptance:
-5/4 row12 (1-based): asks specifically for a goldfish but teaches general fish.
-6/7 row4: red peel/many small seeds/sweet does not uniquely identify apple.
-8/10 row2: goat vocabulary translated more narrowly as mountain goat.
-8/10 row9: pencil case clue assumes a long cylindrical case, one possible shape.
These are not a claim of perfect factual/age acceptance; parent review pending.
Thunderstorm advice (8/10 row11) broadly agrees with NWS indoor-lightning guidance,
but the official distinction is corded equipment and substantial safe shelter:
[NWS Lightning Indoors](https://www.weather.gov/safety/lightning-indoors).

Current build environment restored outside the repo in
/home/kunrg/.cache/dexkids-qa/jdk17 and art-python; SDK/home/kunrg/Android/Sdk.
Final build and21contracts/145coretests PASS. Old/tmp directories were absent.

## Reviewer batch/lexical guards and volume evidence

Third full-pack approved matrix PASS131411ms, but Main again found incorrect
acorn/chestnut/glasses translations and desk ambiguity. Not final content.
Use4cardreviewbatches withoriginalindices, allmustapprove; reject selected
known wrong word/meaning pairs while accepting alternate/new meanings.
148coretests pass,0fail/error/skip; targetbuildpasses.
Primary lexical references: [Cambridge magnifying glass](https://dictionary.cambridge.org/dictionary/english-vietnamese/magnifying-glass)
(kính lúp); [Cambridge acorn](https://dictionary.cambridge.org/dictionary/english-vietnamese/acorn)
(search indexed, direct fetch403; do not claim direct full-page inspection).

Durable BOTH audio result FAILED353945ms solely at final volume assertion;
9completedbeats,4SAU/4ONG/1shared,voiceUnavailable0; priorTTSduck/F10resume/
SoundPooldispose assertions passed. AudioService logs22:10:00–22:10:02 contain
fiveADJUST_LOWER commands fromandroiduid1000 andDeXSystemUI showing volume UI.
Production source has no setStreamVolume/adjustStreamVolume calls. Preserve
FAIL andaskwhether parent changedvolume; targetedvolume smoke pending.
No automatic user-volume reset.
