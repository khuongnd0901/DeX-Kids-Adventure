# T-064 — expand and publish the sample story library

Owner Main; dependencies T-062 remote pack and T-063 bundled capabilities.
User requested many additional sample stories using existing animations, then
commit and push to GitHub. Work on feat/T-063-story-actions-props including the
verified pending T-063 implementation needed by the new pack.

Append to docs/story-source/stories.example.json, preserving its three existing
objects and IDs/revisions exactly. Include the three existing expanded showcases
and eight new fictional story topics with SAU/ONG/BOTH variants: 30 total.
Use exact bundled actions/props; questions refer to narration, fair BOTH turns,
no new locations/assets/animation names or requests for physical vehicle movement.
Document APK capability requirement and protect authored content from accidental
sample-regeneration overwrites. Keep the three-action-showcase expanded file.

Acceptance: production pack validation; preserved originals/unique IDs/audience
balance and semantic cue audit; core and Android/desktop checks; reviewed scoped
commit, publish branch and verify remote tree equals verified local tree. Record
publication receipt and update TASKS/STATUS. Prior T-063 Windows results apply to
unchanged renderer; physical DeX remains NOT VERIFIED. No merge requested here.
