# Story library storage

T-056 uses Android platform SQLite in the private `story-library-v1.db`, independently of disposable creative lesson packs. Version 1 is a new database: there is no prior interactive-story schema to migrate. A future unsupported database version fails closed and preserves the database instead of dropping parent content. This is not evidence that a future migration has been implemented or tested.

Episodes retain exact immutable revisions, approval status, favourites and revision-bound playback cursors. Editing saves a new unapproved revision and repairs local visual cues. An offline edit remains a draft until the independent AI review succeeds online and a parent approves it. Replay of an approved revision uses local data and never calls authoring or image APIs.

Selection filters approved episodes by audience, avoids the five most recent episodes when alternatives exist, prioritizes unheard eligible episodes, then chooses through a seeded random source. Starting valid playback updates played metadata.

Soft deletion allows restoration. The deleted-story menu also offers separately confirmed permanent deletion, which transactionally removes revisions, playback and the episode so the slot and JSON budget are reusable. SQLite reuses freed database pages; filesystem shrink is not promised. Sprite-only schema v1 creates no downloaded media files requiring orphan cleanup. Limits are 200 episodes, 100 revisions per episode and 16 MiB of stored JSON. Uninstall removes the private library; force-stop/process restart preserves it. App backup is disabled by the existing manifest.
