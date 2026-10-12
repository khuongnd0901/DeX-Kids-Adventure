> **Replanned M10 (2026-10-12):** This is a **172-item proposed inventory**, not
> drawn SVGs. The `priority` column in
> [M10_ASSET_INVENTORY.csv](../art/M10_ASSET_INVENTORY.csv) now divides
> **108 P0** (24 poses, 12 vehicles, 48 biome props, 16 FX, 8 UI)
> and **64 P1** (8 poses, 48 biome props, 8 UI).
> Prioritize `T-039.A` manifest/packer, `T-040.A` selection using old art,
> `T-041.A` renderer adapter and `T-042.A` QA baseline **before producing
> the P0 drawing batches**. Detailed dependency audit:
> [M10 reuse review](M10_REUSE_DEPENDENCY_REVIEW.md).
> Names of six art folders **do not constitute verified GPS biome types**.

# M10 — Exact Art Asset Inventory (planning specification)

Status: **PLANNED — 0/172 new source assets created**, not a claim of shipped art. Canonical editable register: [M10_ASSET_INVENTORY.csv](../art/M10_ASSET_INVENTORY.csv). Each row is **one proposed new SVG source/texture region**; runtime palette/tint variants and procedural frame transforms **do not** count as extra source assets. Existing assets are not counted here.

## Baseline audit (2026-10-12)
- Existing original art: 6 Capybara SVG poses, 5 secondary animal SVGs, 6 traffic SVGs, bus/wheel, 18 existing `props/*.svg`, 22 themed POI backdrops, 4 costumes + 4 story + 4 passenger portraits for each child Sâu/Ong, stored in existing art workflow.
- Existing fixed `kids.atlas` packing script uses **2048×2048 RGBA**, `extras.atlas` uses **1024×1024** and **rejects >24 props**. Therefore **do not append M10 to `art/assets-source/props/`** or blindly enlarge `kids.atlas`. Introduce separate M10 pages and tests in T-039/T-041.
- Sâu and Ong are **children's cartoon avatars**, not caterpillar/bee insects. Maintain their previously approved facial proportions and recognizable silhouette; do **not** include raw family photographs.
- All new assets must be original, editable and redistributable, with no third-party cartoon character/logo. GPS scenery is illustrative unless a sourced POI/GeoWorld contract provides provenance.

## Exact scope and counts

| Pack | Unique new source regions | Task | Comment |
|---|---:|---|---|
| Capybara 12 + Sâu 10 + Ong 10 | **32** | T-036 | reaction/face and hand-pose variants |
| Vehicles | **12** | T-037 | 2 bus skins + 10 new stylized vehicles |
| Six biomes × 16 | **96** | T-038 | urban, village, forest, coast, mountain, farm |
| VFX | **16** | T-039 | mostly one-shot, reusable across scenes |
| Stickers / UI | **16** | T-039 | programmatic text localization not baked into sprites unless reviewed |
| **TOTAL** | **172** | T-036–T-039 | all status PLANNED |

Target: **172 prepared concepts**, milestone minimum **100 realized, reviewed, imported and visible assets** in T-041; remaining concepts may proceed after hardware memory review. 26 animation profiles and 8 one-shot triggers are separate from sprite count.

## Character expressions (32)

Use normalized anchors for pupil/nose/mouth, shoulders, hand positions, waist/bus window: no face drift between poses. Preserve old body dimensions; split layers only where animation needs them. Character sprites are visuals, not synthetic voice/mouth motion if narrator is silent.

| ID | Target region | Drawing brief |
|---|---|---|
| CAP-001 | `cap_point_left` | Capybara chỉ tay trái |
| CAP-002 | `cap_point_right` | Capybara chỉ tay phải |
| CAP-003 | `cap_thinking` | Capybara suy nghĩ |
| CAP-004 | `cap_cheer` | Capybara reo vui |
| CAP-005 | `cap_clap` | Capybara vỗ tay |
| CAP-006 | `cap_listening` | Capybara lắng nghe |
| CAP-007 | `cap_puzzled` | Capybara bối rối dễ thương |
| CAP-008 | `cap_laugh` | Capybara cười lớn |
| CAP-009 | `cap_yawn` | Capybara ngáp |
| CAP-010 | `cap_nod` | Capybara gật đầu |
| CAP-011 | `cap_look_up` | Capybara nhìn trời |
| CAP-012 | `cap_open_book` | Capybara mở sách |
| SAU-001 | `sau_high_five` | Sâu giơ tay đập tay chào, khác pose WAVE cũ |
| SAU-002 | `sau_curious` | Sâu tò mò |
| SAU-003 | `sau_think` | Sâu suy nghĩ |
| SAU-004 | `sau_clap` | Sâu vỗ tay |
| SAU-005 | `sau_celebrate` | Sâu ăn mừng |
| SAU-006 | `sau_look_left` | Sâu nhìn trái |
| SAU-007 | `sau_look_right` | Sâu nhìn phải |
| SAU-008 | `sau_listen` | Sâu chú ý nghe |
| SAU-009 | `sau_surprise` | Sâu ngạc nhiên |
| SAU-010 | `sau_sleepy` | Sâu buồn ngủ |
| ONG-001 | `ong_high_five` | Ong giơ tay đập tay chào, khác pose WAVE cũ |
| ONG-002 | `ong_curious` | Ong tò mò |
| ONG-003 | `ong_think` | Ong suy nghĩ |
| ONG-004 | `ong_clap` | Ong vỗ tay |
| ONG-005 | `ong_celebrate` | Ong ăn mừng |
| ONG-006 | `ong_look_left` | Ong nhìn trái |
| ONG-007 | `ong_look_right` | Ong nhìn phải |
| ONG-008 | `ong_listen` | Ong chú ý nghe |
| ONG-009 | `ong_surprise` | Ong ngạc nhiên |
| ONG-010 | `ong_sleepy` | Ong buồn ngủ |

