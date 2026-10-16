# T-067 — replace the public pack with longer, uninterrupted stories

Owner Main. Dependencies T-062/T-063/T-066. Branch feat/T-067-long-fairy-tales.
User authorizes replacement of current stories and publication in this repository.
Latest constraint: 4–7 minute narration, no questions until the ending, then
5–7 seconds for each answer. Interpret the named topics as Rùa và Thỏ,
Rùa con tìm nhà, Sói và bảy dê con, with SAU/ONG/BOTH variants (nine episodes).

Implement original Vietnamese retellings, gentle wolf ending, meaningful child
roles. Use existing schema/renderer only; absent turtle/wolf/goat art remains
book narration, without claims that absent sprites are pictured. Six narration
scenes including an explicit ending; three questions only at/after that ending.
Use six-second answer deadlines. Estimate narration from actual Vietnamese
whitespace tokens at a documented reading-rate range; do not claim a measured
spoken duration. Keep the separate expanded renderer showcase fixture.

Acceptance gates:
- Canonical pack has only nine fresh IDs, three per audience, production codec
  accepts it; valid cue mappings and no mid-story questions/question marks.
- Each narration has a 4–7 minute estimate at 220–320 Vietnamese syllables/min;
  final three questions target SAU/ONG/BOTH fairly and wait six seconds.
- Exercise actual production playback events for all nine: complete ending
  before first question; callback-driven speech, silent six-second waits.
- Core tests, GitHub main publication and anonymous HTTPS byte equality.
- Latest user steering: only commit/push to GitHub; user will sync at next app
  opening. No device refresh or local sample deletion in this slice. Old remote
  IDs disappear when the existing source synchronizes the replacement snapshot.
- Update source docs and STATUS/TASKS; exact voice duration/human listening
  remains NOT VERIFIED unless actually measured.

Completed: local content, codec,230core tests and Android/desktop build gates
verified; source4291f45 pushed to main. Anonymous HTTPS returns exact local bytes
and9stories. See .agent/evidence/T-067-stories/SUMMARY.md and publication.json.
Physical listening and device sync not performed, per final user scope; exact
spoken duration remains an explicitly documented estimate, not measured audio.
