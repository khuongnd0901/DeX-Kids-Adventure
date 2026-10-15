# Final exact-APK host observer

This read-only host observer ran alongside `final-live-soak/instrumentation.log` on the installed final candidate (root records source/APK identity). It did not build, install, start instrumentation, change the activity, invoke providers or inject GPS. It read the app-private external `qa-t059-live/events.json` approximately every 10 seconds and captured two emulator frames using Android `screencap`, with a requested 0.4-second host delay between commands, before pulling both files. Actual command timestamps appear in `observer.json`.

Completed at all 12 audience × narrated-scene keys: **19 pairs / 38 original PNGs**, every PNG header verified **1920×1080**. `capture-summary.json` contains counts. One pair retained exactly the same sampled state through capture and transfer; three retained exactly the same sampled state through the capture window. Events are independently sampled approximately once per second, so this is bounded evidence rather than frame-accurate audio synchronization.

Some pairs cross a narration/question boundary. Their metadata explicitly flags the changed state. For example, the BOTH scene 3 contact-sheet screenshot is already the neutral question visual even though the preceding event still records narration. It proves the correct scene and readable question, not that the preceding PET pose appeared in that particular frame. Targeted story-visual QA is separate.

Visual checks:

- `07-both-scene1-phase0-a.png`: “Có đóa hoa đẹp trên cành cây!” shows the flower on the tall tree canopy **above both children's heads**, with the tree behind the original character sprites and faces unobscured. This resolves the earlier low-flower LOOK_UP placement defect.
- `01-ong-scene1-phase0-a.png`: upward original pose and high bird correspond to “Ong ngước nhìn bạn chim và mỉm cười.”
- `03-ong-scene3-phase0-a/b.png`: unchanged sampled cursor/caption; visible whole-sprite orientation/position changes, clothing follows the body, original face retained. This is finite whole-sprite gesture evidence, **not independent arm/hand articulation** or proof of audible timing.
- `both-scenes-contact.png` is a derived contact sheet of four actual screenshots, not an additional emulator capture. The original 1920×1080 files remain beside it.

The observer stopped after scene coverage; the separate 30-minute soak continued. Physical DeX is not established by these emulator images.
