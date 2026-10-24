# T-076 — additional YouTube story candidates on Fold3

2026-10-20; Owner Main. Dependencies T-071 embedded library/key/DeX. DONE for
requested search and pending-library configuration; full audiovisual parental
review remains PENDING. Overall T-071 physical/field/privacy gates unchanged.

## Research and scope

Owner explicitly requested YouTube search and app configuration. Used web search
and six adult YouTube Data API search.list queries, safeSearch=strict, Vietnamese
relevance, VN and short/embeddable videos; no child profiles/GPS/audio in queries.
Fetched18candidate metadata rows in one videos.list call, excluded4MFK=false,
deduplicated plots and selected6. Web watch-page fetches were throttled; API
metadata succeeded. Title/description screening is not full video/audio review.
No transcript scraping, video download, invented licensing or content certification.
Research data: search.json, candidate-metadata.json, selection.json; rationale and
links in docs/T076_YOUTUBE_STORY_CANDIDATES.md. Selected:

- Nhổ củ cải / Kể Bé Nghe /213s
- Chú đỗ con / Kênh Thiếu Nhi - BHMEDIA /131s
- Cây táo / Cô Nga Mầm non /92s
- Giọt nước tí xíu / BEE KIDS TV /207s
- Chiếc ô của thỏ con / THƯ VIỆN MẦM NON /86s
- Gấu con chia quà / Kể Bé Nghe /215s

Added only these6to optional starter source, preserving original3. All new rows
BOTH/4–7candidate bounds, approved=false, enabled=false, empty recall. Planned
ceiling rounds duration up to minutes. Actual parent audiovisual review required.
No in-app discovery, permanent parental approval, consent or autoplay enablement.

## Acceptance and device evidence

App/testAPK build SUCCESS, core UP-TO-DATE with290results zero failures/errors/
skips. Added adult sample-import instrumentation branch using actual production
parent-review metadata entry before any import, without faking approved flags.
On real SM-F926B/API35/DeX2: all6fresh HTTPS metadata validations PASS via existing
production MFK/embedding/region/no-live/no-age-restriction/duration gates.
Native sample button adds6,total9; exact old rows retained, reload and repeated
import idempotent; no switch/privacy changes. Instrumentation code0.

App installed SHA matches build (verification.json). Private debug key remains
ignored local artifact; raw credential absent from evidence/tracked edits.
Final actual physical HDMI screenshot reviewed (final-library-hdmi.png); app
left in native library showing all9pending rows, opened through actual parent UI.
Seven preference file hashes unchanged: parent, AI keys/settings, learning,
story-manager, YouTube privacy/handoff. Story-source hash changed during normal
sync; original XML not retained, exact key diff NOT VERIFIED. Source fixture
asserts exact catalog/YouTube switch/privacy preservation. No device owner,
Accessibility/navigation/other-repository changes, commit or push.

Initial host apksigner invocation lacked Java PATH, retried with existing JDK;
no API calls/credential exposure on that failed command. An overbroad all8prefs
hash assertion found ordinary story-source synchronization; corrected acceptance
to distinguish7exact files from this explicit exception, no false all-settings
byte-identity claim. Original device-import log remains unmodified.
