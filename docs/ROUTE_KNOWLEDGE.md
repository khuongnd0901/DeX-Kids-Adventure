# 5 offline knowledge corridors — Đồng Nai to coastal/highland destinations

State: IMPLEMENTED IN SOURCE; Android runtime/TTS/physical DeX NOT VERIFIED until CI and device evidence.

## Corridors
| Route ID | Destination | 7 topics |
| --- | --- | --- |
| `dong-nai-vung-tau` | Đồng Nai → Vũng Tàu | Hồ Trị An, thành phố biển, Bãi Sau, hải đăng, Bạch Dinh, núi gần biển, giữ biển sạch |
| `dong-nai-phan-thiet` | Đồng Nai → Phan Thiết | Vườn trái cây, làng chài Mũi Né, thuyền thúng, đồi cát, Suối Tiên, tháp Pô Sah Inư, gió/cánh diều |
| `dong-nai-bao-loc` | Đồng Nai → Bảo Lộc | Hồ Trị An, VQG Cát Tiên, bảo vệ rừng, đồi chè, cây cà phê, thác Đambri, nước chảy |
| `dong-nai-da-lat` | Đồng Nai → Đà Lạt | Rừng Đồng Nai, chè Bảo Lộc, thác Datanla, hồ Xuân Hương, vườn hoa, thụ phấn, bảo vệ cảnh quan |
| `dong-nai-nha-trang` | Đồng Nai → Nha Trang | Miệt vườn, vịnh biển, tháp Bà Po Nagar, Hòn Chồng, Viện Hải dương học, thuyền đánh cá, bảo vệ biển |

35 original short Vietnamese stories for ages 2–6, five themes: geography, nature, culture, science, environment. Each row has an independently provided HTTPS fact source and is loaded from `assets/routes/knowledge.tsv`, without internet access.

## Parent and child experience (external DeX monitor)
1. Open ParentActivity from the external DeX launcher. Choose route with mouse/keyboard (**Chọn tuyến kiến thức**), optionally read all seven source-attributed stories offline (**Xem 7 câu chuyện của tuyến**).
2. **Start DEMO + 7 câu chuyện của tuyến** uses the existing animated simulated journey, with no real GPS. **Start LIVE GPS + 7 câu chuyện của tuyến** first requires Android precise-location permission; only then runs the existing native GPS listener. The route choice never affects GPS location/speed smoothing or path.
3. Every ~90 seconds (first story after ~8 seconds), the app displays an Android-native Vietnamese caption. All seven cards rotate in catalogue order. In all cases the user sees **KIẾN THỨC THAM KHẢO, KHÔNG XÁC NHẬN VỊ TRÍ**. These are educational route themes, NOT verified geofenced landmarks, crossings, turn directions, road matching or navigation alerts.
4. Only when the parent has independently allowed offline speech **and** disabled Quiet can the app request Vietnamese local non-network TTS. If no offline voice is installed, text remains visible and no cloud voice is used.
5. The normal LibGDX game/animation, F10/mouse parent menu, Audio-only, existing HCMC source-audited GPX preview, session deadline, GPS privacy, IPC permission and life-cycle remain unchanged.

## Geography and source rules
The "Đồng Nai → destination" names are *content collections*, not road itineraries. They do NOT infer actual stops or say the vehicle is near named destinations. Example: Cát Tiên and hồ Trị An are background knowledge about Đồng Nai, **not assumed waypoints** on a trip to Bảo Lộc or Vũng Tàu. Similarly, Dambri, Suối Tiên, tháp Bà and Bãi Sau are possible attractions, not verified road-adjacent POIs.

This dataset contains **NO GPS coordinates or OSM feature IDs**; the existing `assets/poi/reviewed.tsv` production location pack intentionally remains EMPTY. Coordinates, road proximity, map-matching, stop-to-route ordering and localized announcements cannot be shipped until independent human review and appropriate map/source licensing.

Each TSV fact has its own `fact_url`. Primary examples:
- [Cục Du lịch Quốc gia: Vũng Tàu](https://csdl.vietnamtourism.gov.vn/dest/?item=211), [Bãi Sau](https://vietnamtourism.vn/index.php/tourism/items/2119/4), [Bạch Dinh](https://vietnamtourism.vn/index.php/tourism/items/2201/2).
- [Du lịch Bình Thuận: Làng Chài Mũi Né](https://mybinhthuan.vn/vi/langchaimuine), [Đồi Cát Bay](https://mybinhthuan.vn/vi/doicatbaymuine), [Tháp Pô Sah Inư](https://mybinhthuan.vn/vi/posanu).
- [Du lịch Lâm Đồng: Đambri](https://visitlamdong.vn/vi/thacdambri), [Đà Lạt: Hồ Xuân Hương](https://dalat.vn/vi/hoxuanhuong), [Thác Datanla](https://dalat.vn/vi/thacdatanla), [Vườn hoa thành phố](https://dalat.vn/vi/vuonhoathanhpho).
- [Du lịch Khánh Hòa: Tháp Bà Po Nagar](https://ttdhsdl.khanhhoa.gov.vn/TourismResources/Detail/VanHoa-2), [Vietnam Tourism Nha Trang](https://vietnam.travel/vi/things-to-do/perfect-weekend-nha-trang).
- [Báo Đồng Nai: Hồ Trị An](https://baodongnai.com.vn/kinh-te/202406/ho-tri-an-duoc-quy-hoach-de-phat-trien-thanh-khu-du-lich-quoc-gia-c7907f0/), [Cát Tiên](https://baodongnai.com.vn/dong-nai-cuoi-tuan/202609/ua-cat-tien-len-ban-do-du-lich-the-gioi-b7c72d0/).

Article/source URLs may have historical provincial administrative wording; do not automatically rewrite historical claims based on post-2025 boundary changes. Keep preview sourced until human fact check before promoting factual GPS alerts. Descriptions are original short paraphrases, not copied articles.

## Checks
- Core JUnit `RouteKnowledgeCatalogTest`: exact 5×7 age-safe, HTTPS source, order, schema validation and hostile/unattributed/location-claim rejection.
- Static script `python3 tools/test_route_knowledge.py`: unique IDs, category diversity, no production POI promotion, original consent/route markers.
- CI main `.github/workflows/build.yml`: static and Gradle core+Android/AndroidTest build.
- CI `.github/workflows/p0-android-integration.yml`: Android emulator `p0_route` actual native caption, offline pack, one display, default no voice; existing HCMC/GPS/performance tests stay in place.
- Real Fold3 mouse/keyboard-only external DeX, offline TTS voice/lipsync and 60-min FPS/battery **NOT VERIFIED**.

Not an offline navigable road map, driving directions, child location tracker, or location-triggered fact announcement.