## Vehicle variants (12)

No claim that fictional moving traffic exists at real GPS position. Preserve road lane orientation and separately animated wheel pivots.

| ID | Target region | Drawing brief |
|---|---|---|
| VEH-001 | `double_decker_tour_bus` | Xe buýt hai tầng tham quan, silhouette mới |
| VEH-002 | `camper_van` | Xe nhà di động có khoang ngủ, không chỉ đổi màu |
| VEH-003 | `family_van` | Xe gia đình 7 chỗ |
| VEH-004 | `electric_city_bus` | Xe buýt điện đô thị |
| VEH-005 | `mini_train` | Đầu tàu hoạt hình nhỏ |
| VEH-006 | `farm_tractor` | Máy kéo mini |
| VEH-007 | `fire_engine` | Xe cứu hỏa vui nhộn |
| VEH-008 | `ambulance_cartoon` | Xe cứu thương hoạt hình |
| VEH-009 | `recycling_truck` | Xe tái chế |
| VEH-010 | `ice_cream_van` | Xe kem |
| VEH-011 | `sailboat_family` | Thuyền buồm gia đình |
| VEH-012 | `cable_car` | Cáp treo vùng núi |

## Biome props (96)

All geography-specific assets are **fictional art vocabulary**, not automatically a claim about the user's immediate surroundings. For real POI illustration, use explicit `PoiSceneDirector` priority and approved map provenance. Never depict weather as real conditions without a source.

### urban (16)

| ID | Target region | Drawing brief |
|---|---|---|
| URB-001 | `library_front` | Thư viện màu xanh |
| URB-002 | `bakery_front` | Tiệm bánh nhỏ |
| URB-003 | `fruit_shop` | Cửa hàng trái cây |
| URB-004 | `toy_store` | Tiệm đồ chơi |
| URB-005 | `apartment_corner` | Góc chung cư |
| URB-006 | `bus_shelter` | Nhà chờ xe buýt |
| URB-007 | `pedestrian_island` | Đảo đi bộ |
| URB-008 | `mailbox_blue` | Hộp thư xanh |
| URB-009 | `street_planter` | Bồn cây phố |
| URB-010 | `flower_pot_round` | Chậu hoa tròn |
| URB-011 | `fire_hydrant_small` | Trụ nước cứu hỏa |
| URB-012 | `fountain_square` | Đài phun nước |
| URB-013 | `cycle_parking` | Giá để xe đạp |
| URB-014 | `soft_traffic_cone` | Cọc tiêu hoạt hình |
| URB-015 | `street_banner` | Băng rôn đường phố |
| URB-016 | `rooftop_garden` | Vườn sân thượng |

### village (16)

| ID | Target region | Drawing brief |
|---|---|---|
| VIL-001 | `tile_roof_home` | Nhà mái ngói |
| VIL-002 | `village_gate` | Cổng làng giả tưởng |
| VIL-003 | `bamboo_grove` | Bụi tre |
| VIL-004 | `haystack_round` | Đống rơm tròn |
| VIL-005 | `wooden_fence` | Hàng rào gỗ |
| VIL-006 | `farm_pump` | Máy bơm ruộng |
| VIL-007 | `poultry_house` | Chuồng gà |
| VIL-008 | `scarecrow_smile` | Bù nhìn cười |
| VIL-009 | `pond_jetty` | Cầu gỗ ven ao |
| VIL-010 | `mango_tree` | Cây xoài |
| VIL-011 | `banana_bunch` | Buồng chuối |
| VIL-012 | `hand_cart` | Xe kéo tay |
| VIL-013 | `clay_pot` | Chum đất |
| VIL-014 | `lotus_planter` | Bồn sen |
| VIL-015 | `wood_path_sign` | Bảng chỉ lối gỗ |
| VIL-016 | `lantern_string` | Dây lồng đèn |

