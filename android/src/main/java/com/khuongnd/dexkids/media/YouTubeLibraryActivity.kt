package com.khuongnd.dexkids.media

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import com.khuongnd.dexkids.KidsSessionControl
import com.khuongnd.dexkids.story.EntertainmentDirector.Audience

/** Personal-use local links. Google account/Premium stays inside official YouTube. */
class YouTubeLibraryActivity : Activity() {
    private lateinit var store: YouTubeStore
    private var rows: List<YouTubeVideo> = emptyList()
    private var corrupt = false
    private var returnDialog: AlertDialog? = null
    override fun onCreate(state: Bundle?) { super.onCreate(state); store = YouTubeStore(this); render() }
    override fun onResume() {
        super.onResume()
        if (::store.isInitialized) {
            if (store.pendingEmbedded && !KidsSessionControl.active()) store.clearPending()
            render()
        }
        if (::store.isInitialized && store.pendingId.isNotEmpty() && returnDialog == null) {
            returnDialog = AlertDialog.Builder(this).setTitle("Quay lại từ YouTube")
                .setMessage("Hãy dừng video trong YouTube trước khi tiếp tục; Premium có thể vẫn phát nền.")
                .setCancelable(false).setPositiveButton("Tôi đã dừng YouTube") { _, _ ->
                    store.clearPending(); returnDialog = null; render()
                }.show()
        }
    }
    override fun onDestroy() { returnDialog?.dismiss(); returnDialog = null; super.onDestroy() }
    private fun render() {
        corrupt = false
        rows = try { store.videos() } catch (_: Exception) { corrupt = true; emptyList() }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = (20 * resources.displayMetrics.density).toInt(); setPadding(p,p,p,p)
        }
        fun label(text: String) { column.addView(TextView(this).apply { this.text = text; textSize = 18f }) }
        fun button(text: String, action: () -> Unit) {
            column.addView(Button(this).apply { this.text = text; isAllCaps = false; setOnClickListener { action() } })
        }
        val embed = YouTubeEmbedSettings(this)
        label("YouTube · thư viện phụ huynh")
        button("Cấu hình YouTube API v3 / trình phát trong app") {
            startActivity(android.content.Intent(this, YouTubeEmbedSetupActivity::class.java))
        }
        button("Cách phát: " + if (embed.selected) "Trong app" else "Ứng dụng YouTube riêng") {
            AlertDialog.Builder(this).setTitle("Chọn cách phát")
                .setItems(arrayOf("Trong app · API v3 + IFrame", "YouTube riêng · cách cũ")) { _, index ->
                    embed.selected = index == 0; render()
                }.show()
        }
        if (embed.selected) label(if (embed.ready()) "Phát nhúng đã cấu hình; video sẽ được kiểm tra trước khi phát."
            else "Phát nhúng chưa cấu hình đủ: chỉ dùng nội dung offline, không tự chuyển sang YouTube riêng.")
        label(if (embed.selected) "Video phát trong app trên DeX, cạnh Capybara; giữ logo và điều khiển YouTube. Tự quay lại khi video kết thúc. Lỗi / không được tự phát thì tiếp tục offline. Hành trình/GPS/POI riêng của app tạm dừng trong lượt video. Audio-only bỏ qua video. Không cần quyền đọc thông báo."
            else "Mở bằng ứng dụng YouTube chính thức trên DeX. Premium do YouTube quản lý; khi quay lại cần xác nhận đã dừng hoặc bật điều khiển tùy chọn. Hành trình/GPS/POI riêng của app tạm dừng trong lượt video.")
        column.addView(CheckBox(this).apply {
            text = if (embed.selected) "Cho phép video YouTube đã duyệt trong app" else "Cho phép mở link đã duyệt bằng YouTube"; isChecked = store.enabled
            setOnCheckedChangeListener { _, checked ->
                if (!checked) { store.enabled = false; store.automatic = false; render() }
                else if (store.consented) { store.enabled = true; render() }
                else {
                    isChecked = false
                    AlertDialog.Builder(this@YouTubeLibraryActivity).setTitle("Trước khi bật YouTube")
                        .setMessage(if (embed.selected) "App dùng YouTube API Services để kiểm tra và phát video trong app. Trước khi phát cần hoàn tất cấu hình API và đồng ý chính sách quyền riêng tư ở màn hình cấu hình. Có thể có quảng cáo, logo, liên kết, đề xuất YouTube. Chỉ video app tự chọn được giới hạn theo danh sách duyệt. Không gửi tên/tuổi, GPS, âm thanh hoặc câu trả lời của trẻ. Chọn dùng tính năng đồng nghĩa đồng ý Điều khoản YouTube: https://www.youtube.com/t/terms · Chính sách Google: https://policies.google.com/privacy" else "Chỉ link video được gửi đến ứng dụng YouTube chính thức. Google/YouTube quản lý đăng nhập, lịch sử xem, quảng cáo, đề xuất và dữ liệu phát theo chính sách của họ; có thể có nội dung chưa được phụ huynh duyệt. Ứng dụng không gửi tên/tuổi, GPS, âm thanh hay câu trả lời của trẻ.\nYouTube có thể tiếp tục phát nền; phụ huynh cần dừng trước khi quay lại. Không tự nhận biết video kết thúc. Hành trình/GPS/POI riêng của app tạm dừng trong lượt YouTube; không thay đổi GPS hệ thống hay Maps/VietMap.\nĐiều khoản: https://www.youtube.com/t/terms\nRiêng tư Google: https://policies.google.com/privacy\nTắt tính năng hoặc xóa thư viện tại đây; lịch sử Google do bạn quản lý trong YouTube. Các lựa chọn của app không thay đổi tài khoản Google.")
                        .setNegativeButton("Chưa bật", null)
                        .setPositiveButton("Đã hiểu · Bật") { _, _ -> store.acceptNotice(); store.enabled = true; render() }.show()
                }
            }
        })
        column.addView(CheckBox(this).apply {
            text = "Tự mở video ở lượt xen kẽ"; isChecked = store.automatic; isEnabled = store.enabled
            setOnCheckedChangeListener { _, checked -> store.automatic = checked }
        })
        if (!embed.selected) {
            column.addView(CheckBox(this).apply {
                text = "Tự tạm dừng YouTube ở lượt xen kẽ và thử quay lại khi hết thời lượng đã nhập"
                isChecked = store.managedStop
                setOnCheckedChangeListener { _, checked ->
                    if (!checked) store.managedStop = false
                    else {
                        isChecked = false
                        AlertDialog.Builder(this@YouTubeLibraryActivity)
                            .setTitle("Cho phép điều khiển phát YouTube")
                            .setMessage("Android yêu cầu quyền truy cập thông báo; quyền hệ thống này rộng và có thể cho phép đọc thông báo. App không đọc/lưu nội dung thông báo, chỉ điều khiển phiên phát của YouTube, không điều khiển Maps/VietMap hay app khác.\nApp gửi lệnh tạm dừng theo thời lượng bạn nhập và khi quay lại hành trình; đây không phải nhận biết video đã hết. Chỉ tiếp tục khi xác nhận phiên phát đã dừng. Android có thể chặn tự đưa cửa sổ về trước. Logo, đề xuất và đường dẫn của YouTube vẫn hiện. Bạn có thể thu hồi quyền trong Cài đặt Android.")
                            .setNegativeButton("Hủy", null)
                            .setPositiveButton("Đồng ý · Thiết lập") { _, _ ->
                                store.managedStop = true
                                if (!YouTubeControlService.permitted(this@YouTubeLibraryActivity)) {
                                    runCatching {
                                        startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                                            .putExtra(android.provider.Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                                                YouTubeControlService.component(this@YouTubeLibraryActivity).flattenToString()))
                                    }.onFailure { message("Không mở được cài đặt quyền. Chế độ dừng tự động chưa sẵn sàng.") }
                                }
                                render()
                            }.show()
                    }
                }
            })
            label("Điều khiển tự động: " + if (store.managedStop && YouTubeControlService.permitted(this))
                "đã bật; cần kiểm tra trên DeX thật." else "chưa bật hoặc chưa được cấp quyền Android.")
        }
        button("Tỷ lệ: 1 video mỗi ${store.every} lượt xen kẽ (mỗi lượt sau 5 câu)") {
            AlertDialog.Builder(this).setTitle("Tỷ lệ truyện / video")
                .setItems(arrayOf("1 truyện : 1 video", "3 truyện : 1 video")) { _, i -> store.every = if (i == 0) 2 else 4; render() }.show()
        }
        label(if (embed.selected) "Tối đa 2 lượt video / giờ và 15 phút video dự kiến / giờ. Nhập thời lượng làm tròn lên; video dài hơn mức đã nhập sẽ bị bỏ qua. Lượt video kết thúc theo tín hiệu trình phát; không giới hạn hành trình."
            else "Tối đa 2 yêu cầu mở / giờ và 15 phút video dự kiến / giờ; mốc dừng tự động dùng thời lượng đã nhập nếu bật điều khiển; không giới hạn hành trình.")
        if (corrupt) label("Không đọc được thư viện. Không mở video. Bạn có thể xóa thư viện hỏng để tạo lại.")
        if (!corrupt) {
            button("Thêm video kể chuyện mẫu · chờ phụ huynh duyệt") {
                val existing = rows.map { it.videoId() }.toSet()
                val additions = YouTubeSampleVideos.pending().filterNot { it.videoId() in existing }
                if (additions.isEmpty()) message("Các link mẫu đã có trong thư viện; giữ nguyên nội dung và trạng thái bạn đã đặt.")
                else if (rows.size + additions.size > YouTubeLibrary.MAX_VIDEOS)
                    message("Thư viện gần đầy. Hãy xóa bớt link trước khi thêm bộ mẫu.")
                else try {
                    store.save(rows + additions)
                    render()
                    message("Đã thêm ${additions.size} link mẫu, đang tắt và chưa duyệt. Chọn từng video → Sửa / duyệt lại, xem toàn bộ rồi xác nhận duyệt và bật link. Made for Kids không thay thế việc phụ huynh xem nội dung.")
                } catch (_: Exception) { message("Không lưu được mẫu. Thư viện đã lưu trước được giữ nguyên.") }
            }
            for (video in rows) button("${if (video.approved() && video.enabled()) "✓" else "○"} ${video.title()} · ${video.audience()} · ${video.minAge()}–${video.maxAge()}") {
                AlertDialog.Builder(this).setTitle(video.title())
                    .setItems(arrayOf("Sửa / duyệt lại", if (embed.selected) "Xem thử trong app" else "Xem thử bằng YouTube", "Xóa")) { _, i ->
                        when (i) {
                            0 -> edit(video)
                            1 -> preview(video)
                            2 -> AlertDialog.Builder(this).setTitle("Xóa link này?")
                                .setNegativeButton("Hủy", null).setPositiveButton("Xóa") { _, _ ->
                                    save(rows.filterNot { it.videoId() == video.videoId() })
                                }.show()
                        }
                    }.show()
            }
            if (rows.size < YouTubeLibrary.MAX_VIDEOS) button("Thêm link video") { edit(null) }
        }
        button("Xóa toàn bộ thư viện và tắt YouTube") {
            AlertDialog.Builder(this).setTitle("Xóa toàn bộ link đã lưu?")
                .setNegativeButton("Hủy", null).setPositiveButton("Xóa") { _, _ -> store.deleteLibrary(); render() }.show()
        }
        button("Quay lại") { finish() }
        setContentView(ScrollView(this).apply { addView(column) })
    }
    private fun save(videos: List<YouTubeVideo>) {
        try { store.save(videos); render() }
        catch (_: Exception) { message("Không lưu được. Bản đã lưu trước được giữ nguyên.") }
    }
    private fun edit(old: YouTubeVideo?) {
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,16,24,16) }
        fun field(hint: String, text: String): EditText {
            return EditText(this).apply { this.hint = hint; setText(text); column.addView(this) }
        }
        val url = field("Link HTTPS YouTube hoặc ID video", old?.url().orEmpty()).apply {
            setSingleLine(true)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_URI or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            imeOptions = android.view.inputmethod.EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING or
                android.view.inputmethod.EditorInfo.IME_FLAG_FORCE_ASCII
        }
        column.addView(Button(this).apply {
            text = "Dán link/ID từ bộ nhớ tạm"; isAllCaps = false
            setOnClickListener {
                // Read clipboard only on this explicit parent action; no automatic access.
                val clipboard = getSystemService(android.content.ClipboardManager::class.java)
                val clip = clipboard.primaryClip
                val text = if (clip != null && clip.itemCount > 0) clip.getItemAt(0).text?.toString() else null
                try {
                    val id = YouTubeVideo.idFromInput(text)
                    url.setText("https://www.youtube.com/watch?v=$id")
                    url.setSelection(url.text.length); url.error = null
                } catch (_: IllegalArgumentException) { url.error = "Hãy sao chép một link hoặc ID video YouTube hợp lệ." }
            }
        })
        column.addView(TextView(this).apply {
            text = "Link/ID phân biệt chữ hoa thường. Nếu gõ tay, chuyển bàn phím sang English để tránh bộ gõ tiếng Việt đổi ký tự."
        })
        val title = field("Tên do phụ huynh đặt", old?.title().orEmpty())
        val audience = Spinner(this).apply {
            adapter = ArrayAdapter(this@YouTubeLibraryActivity, android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Cả Sâu và Ong", "Chỉ Sâu", "Chỉ Ong"))
            setSelection(when (old?.audience()) { Audience.SAU -> 1; Audience.ONG -> 2; else -> 0 }); column.addView(this)
        }
        val min = field("Tuổi nhỏ nhất (4–7)", (old?.minAge() ?: 4).toString())
        val max = field("Tuổi lớn nhất (4–7)", (old?.maxAge() ?: 7).toString())
        val minutes = field("Thời lượng dự kiến, phút (1–30)", ((old?.plannedSeconds() ?: 300) / 60).coerceAtLeast(1).toString())
        listOf(min,max,minutes).forEach { it.inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        val recall = field("Tùy chọn: tối đa 2 câu hỏi đã duyệt, mỗi dòng 1 câu", old?.recall().orEmpty())
        val approved = CheckBox(this).apply { text = "Tôi đã xem và duyệt toàn bộ video cho người xem / độ tuổi ở trên"; column.addView(this) }
        val enabled = CheckBox(this).apply { text = "Dùng link này trong các lượt video"; isChecked = old?.enabled() ?: true; column.addView(this) }
        val dialog = AlertDialog.Builder(this).setTitle("Link do phụ huynh chọn · không tự nhập playlist")
            .setView(ScrollView(this).apply { addView(column) }).setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                try {
                    val video = YouTubeVideo(YouTubeVideo.idFromInput(url.text.toString()), title.text.toString().trim(),
                        arrayOf(Audience.BOTH,Audience.SAU,Audience.ONG)[audience.selectedItemPosition],
                        min.text.toString().toInt(), max.text.toString().toInt(), Math.multiplyExact(minutes.text.toString().toInt(),60),
                        approved.isChecked, enabled.isChecked, recall.text.toString().trim())
                    require(rows.none { it.videoId() == video.videoId() && it.videoId() != old?.videoId() })
                    store.save(rows.filterNot { it.videoId() == old?.videoId() } + video)
                    dialog.dismiss(); render()
                } catch (_: Exception) { url.error = "Kiểm tra link video, tên, tuổi 4–7, phút 1–30, tối đa 2 câu hỏi / 400 ký tự; không trùng ID." }
            }
        }; dialog.show()
    }
    private fun preview(video: YouTubeVideo) {
        if (YouTubeEmbedSettings(this).selected) {
            if (KidsSessionControl.active()) { message("Hãy kết thúc hành trình trước khi xem thử từ thư viện."); return }
            if (store.pendingId.isNotEmpty()) { message("Hãy xác nhận video bên ngoài đã dừng trước khi xem thử."); return }
            val display = OfficialYouTube.externalDisplay(this)
            if (display == null) { message("Cần màn hình DeX ngoài đang bật để xem thử trong app."); return }
            if (!YouTubeEmbedSettings(this).ready()) {
                AlertDialog.Builder(this).setTitle("Cấu hình trước khi xem thử")
                    .setMessage("Cần API key, thông tin liên hệ và đồng ý quyền riêng tư. Không cần duyệt video hoặc bật tự xen kẽ để xem thử.")
                    .setNegativeButton("Để sau", null).setPositiveButton("Mở cấu hình") { _, _ ->
                        startActivity(android.content.Intent(this, YouTubeEmbedSetupActivity::class.java))
                    }.show()
                return
            }
            try {
                startActivity(android.content.Intent(this, YouTubePreviewActivity::class.java).putExtra("video_id", video.videoId()),
                    android.app.ActivityOptions.makeBasic().apply { launchDisplayId = display }.toBundle())
            } catch (_: Exception) { message("Không mở được màn hình xem thử trên DeX.") }
            return
        }
        if (KidsSessionControl.active()) { message("Hãy kết thúc hành trình trước khi xem thử từ thư viện."); return }
        if (!store.enabled || !video.approved() || !video.enabled()) { message("Cần bật YouTube và duyệt link trước."); return }
        try {
            OfficialYouTube.preflight(this,video)?.let { message(it); return }
            store.begin(video)
            OfficialYouTube.launch(this,video)
        } catch (_: Exception) { store.clearPending(); message("Không mở được YouTube trên DeX; không mở trên điện thoại.") }
    }
    private fun message(text: String) { AlertDialog.Builder(this).setMessage(text).setPositiveButton("Đóng",null).show() }
}
