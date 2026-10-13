# T-047 M3 CPU/memory lifecycle
Owner Main; dependency M2 1402742.
Reuse immutable inactive scenes; reuse active POI/Geo snapshots while their values
are unchanged, preserving simulation and provenance priority. Existing ring world
cache and single-worker ownership remain unchanged.
WAV cache v2 checks length and full PCM header, writes atomically under a shared
cache lock and survives SoundPool disposal. Six unchanged synthesized waveforms;
no voice/navigation/volume/focus/microphone changes.

Local contracts and 172 core tests PASS; Android unit NO-SOURCE; both APKs built.
Actual Fold3 sound_cache PASS: six bounded WAVs, reuse without rewrite, corrupt
file repaired to the original SHA. Actual 100 GL cycles PASS; managed count one
in Audio-only, native allocated first/last Full 21,635,696/16,114,240 bytes.
Main APK SHA d4fdba81aead718f8743762e3c8ae68c6ac7d4dd6c01f2033b5493fcc035afbb.
Installed signed APK was reconstructed from identical existing ZIP payloads plus
866,241 transferred bytes; whole device SHA matched complete locally built APK
before package installer reported Success. Temporary artifacts removed; no user
cache/key/permission removed. Helper is local-only in the host QA cache.

Performance report separates old/M1+M2 measurements; M3 600s heap details and
30/60min full acceptance remain pending. Native PSS retention must be investigated;
do not mark T-047 DONE or release-ready.