### forest (16)

| ID | Target region | Drawing brief |
|---|---|---|
| FOR-001 | `oak_fictional` | Cây tán lớn giả tưởng |
| FOR-002 | `fern_cluster` | Khóm dương xỉ |
| FOR-003 | `mushroom_red` | Nấm đỏ hoạt hình |
| FOR-004 | `mushroom_brown` | Nấm nâu hoạt hình |
| FOR-005 | `moss_log` | Khúc gỗ rêu |
| FOR-006 | `vine_arch` | Vòm dây leo |
| FOR-007 | `forest_rock_pair` | Cặp đá rừng |
| FOR-008 | `hollow_tree` | Thân cây rỗng vui nhộn, không ghép lại sprite pine cũ |
| FOR-009 | `forest_flower_patch` | Thảm hoa rừng |
| FOR-010 | `squirrel_cartoon` | Sóc hoạt hình |
| FOR-011 | `hedgehog_cartoon` | Nhím hoạt hình |
| FOR-012 | `deer_fictional` | Hươu hoạt hình |
| FOR-013 | `birdhouse` | Tổ chim hộp |
| FOR-014 | `tree_stump` | Gốc cây |
| FOR-015 | `stream_stepping_stones` | Suối đá |
| FOR-016 | `fireflies_group` | Đom đóm giả tưởng |

### coast (16)

| ID | Target region | Drawing brief |
|---|---|---|
| CST-001 | `sand_dune` | Đụn cát |
| CST-002 | `foam_wave` | Sóng bọt trắng |
| CST-003 | `shell_white` | Vỏ sò trắng |
| CST-004 | `shell_spiral` | Vỏ ốc xoắn |
| CST-005 | `starfish_cartoon` | Sao biển hoạt hình |
| CST-006 | `crab_cartoon` | Cua hoạt hình |
| CST-007 | `beach_parasol` | Dù che nắng bãi biển minh họa |
| CST-008 | `fishing_net_rack` | Giá treo lưới đánh cá, khác water_boat cũ |
| CST-009 | `dock_piles` | Cột bến thuyền |
| CST-010 | `lifebuoy_ring` | Phao cứu sinh |
| CST-011 | `coconut_pair` | Đôi dừa |
| CST-012 | `coral_cartoon` | San hô minh họa |
| CST-013 | `dune_grass` | Cỏ ven cát |
| CST-014 | `kite_beach` | Diều bãi biển |
| CST-015 | `striped_buoy` | Phao sọc |
| CST-016 | `shore_rock_ledge` | Đá ven bờ |

### mountain (16)

| ID | Target region | Drawing brief |
|---|---|---|
| MTN-001 | `mountain_ridge` | Dãy núi nhiều lớp |
| MTN-002 | `pine_canopy` | Tán thông |
| MTN-003 | `cascade_small` | Suối thác nhỏ |
| MTN-004 | `hill_cabin` | Nhà gỗ cao nguyên |
| MTN-005 | `viewpoint_platform` | Đài ngắm cảnh |
| MTN-006 | `rope_guardrail` | Lan can dây |
| MTN-007 | `scree_pile` | Đá dốc |
| MTN-008 | `highland_flower` | Hoa cao nguyên |
| MTN-009 | `low_cloud_patch` | Mây sà thấp |
| MTN-010 | `plateau_field` | Đồng cỏ cao nguyên |
| MTN-011 | `mist_ribbon` | Dải sương |
| MTN-012 | `wood_trailpost` | Bảng đường mòn |
| MTN-013 | `valley_creek` | Suối thung lũng |
| MTN-014 | `cliff_outcrop` | Vách đá |
| MTN-015 | `small_cave` | Hang nhỏ giả tưởng |
| MTN-016 | `mountain_goat_cartoon` | Dê núi hoạt hình |

### farm (16)

| ID | Target region | Drawing brief |
|---|---|---|
| FRM-001 | `rice_bundle` | Bó lúa |
| FRM-002 | `rice_green_stage` | Lúa xanh |
| FRM-003 | `rice_gold_stage` | Lúa chín vàng |
| FRM-004 | `irrigation_channel` | Kênh tưới |
| FRM-005 | `waterwheel_small` | Bánh xe nước |
| FRM-006 | `tractor_field` | Máy kéo trên đồng |
| FRM-007 | `ox_cart` | Xe bò |
| FRM-008 | `sunflower_row` | Hàng hướng dương |
| FRM-009 | `pumpkin_patch` | Vườn bí ngô |
| FRM-010 | `corn_cluster` | Cụm bắp |
| FRM-011 | `greenhouse_tiny` | Nhà kính mini |
| FRM-012 | `orchard_tree` | Cây ăn quả |
| FRM-013 | `fruit_basket` | Giỏ hoa quả |
| FRM-014 | `field_sign` | Bảng cánh đồng |
| FRM-015 | `chicken_friend` | Gà hoạt hình |
| FRM-016 | `buffalo_calf` | Nghé hoạt hình |

