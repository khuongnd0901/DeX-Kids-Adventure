> 2026-10-19 owner revision: personal/family sideload-only APK; owner explicitly
> declines Google designation and store publication. Removed the local mandatory
> notification-reference gate and completion attestation, retaining explicit
> privacy consent, contact, key and video checks. This is an owner-directed
> configuration change; III.J notification and privacy findings remain unresolved.
> No notification receipt, Google approval or personal-use policy exemption is
> claimed. Current setup: [embedded design](T071_EMBEDDED_YOUTUBE.md).

> 2026-10-19 key-test update: owner supplied local YOUTUBE_KEY. Adult/developer
> metadata diagnostics returned HTTP200 for the owner video, including explicit
> MFK/embeddable status, on the host and actual Windows Android emulator through
> the production client. Debug seed/Keystore and an offline-only real WebView
> port test are verified. This clears those technical checks, not Google child
> notification, privacy/tracking certification, live playback or physical DeX.
> Evidence: `.agent/evidence/T-071-env-youtube-key/`.

> 2026-10-19 update: owner requests dormant embedded implementation and setup UI,
> accepts branding/native YouTube surfaces, and confirms no project/key or child
> notification yet. Source now exists behind explicit configuration/consent gates;
> **live M0 acceptance remains NO-GO/NOT VERIFIED**. A filled reference is an owner
> declaration, not Google confirmation or legal certification. See
> [current embedded design](T071_EMBEDDED_YOUTUBE.md). Historical findings below
> are retained; their NOT IMPLEMENTED labels describe the 2026-10-18 checkpoint.

# T-071 YouTube M0 compliance matrix

**Historical embedded-player gate.** Owner subsequently chose personal-use
Premium playback through the official YouTube app, accepting layout/automation
changes. See [revised scope](T071_OFFICIAL_YOUTUBE.md). The NO-GO below remains
for the deferred iframe/Data API design; it is not a certification requirement
asserted for simple handoff to official YouTube.


Reviewed 2026-10-18 by Main. Scope: issue #65, YouTube media (distinct from
whole-character artwork also numbered T-071). Baseline `a86b0cb` on `main`.

**Decision: NO-GO for M1–M4 at this checkpoint.** M0 research deliverable is
complete; M0 acceptance has not passed. This is an implementation gate decision,
not a claim that YouTube universally prohibits children's apps. No player,
metadata request, account registration, or consent UI was added or exercised.

The [approved ExecPlan](../.agent/plans/active/T-071-youtube-media-entertainment.md)
requires stopping when M0 is unresolved. Existing offline gameplay stays available.

## Requirements and evidence

All sources below were accessed on 2026-10-18. Status refers to this repository,
not a certification of a service or legal opinion.

