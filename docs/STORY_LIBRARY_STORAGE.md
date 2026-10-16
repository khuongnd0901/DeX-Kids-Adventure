# Story library storage

T-056 uses Android platform SQLite in the private `story-library-v1.db`, independently of disposable creative lesson packs. T-062 advances the database to version 2 through an additive migration from version 1: local episode/revision/playback tables remain intact and remote-pack/remote-episode tables are added. Unsupported upgrades fail closed rather than dropping parent content. The story content schema remains version 1.

Episodes retain exact immutable revisions, approval status, favourites and revision-bound playback cursors. Editing saves a new unapproved revision and repairs local visual cues. An offline edit remains a draft until the independent AI review succeeds online and a parent approves it. Replay of an approved revision uses local data and never calls authoring or image APIs.

Selection filters approved episodes by audience, avoids the five most recent episodes when alternatives exist, prioritizes unheard eligible episodes, then chooses through a seeded random source. Starting valid playback updates played metadata.

Soft deletion allows restoration. The deleted-story menu also offers separately confirmed permanent deletion, which transactionally removes revisions, playback and the episode so the slot and JSON budget are reusable. SQLite reuses freed database pages; filesystem shrink is not promised. Sprite-only schema v1 creates no downloaded media files requiring orphan cleanup. Local-library limits are 200 episodes, 100 revisions per episode and 16 MiB of stored JSON. Uninstall removes the private library; force-stop/process restart preserves it. App backup is disabled by the existing manifest.


T-062 remote content uses separate `remote_pack` and `remote_episode` tables, so
publisher IDs cannot overwrite local IDs. Each configured HTTPS source has up to
100 episodes and a 4 MiB pack; at most three source caches are retained. Snapshots
and compiled episode JSON both occupy SQLite storage, separately from the local
16 MiB budget. Removed remote episodes are discarded only during idle atomic
activation; unchanged content preserves its local revision, favourite and cursor.
Changed content advances the remote revision (maximum 100000), retains favourites
and resets the cursor. Remote history/undo is not retained; GitHub content history
is outside the app. Remote revisions are immediately playable because the parent
authored the source, with `remote=true`, `reviewed=false`, `approved=true`; the UI
labels this provenance rather than claiming AI review. Editing requires a local
draft copy and the existing local review workflow. Playback leases cover loading,
paused and active controllers; source activation/eviction waits until none remain.
A staged snapshot survives process death and is retried on foreground before the
network check; a failure to activate does not prevent downloading a corrected pack.
Authoring, configuration and cache behavior: [remote source guide](story-source/README.md).