## FX (16)

| ID | Target region | Drawing brief |
|---|---|---|
| FX-001 | `sparkle_cluster` | Cụm sao lấp lánh |
| FX-002 | `confetti_burst` | Pháo giấy mềm |
| FX-003 | `dust_puff_round` | Bụi bánh xe |
| FX-004 | `leaf_swirl` | Lá xoay |
| FX-005 | `gentle_raindrops` | Mưa hoạt hình nhẹ |
| FX-006 | `bubble_rise` | Bong bóng nổi |
| FX-007 | `water_ripple` | Gợn nước |
| FX-008 | `butterfly_trail` | Vệt bay bướm |
| FX-009 | `petal_drift` | Cánh hoa rơi |
| FX-010 | `cartoon_heart_pop` | Trái tim bật lên |
| FX-011 | `surprise_rays` | Tia bất ngờ |
| FX-012 | `reward_halo` | Vòng thưởng |
| FX-013 | `vehicle_speed_lines` | Nét tốc độ |
| FX-014 | `cloud_puff` | Đốm mây xốp |
| FX-015 | `question_pop` | Dấu hỏi xuất hiện |
| FX-016 | `correct_starburst` | Sao nổ khi đúng |

## Sticker/UI (16)

| ID | Target region | Drawing brief |
|---|---|---|
| UI-001 | `sticker_wow` | Nhãn Wow |
| UI-002 | `sticker_correct` | Nhãn Đúng rồi |
| UI-003 | `sticker_retry` | Nhãn Thử lại nào |
| UI-004 | `sticker_discover` | Nhãn Khám phá |
| UI-005 | `sticker_english` | Nhãn Từ tiếng Anh |
| UI-006 | `reward_star` | Sao thưởng |
| UI-007 | `reward_heart` | Tim thưởng |
| UI-008 | `audience_sau` | Nhãn Sâu |
| UI-009 | `audience_ong` | Nhãn Ong |
| UI-010 | `audience_both` | Nhãn cả hai |
| UI-011 | `countdown_ring` | Vòng đếm thời gian |
| UI-012 | `question_card` | Khung câu hỏi |
| UI-013 | `answer_card` | Khung câu trả lời |
| UI-014 | `badge_explorer` | Huy hiệu khám phá |
| UI-015 | `sticker_hello` | Chào nhé |
| UI-016 | `sticker_goodbye` | Hẹn gặp lại |

## Drawing handoff / source naming
- Preferred originals: `art/assets-source/m10/<family>/<slug>.svg`; reproducible raster + atlas in `assets/generated/` only (keep generated outputs gitignored).
- Suggested nominal SVG viewBox per category (validate before implementing): props and FX `0 0 160 160`, vehicles `0 0 280 160`, child/capybara pose `0 0 256 320`, UI badges `0 0 160 96`. These are **authoring guidelines**, not existing packer contracts.
- Transparent background for props/characters/FX; no baked-in full-screen rectangles; stable baseline/anchor and appropriate negative space for animation. Avoid raster asset overdraw and SVG filters/embedded external URLs.
- Include metadata `asset_id`, `family`, `animation_role`, `anchor`, `layer`, `biome`, `license`, `review_status` in a future runtime manifest; only allowlisted IDs accepted. **Text not embedded in SVG** for Vietnamese/English stickers except approved iconographic text that passes fonts/accent tests.
- Artistic review: compare exact face landmarks and bus-window silhouettes, check legibility at **1920×1080**, screenshot in SAU / ONG / BOTH and age **4–7**, no existing icon reuse mistaken for a new file.
- Rollout P0: keep memory within bounds and import assets in batches; never load all 172 at once. Final source count must match the checked-in manifest and atlas IDs; do not claim produced art before files and tests exist.

## Machine-validation checklist for T-039/T-042
- CSV unique ids/paths, CSV filename matches actual SVG, no duplicates vs existing `SOURCES` or `EXTRA_NAMES`.
- Source integrity: valid SVG viewport/size, no script/external links, actual alpha bbox, no zero-size assets, no broken child portraits.
- Multi-page atlas references accessible on Android and Desktop, texture allocation/disposal and render thread isolation checked.
- FullHD screenshots (intro, road, quiz, answer, long-caption, reward, night, POI override), QA review without invented GPS evidence.
- Source inventory count must report `PLANNED`, `CREATED`, `PACKED`, `RENDERED`, `APPROVED` separately, never infer `APPROVED` from source presence alone.
