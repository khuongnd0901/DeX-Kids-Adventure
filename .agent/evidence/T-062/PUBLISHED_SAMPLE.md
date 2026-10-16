# T-062 — published app-repository sample — 2026-10-16

Owner Main; dependencies T-062 implementation. User requested creating test
content in the app repository and will change HTTPS URLs in the app later.
Sample publication acceptance met; full T-062 physical gates remain open.

Repository confirmed publicly accessible without authentication via GitHub API.
Published only `docs/story-source/stories.example.json` on main through GitHub
connector contents API; commit `d24e0f9011ec22b9b71ff4052988fed9af1dc966`.
No app source or pending implementation was included in this publication.

URL (follows main, editable content without APK rebuild):

https://raw.githubusercontent.com/khuongnd0901/DeX-Kids-Adventure/main/docs/story-source/stories.example.json?source=app

Three authored stories: garden-sau/SAU, garden-ong/ONG, garden-both/BOTH.
13748 UTF-8 bytes; SHA256
`d743184f8ef2a63f751c668fa9a35714d334724a431feb3b06e9d02bdcca0953`.

Verification:

- `:core:checkStoryPack` BUILD SUCCESSFUL;3stories validated.
- Anonymous curl download HTTP200; byte-for-byte equal to local sample.
- Actual `StorySourceClient.fetch` + `StoryPackCodec.parse` on JDK17 downloaded
  and validated3stories from the public URL; next conditional GET unchanged=true
  (304). Exact stdout: `published-sample-https.txt`.
- Git fetch confirms origin/main d24e0f9; diff from previous main adds only sample.
  Local feature branch fast-forwarded to this commit; unrelated pending work kept.
- `git diff --check` used for local documentation additions.

Initial plain Raw URL had been probed before publication and returned404. A
post-publication request hit cached404; fresh `?source=app` and commit-pinned Raw
URLs returned200. This query parameter avoids the prior negative cache while
retaining an editable main-branch source. No actual Android-to-GitHub connection
or physical DeX run was performed in this follow-up. Existing APK already has
URL settings; no new app source changes or rebuild required for this sample.