| Gate | Authoritative requirement / finding | Implementation decision and current evidence |
| --- | --- | --- |
| Child-directed client | [Developer Policies III.J](https://developers.google.com/youtube/terms/developer-policies#j.-child-directed-api-clients) requires Google notification, applicable-law compliance and no personalized advertising; child-directed clients cannot perform or encourage YouTube write actions. | **BLOCKED:** no notification receipt supplied. Local parent catalog would not create YouTube playlists, likes, comments or uploads. |
| Registration route | [Google designation tools](https://support.google.com/policies/answer/9664901) lists websites, Play apps and iOS apps. | **BLOCKED:** installation here is an Android APK. The page does not document a standalone sideloaded-APK route. Do not invent a URL flag or treat video MFK status as app registration. Owner must establish the applicable route with Google and retain a receipt. |
| Disclosure | [Developer Policies III.A](https://developers.google.com/youtube/terms/developer-policies#a.-api-client-terms-of-use-and-privacy-policies) requires terms/privacy links, acceptance and disclosure of third-party collection and advertising. | **BLOCKED:** existing parent settings have no YouTube notice/consent. Draft below requires review before UI implementation. |
| Children's data | [FTC COPPA FAQ A.3, I, J](https://www.ftc.gov/business-guidance/resources/complying-coppa-frequently-asked-questions) covers persistent identifiers, children's voice and precise location; consent exceptions are conditional. | **BLOCKED:** applicable jurisdictions, third-party processing and any consent mechanism remain unassessed. A local checkbox alone is not evidence of legally sufficient consent. |
| MFK verification | [Official MFK guide](https://developers.google.com/youtube/data/guides/made_for_kids_status) specifies `videos.list`, parts `id,status`, then `status.madeForKids`; MFK embeds require tracking disabled and lawful collection. | **NOT VERIFIED:** no authorized project/key or parent-approved test video provided. Manual review remains required. Missing MFK means ineligible; never infer from a title. |
| Metadata eligibility | [Videos resource](https://developers.google.com/youtube/data/reference/rest/v3/videos) documents `status.embeddable`, region restrictions and content ratings. `status.selfDeclaredMadeForKids` is owner-authorized; it is distinct from `status.madeForKids`. | Request `id,snippet,status,contentDetails` by video ID. Require explicit MFK/embeddable, available supported duration, no disqualifying rating/restriction and current parent review. Unknown region/status fails closed. Actual key-only response remains untested; do not assert owner OAuth is required for `madeForKids`. |
| Project/quota | [MFK guide](https://developers.google.com/youtube/data/guides/made_for_kids_status) requires an API-enabled Google project and discusses quota. | **NOT VERIFIED:** owner project, effective quota and restricted credential setup. Do not use AI-provider credentials or commit keys. Metadata requests contain video IDs, never child names, ages, answers or GPS. |
| Embedded identity | [Required Minimum Functionality, identity/WebView](https://developers.google.com/youtube/terms/required-minimum-functionality) describes OS WebView, HTTPS app identity and Android `loadDataWithBaseURL`/Referer. | Future local page uses identity derived from actual package `com.khuongnd.dexkids`, not an invented public website. Inspect actual iframe request; `origin` alone is not evidence of correct Referer. **NOT VERIFIED** on device. |
| Geometry/autoplay | [Required Minimum Functionality, player size/autoplay/overlays](https://developers.google.com/youtube/terms/required-minimum-functionality) requires at least 200×200, recommends 480×270, over half visible before automatic start, one autoplay player and no covering elements. | Preserve approved 75%/25% sibling layout. Audio-only skips YouTube unless a future separate parent option explicitly permits visible video. No hidden playback. **NOT IMPLEMENTED.** |
| Failures | [IFrame API events](https://developers.google.com/youtube/iframe_api_reference#Events) documents `onAutoplayBlocked`, unavailable/non-embeddable errors and identity error 153. | Future bounded timeout and single-owner restoration must return to offline flow once. No synthetic clicks, silent-start trick, error-page bypass or phone fallback. **NOT VERIFIED.** |
| Privacy-enhanced player | [Embed help](https://support.google.com/youtube/answer/171780) says child-directed self-designation still applies with privacy-enhanced mode. [Player parameters](https://developers.google.com/youtube/player_parameters#autoplay) explains autoplay collection/sharing at page load. | No network WebView, thumbnail, iframe or script before applicable notice/consent and registration gates. `youtube-nocookie.com` is not zero collection or a notification exemption. |
| Reviewed-only promise | [Player parameters, rel](https://developers.google.com/youtube/player_parameters#rel) says `rel=0` limits related videos to the same channel rather than removing them. | **BLOCKED product conflict:** the app can whitelist its own scheduled selections, but cannot promise every native recommendation/link is parent-approved. Preserve controls; no covers, DOM removal or endscreen hacks. Resolve disclosure/acceptance of native YouTube surfaces before GO. |

## Draft parent-facing notice (Vietnamese, not shipped)

> YouTube đang tắt. Nếu bật sau khi hoàn tất các điều kiện cần thiết, ứng dụng
> sẽ kết nối với Google/YouTube để xác minh và phát video do phụ huynh chọn.
> Google/YouTube có thể nhận thông tin mạng, thiết bị và hoạt động phát; có thể
> hiển thị quảng cáo, liên kết và nội dung đề xuất trong trình phát.
> Danh sách do phụ huynh duyệt chỉ giới hạn video ứng dụng chủ động chọn.
> Ứng dụng không gửi tên/tuổi của trẻ, câu trả lời, âm thanh hay GPS đến YouTube.
> Video luôn hiện trên màn hình; nếu không phát được, ứng dụng tiếp tục truyện
> hoặc câu hỏi offline. Phụ huynh có thể tắt YouTube và xóa thư viện cục bộ.

Future UI must show accessible [YouTube Terms](https://www.youtube.com/t/terms),
[Google Privacy Policy](https://policies.google.com/privacy) and the app's actual
privacy policy/contact/deletion instructions before acceptance. No app privacy
URL, operator contact or lawful consent assertion is fabricated in this draft.

## Evidence required to reopen M0

1. Owner establishes Google notification route for the actual Android distribution;
   retain acknowledgement and project/app identity without committing secrets.
2. Review applicable privacy obligations and finalize operator privacy/terms,
   consent handling, retention/deletion, and MFK tracking behavior.
3. Resolve native recommendations/link behavior against the reviewed-only promise;
   record a product decision without suppressing standard YouTube UI.
4. Supply a parent-reviewed embeddable MFK test video and project credential through
   a suitable local secret mechanism. Verify current metadata/quota without child
   information, then reassess GO before implementing/loading the M1 player.

M1 hardware/autoplay/audio/GPS/performance gates remain NOT VERIFIED and cannot
be cleared by this research. Overall T-071 remains IN_PROGRESS, blocked at M0.
Source HTTP receipts and scope checks: [.agent evidence](../.agent/evidence/T-071-youtube-m0/SUMMARY.md).
