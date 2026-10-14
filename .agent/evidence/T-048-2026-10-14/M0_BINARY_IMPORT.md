# T-048 M0 actual Git binary import — 2026-10-14

Owner Main; original ZIP supplied at `D:\DeX-Kids-Adventure\DeX-Kids-Adventure-art-v1.zip` (7,294,553 bytes). This clears the earlier local missing-ZIP blocker. Preview PNG not present; originals were inspected directly.

Existing QA virtualenv/Pillow12.3.0 runs importer verify-only and real import successfully, 5/5 original RGBA PNG hashes/dimensions/alpha bounds match both manifests. Binary-only commit **faba918**, successful push to `feat/T-048-capybara-bus-art-import`. Fetched branch; SHA256 independently computed from all five remote Git blobs.

| Source | Format | SHA256 from remote blob | Repository/ZIP match |
|---|---|---|---|
| capybara_idle.png | 1122×1402 RGBA | `2ed7f5c147e519746c91a4702e12d406c2511f6e1753dd58a775fb8572c2e835` | MATCH |
| capybara_laugh.png | 1122×1402 RGBA | `f98b2f3136af6843dd56200bf2ca4660ffbeac886010401efebf51f5f86a1483` | MATCH |
| capybara_ask.png | 1122×1402 RGBA | `5cde0d728b21e05528228b5a15115358fa89033e647dbbaf9fa5da4f9d46d31f` | MATCH |
| capybara_wave.png | 1122×1402 RGBA | `f3bb006f3af57b79716e18667dc0f35271a34df9894f711558f90f1023266bb3` | MATCH |
| bus_side_with_driver.png | 1448×1086 RGBA | `7f77484b78c92b426e5bd33645a2bde421b81855b4e09091b9f1c1e930acebc9` | MATCH |

Importer stdout: `local/source-verify.txt`. Original PNG bytes retained without edits. Source manifest status updated separately after this verified binary commit. Original art remains. M0 acceptance met for immutable import/remote upload; this is not renderer/device/license acceptance.
